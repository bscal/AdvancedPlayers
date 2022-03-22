package me.bscal.advancedplayer.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidFillable;
import net.minecraft.fluid.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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

@Mixin(FlowableFluid.class) public abstract class FlowableFluidMixin extends Fluid
{

	@Shadow protected abstract boolean isMatchingAndStill(FluidState state);

	@Shadow
	protected abstract FluidState getUpdatedState(WorldView world, BlockPos pos, BlockState state);

	@Shadow
	protected abstract boolean canFlow(BlockView world, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo,
			BlockState flowToBlockState, FluidState fluidState, Fluid fluid);

	@Shadow
	protected abstract void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState);

	@Shadow
	protected abstract int method_15740(WorldView world, BlockPos pos);

	@Shadow
	protected abstract void method_15744(WorldAccess world, BlockPos pos, FluidState fluidState, BlockState blockState);

	@Shadow
	protected abstract boolean method_15736(BlockView world, Fluid fluid, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState);

	@Inject(method = "appendProperties", at = @At(value = "HEAD"))
	public void appendProperties(StateManager.Builder<Fluid, FluidState> builder, CallbackInfo ci)
	{
		if (this.getClass().isInstance(WaterFluid.Still.class)) builder.add(FlowableFluid.LEVEL);
	}

	@Inject(method = "onScheduledTick", at = @At(value = "HEAD"), cancellable = true)
	protected void process(World world, BlockPos pos, FluidState state, CallbackInfo ci)
	{
		ci.cancel();

		if (world.isClient || state.isEmpty()) return;
		if (state.isOf(Fluids.EMPTY))
		{
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
		}

		boolean update = false;
		int level = state.getLevel();

		if (state.isOf(Fluids.WATER) || state.isOf(Fluids.FLOWING_WATER))
		{
			BlockState blockState = world.getBlockState(pos);
			BlockPos downPos = pos.down();
			BlockState downState = world.getBlockState(downPos);
			FluidState downFluidState = downState.getFluidState();
			if ((downFluidState.getFluid().matchesType(this) && downFluidState.getLevel() < 8) || this.canFlow(world, pos, blockState, Direction.DOWN, downPos, downState, downFluidState, state.getFluid()))
			{
				if (downState instanceof FluidFillable)
				{
					((FluidFillable) downState.getBlock()).tryFillWithFluid(world, downPos, downState, downFluidState);
				}
				else
				{
					if (!downState.isAir())
					{
						this.beforeBreakingBlock(world, downPos, downState);
					}
					--level;
					var l = Fluids.WATER.getDefaultState().with(LEVEL, downFluidState.getLevel() + 1);
					world.setBlockState(downPos, l.getBlockState(), Block.NOTIFY_ALL);
					update = true;
				}
			}

			for (Direction direction : Direction.Type.HORIZONTAL)
			{
				if (level <= 1) break;
				BlockPos dirPos = pos.offset(direction);
				BlockState dirState = world.getBlockState(dirPos);
				FluidState dirFluidState = dirState.getFluidState();

				if ((dirFluidState.getFluid().matchesType(this) && dirFluidState.getLevel() < 8) || this.canFlow(world, pos, blockState, direction, dirPos, dirState, dirFluidState, state.getFluid()))
				{
					if (downState instanceof FluidFillable)
					{
						((FluidFillable) dirState.getBlock()).tryFillWithFluid(world, dirPos, dirState, dirFluidState);
					}
					else
					{
						if (!dirState.isAir())
						{
							this.beforeBreakingBlock(world, dirPos, dirState);
						}
						--level;
						var l = Fluids.WATER.getDefaultState().with(LEVEL, dirFluidState.getLevel() + 1);
						world.setBlockState(dirPos, l.getBlockState(), Block.NOTIFY_ALL);
						update = true;
					}
				}
			}
			if (update)
			{
				if (level <= 0) world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
				else world.setBlockState(pos, state.getBlockState().with(LEVEL, level), Block.NOTIFY_ALL);
			}
			else if (state.isEmpty())
			{
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
			}
			world.createAndScheduleFluidTick(pos, state.getFluid(), 5);
			world.updateNeighborsAlways(pos, state.getBlockState().getBlock());
		}
	}

	@Shadow
	public abstract Fluid getFlowing();

	@Shadow
	public abstract FluidState getFlowing(int level, boolean falling);

	@Shadow
	protected abstract boolean receivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState);

	@Shadow
	protected abstract int getLevelDecreasePerBlock(WorldView var1);

	@Shadow
	public abstract int getLevel(FluidState var1);

	@Shadow
	protected abstract void beforeBreakingBlock(WorldAccess var1, BlockPos var2, BlockState var3);

	@Shadow @Final public static IntProperty LEVEL;

	@Shadow
	protected abstract int getNextTickDelay(World world, BlockPos pos, FluidState oldState, FluidState newState);

	@Shadow @Final public static BooleanProperty FALLING;

}
