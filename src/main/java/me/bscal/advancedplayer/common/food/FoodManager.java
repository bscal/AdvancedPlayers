package me.bscal.advancedplayer.common.food;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public final class FoodManager
{

	public static final FoodComponent BreadSlices = new FoodComponent.Builder().hunger(1).saturationModifier(0.1f).build();

	private static final Object2ObjectOpenHashMap<Item, MultiFood.Ingredient> ITEM_TO_INGREDIENTS = new Object2ObjectOpenHashMap<>();

	public static MultiFood.Ingredient Register(Item item, MultiFood.Ingredient ingredient)
	{
		ITEM_TO_INGREDIENTS.put(item, ingredient);
		return ingredient;
	}

	public static MultiFood.Ingredient GetIngredient(Item item)
	{
		return ITEM_TO_INGREDIENTS.get(item);
	}

	public static boolean IsAnIngredient(Item item)
	{
		return ITEM_TO_INGREDIENTS.containsKey(item);
	}

	static
	{
		var carrot = Register(Items.CARROT, new MultiFood.Ingredient("Carrots"));
		carrot.Type = MultiFood.FoodType.Plain;
		carrot.FoodGroups.Calories = 10;
	}


}
