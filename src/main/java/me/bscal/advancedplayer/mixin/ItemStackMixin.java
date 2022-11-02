package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.FoodSpoilage;
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
            long currentTick = FoodSpoilage.NextSpoilTick;
            FoodSpoilage.SpoilageData spoilageData = FoodSpoilage.SPOILAGE_MAP.get(item.asItem());
            InitSpoilage(currentTick, 20 * 30, 1.0f);
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
        if (itemStack.isFood())
        {
            NbtCompound nbt = itemStack.getNbt();
            if (nbt == null) return;
            if (!nbt.contains(AdvancedPlayer.KEY_ITEMSTACK_SPOIL)) return;
            nbt.putFloat(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_RATE, rate);
            itemStack.setNbt(nbt);
        }
    }

    @Override
    public long GetTicksTillSpoiled(long currentTime)
    {
        ItemStack itemStack = (ItemStack) (Object) this;
        if (itemStack.isFood())
        {
            NbtCompound root = itemStack.getNbt();
            if (root == null) return FoodSpoilage.DOES_NOT_SPOIL;
            if (!root.contains(AdvancedPlayer.KEY_ITEMSTACK_SPOIL)) return FoodSpoilage.DOES_NOT_SPOIL;
            long start = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL);
            long end = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_END);
            float rate = root.getFloat(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_RATE);
            long progressedTicks = GetSpoilProgress(currentTime, start, rate);
            long progressFromStart = start + progressedTicks;
            return end - progressFromStart;
        }
        return FoodSpoilage.DOES_NOT_SPOIL;
    }

    @Override
    public boolean IsFresh(long currentTime)
    {
        return GetTicksTillSpoiled(currentTime) > 0;
    }

    @Override
    public void UpdateSpoilage(long currentTick)
    {
        ItemStack stack = (ItemStack) (Object) this;
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) return;
        if (!nbt.contains(AdvancedPlayer.KEY_ITEMSTACK_SPOIL)) return;

        long start = nbt.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL);
        long end = nbt.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_END);
        float rate = nbt.getFloat(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_RATE);

        long progressedTicks = GetSpoilProgress(currentTick, start, rate);
        long progressFromStart = start + progressedTicks;
        long remainingTicks = end - progressFromStart;

        if (remainingTicks > 0)
            nbt.putLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL, progressFromStart);
        else
            ((ItemStackMixinInterface)(Object)stack).SetSpoiled(nbt);

        stack.setNbt(nbt);
    }

    private long GetSpoilProgress(long currentTick, long start, float rate)
    {
        long baseProgress = currentTick - start;
        return (long)(baseProgress * rate);
    }


}
