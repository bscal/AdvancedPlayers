package me.bscal.advancedplayer.common.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class GhoulEntity extends PathAwareEntity implements IAnimatable
{

	private final AnimationFactory Factory = new AnimationFactory(this);

	protected GhoulEntity(EntityType<? extends PathAwareEntity> entityType, World world)
	{
		super(entityType, world);
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
		return Factory;
	}

	public static DefaultAttributeContainer.Builder createMobAttributes()
	{
		return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
	}
}
