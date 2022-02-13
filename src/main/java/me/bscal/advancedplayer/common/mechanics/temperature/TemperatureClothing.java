package me.bscal.advancedplayer.common.mechanics.temperature;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class TemperatureClothing
{
	public static final Reference2ObjectOpenHashMap<Item, ClothingData> CLOTHING_MAP = new Reference2ObjectOpenHashMap<>();

	public static void LoadClothingMap()
	{
		CLOTHING_MAP.clear();

		ClothingData leather = new ClothingData();
		leather.Insulation = .3f;
		leather.WindResistance = .3f;

		CLOTHING_MAP.put(Items.LEATHER_BOOTS, leather);
		CLOTHING_MAP.put(Items.LEATHER_LEGGINGS, leather);
		CLOTHING_MAP.put(Items.LEATHER_CHESTPLATE, leather);
		CLOTHING_MAP.put(Items.LEATHER_HELMET, leather);
	}


	public static class ClothingData
	{
		public float Insulation;
		public float WindResistance;
	}
}
