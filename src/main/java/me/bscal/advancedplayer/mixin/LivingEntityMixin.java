package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.common.events.DamageEvents;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.ArtemisEffectManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class) public class LivingEntityMixin
{

	@Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
	public void OnDamageHead(DamageSource source, float amount, CallbackInfo ci)
	{
		DamageEvents.BeforeReductions brEvent = new DamageEvents.BeforeReductions();
		brEvent.Source = source;
		brEvent.Amount = amount;
		brEvent.Entity = (LivingEntity) (Object) this;

		ArtemisEffectManager.EventSystem.dispatch(brEvent);
		if (brEvent.ShouldCancel) ci.cancel();
	}

	@Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/entity/Entity;)V"), cancellable = true)
	public void OnDamageReturn(DamageSource source, float amount, CallbackInfo ci)
	{
		DamageEvents.Received rEvent = new DamageEvents.Received();
		rEvent.Source = source;
		rEvent.Amount = amount;
		rEvent.Entity = (LivingEntity) (Object) this;

		ArtemisEffectManager.EventSystem.dispatch(rEvent);
		if (rEvent.ShouldCancel) ci.cancel();
	}

	@Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
	public void OnShieldBlock(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		DamageEvents.Blocked rEvent = new DamageEvents.Blocked();
		rEvent.Source = source;
		rEvent.Amount = amount;
		rEvent.Entity = (LivingEntity) (Object) this;

		ArtemisEffectManager.EventSystem.dispatch(rEvent);
	}
}
