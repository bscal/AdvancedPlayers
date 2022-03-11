package me.bscal.advancedplayer.common.items;

import me.bscal.advancedplayer.common.food.MultiFood;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MultiFoodItem extends Item
{

	public MultiFoodItem(@NotNull Settings settings, @NotNull FoodComponent foodComponent)
	{
		super(settings.food(foodComponent));
	}

	@Override
	public ItemStack getDefaultStack()
	{
		var itemStack = super.getDefaultStack();
		var nbt = itemStack.getOrCreateNbt();
		var multifood = new MultiFood();
		multifood.Serialize(nbt);
		return itemStack;
	}

	public MultiFood GetMultiFood(ItemStack itemStack)
	{
		if (itemStack.getItem() instanceof MultiFoodItem)
		{
			var nbt = itemStack.getNbt();
			if (nbt == null) return null;
			return MultiFood.Deserialize(nbt);
		}
		return null;
	}

	@Override
	public boolean isFood()
	{
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		// TODO check if needs client side check
		var multifood = GetMultiFood(stack);
		if (multifood != null) multifood.AppendTooltip(stack, tooltip, context);

		super.appendTooltip(stack, world, tooltip, context);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user)
	{
		if (!world.isClient)
		{
			var multifood = GetMultiFood(stack);
			if (multifood != null) multifood.OnEat(stack, user);
		}
		return super.finishUsing(stack, world, user);
	}

}
