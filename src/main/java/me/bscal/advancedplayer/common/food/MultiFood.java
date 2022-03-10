package me.bscal.advancedplayer.common.food;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import java.util.List;

public class MultiFood
{

	public int BonusHunger;
	public float BonusSaturation;
	public boolean IsEdible;
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

	public void OnEat(final ItemStack itemStack, LivingEntity user)
	{
		// TODO add food groups to player component
		OnEatEffects.forEach(func -> func.OnEat(itemStack, this, user));
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

		public boolean IsCookable()
		{
			return TicksForCooked == -1;
		}
	}

	public static class Perishable
	{
		public long TicksForSpoiled;
		public long SpawnedTick;

		public boolean IsPerishable()
		{
			return TicksForSpoiled == -1;
		}
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
		void OnEat(ItemStack itemStack, MultiFood food, LivingEntity player);
	}

}