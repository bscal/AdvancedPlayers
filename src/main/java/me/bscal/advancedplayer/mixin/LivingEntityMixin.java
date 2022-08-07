package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.events.DamageEvents;
import me.bscal.advancedplayer.common.utils.FloatReference;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin
{
    @Shadow
    public abstract void equipStack(EquipmentSlot var1, ItemStack var2);

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    public void OnDamageHead(DamageSource source, float amount, CallbackInfo ci)
    {
        ActionResult result = DamageEvents.BEFORE_REDUCTIONS.invoker().OnBeforeReductions(source, amount, (LivingEntity) (Object) this);
        if (result == ActionResult.FAIL) ci.cancel();
    }


    private final FloatReference m_NewDamageAmount = new FloatReference();

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
    public void OnShieldBlock(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
        m_NewDamageAmount.Value = 0f;
        ActionResult result = DamageEvents.BLOCKED.invoker().OnBlocked(source, amount, m_NewDamageAmount, (LivingEntity) (Object) this);
        AdvancedPlayer.LOGGER.info("damage: " + m_NewDamageAmount.Value);
        if (result == ActionResult.FAIL)
            m_NewDamageAmount.Value = 0f;
    }

    @ModifyVariable(method = "damage", argsOnly = true, ordinal = 0, at = @At(value = "STORE", ordinal = 0))
    public float OnModifyAmountShieldBlock(float amount)
    {
        AdvancedPlayer.LOGGER.info("damage: " + m_NewDamageAmount.Value);
        // This value is set in OnShieldBlock, used incase event wants to add damage taken after a block
        return m_NewDamageAmount.Value;
    }
}
