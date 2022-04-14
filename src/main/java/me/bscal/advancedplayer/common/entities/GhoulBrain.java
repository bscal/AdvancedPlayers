package me.bscal.advancedplayer.common.entities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.mixin.MemoryModuleTypeAccessor;
import me.bscal.advancedplayer.mixin.SensorTypeAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.sensor.NearestLivingEntitiesSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.mob.PiglinBruteBrain;
import net.minecraft.entity.mob.PiglinBruteEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GhoulBrain
{

	protected static Brain<?> create(GhoulEntity ghoul, Brain<GhoulEntity> brain)
	{
		GhoulBrain.AddCoreActivities(brain);
		GhoulBrain.AddIdleActivities(brain);
		GhoulBrain.AddFightActivities(ghoul, brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.resetPossibleActivities();
		return brain;
	}

	public static void AddCoreActivities(Brain<GhoulEntity> brain)
	{
		brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90), new WanderAroundTask()));
	}

	public static void AddIdleActivities(Brain<GhoulEntity> brain)
	{
		brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(new UpdateAttackTargetTask<GhoulEntity>(GhoulBrain::GetAttackTarget)));
	}

	private static Optional<? extends LivingEntity> GetAttackTarget(GhoulEntity ghoulEntity)
	{
		return Optional.empty();
/*		var optional = ghoulEntity.getBrain().getOptionalMemory(MemoryModuleType.MOBS);
		if (optional.isPresent() && optional.get().size() > 0)
		{
			return Optional.of(optional.get().get(0));
		}
		return Optional.empty();*/
	}

	public static void AddFightActivities(GhoulEntity ghoul, Brain<GhoulEntity> brain)
	{
		//brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(new ForgetAttackTargetTask<>(), new MeleeAttackTask(15)), MemoryModuleType.ATTACK_TARGET);
	}

	protected static void tick(GhoulEntity ghoul) {
		Brain<GhoulEntity> brain = ghoul.getBrain();
		Activity activity = brain.getFirstPossibleNonCoreActivity().orElse(null);
		brain.resetPossibleActivities(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
		Activity activity2 = brain.getFirstPossibleNonCoreActivity().orElse(null);
		//if (activity != activity2) {
			//PiglinBruteBrain.playSoundIfAngry(piglinBrute);
		//}
		ghoul.setAttacking(brain.hasMemoryModule(MemoryModuleType.ATTACK_TARGET));
	}

	public static final SensorType<NearestLowHealthLivingEntities> NEAREST_LOW_HP_ENTITIES = SensorTypeAccessor.callRegister("ap_low_hp_sensor", NearestLowHealthLivingEntities::new);

	public static class NearestLowHealthLivingEntities extends Sensor<LivingEntity>
	{

		@Override
		protected void sense(ServerWorld world, LivingEntity entity) {
			Box box = entity.getBoundingBox().expand(16.0, 16.0, 16.0);
			List<LivingEntity> list = world.getEntitiesByClass(LivingEntity.class, box, e -> e != entity && e.isAlive());
			list.sort(Comparator.comparingDouble(entity::squaredDistanceTo));
			Brain<?> brain = entity.getBrain();
			brain.remember(MemoryModuleType.MOBS, list);
			brain.remember(MemoryModuleType.VISIBLE_MOBS, new LivingTargetCache(entity, list));
			brain.remember(LOWEST_HP_ENTITIES, list);
		}

		@Override
		public Set<MemoryModuleType<?>> getOutputMemoryModules() {
			return ImmutableSet.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS);
		}
	}

	public static final MemoryModuleType<List<LivingEntity>> LOWEST_HP_ENTITIES = MemoryModuleTypeAccessor.callRegister("ap_low_hp_memory");

}
