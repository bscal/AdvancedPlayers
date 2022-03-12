package me.bscal.advancedplayer.common.items;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.food.FoodManager;
import me.bscal.advancedplayer.common.food.MultiFood;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ItemRegistry
{

	public static MultiFoodItem BreadSlice;

	public static void Init()
	{
		BreadSlice = Register(IdOf("sandwich"), new MultiFoodItem(new FabricItemSettings().group(ItemGroup.FOOD), FoodManager.BreadSlices,
				() -> {
					var multifood = new MultiFood();
					multifood.Perishable.SpawnedTick = System.currentTimeMillis();
					return multifood;
				}));
	}

	public static MultiFoodItem Register(Identifier id, MultiFoodItem item)
	{
		Registry.register(Registry.ITEM, id, item);
		return item;
	}

	public static Item Register(Identifier id, Item item)
	{
		Registry.register(Registry.ITEM, id, item);
		return item;
	}

	public static Identifier IdOf(String name)
	{
		return new Identifier(AdvancedPlayer.MOD_ID, name);
	}

}
