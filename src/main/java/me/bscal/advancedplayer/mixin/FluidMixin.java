package me.bscal.advancedplayer.mixin;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fluid.class)
public class FluidMixin
{

/*
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/state/StateManager$Builder;build(Ljava/util/function/Function;Lnet/minecraft/state/StateManager$Factory;)Lnet/minecraft/state/StateManager;"))
	public void init(CallbackInfo ci)
	{
	}
*/

	@Inject(method = "appendProperties", at = @At(value = "HEAD"))
	public void appendProperties(StateManager.Builder<Fluid, FluidState> builder, CallbackInfo ci)
	{
		if (this.getClass().isInstance(WaterFluid.Still.class))
			builder.add(FlowableFluid.LEVEL);
	}

}
