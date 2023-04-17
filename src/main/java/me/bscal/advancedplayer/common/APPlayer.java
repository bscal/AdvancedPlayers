package me.bscal.advancedplayer.common;

import io.netty.buffer.PooledByteBufAllocator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.bscal.advancedplayer.AdvancedPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

import java.io.Serializable;
import java.util.BitSet;

public class APPlayer implements Serializable
{

    public static final long serialVersionUID = 1L;
    public static final Identifier SYNC_PACKET = new Identifier(AdvancedPlayer.MOD_ID, "applayer_sync");
    public static final Random RANDOM = Random.create();

    private static final int APPLAYER_SIZE = 64;
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
    public float Wetness;

    public float BodyTemperature;
    public float OutsideTemperature;
    public float BiomeTemperature;
    public float HeightTemperature;
    public float BlockTemperature;
    public float TempDelta;

    public BitSet Traits;
    public Object2ObjectOpenHashMap<String, TraitsInstance> TraitsMap;

    private transient int m_SyncCounter;
    private transient int m_SecondCounter;
    private int m_LightCounterTick;
    private BlockPos m_LastLightPos;

    public APPlayer(MinecraftClient client)
    {
        Player = null;
        Traits = new BitSet();
        TraitsMap = new Object2ObjectOpenHashMap<>();
    }

    public APPlayer(ServerPlayerEntity serverPlayerEntity)
    {
        Player = serverPlayerEntity;
        Traits = new BitSet();
        TraitsMap = new Object2ObjectOpenHashMap<>();
    }

    public void OnLoadPlayer()
    {
        for (var value : TraitsMap.values())
        {
            var trait = AdvancedPlayer.APPlayerManager.TraitsRegister.get(value.TraitsName);
            if (trait != null)
                trait.StartFunction.Run(this, value);
        }
    }

    public void AddTrait(Traits trait)
    {
        Traits.set(trait.Id);

        TraitsInstance instance = trait.DefaultInstance;

        if (trait.AppliedFunction != null)
            trait.AppliedFunction.Run(this, instance);

        if (trait.StartFunction != null)
            trait.StartFunction.Run(this, instance);

        TraitsMap.put(trait.Name, instance);

        AdvancedPlayer.LOGGER.info(String.format("ADDED trait %s to player %s", trait.Name, Player.getName().getString()));
    }

    public void RemoveTrait(Traits trait)
    {
        Traits.clear(trait.Id);
        TraitsInstance traitInstance = TraitsMap.remove(trait.Name);

        if (traitInstance != null && trait.RemovedFunction != null)
            trait.RemovedFunction.Run(this, traitInstance);

        AdvancedPlayer.LOGGER.info(String.format("REMOVED trait %s to player %s", trait.Name, Player.getName().getString()));
    }

    public void Update(MinecraftServer server, int serverTickTime)
    {
        boolean secondTick = m_SecondCounter++ == 20;
        if (secondTick) m_SecondCounter = 0;
        DamageSources src = new DamageSources(server.getRegistryManager());

        for (var value : TraitsMap.values())
        {
            var trait = AdvancedPlayer.APPlayerManager.TraitsRegister.get(value.TraitsName);
            if (trait != null)
                trait.UpdateFunction.Run(this, value);
        }

        UpdateSpoiledItemStacks();

        Thirst -= 0.01f;
        Hunger -= 0.0025f;

        if (BleedTicks > 0)
        {
            --BleedTicks;
            Player.damage(src.generic(), 0.05f);
        }

        if (HeavyBleedTicks > 0 && HeavyBleedTicks % 20 == 0)
        {
            --HeavyBleedTicks;
            Player.damage(src.generic(), 2.5f);
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
                Player.damage(src.generic(), 2.0f);
            }
        }

        if (Player.getMainHandStack().getItem() == Items.TORCH)
        {
            // TODO works for now
            var light = new DynamicLights.Light();
            light.Position = new BlockPos(0,0,0);
            AdvancedPlayer.DynLights.Lights.putIfAbsent(Player.getUuid(), light);
        }


        Wetness = ProcessWetness();

        if (secondTick)
        {
            ProcessTemperature(Player.getBlockPos());
        }

