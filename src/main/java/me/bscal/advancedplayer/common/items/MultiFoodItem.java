package me.bscal.advancedplayer.common.items;

import me.bscal.advancedplayer.common.food.FoodManager;
import me.bscal.advancedplayer.common.food.MultiFood;
import me.bscal.advancedplayer.common.food.MultiFood.Ingredient;
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

public class MultiFoodItem extends Item
{

	public static final Text INGREDIENTS_HEADER = Text.of("Ingredients:");

	public final MultiFood MultiFood;

	public MultiFoodItem(@NotNull Settings settings, @NotNull FoodComponent foodComponent)
	{
		super(settings.food(foodComponent));

		MultiFood = new MultiFood();
	}

	public void AddIngredient(@NotNull Ingredient ingredient)
	{
		MultiFood.AddIngredient(ingredient);
	}

	public void AddIngredient(@NotNull Item item)
	{
		var ingredient = FoodManager.GetIngredient(item);
		if (ingredient != null) AddIngredient(ingredient);
	}

	/// TODO not sure how I want to handle this
	public FoodComponent.Builder GetBonusFood()
	{
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
		tooltip.add(INGREDIENTS_HEADER);
		for (var ingredient : MultiFood.Ingredients)
		{
			tooltip.add(Text.of(ingredient.Name));
		}

		super.appendTooltip(stack, world, tooltip, context);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user)
	{
		MultiFood.OnEat(stack, user); // Processed first because I do not want to deal with item stack returning from super.finishUsing()
		return super.finishUsing(stack, world, user);
	}
}
