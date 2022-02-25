package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.common.events.DamageEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class) public class LivingEntityMixin
{

	// This is a cached event because we @Inject and dispatch it first and then use the value
	// CurrentAmount in a @ModifyVariable
	private final DamageEvents.Blocked m_BlockEvent = new DamageEvents.Blocked();

	@Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
	public void OnDamageHead(DamageSource source, float amount, CallbackInfo ci)
	{
		DamageEvents.BeforeReductions event = new DamageEvents.BeforeReductions();
		event.Source = source;
		event.Amount = amount;
		event.Entity = (LivingEntity) (Object) this;

		DamageEvents.BEFORE_REDUCTIONS.invoker().OnBeforeReductions(event);
		if (event.ShouldCancel) ci.cancel();
	}

	@Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;emitGameEvent" +
			"(Lnet/minecraft/world/event/GameEvent;Lnet/minecraft/entity/Entity;)V"), cancellable = true)
	public void OnDamageInvoke(DamageSource source, float amount, CallbackInfo ci)
	{
		DamageEvents.Received event = new DamageEvents.Received();
		event.Source = source;
		event.Amount = amount;
		event.Entity = (LivingEntity) (Object) this;

		DamageEvents.RECEIVED.invoker().OnReceivedDamage(event);
		if (event.ShouldCancel) ci.cancel();
	}

	@Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
	public void OnShieldBlock(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		m_BlockEvent.Source = source;
		m_BlockEvent.AmountAfterBlocking = 0.0f;
		m_BlockEvent.Amount = amount;
		m_BlockEvent.Entity = (LivingEntity) (Object) this;
		DamageEvents.BLOCKED.invoker().OnBlocked(m_BlockEvent);
	}

	@ModifyVariable(method = "damage", argsOnly = true, ordinal = 0, at = @At(value = "STORE", ordinal = 0))
	public float OnModifyAmountShieldBlock(float amount)
	{
		// This value is set in OnShieldBlock, used incase event wants to add damage taken after a block
		return m_BlockEvent.AmountAfterBlocking;
	}
}
