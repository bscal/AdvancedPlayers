package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.FoodSpoilage;
import me.bscal.advancedplayer.common.ItemStackMixinInterface;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
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
        if (item.isFood() && world != null)
        {
            var root = stack.getNbt();
            if (root == null) return;

            long start = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL);
            long end = root.getLong(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_END);

            if (start > end)
            {
                AppendString(tooltip, "Spoiled");
                return;
            }

            float rate = root.getFloat(AdvancedPlayer.KEY_ITEMSTACK_SPOIL_RATE);
            long timeAdvancedSinceLastUpdate = world.getTime() - start;
            long timeTillSpoilWithModifier = (long) (timeAdvancedSinceLastUpdate * rate);
            long duration = start + timeTillSpoilWithModifier;

            // Used for recipes
            if (start == FoodSpoilage.INVALID_SPOILAGE)
            {
                AppendString(tooltip, FormatSpoilTicksToStr(end));
                return;
            }

            long timeTillSpoil = end - duration;
            if (duration < end)
                AppendString(tooltip, FormatSpoilTicksToStr(timeTillSpoil));
            else
                AppendString(tooltip, "Spoiled");
        }
    }

    private void AppendString(List<Text> tooltip, String str)
    {
        MutableText text = Text.empty();
        text.append(str);
        tooltip.add(text);
    }

    private String FormatSpoilTicksToStr(long ticks)
    {
        long formattedTick;
        String timeframe;

        if (ticks > 24000) //days
        {
            formattedTick = ticks / 24000;
            timeframe = "days";
        }
        else if (ticks > 20 * 60) //minutes
        {
            formattedTick = ticks / 20 * 60;
            timeframe = "minutes";
        }
        else
        {
            formattedTick = ticks / 20;
            timeframe = "seconds";
        }

        return String.format("Spoils in %d %s", formattedTick, timeframe);
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

    @Inject(method = "inventoryTick", at = @At(value = "HEAD"))
    public void InventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci)
    {
        if (entity instanceof ServerPlayerEntity player && stack.isFood())
        {
        }
    }

}
