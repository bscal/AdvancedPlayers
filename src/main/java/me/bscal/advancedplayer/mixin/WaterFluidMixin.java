package me.bscal.advancedplayer.mixin;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.SoftOverride;

@Mixin(WaterFluid.class) public abstract class WaterFluidMixin extends FlowableFluid
{

	@Mixin(WaterFluid.Still.class) public abstract static class StillWaterMixin extends WaterFluidMixin
	{

		public void appendProperties(StateManager.Builder<Fluid, FluidState> builder)
		{
			super.appendProperties(builder);
			builder.add(LEVEL);
		}

		public boolean isStill(FluidState state)
		{
			return true;
		}

		public int getLevel(FluidState state)
		{
			return state.get(LEVEL);
		}

	}
}


