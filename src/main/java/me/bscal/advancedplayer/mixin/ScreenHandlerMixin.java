package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ItemStackMixinInterface;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin
{

    @Inject(method = "onSlotClick", at = @At(value = "HEAD"))
    public void Click(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci)
    {
/*        ItemStack stack = player.currentScreenHandler.getCursorStack();
        if (!stack.isFood()) return;
        ((ItemStackMixinInterface) (Object) stack).UpdateSpoilage(stack, player.world.getTime());*/
    }


    @Inject(method = "close", at = @At(value = "HEAD"))
    public void Close(PlayerEntity player, CallbackInfo ci)
    {
        AdvancedPlayer.LOGGER.info("Working $ " + player.world.isClient);
        for (var slot : player.currentScreenHandler.slots)
        {
/*            ItemStack stack = slot.getStack();
            if (!stack.isFood()) continue;
            ((ItemStackMixinInterface) (Object) stack).UpdateSpoilage(stack, player.world.getTime());*/

            // TODO detect chest type and update rate
        }
    }

}
