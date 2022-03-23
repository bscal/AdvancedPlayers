package me.bscal.advancedplayer.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BucketItem.class) public class BucketItemMixin
{

	@Shadow @Final private Fluid fluid;

	@ModifyArg(method = "placeFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;" +
			"Lnet/minecraft/block/BlockState;I)Z"), index = 1)
	public BlockState setBlockStartMixin(BlockState state)
	{
		return fluid.getDefaultState().with(FlowableFluid.LEVEL, 8).getBlockState();
	}

}
