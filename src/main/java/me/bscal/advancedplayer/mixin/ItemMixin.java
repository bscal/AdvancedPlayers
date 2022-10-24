package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.common.ItemStackMixinInterface;
import net.minecraft.client.item.TooltipContext;
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
    public void InitItemCount(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci)
    {
        Item item = (Item) (Object) this;
        if (item.isFood())
        {
            MutableText text = Text.empty();

            long time = System.currentTimeMillis();
            long spoilDuration = ((ItemStackMixinInterface) (Object) stack).GetSpoilDuration();
            long seconds = (spoilDuration - time) / 1000;

            String str;
            if (seconds > 0)
                str = String.format("Spoils in %d seconds", seconds);
            else
                str = "Spoiled";

            text.append(str);
            tooltip.add(text);
        }
    }

}
