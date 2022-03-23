package me.bscal.advancedplayer.mixin;

import net.minecraft.block.*;
import net.minecraft.fluid.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowableFluid.class) public abstract class FlowableFluidMixin extends Fluid
{

	@Shadow
	protected abstract boolean canFlow(BlockView world, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo,
			BlockState flowToBlockState, FluidState fluidState, Fluid fluid);

	@Shadow
	protected abstract void beforeBreakingBlock(WorldAccess var1, BlockPos var2, BlockState var3);

	@Shadow @Final public static IntProperty LEVEL;

	@Shadow protected abstract boolean canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid);

	@Inject(method = "appendProperties", at = @At("TAIL"))
	public void appendMixin(StateManager.Builder<Fluid, FluidState> builder, CallbackInfo ci)
	{}

	@Inject(method = "getBlockStateLevel", cancellable = true, at = @At(value = "RETURN", ordinal = 0))
	private static void getBlockStateLevel(FluidState state, CallbackInfoReturnable<Integer> cir)
	{
		int level = Math.min(GetLevel(state), 8);
		cir.setReturnValue(level);
	}

	private static final int MIN_LEVEL = 1;
	private static final int MAX_LEVEL = 8;

	@Inject(method = "onScheduledTick", at = @At(value = "HEAD"), cancellable = true)
	protected void process(World world, BlockPos pos, FluidState state, CallbackInfo ci)
	{
		ci.cancel();
		boolean update = false;

		if (state.isOf(Fluids.WATER) || state.isOf(Fluids.FLOWING_WATER))
		{
			int level = GetLevel(state.getBlockState(), state);

			BlockState blockState = world.getBlockState(pos);
			BlockPos downPos = pos.down();
			BlockState downState = world.getBlockState(downPos);
			FluidState downFluidState = downState.getFluidState();

			int downLevel = GetLevel(downState, downFluidState);
			if (this.canFlow(world, pos, blockState, Direction.DOWN, downPos, downState, downFluidState, downFluidState.getFluid()))
			{
				if (downState instanceof FluidFillable)
				{
					((FluidFillable) downState.getBlock()).tryFillWithFluid(world, downPos, downState, downFluidState);
					level = 0;
					update = true;
				}
				else
				{
					if (!downState.isAir())
					{
						this.beforeBreakingBlock(world, downPos, downState);
					}
					int diff = 8 - downLevel;
					if (downLevel != downLevel + diff)
					{
						var l = FluidToState(state, downLevel + diff);
						world.setBlockState(downPos, l, Block.NOTIFY_ALL);
						level -= diff;
						update = true;
					}
				}
			}

			if (!update)
			{
				for (Direction direction : Direction.Type.HORIZONTAL)
				{
					if (level <= MIN_LEVEL) break;

					BlockPos dirPos = pos.offset(direction);
					BlockState dirState = world.getBlockState(dirPos);
					FluidState dirFluidState = dirState.getFluidState();

					int dirLevel = GetLevel(dirState, dirFluidState);
					if (dirLevel >= level) continue;
					if (CanFlow(world, direction, dirPos, dirState, dirFluidState, dirFluidState.getFluid()))
					{
						if (dirState instanceof FluidFillable)
						{
							((FluidFillable) dirState.getBlock()).tryFillWithFluid(world, dirPos, dirState, dirFluidState);
							level = 0;
						}
						else
						{
							if (!dirState.isAir())
							{
								this.beforeBreakingBlock(world, dirPos, dirState);
							}
							var l = FluidToState(state, dirLevel + 1);
							world.setBlockState(dirPos, l, Block.NOTIFY_ALL);
							--level;
						}
						update = true;
					}
				}
			}
			if (update)
			{
				if (level < MIN_LEVEL) world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
				else world.setBlockState(pos, FluidToState(state, level), Block.NOTIFY_ALL);
			}
			if (state.isEmpty())
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
			}
			int tickRate = state.getFluid().getTickRate(world);
			world.createAndScheduleFluidTick(pos, state.getFluid(), tickRate);
			world.updateNeighborsAlways(pos, state.getBlockState().getBlock());
		}
	}

	private boolean CanFlow(BlockView world, Direction flowDirection, BlockPos flowTo, BlockState flowToBlockState, FluidState fluidState, Fluid fluid)
	{
		return fluidState.canBeReplacedWith(world, flowTo, fluid, flowDirection) && this.canFill(world, flowTo, flowToBlockState, fluid);
	}

	private BlockState FluidToState(FluidState state, int level)
	{
		return state.getFluid().getDefaultState().with(LEVEL, MathHelper.clamp(level, MIN_LEVEL, MAX_LEVEL)).getBlockState();
	}

	private static int GetLevel(BlockState state, FluidState fluidState)
	{
		int fLvl;
		if (fluidState.contains(LEVEL))
			fLvl = fluidState.get(LEVEL);
		else
			fLvl = fluidState.getLevel();
		return fLvl;
	}

	private static int GetLevel(FluidState fluidState)
	{
		int fLvl;
		if (fluidState.contains(LEVEL))
			fLvl = 8 - fluidState.get(LEVEL);
		else
			fLvl = fluidState.getLevel();
		return fLvl;
	}


}
