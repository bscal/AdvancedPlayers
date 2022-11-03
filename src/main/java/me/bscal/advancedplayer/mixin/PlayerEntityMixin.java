package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ItemStackMixinInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin
{

    @Inject(method = "eatFood", at = @At(value = "HEAD"))
    public void AdvancedPlayerEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir)
    {
        if (world.isClient) return;

        PlayerEntity player = (PlayerEntity)(Object)this;

        boolean IsSpoiled = ((ItemStackMixinInterface)(Object)stack).IsFresh(world.getTime());

        if (IsSpoiled)
        {
            AdvancedPlayer.LOGGER.info("Ate spoiled food!");
        }
        else
        {
            AdvancedPlayer.LOGGER.info("Are fresh food!");
        }
    }

}
