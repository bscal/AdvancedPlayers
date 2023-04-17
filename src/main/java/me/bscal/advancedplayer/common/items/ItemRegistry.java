package me.bscal.advancedplayer.common.items;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.food.FoodManager;
import me.bscal.advancedplayer.common.food.MultiFood;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public final class ItemRegistry
{

	public static MultiFoodItem Sandwich;
	//public static MultiFoodItem Stew;
	//public static MultiFoodItem Pasta;
	//public static MultiFoodItem Brownie;
	//public static MultiFoodItem Salad;
	//public static MultiFoodItem Pizza;

	public static void Init()
	{
		Sandwich = Register(IdOf("sandwich"), new MultiFoodItem(new FabricItemSettings(), FoodManager.BreadSlices,
				() -> {
					var multifood = new MultiFood();
					multifood.Perishable.SpawnedTick = System.currentTimeMillis();
					return multifood;
				}));
	}

	public static MultiFoodItem Register(Identifier id, MultiFoodItem item)
	{
		Registry.register(Registries.ITEM, id, item);
		return item;
	}

	public static Item Register(Identifier id, Item item)
	{
		Registry.register(Registries.ITEM, id, item);
		return item;
	}

	public static Identifier IdOf(String name)
	{
		return new Identifier(AdvancedPlayer.MOD_ID, name);
	}

}
