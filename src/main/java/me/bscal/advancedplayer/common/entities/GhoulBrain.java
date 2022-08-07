package me.bscal.advancedplayer.common.entities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import me.bscal.advancedplayer.common.entities.ai.NearestLowHealthLivingEntities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class GhoulBrain
{

	protected static Brain<?> InitBrain(GhoulEntity ghoul, Brain<GhoulEntity> brain)
	{
		GhoulBrain.AddCoreActivities(brain);
		GhoulBrain.AddIdleActivities(brain);
		GhoulBrain.AddFightActivities(ghoul, brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.doExclusively(Activity.IDLE);
		return brain;
	}

	public static void AddCoreActivities(Brain<GhoulEntity> brain)
	{
		brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new LookAroundTask(45, 90)));
	}

	public static void AddIdleActivities(Brain<GhoulEntity> brain)
	{
		brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(new UpdateAttackTargetTask<GhoulEntity>(GhoulBrain::GetAttackTarget), MakeRandomWalkTask()));
	}

	private static RandomTask<GhoulEntity> MakeRandomWalkTask() {
		return new RandomTask(ImmutableList.of(Pair.of(new StrollTask(0.4f), 2), Pair.of(new GoTowardsLookTarget(0.4f, 3), 2), Pair.of(new WaitTask(30, 60), 1)));
	}

	private static Optional<? extends LivingEntity> GetAttackTarget(GhoulEntity ghoul)
	{
		var brain = ghoul.getBrain();

		var optionalLowestHp = brain.getOptionalMemory(NearestLowHealthLivingEntities.LOWEST_HP_PLAYER);
		if (optionalLowestHp.isPresent())
		{
			var ent = optionalLowestHp.get();
			if (ent.getHealth() < 6f) return optionalLowestHp;
		}

		var optionalClosest = brain.getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
		if (optionalClosest.isPresent())
		{
			return optionalClosest;
		}

		return optionalLowestHp;
	}

	public static void AddFightActivities(GhoulEntity ghoul, Brain<GhoulEntity> brain)
	{
		//brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(new ForgetAttackTargetTask<>(), new MeleeAttackTask(15)), MemoryModuleType.ATTACK_TARGET);
	}

	public static class HuntTask<E extends MobEntity> extends Task<E>
	{
		private final Predicate<E> m_StartCondition;
		private final Function<E, Optional<? extends LivingEntity>> m_TargetGetter;

		public HuntTask(Predicate<E> startCondition, Function<E, Optional<? extends LivingEntity>> targetGetter)
		{
			super(ImmutableMap.of());
			m_StartCondition = startCondition;
			m_TargetGetter = targetGetter;
		}

		@Override
		protected boolean shouldRun(ServerWorld serverWorld, E mobEntity) {
			if (!m_StartCondition.test(mobEntity)) return false;
			Optional<? extends LivingEntity> optional = m_TargetGetter.apply(mobEntity);
			return optional.filter(mobEntity::canTarget).isPresent();
		}

		@Override
		protected void run(ServerWorld serverWorld, E mobEntity, long l)
		{
			m_TargetGetter.apply(mobEntity).ifPresent(livingEntity -> this.UpdateAttackTarget(mobEntity, (LivingEntity)livingEntity));
		}

		private void UpdateAttackTarget(E entity, LivingEntity target)
		{
			entity.getBrain().remember(MemoryModuleType.ATTACK_TARGET, target);
			entity.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
		}
	}

}