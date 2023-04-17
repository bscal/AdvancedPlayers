package me.bscal.advancedplayer.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class DynamicLights
{

    public static class Light
    {
        public BlockPos Position;
    }

    public Object2ObjectOpenHashMap<UUID, Light> Lights = new Object2ObjectOpenHashMap<>();

    private final int m_UpdateCounterTickLength = 6;
    private int m_UpdateCounterTicks = m_UpdateCounterTickLength;

    public void UpdateLights(MinecraftServer server)
    {
        --m_UpdateCounterTicks;
        if (m_UpdateCounterTicks == 0)
        {
            m_UpdateCounterTicks = m_UpdateCounterTickLength;

            for (var entry : Lights.object2ObjectEntrySet())
            {
                Light light = entry.getValue();
                var world = server.getOverworld();
                var entity = world.getEntity(entry.getKey());
                // TODO change this
                if (entity != null && entity.isAlive()
                    && entity instanceof ServerPlayerEntity spe && spe.getMainHandStack().isOf(Items.TORCH))
                {
                    Vec3d curPos = entity.getEyePos();
                    BlockPos curBlockPos = BlockPos.ofFloored(curPos.x, curPos.y, curPos.z);

                    if (light.Position.equals(curBlockPos))
                        continue;

                    if (world.getBlockState(light.Position).isOf(Blocks.LIGHT))
                        world.setBlockState(light.Position, Blocks.AIR.getDefaultState());

                    if (world.getBlockState(curBlockPos).isAir())
                    {
                        world.setBlockState(curBlockPos, Blocks.LIGHT.getDefaultState());
                        light.Position = curBlockPos;
                    }
                }
                else
                {
                    if (world.getBlockState(light.Position).isOf(Blocks.LIGHT))
                        world.setBlockState(light.Position, Blocks.AIR.getDefaultState());

                    Lights.remove(entry.getKey());
                }
            }
        }
    }

    public void Free(MinecraftServer server)
    {
        for (var entry : Lights.object2ObjectEntrySet())
        {
            Light light = entry.getValue();
            var world = server.getOverworld();

            world.setBlockState(light.Position, Blocks.AIR.getDefaultState());
            Lights.remove(entry.getKey());
        }
    }

}
