package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ItemStackMixinInterface;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackMixinInterface
{

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At(value = "TAIL"))
    public void InitItemCount(ItemConvertible item, int count, CallbackInfo ci)
    {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.isFood())
        {
            NbtCompound root = itemStack.getOrCreateNbt();
            long spoilDuration = System.currentTimeMillis() + 1000 * 30;
            root.putLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL, spoilDuration);
            itemStack.setNbt(root);
        }
    }

    @Override
    public long GetSpoilDuration()
    {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.isFood())
        {
            NbtCompound root = itemStack.getNbt();
            if (root == null) return -1;
            return root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL);
        }
        return -1;
    }

    @Override
    public boolean IsFresh()
    {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.isFood())
        {
            NbtCompound root = itemStack.getOrCreateNbt();
            long spoilDuration = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL);
            return spoilDuration > System.currentTimeMillis();
        }
        return false;
    }

}
