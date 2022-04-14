package me.bscal.advancedplayer.common.entities;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PiglinBruteBrain;
import net.minecraft.entity.passive.AxolotlBrain;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class GhoulEntity extends HostileEntity implements IAnimatable
{

	private static final int MAX_HEALTH = 40;
	private static final float MOVEMENT_SPEED = 0.35f;
	private static final int ATTACK_DAMAGE = 5;

	protected static final ImmutableList<? extends SensorType<? extends Sensor<? super GhoulEntity>>> SENSORS = ImmutableList.of(GhoulBrain.NEAREST_LOW_HP_ENTITIES, SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
	protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(GhoulBrain.LOWEST_HP_ENTITIES, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);

	// Bleed chance on hit
	// LifeSteal
	// Increase the lower hp you are?
	// If bleed increased healing and can smell you?

	private final AnimationFactory m_Factory = new AnimationFactory(this);

	protected GhoulEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}

	@Override
	protected Brain.Profile<GhoulEntity> createBrainProfile() {
		return Brain.createProfile(MEMORY_MODULES, SENSORS);
	}

	@Override
	protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
		Brain<GhoulEntity> brain = this.createBrainProfile().deserialize(dynamic);
		this.initBrain(brain);
		return brain;
	}

	public void reinitializeBrain(ServerWorld world) {
		Brain<GhoulEntity> brain = this.getBrain();
		brain.stopAllTasks(world, this);
		this.brain = brain.copy();
		this.initBrain(this.getBrain());
	}

	private void initBrain(Brain<GhoulEntity> brain) {
		GhoulBrain.AddCoreActivities(brain);
		GhoulBrain.AddIdleActivities(brain);
		GhoulBrain.AddFightActivities(this, brain);
		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.IDLE);
		brain.doExclusively(Activity.IDLE);
	}

	public Brain<GhoulEntity> getBrain() {
		return (Brain<GhoulEntity>) super.getBrain();
	}

	@Override
	protected void mobTick() {
		this.getBrain().tick((ServerWorld)this.world, this);
		//GhoulBrain.tick(this);
		//PiglinBruteBrain.tick(this);
		//PiglinBruteBrain.playSoundRandomly(this);
		super.mobTick();
	}

	@Override
	public void registerControllers(AnimationData animData)
	{
		animData.addAnimationController(new AnimationController<>(this, "controller", 0, this::Predicate));
	}

	private <E extends IAnimatable> PlayState Predicate(AnimationEvent<E> event)
	{
		var builder = new AnimationBuilder();
		builder.addAnimation("animation.ghoul.idle", true);
		event.getController().setAnimation(builder);
		return PlayState.CONTINUE;
	}

	@Override
	public AnimationFactory getFactory()
	{
		return m_Factory;
	}

	public static DefaultAttributeContainer.Builder createMobAttributes()
	{
		return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVEMENT_SPEED).add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
	}
}
