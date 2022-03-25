package me.bscal.advancedplayer.mixin;

import net.minecraft.block.*;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(FlowableFluid.class) public abstract class FlowableFluidMixin extends Fluid
{

	@Shadow
	protected abstract boolean canFlow(BlockView world, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo,
			BlockState flowToBlockState, FluidState fluidState, Fluid fluid);

	@Shadow
	protected abstract void beforeBreakingBlock(WorldAccess var1, BlockPos var2, BlockState var3);

	@Shadow @Final public static IntProperty LEVEL;

	@Shadow
	protected abstract boolean canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid);

	@Shadow
	protected abstract boolean isMatchingAndStill(FluidState state);

	@Shadow
	protected abstract int getNextTickDelay(World world, BlockPos pos, FluidState oldState, FluidState newState);

	@Shadow @Final public static BooleanProperty FALLING;

	@Inject(method = "appendProperties", at = @At("TAIL"))
	public void appendMixin(StateManager.Builder<Fluid, FluidState> builder, CallbackInfo ci)
	{
	}

	@Inject(method = "getBlockStateLevel", cancellable = true, at = @At(value = "RETURN", ordinal = 0))
	private static void getBlockStateLevel(FluidState state, CallbackInfoReturnable<Integer> cir)
	{
		int level = Math.min(Math.min(state.getLevel(), 8) + (state.get(FALLING) ? 8 : 0), 15);
		cir.setReturnValue(level);
	}

	private static final int MIN_LEVEL = 7;
	private static final int MAX_LEVEL = 0;
	private static final int REMOVE_LEVEL = Integer.MIN_VALUE;
	private static final int FULL_LEVEL = 8;

	private static final Random Random = new Random();
	private static final List<Direction> ShuffledDirections = new ArrayList<>(
			List.of(new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST }));

	/**
	 * TODO lava
	 * 	TODO improve filling liquids with bucket
	 */
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
			if (CanWaterFall(state, level, downFluidState, downLevel) || this.canFlow(world, pos, blockState, Direction.DOWN, downPos, downState,
					downFluidState, downFluidState.getFluid()))
			{
				if (downState instanceof FluidFillable)
				{
					((FluidFillable) downState.getBlock()).tryFillWithFluid(world, downPos, downState, downFluidState);
					level = REMOVE_LEVEL;
				}
				else
				{
					if (!downState.isAir())
					{
						this.beforeBreakingBlock(world, downPos, downState);
					}

					int newLevel;
					if (downFluidState.isEmpty())
					{
						newLevel = level;
						level = REMOVE_LEVEL;
					}
					else
					{
						int[] diff = Diff(downFluidState, downLevel, level);
						newLevel = diff[0];
						level = diff[1];
					}
					var l = FluidToState(state, newLevel);
					world.setBlockState(downPos, l, Block.NOTIFY_ALL);
				}
				update = true;
			}

			if (!update)
			{
				Collections.shuffle(ShuffledDirections, Random);
				for (Direction direction : ShuffledDirections)
				{
					if (level == MIN_LEVEL || level == REMOVE_LEVEL) break;

					BlockPos dirPos = pos.offset(direction);
					BlockState dirState = world.getBlockState(dirPos);
					FluidState dirFluidState = dirState.getFluidState();

					int dirLevel = GetLevel(dirState, dirFluidState);
					if (CanFillWater(state, level, dirFluidState, dirLevel) || CanFlow(world, direction, dirPos, dirState, dirFluidState,
							dirFluidState.getFluid()))
					{
						if (dirState instanceof FluidFillable)
						{
							((FluidFillable) dirState.getBlock()).tryFillWithFluid(world, dirPos, dirState, dirFluidState);
							level = REMOVE_LEVEL;
						}
						else
						{
							if (!dirState.isAir())
							{
								this.beforeBreakingBlock(world, dirPos, dirState);
							}
							level = ShrinkLevel(level);
							var l = FluidToState(state, GrowLevel(dirFluidState, dirLevel));
							world.setBlockState(dirPos, l, Block.NOTIFY_ALL);

						}
						update = true;
					}
				}
			}

			if (level == REMOVE_LEVEL || state.isEmpty())
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
			}
			else if (update)
			{
				int i = this.getNextTickDelay(world, pos, state, state);
				world.setBlockState(pos, FluidToState(state, level), Block.NOTIFY_ALL);
				world.createAndScheduleFluidTick(pos, state.getFluid(), i);
				world.updateNeighborsAlways(pos, state.getBlockState().getBlock());
			}
			else
			{
				int i = this.getNextTickDelay(world, pos, state, state);
				world.createAndScheduleFluidTick(pos, state.getFluid(), i);
			}
		}
	}

	protected boolean CanWaterFall(FluidState state, int level, FluidState downFluidState, int downLevel)
	{
		return downLevel != 8 && state.getFluid().matchesType(downFluidState.getFluid());
	}

	protected boolean CanFillWater(FluidState state, int level, FluidState dirFluidState, int dirLevel)
	{
		return dirLevel != 8 && (level < dirLevel || level == 8) && state.getFluid().matchesType(dirFluidState.getFluid());
	}

	/**
	 * 0 <----- 7 :: 8 Full
	 */
	protected int GrowLevel(FluidState dstState, int dstLevel)
	{
		if (dstState.isEmpty()) return MIN_LEVEL;

		if (dstLevel == 0)
		{
			return FULL_LEVEL;
		}
		return dstLevel - 1;
	}

	protected int[] Diff(FluidState state, int dstLevel, int level)
	{
		if (state.isEmpty()) return new int[] { level, REMOVE_LEVEL };
		for (int i = 0; i < 9; i++)
		{
			if (dstLevel == 8 || level == REMOVE_LEVEL) break;
			dstLevel = GrowLevel(state, dstLevel);
			level = ShrinkLevel(level);
		}
		return new int[] { dstLevel, level };
	}

	/**
	 * 0 -----> 7 :: 8 Full
	 */
	protected int ShrinkLevel(int level)
	{
		if (level == MIN_LEVEL)
		{
			return REMOVE_LEVEL;
		}
		if (level == FULL_LEVEL)
		{
			return MAX_LEVEL;
		}
		return level + 1;
	}

	private boolean CanFlow(BlockView world, Direction flowDirection, BlockPos flowTo, BlockState flowToBlockState, FluidState fluidState, Fluid fluid)
	{
		return fluidState.canBeReplacedWith(world, flowTo, fluid, flowDirection) && this.canFill(world, flowTo, flowToBlockState, fluid);
	}

	private BlockState FluidToState(FluidState state, int level)
	{
		level = MathHelper.clamp(level, 0, 8);
		return state.getFluid().getDefaultState().getBlockState().with(FluidBlock.LEVEL, level);
	}

	private static int GetLevel(BlockState state, FluidState fluidState)
	{
		int fLvl;
		if (state.contains(FluidBlock.LEVEL)) fLvl = state.get(FluidBlock.LEVEL);
		else fLvl = fluidState.getLevel();
		return fLvl;
	}

	private static int GetLevel(FluidState fluidState)
	{
		int fLvl;
		var bs = fluidState.getBlockState();
		if (bs.contains(FluidBlock.LEVEL)) fLvl = bs.get(FluidBlock.LEVEL);
		else fLvl = fluidState.getLevel();
		return fLvl;
	}

}
