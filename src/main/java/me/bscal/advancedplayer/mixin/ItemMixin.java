package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ItemStackMixinInterface;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin
{

    @Inject(method = "appendTooltip", at = @At(value = "TAIL"))
    public void AppendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci)
    {
        Item item = (Item) (Object) this;
        if (item.isFood())
        {
            var root = stack.getNbt();
            if (root == null) return;

            if (root.contains(AdvancedPlayer.KEY_ITEMSTACK_IS_SPOILED))
            {
                AppendString(tooltip, "Spoiled");
                return;
            }

            long start = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL);
            long end = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_END);

            // Used for recipes
            if (start == 0)
            {
                AppendString(tooltip, String.format("Takes in %d seconds to spoil", end / 20));
                return;
            }

            float rate = root.getFloat(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_RATE);
            long diff = world.getTime() - start;
            long spoilageWithModifier = (long) (diff * rate);
            long duration = start + spoilageWithModifier;
            long timeTillSpoil = end - duration;
            root.putLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL, duration);

            String str;
            if (timeTillSpoil > 0)
                str = String.format("Spoils in %d seconds", timeTillSpoil / 20);
            else
            {
                str = "Spoiled";
                ((ItemStackMixinInterface)(Object)stack).SetSpoiled(root);
            }
            AppendString(tooltip, str);

            stack.setNbt(root);
        }
    }

    private void AppendString(List<Text> tooltip, String str)
    {
        MutableText text = Text.empty();
        text.append(str);
        tooltip.add(text);
    }

    @Inject(method = "onCraft", at = @At(value = "HEAD"))
    public void OnCraft(ItemStack stack, World world, PlayerEntity player, CallbackInfo ci)
    {
        if (!world.isClient && stack.isFood())
        {
            ItemStackMixinInterface is = ((ItemStackMixinInterface) (Object) stack);
            is.InitSpoilage(world.getTime(), 20 * 30, 1.0f);
        }
    }


}
