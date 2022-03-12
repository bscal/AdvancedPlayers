package me.bscal.advancedplayer.common.food;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.Gson;
import me.bscal.advancedplayer.AdvancedPlayer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class that stores and handles "food" items that can contain multiple ingredients.
 * TODO test and decide if we want to use Kyro serialization.
 */
public class MultiFood
{

	public int BonusHunger;
	public float BonusSaturation;
	public boolean IsEdible;
	public List<Ingredient> Ingredients;
	public List<FoodFunction> OnEatEffects;
	public FoodGroups TotalFoodGroups;
	public Cookable Cookable;
	public Perishable Perishable;

	private static final Text INGREDIENTS_HEADER = Text.of("Ingredients:");
	private static final String KEY = "multifood_bin_data";

	public MultiFood()
	{
		Ingredients = new ArrayList<>();
		OnEatEffects = new ArrayList<>();
		TotalFoodGroups = new FoodGroups();
		Cookable = new Cookable();
		Perishable = new Perishable();
	}

	public void Serialize(@NotNull NbtCompound nbt)
	{
		Output output = new Output(64, 1024);
		AdvancedPlayer.Kryo.writeObject(output, this);
		nbt.putByteArray(KEY, output.getBuffer());
	}

	public static Optional<MultiFood> Deserialize(@NotNull NbtCompound nbt)
	{
		var buffer = nbt.getByteArray(KEY);
		if (buffer == null) return null;
		Input input = new Input(buffer);
		return Optional.ofNullable(AdvancedPlayer.Kryo.readObject(input, MultiFood.class));
	}

	public void PrintDebug()
	{
		String json = AdvancedPlayer.Gson.toJson(this);
		AdvancedPlayer.LOGGER.info(json);
	}

	public void AddIngredient(Ingredient ingredient)
	{
		Ingredients.add(ingredient);
		TotalFoodGroups.Add(ingredient.FoodGroups);
		ingredient.Effects.forEach(effect -> effect.Effect.Combine(this));
	}

	public void OnEat(final ItemStack itemStack, LivingEntity user)
	{
		// TODO add food groups to player component
		OnEatEffects.forEach(func -> func.OnEat(itemStack, this, user));
	}

	public void AppendTooltip(final ItemStack stack, @NotNull final List<Text> tooltip, final TooltipContext context)
	{
		if (Ingredients.isEmpty()) return;
		tooltip.add(INGREDIENTS_HEADER);
		for (var ingredient : Ingredients)
		{
			tooltip.add(Text.of(ingredient.Name));
		}
	}

	public static class Ingredient
	{
		public String Name;
		public FoodType Type;
		public FoodGroups FoodGroups;
		public List<IngredientCombineEffect> Effects;
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
		public long CookTime;
		public long TimeTillCooked;
	}

	public static class Perishable
	{
		public long TicksForSpoiled;
		public long SpawnedTick;

		public boolean HasExpired()
		{
			return System.currentTimeMillis() > SpawnedTick + TicksForSpoiled;
		}
	}

	public enum FoodType
	{
		Savory, Sweet, Spicy
	}

	public enum IngredientCombineEffect
	{
		BadTaste((food) -> {
		});

		public final IngredientFunction Effect;

		IngredientCombineEffect(IngredientFunction effect)
		{
			Effect = effect;
		}
	}

	public enum FoodEatEffect
	{
		BadTaste((itemStack, food, player) -> {
		});

		public final FoodFunction Effect;

		FoodEatEffect(FoodFunction effect)
		{
			Effect = effect;
		}
	}

	public interface IngredientFunction
	{
		void Combine(MultiFood food);
	}

	public interface FoodFunction
	{
		void OnEat(ItemStack itemStack, MultiFood food, LivingEntity player);
	}

}