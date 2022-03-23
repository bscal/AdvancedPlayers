package me.bscal.advancedplayer.mixin;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterFluid.class) public abstract class WaterFluidMixin extends FlowableFluidMixin
{
	@Override
	public void appendMixin(StateManager.Builder<Fluid, FluidState> builder, CallbackInfo ci)
	{
	}

	@Mixin(WaterFluid.Still.class) public abstract static class StillWaterMixin extends WaterFluidMixin
	{

		@Override
		public void appendMixin(StateManager.Builder<Fluid, FluidState> builder, CallbackInfo ci)
		{
			builder.add(LEVEL);
		}

/*		public boolean isStill(FluidState state)
		{
			return true;
		}*/

/*		public int getLevel(FluidState state)
		{
			return 8;
		}*/

	}
}


