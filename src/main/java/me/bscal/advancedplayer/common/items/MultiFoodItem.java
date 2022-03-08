package me.bscal.advancedplayer.common.items;

import me.bscal.advancedplayer.common.food.MultiFood;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiFoodItem extends Item
{

	public MultiFood MultiFood;

	public MultiFoodItem(Settings settings)
	{
		super(settings);

		MultiFood = new MultiFood();
	}

	@Override
	public boolean isFood()
	{
		return true;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user)
	{
		var is = super.finishUsing(stack, world, user);
		MultiFood.OnEat(stack, world, user);
		return is;
	}
}
