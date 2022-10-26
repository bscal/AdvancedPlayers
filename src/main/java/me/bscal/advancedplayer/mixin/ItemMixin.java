package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ItemStackMixinInterface;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
            MutableText text = Text.empty();

            var nbt = stack.getNbt();
            if (nbt == null) return;


            long durationInSecs =  ((ItemStackMixinInterface) (Object) stack).UpdateSpoilage(world.getTime());
            durationInSecs /= 20;

            String str;
            if (durationInSecs > 0)
                str = String.format("Spoils in %d seconds", durationInSecs);
            else
                str = "Spoiled";

            text.append(str);
            tooltip.add(text);
        }
    }

/*    @Inject(method = "inventoryTick", at = @At(value = "HEAD"))
    public void InventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci)
    {
        if (!world.isClient && stack.isFood())
        {
            ItemStackMixinInterface is = ((ItemStackMixinInterface) (Object) stack);
            is.UpdateSpoilage(world.getTime());
        }
    }*/


}
