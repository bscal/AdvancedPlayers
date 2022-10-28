package me.bscal.advancedplayer.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;

public class FoodSpoilage
{

    public static final long INVALID_SPOILAGE = 0;
    public static final long DOES_NOT_SPOIL = Long.MIN_VALUE;
    public static final SpoilageData DEFAULT_SPOILAGE = new SpoilageData();

    public static final Object2ObjectOpenHashMap<Item, SpoilageData> SPOILAGE_MAP = new Object2ObjectOpenHashMap<>(48);
    static
    {
        DEFAULT_SPOILAGE.TicksTillSpoiled = 24000 * 4;

        SPOILAGE_MAP.defaultReturnValue(DEFAULT_SPOILAGE);
    }

    public static void Register(Item item, long ticksTillSpoiled)
    {
        var data = new SpoilageData();
        data.TicksTillSpoiled = ticksTillSpoiled;

        SPOILAGE_MAP.put(item, data);
    }

    public static boolean IsSpoilageFood(Item item)
    {
        return item.isFood() && SPOILAGE_MAP.containsKey(item);
    }

    public static class SpoilageData
    {
        public long TicksTillSpoiled;
    }

}