        if (m_SyncCounter++ == 20 * 3)
        {
            Sync();
        }
    }

    private float ProcessWetness()
    {
        return MathHelper.lerp(
                Player.isSubmergedInWater() ? 1 : -.25f, 0f, 100f);
    }

    public static final ObjectOpenHashSet<Block> TemperatureBlocks = new ObjectOpenHashSet<>();

    static
    {
        TemperatureBlocks.add(Blocks.LAVA);
        TemperatureBlocks.add(Blocks.FURNACE);
        TemperatureBlocks.add(Blocks.BLAST_FURNACE);
        TemperatureBlocks.add(Blocks.MAGMA_BLOCK);
        TemperatureBlocks.add(Blocks.FIRE);
        TemperatureBlocks.add(Blocks.SOUL_FIRE);
        TemperatureBlocks.add(Blocks.CAMPFIRE);
        TemperatureBlocks.add(Blocks.SOUL_CAMPFIRE);
    }

    private void UpdateSpoiledItemStacks()
    {
        if (FoodSpoilage.NextSpoilCounter == FoodSpoilage.NextSpoilIncrement)
        {
            var screen = Player.currentScreenHandler;
            if (screen != null)
            {
                for (var slot : screen.slots)
                {
                    var stack = slot.getStack();
                    ((ItemStackMixinInterface) (Object) stack).UpdateSpoilage(Player.world.getTime());
                }
            }
        }
    }

    private float ProcessBlockTemperature(BlockPos pos, int radius)
    {
        float blockTemperature = 0;
        for (BlockPos itPos : BlockPos.iterateOutwards(pos, radius, radius, radius))
        {
            BlockState blockState = Player.world.getBlockState(itPos);
            Block block = blockState.getBlock();

            if (TemperatureBlocks.contains(block))
            {
                if (blockState.contains(Properties.LIT))
                {
                    if (blockState.get(Properties.LIT))
                        blockTemperature = 5;
                    break;
                }
                blockTemperature = 5;
            }
        }
        return blockTemperature;
    }

    private void ProcessTemperature(BlockPos pos)
    {
        RegistryEntry<Biome> biome = Player.world.getBiome(pos);
        BlockTemperature = ProcessBlockTemperature(pos, 3);
        BiomeTemperature = AdvancedPlayer.BiomeTemperatures.GetTemperature(biome.value());
        HeightTemperature = AdvancedPlayer.BiomeTemperatures.CalculateTemperatureHeight(pos.getY());
        OutsideTemperature = BlockTemperature + BiomeTemperature + HeightTemperature;
        TempDelta = .1f;
        if (OutsideTemperature < 0)
            BodyTemperature = Math.max(BodyTemperature - TempDelta, OutsideTemperature);
        else
            BodyTemperature = Math.min(BodyTemperature + TempDelta, OutsideTemperature);
    }

    public void Sync()
    {
        PacketByteBuf buf = new PacketByteBuf(
                PooledByteBufAllocator.DEFAULT.directBuffer(
                        APPLAYER_SIZE,
                        1024));
        Serialize(buf);
        ServerPlayNetworking.send(Player, SYNC_PACKET, buf);
        m_SyncCounter = 0;

        AdvancedPlayer.LOGGER.info(
                String.format("Syncing(%s) WrittenBytes: %d, Buf Cap: %d, Buf MaxCap: %d",
                        Player.getDisplayName().getString(),
                        buf.getWrittenBytes().length,
                        buf.capacity(),
                        buf.maxCapacity()));
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
        buffer.writeFloat(Wetness);
        buffer.writeFloat(BodyTemperature);
        buffer.writeFloat(BlockTemperature);
        buffer.writeFloat(OutsideTemperature);
        buffer.writeFloat(BiomeTemperature);
        buffer.writeFloat(HeightTemperature);
        buffer.writeFloat(TempDelta);
        buffer.writeBitSet(Traits);
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
        Wetness = buffer.readFloat();
        BodyTemperature = buffer.readFloat();
        BlockTemperature = buffer.readFloat();
        OutsideTemperature = buffer.readFloat();
        BiomeTemperature = buffer.readFloat();
        HeightTemperature = buffer.readFloat();
        TempDelta = buffer.readFloat();
        Traits = buffer.readBitSet();
    }

    private static boolean Chance(float chance)
    {
        return RANDOM.nextFloat() < chance;
    }

}
