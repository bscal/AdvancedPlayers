package me.bscal.advancedplayer.common;

import io.netty.buffer.PooledByteBufAllocator;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.seasons.common.seasons.SeasonClimateManager;
import me.bscal.seasons.common.seasons.SeasonTypes;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.io.Serializable;

public class APPlayer implements Serializable
{

    public static final Identifier SYNC_PACKET = new Identifier(AdvancedPlayer.MOD_ID, "applayer_sync");
    public static final Random RANDOM = Random.create();

    private static final int TICKRATE_SYNC_PLAYER = 1000 * 5;
    private static final int TICKRATE_TEMPERATURE = 20;

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

    public int BodyTemperature;
    public int OutsideTemperature;
    public int HeightTemperature;
    public float TempDelta;

    private transient long m_LastSyncTime;
    private transient int m_TemperatureUpdateCounter;

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

        if (m_TemperatureUpdateCounter-- < 1)
        {
            m_TemperatureUpdateCounter = TICKRATE_TEMPERATURE;
            ProcessTemperature(Player.getBlockPos());
        }

        long systemTime = System.currentTimeMillis();
        if (systemTime - m_LastSyncTime > TICKRATE_SYNC_PLAYER)
        {
            Sync(systemTime);
        }
    }

    public void Sync(long systemMilliTime)
    {
        m_LastSyncTime = systemMilliTime;
        PacketByteBuf buf = new PacketByteBuf(
                PooledByteBufAllocator.DEFAULT.directBuffer(
                        128,
                        1024));
        Serialize(buf);
        ServerPlayNetworking.send(Player, SYNC_PACKET, buf);

        AdvancedPlayer.LOGGER.info(
                String.format("Syncing(%s) WrittenBytes: %d, Buf Cap: %d, Buf MaxCap: %d",
                        Player.getDisplayName().getString(),
                        buf.getWrittenBytes().length,
                        buf.capacity(),
                        buf.maxCapacity()));
    }

    private static boolean Chance(float chance)
    {
        return RANDOM.nextFloat() < chance;
    }

    private void ProcessTemperature(BlockPos pos)
    {
        RegistryEntry<Biome> biome = Player.world.getBiome(pos);
        SeasonTypes climate = SeasonClimateManager.getSeasonType(biome.value());
        OutsideTemperature = 1;
        HeightTemperature = GetYTemperature(pos);

        int finalTemp = OutsideTemperature + HeightTemperature;
        BodyTemperature = (int) MathHelper.lerp(TempDelta, BodyTemperature, finalTemp);
    }

    private static int GetYTemperature(BlockPos pos)
    {
        float y = pos.getY();

        int temp;
        if (y >= 100.0f)
            temp = (int) Math.floor((y - 100.0f) * .1f);
        else if (y <= 0.0f)
            temp = (int) Math.floor(Math.abs(y) * .1f);
        else
            temp = 0;
        return temp;
    }

    private static float GetLightTemperature(int lightLevel)
    {
        return MathHelper.lerp(lightLevel / 15f, -4.5f, 4.5f);
    }

    private void Serialize(PacketByteBuf buffer)
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
        buffer.writeVarInt(BodyTemperature);
        buffer.writeVarInt(OutsideTemperature);
        buffer.writeVarInt(HeightTemperature);
        buffer.writeFloat(TempDelta);
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
        BodyTemperature = buffer.readVarInt();
        OutsideTemperature = buffer.readVarInt();
        HeightTemperature = buffer.readVarInt();
        TempDelta = buffer.readFloat();
    }

}
