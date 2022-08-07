package me.bscal.advancedplayer.common.food;

import me.bscal.advancedplayer.AdvancedPlayer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class that stores and handles "food" items that can contain multiple ingredients.
 * TODO test and decide if we want to use Kyro serialization.
 */
public class MultiFood implements Serializable
{

    public int BonusHunger;
    public float BonusSaturation;
    public boolean IsEdible;
    public List<Ingredient> Ingredients;
    public List<FoodFunction> OnEatEffects;
    public FoodGroups TotalFoodGroups;
    public Cookable Cookable;
    public Perishable Perishable;
    public String Id;

    private static final Text INGREDIENTS_HEADER = Text.of("Ingredients:");
    private static final String KEY = "multifood_bin_data";

    public MultiFood()
    {
        Ingredients = new ArrayList<>(4);
        OnEatEffects = new ArrayList<>(4);
        TotalFoodGroups = new FoodGroups();
        Cookable = new Cookable();
        Perishable = new Perishable();
        Id = "TEST_STRING_FIX_THIS!";
    }

    public boolean IsCookable()
    {
        return Cookable.TotalTicks > 0;
    }

    public boolean IsPerishable()
    {
        return Perishable.TicksForSpoiled > 0;
    }

    public void Serialize(@NotNull ItemStack itemStack)
    {
        var root = itemStack.getOrCreateNbt();
        root.putString(KEY, Id);
        var data = SerializationUtils.serialize(this);
        root.putByteArray(KEY, data);
    }

    public static Optional<MultiFood> Deserialize(@NotNull NbtCompound nbt)
    {
        var id = nbt.getString(KEY);
        // TODO lookup multifood in idMap
        return Optional.empty();
    }

    public void PrintDebug()
    {
        String json = AdvancedPlayer.Gson.toJson(this);
        AdvancedPlayer.LOGGER.info(json);
    }

    public void AddIngredient(ItemStack destStack, Ingredient ingredient)
    {
        Ingredients.add(ingredient);
        TotalFoodGroups.Add(ingredient.FoodGroups);
        ingredient.Effects.forEach(effect -> effect.Effect.Combine(this));
        Serialize(destStack);
    }

    public void OnEat(ItemStack itemStack, LivingEntity user)
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
            tooltip.add(Text.of("- " + ingredient.Name));
        }
    }

    public static class Ingredient implements Serializable
    {
        public String Name;
        public FoodType Type;
        public FoodGroups FoodGroups;
        public List<IngredientCombineEffect> Effects;

        public Ingredient()
        {
            FoodGroups = new FoodGroups();
            Effects = new ArrayList<>(4);
        }

        public Ingredient(String name)
        {
            this();
            Name = name;
        }
    }

    public static class FoodGroups implements Serializable
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

    public static class Cookable implements Serializable
    {
        public int TotalTicks;
        public int TicksCooked;
        public boolean IsRaw;
        public boolean IsBurnt;

        public void Tick(int step)
        {
            TicksCooked -= step;
            IsRaw = TicksCooked > 0;
            IsBurnt = TicksCooked < -100;
        }
    }

    public static class Perishable implements Serializable
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
        Plain, Savory, Sweet, Spicy
    }

    public enum IngredientCombineEffect
    {
        BadTaste((food) ->
        {
        });

        public final IngredientFunction Effect;

        IngredientCombineEffect(IngredientFunction effect)
        {
            Effect = effect;
        }
    }

    public enum FoodEatEffect
    {
        BadTaste((itemStack, food, player) ->
        {
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