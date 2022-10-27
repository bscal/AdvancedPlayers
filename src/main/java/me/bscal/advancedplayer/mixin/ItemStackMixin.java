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
        if (((ItemStack) (Object) this).isFood())
        {
            long tick = 0;
            if (AdvancedPlayer.Server != null && AdvancedPlayer.Server.getOverworld() != null)
            {
                tick = AdvancedPlayer.Server.getOverworld().getTime();
            }
            InitSpoilage(tick, 20 * 30, 1.0f);
        }
    }

    @Override
    public void InitSpoilage(long spoilage, long spoilageEnd, float rate)
    {
        ItemStack itemStack = (ItemStack) (Object) this;
        NbtCompound root = itemStack.getOrCreateNbt();
        root.putLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL, spoilage);
        root.putLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_END, spoilage + spoilageEnd);
        root.putFloat(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_RATE, rate);
        itemStack.setNbt(root);
    }

    @Override
    public void SetSpoiled(NbtCompound nbt)
    {
        nbt.remove(AdvancedPlayer.KEY_ITEMSTACK_SPOIL);
        nbt.remove(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_END);
        nbt.remove(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_RATE);
        nbt.putBoolean(AdvancedPlayer.KEY_ITEMSTACK_IS_SPOILED, true);
    }

    @Override
    public void SetSpoilageRate(float rate)
    {
        ItemStack itemStack = (ItemStack) (Object) this;
        NbtCompound root = itemStack.getNbt();
        if (root == null) return;
        root.putFloat(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_RATE, rate);
        itemStack.setNbt(root);
    }

    @Override
    public long GetTicksTillSpoiled()
    {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.isFood())
        {
            NbtCompound root = itemStack.getNbt();
            if (root == null) return Integer.MIN_VALUE;
            long start = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL);
            long end = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_END);
            return end - start;
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean IsFresh()
    {
        return GetTicksTillSpoiled() > 0;
    }


}
