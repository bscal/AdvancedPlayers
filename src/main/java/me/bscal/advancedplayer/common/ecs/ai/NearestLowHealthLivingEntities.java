package me.bscal.advancedplayer.common.ecs.ai;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.bscal.advancedplayer.mixin.MemoryModuleTypeAccessor;
import me.bscal.advancedplayer.mixin.SensorTypeAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestPlayersSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class NearestLowHealthLivingEntities extends Sensor<LivingEntity>
{
    public static final SensorType<NearestLowHealthLivingEntities> NEAREST_LOW_HP_ENTITIES =
            SensorTypeAccessor.callRegister("ap_low_hp_sensor", () -> new NearestLowHealthLivingEntities(32f));

    public static final MemoryModuleType<LivingEntity> LOWEST_HP_PLAYER =
            MemoryModuleTypeAccessor.callRegister("ap_low_p_memory");

    private static final int SUBLIST_SIZE = 6;

    private final ObjectArrayList<PlayerEntity> m_CacheList;
    private final float m_Distance;

    public NearestLowHealthLivingEntities(float distance)
    {
        super(25);
        m_CacheList = new ObjectArrayList<>();
        m_Distance = distance;
    }

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules()
    {
        return ImmutableSet.of(LOWEST_HP_PLAYER, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_HOSTILE, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity)
    {
        m_CacheList.clear();
        m_CacheList.addAll(0, world.getPlayers());
        m_CacheList.sort(Comparator.comparingDouble(entity::squaredDistanceTo));

        var subList = m_CacheList.subList(0, Math.max(m_CacheList.size(), SUBLIST_SIZE));
        subList.removeIf(player -> entity.distanceTo(player) <= m_Distance);
        var brain = entity.getBrain();
        brain.remember(MemoryModuleType.NEAREST_PLAYERS, subList);
        brain.remember(MemoryModuleType.NEAREST_HOSTILE, subList.get(0));

        PlayerEntity nearestAttackable = null;
        PlayerEntity lowest = null;
        float lowestHp = Float.MAX_VALUE;
        for (var p : subList)
        {
            if (nearestAttackable == null && NearestPlayersSensor.testAttackableTargetPredicate(entity, p))
            {
                nearestAttackable = p;
            }
            if (NearestPlayersSensor.testAttackableTargetPredicateIgnoreVisibility(entity, p))
            {
                var hp = p.getHealth();
                if (lowestHp > hp)
                {
                    lowestHp = hp;
                    lowest = p;
                }
            }

        }
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, Optional.ofNullable(nearestAttackable));
        brain.remember(LOWEST_HP_PLAYER, Optional.ofNullable(lowest));
    }

    public static class DistanceComparator implements Comparator<LivingEntity>
    {

        public final float Distance;

        public DistanceComparator(float distance)
        {
            Distance = distance;
        }

        @Override
        public int compare(LivingEntity o1, LivingEntity o2)
        {
            float dist = o1.distanceTo(o2);
            if (dist < Distance)
                return -1;
            else if (dist > Distance)
                return 1;
            return 0;
        }
    }
}