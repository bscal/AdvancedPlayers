package me.bscal.advancedplayer.common.food;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MultiFood
{


	public boolean IsEdible;
	public Item Item;
	public ItemStack ItemStack;
	public List<Ingredient> Ingredients;
	public List<FoodEatEffectFunc> OnEatEffects;
	public FoodGroups TotalFoodGroups;
	public Cookable Cookable;
	public Perishable Perishable;

	public void AddIngredient(Ingredient ingredient)
	{
		Ingredients.add(ingredient);
		TotalFoodGroups.Add(ingredient.FoodGroups);
		ingredient.Effects.forEach(effect -> effect.Effect.ProcessItem(this));
	}

	public void OnEat(ItemStack stack, World world, LivingEntity user)
	{
		// TODO add food groups to player component

		OnEatEffects.forEach(func -> func.OnEat(this, user));
	}

	public void UpdateLore(PlayerEntity player)
	{
		List<Text> text = new ArrayList<>();
		for (var ingredient : Ingredients)
		{
			text.add(Text.of(ingredient.Name));
		}

		Item.appendTooltip(ItemStack, player.world, text, TooltipContext.Default.NORMAL);
	}

	public static class Ingredient
	{
		public String Name;
		public FoodType Type;
		public FoodGroups FoodGroups;
		public List<FoodEffect> Effects;
	}

	public static class FoodGroups
	{
		public float Calories;
		public float Fats;
		public float Carbs;
		public float Sugars;
		public float Protein;
		public float Vitamins;

		public FoodGroups Add(FoodGroups groups)
		{
			Calories += groups.Calories;
			Fats += groups.Fats;
			Carbs += groups.Carbs;
			Sugars += groups.Sugars;
			Protein += groups.Protein;
			Vitamins += groups.Vitamins;
			return this;
		}
	}

	public static class Cookable
	{
		public boolean IsRaw;
		public int TicksCooked;
		public int TicksForCooked;
		public int TicksForBurnt;

		public boolean IsCookable() { return TicksForCooked == -1; }
	}

	public static class Perishable
	{
		public long TicksForSpoiled;
		public long SpawnedTick;

		public boolean IsPerishable() { return TicksForSpoiled == -1; }
	}

	public enum FoodType
	{
		Savory, Sweet, Spicy
	}

	public enum FoodEffect
	{
		BadTaste((food) -> {
		});

		public final FoodEffectFunc Effect;

		FoodEffect(FoodEffectFunc effect)
		{
			Effect = effect;
		}
	}

	public interface FoodEffectFunc
	{
		void ProcessItem(MultiFood food);
	}

	public interface FoodEatEffectFunc
	{
		void OnEat(MultiFood food, LivingEntity player);
	}

}