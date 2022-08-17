package me.bscal.advancedplayer.common;

import io.netty.buffer.PooledByteBufAllocator;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.temperature.BiomeClimate;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBiomeRegistry;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

import java.io.Serializable;

public class APPlayer implements Serializable
{

    public static final Identifier SYNC_PACKET = new Identifier(AdvancedPlayer.MOD_ID, "applayer_sync");
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
    public float BodyTemperature;
    public float CoreBodyTemperature = TemperatureBody.NORMAL;
    public float Work;
    public float HeatLossRate;
    public float Delta;
    public float OutsideTemp;
    public float Insulation;
    public float WindResistance;
    public float AirTemperature;
    public float YTemperature;
    public float LightTemperature;
    public float Humidity;
    public float Wind;
    public TemperatureBody.TemperatureShiftType ShiftType = TemperatureBody.TemperatureShiftType.Normal;

    private transient int m_LastSyncedTick;

    public APPlayer(MinecraftClient client)
    {
        Player = null;
    }

    public APPlayer(ServerPlayerEntity serverPlayerEntity)
    {
        Player = serverPlayerEntity;
    }

    public void Update(MinecraftServer server)
    {
        boolean secondTick = server.getTicks() % 20 == 0;

        Thirst -= 0.01f;
        Hunger -= 0.0025f;

        if (BleedTicks > 0)
        {
            --BleedTicks;
            Player.damage(DamageSource.GENERIC, 0.05f);
        }

        if (HeavyBleedTicks > 0 && HeavyBleedTicks % 20 == 0)
        {
            --HeavyBleedTicks;
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

        int tickDifference = server.getTicks() - m_LastSyncedTick;
        if (tickDifference > 20 * 5) Sync();
    }

    public void Sync()
    {
        if (Player.world == null || Player.world.isClient || Player.isDisconnected()) return;
        m_LastSyncedTick = Player.world.getServer().getTicks();
        PacketByteBuf buf = new PacketByteBuf(
                PooledByteBufAllocator.DEFAULT.directBuffer(128, 1024));
        Serialize(buf);
        AdvancedPlayer.LOGGER.info(
                String.format("Syncing(%s) WrittenBytes: %d, Buf Cap: %d, Buf MaxCap: %d",
                        Player.getDisplayName().getString(),
                        buf.getWrittenBytes().length,
                        buf.capacity(),
                        buf.maxCapacity()));
        ServerPlayNetworking.send(Player, SYNC_PACKET, buf);
    }

    // TODO handle a serialization to/from file so handle updated variables

    public void Serialize(PacketByteBuf buffer)
    {
        buffer.writeVarInt(BleedTicks);
        buffer.writeVarInt(HeavyBleedTicks);
        buffer.writeVarInt(LeftLegFracturedTicks);
        buffer.writeVarInt(RightLegFracturedTicks);
        buffer.writeBoolean(LeftLegSplinted);
        buffer.writeBoolean(RightLegSplinted);
        buffer.writeVarInt(Thirst);
        buffer.writeVarInt(Hunger);
        buffer.writeVarInt(Wetness);
        buffer.writeFloat(BodyTemperature);
        buffer.writeFloat(CoreBodyTemperature);
        buffer.writeFloat(Work);
        buffer.writeFloat(HeatLossRate);
        buffer.writeFloat(Delta);
        buffer.writeFloat(OutsideTemp);
        buffer.writeFloat(Insulation);
        buffer.writeFloat(WindResistance);
        buffer.writeFloat(AirTemperature);
        buffer.writeFloat(YTemperature);
        buffer.writeFloat(LightTemperature);
        buffer.writeFloat(Humidity);
        buffer.writeFloat(Wind);
        buffer.writeEnumConstant(ShiftType);
    }

    public void Deserialize(PacketByteBuf buffer)
    {
        BleedTicks = buffer.readVarInt();
        HeavyBleedTicks = buffer.readVarInt();
        LeftLegFracturedTicks = buffer.readVarInt();
        RightLegFracturedTicks = buffer.readVarInt();
        LeftLegSplinted = buffer.readBoolean();
        RightLegSplinted = buffer.readBoolean();
        Thirst = buffer.readVarInt();
        Hunger = buffer.readVarInt();
        Wetness = buffer.readVarInt();
        BodyTemperature = buffer.readFloat();
        CoreBodyTemperature = buffer.readFloat();
        Work = buffer.readFloat();
        HeatLossRate = buffer.readFloat();
        Delta = buffer.readFloat();
        OutsideTemp = buffer.readFloat();
        Insulation = buffer.readFloat();
        WindResistance = buffer.readFloat();
        AirTemperature = buffer.readFloat();
        YTemperature = buffer.readFloat();
        LightTemperature = buffer.readFloat();
        Humidity = buffer.readFloat();
        Wind = buffer.readFloat();
        ShiftType = buffer.readEnumConstant(TemperatureBody.TemperatureShiftType.class);
    }

    public static boolean Chance(float chance)
    {
        return RANDOM.nextFloat() < chance;
    }

    private void ProcessTemperature(MinecraftServer server)
    {
        BlockPos pos = Player.getBlockPos();
        RegistryEntry<Biome> biome = Player.world.getBiome(pos);
        BiomeClimate climate = TemperatureBiomeRegistry.Get(biome.value());
        AirTemperature = 41.0f;
        YTemperature = GetYTemperature(pos);
        LightTemperature = GetLightTemperature(Player.world.getLightLevel(LightType.SKY, pos));
        Humidity = 0.5f;
        Wind = 3f;

        //TODO
        //TemperatureClothing.ClothingData clothingData = GetProviderClothingData(Player);
        //Insulation = clothingData.Insulation;
        //WindResistance = clothingData.WindResistance;

        float baseBodyTemp = TemperatureBody.NORMAL;
        float baseWork = 0.0f; // Players body always doing some work.
        float currentWork = MathHelper.clamp(Work + baseWork, 0f, 10f);
        CoreBodyTemperature = BodyTemperature + currentWork;
        OutsideTemp = AirTemperature + YTemperature + LightTemperature - (Wind - WindResistance);
        ShiftType = TemperatureBody.TemperatureShiftType.TypeForTemp(OutsideTemp);
        HeatLossRate = 0.05f; // TODO change
        Delta = 0.01f;
        BodyTemperature = MathHelper.lerp(Delta, BodyTemperature, baseBodyTemp);
        BodyTemperature = MathHelper.lerp(HeatLossRate, BodyTemperature, OutsideTemp);
        BodyTemperature = MathHelper.clamp(BodyTemperature, TemperatureBody.MIN_COLD, TemperatureBody.MAX_HOT);
    }

    private static float GetYTemperature(BlockPos pos)
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

    private static float GetLightTemperature(int lightLevel)
    {
        return MathHelper.lerp(lightLevel / 15f, -4.5f, 4.5f);
    }


}
