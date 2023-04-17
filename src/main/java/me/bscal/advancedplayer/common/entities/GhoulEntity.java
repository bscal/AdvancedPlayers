package me.bscal.advancedplayer.common.entities;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import me.bscal.advancedplayer.common.entities.ai.NearestLowHealthLivingEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhoulEntity extends HostileEntity implements GeoEntity
{

    private static final float[] LIFESTEAL = new float[]{2.0f, 2.0f, 3.0f, 4.0f};

    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super GhoulEntity>>> SENSORS = ImmutableList.of(NearestLowHealthLivingEntities.NEAREST_LOW_HP_ENTITIES);

    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
            NearestLowHealthLivingEntities.LOWEST_HP_PLAYER,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.NEAREST_HOSTILE,
            MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.ATTACK_COOLING_DOWN
    );
    // Bleed chance on hit
    // LifeSteal
    // Increase the lower hp you are?
    // If bleed increased healing and can smell you?

    private final AnimatableInstanceCache Cache = GeckoLibUtil.createInstanceCache(this);

    protected GhoulEntity(EntityType<? extends HostileEntity> entityType, World world)
    {
        super(entityType, world);
    }

    @Override
    protected Brain.Profile<GhoulEntity> createBrainProfile()
    {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic)
    {
        return GhoulBrain.InitBrain(this, this.createBrainProfile().deserialize(dynamic));
    }

    public void ReinitializeBrain(ServerWorld world)
    {
        Brain<GhoulEntity> brain = this.getBrain();
        brain.stopAllTasks(world, this);
        this.brain = brain.copy();
        GhoulBrain.InitBrain(this, this.getBrain());
    }

    @Override
    public boolean tryAttack(Entity target)
    {
        float f = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        // TODO bleed gives effects

        DamageSources src = new DamageSources(target.getWorld().getRegistryManager());
        boolean bl = target.damage(src.mobAttack(this), f);
        if (bl)
        {
            int difficulty = this.world.getDifficulty().getId();
            float lifesteal = Math.min(f, LIFESTEAL[difficulty]);
            this.heal(lifesteal);

            float g = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
            if (g > 0.0f && target instanceof LivingEntity)
            {
                ((LivingEntity) target).takeKnockback(
                        g * 0.5f,
                        MathHelper.sin(this.getYaw() * ((float) Math.PI / 180)),
                        -MathHelper.cos(this.getYaw() * ((float) Math.PI / 180)));
                this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            }
            this.applyDamageEffects(this, target);
            this.onAttacking(target);
        }
        return bl;
    }


    @Override
    public Brain<GhoulEntity> getBrain()
    {
        return (Brain<GhoulEntity>) super.getBrain();
    }

    @Override
    protected void mobTick()
    {
        this.getBrain().tick((ServerWorld) this.world, this);
        super.mobTick();
    }

    public static DefaultAttributeContainer.Builder CreateMobAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, .35f)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0f)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controller)
    {
        controller.add(new AnimationController<>(this, "controller", 0, this::IdleAnimation));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache()
    {
        return Cache;
    }

    private PlayState IdleAnimation(AnimationState<GeoAnimatable> state)
    {
        RawAnimation anim = RawAnimation.begin().thenLoop("animation.ghoul.idle");
        return state.setAndContinue(anim);
    }
}
