package me.bscal.advancedplayer.common.items;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.food.MultiFood;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class MultiFoodItem extends Item
{

	private final Supplier<MultiFood> m_MultiFoodFactory;

	public MultiFoodItem(@NotNull Settings settings, @NotNull FoodComponent foodComponent)
	{
		this(settings, foodComponent, null);
	}

	public MultiFoodItem(@NotNull Settings settings, @NotNull FoodComponent foodComponent, @Nullable Supplier<MultiFood> supplier)
	{
		super(settings.food(foodComponent));
		if (supplier == null)
			m_MultiFoodFactory = MultiFood::new;
		else
			m_MultiFoodFactory = supplier;
	}

	public ItemStack CreateMultiFood(ItemStack itemStack)
	{
		var nbt = itemStack.getOrCreateNbt();
		var multifood =  m_MultiFoodFactory.get();
		AdvancedPlayer.Kryo.copy(multifood);
		multifood.PrintDebug();
		multifood.Serialize(nbt);
		return itemStack;
	}

	@Override
	public ItemStack getDefaultStack()
	{
		return CreateMultiFood(super.getDefaultStack());
	}

	public Optional<MultiFood> GetMultiFood(ItemStack itemStack)
	{
		if (itemStack.getItem() instanceof MultiFoodItem)
		{
			var nbt = itemStack.getNbt();
			if (nbt == null) return Optional.empty();
			return MultiFood.Deserialize(nbt);
		}
		return Optional.empty();
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
		GetMultiFood(stack).ifPresent(food -> food.AppendTooltip(stack, tooltip, context));
		super.appendTooltip(stack, world, tooltip, context);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user)
	{
		if (!world.isClient)
		{
			GetMultiFood(stack).ifPresent(food -> food.OnEat(stack, user));
		}
		return super.finishUsing(stack, world, user);
	}

	@Override
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks)
	{
		if (this.isIn(group))
		{
			stacks.add(CreateMultiFood(new ItemStack(this)));
		}
	}

}
