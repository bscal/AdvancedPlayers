package me.bscal.advancedplayer.common;

import io.netty.buffer.Unpooled;
import me.bscal.advancedplayer.common.ecs.ECSManagerServer;
import me.bscal.advancedplayer.common.mechanics.temperature.BiomeClimate;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBiomeRegistry;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureClothing;
import me.bscal.seasons.api.SeasonAPI;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class APPlayer implements Serializable
{

    public static final Random RANDOM = Random.create();

    public transient ServerPlayerEntity Player;

    public int BleedTicks;
    public int HeavyBleedTicks;
    public int LeftLegFracturedTicks;
    public boolean LeftLegSplinted;
    public int RightLegFracturedTicks;
    public boolean RightLegSplinted;

    public int Thirst;
    public int Hunger;
    public int Wetness;
    public float CoreBodyTemperature = TemperatureBody.NORMAL;
    public float Work;
    public float HeatLossRate;
    public float Delta;
    public float OutsideTemp;
    public float Insulation;
    public float WindResistance;
    public TemperatureBody.TemperatureShiftType ShiftType;

    private int m_LastSyncedTick;

    public APPlayer(PlayerEntity player)
    {
        assert !player.world.isClient : "player cannot be null on server";
        if (player instanceof ServerPlayerEntity serverPlayerEntity)
            Player = serverPlayerEntity;
    }

    public void Update(MinecraftServer server)
    {
        boolean secondTick = server.getTicks() % 20 == 0;

        Thirst -= 0.01f;
        Hunger -= 0.0025f;

        if (BleedTicks-- > 0)
        {
            Player.damage(DamageSource.GENERIC, 0.05f);
        }

        if (HeavyBleedTicks-- > 0 && HeavyBleedTicks % 20 == 0)
        {
            Player.damage(DamageSource.GENERIC, 2.5f);
        }

        int fracturedLegLevel = 0;
        boolean leftLegFractured = LeftLegFracturedTicks > 0;
        boolean rightLegFractured = RightLegFracturedTicks > 0;
        if (leftLegFractured)
        {
            ++fracturedLegLevel;
            LeftLegFracturedTicks += (LeftLegSplinted) ? 5 : 1;
            if (LeftLegFracturedTicks > 20 * 60 * 5 && Chance(.5f))
            {
                LeftLegFracturedTicks = 0;
            }
        }
        if (rightLegFractured)
        {
            ++fracturedLegLevel;
            RightLegFracturedTicks += (RightLegSplinted) ? 5 : 1;
            if (RightLegFracturedTicks > 20 * 60 * 5 && Chance(.5f))
            {
                RightLegFracturedTicks = 0;
            }
        }
        if (Player.isSprinting() && (leftLegFractured || rightLegFractured))
        {
            StatusEffects.SLOWNESS.applyUpdateEffect(Player, fracturedLegLevel);
            if (secondTick && Chance(.4f))
            {
                Player.damage(DamageSource.GENERIC, 2.0f);
            }
        }

        Wetness = MathHelper.clamp(Wetness + (Player.isSubmergedInWater() ? -1 : 1), 0, 100);

        ProcessTemperature(server);
    }

    public void Sync()
    {
        if (Player.world == null || Player.world.isClient || Player.isDisconnected()) return;

        int tick = Player.world.getServer().getTicks();
        int lastTick = tick - m_LastSyncedTick;
        if (lastTick < 20 * 5) return;
        m_LastSyncedTick = tick;

        byte[] data = Serialize(this);
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.wrappedBuffer(data));
        ServerPlayNetworking.send(Player, ECSManagerServer.SYNC_CHANNEL, buffer);
    }

    public static byte[] Serialize(APPlayer player)
    {
        return SerializationUtils.serialize(player);
    }

    public static APPlayer Deserialize(ServerPlayerEntity player, byte[] data)
    {
        APPlayer apPlayer = SerializationUtils.deserialize(data);
        apPlayer.Player = player;
        return apPlayer;
    }

    public static boolean Chance(float chance)
    {
        return RANDOM.nextFloat() < chance;
    }

    protected void ProcessTemperature(MinecraftServer server)
    {
        BlockPos pos = Player.getBlockPos();
        RegistryEntry<Biome> biome = Player.world.getBiome(pos);
        BiomeClimate climate = TemperatureBiomeRegistry.Get(biome.value());
        float airTemperature = climate.GetCurrentTemperature();
        float yTemperature = GetYTemperature(pos);
        float lightTemperature = GetLightTemperature(Player.world.getLightLevel(LightType.SKY, pos));
        float humidity = 0.5f;
        float wind = 3f;

        //TODO
        //TemperatureClothing.ClothingData clothingData = GetProviderClothingData(Player);
        //Insulation = clothingData.Insulation;
        //WindResistance = clothingData.WindResistance;

        float m_BaseWork = 1.0f; // Players body always doing some work.
        float bodyTemp = CoreBodyTemperature + Work + m_BaseWork;

        OutsideTemp = airTemperature + yTemperature + lightTemperature - (wind - WindResistance);
        float diff = bodyTemp - OutsideTemp;
        ShiftType = TemperatureBody.TemperatureShiftType.TypeForTemp(OutsideTemp);

        // 100% insulation would mean you lose 0 heat, 0% you lose all the heat;
        HeatLossRate = MathHelper.lerp(Insulation, diff / 200, .0f);
        // Since Work is temporary
        Work = MathHelper.clamp(Work - HeatLossRate, 0, 10f);
        // Body moving towards the outside temperature. Not an expert at thermodynamics but this seems like a
        // decent system even though not 100% accurate
        CoreBodyTemperature = MathHelper.clamp(CoreBodyTemperature, TemperatureBody.MIN_COLD, TemperatureBody.MAX_HOT);
        CoreBodyTemperature = MathHelper.lerp(HeatLossRate, CoreBodyTemperature, OutsideTemp);
        // 100% would not allow evaporation to take place. This does not matter if it is cold.
        Delta = TemperatureBody.TemperatureShiftType.IsWarming(ShiftType) ? MathHelper.lerp(humidity, .1f, .0f) : .1f;
        CoreBodyTemperature = MathHelper.lerp(Delta, CoreBodyTemperature, TemperatureBody.NORMAL);
    }

    public static float GetYTemperature(BlockPos pos)
    {
        float y = pos.getY();
        if (y <= -32)
        {
            // 15-31C
            return ((-y) - 32) * 0.5f;
        }
        if (y >= 128)
        {
            // 320 max height = -57.6 | 41.6
            // 256 max gen h = -38.4 | -23.4
            // 128 start h = 0 | 15
            // Usually base temp is 15;
            return -((y - 128) * .3f);
        }
        return 0f;
    }

    public static float GetLightTemperature(int lightLevel)
    {
        return MathHelper.lerp(lightLevel / 15f, -4.5f, 4.5f);
    }


}