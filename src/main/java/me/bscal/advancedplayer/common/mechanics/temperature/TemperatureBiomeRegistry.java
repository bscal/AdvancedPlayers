package me.bscal.advancedplayer.common.mechanics.temperature;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class TemperatureBiomeRegistry
{
    public static final float EVEN_BODY_TEMP = 21f;
    public static final float COOLING_BODY_TEMP = 19f;
    public static final float WARMING_BODY_TEMP = 23f;
    public static final float FREEZING_BODY_TEMP = 0f;
    public static final float BURNING_BODY_TEMP = 37f;

    public static final float BASE_TEMP = 15f;
    public static final float BASE_HOT_TEMP = 25f;
    public static final float BASE_COLD_TEMP = 0f;
    public static final BiomeClimate DEFAULT_CLIMATE;
    public static final BiomeClimate COLD_CLIMATE;
    public static final BiomeClimate HOT_CLIMATE;
    public static final BiomeClimate TROPICAL_CLIMATE;

    public static final Reference2ObjectOpenHashMap<Biome, BiomeClimate>
            BiomeIdToClimate = new Reference2ObjectOpenHashMap<>(32);
    ;

    public static void Put(World world, Identifier id, BiomeClimate climate)
    {
        var biome = world.getRegistryManager().get(RegistryKeys.BIOME).get(id);
        if (biome == null) return;
        BiomeIdToClimate.put(biome, climate);
    }

    public static BiomeClimate Get(Biome biome)
    {
        return BiomeIdToClimate.get(biome);
    }

    public static boolean IsNormalTemperature(float t)
    {
        return t < WARMING_BODY_TEMP && t > COOLING_BODY_TEMP;
    }

    static
    {
        DEFAULT_CLIMATE = new BiomeClimate();
        DEFAULT_CLIMATE.Type = TemperatureType.Neutral;
        DEFAULT_CLIMATE.BaseTemperature = 11f;
        DEFAULT_CLIMATE.TemperaturePerSeason = new float[]{15f, 26f, 15f, 0f};

        BiomeIdToClimate.defaultReturnValue(DEFAULT_CLIMATE);

        COLD_CLIMATE = new BiomeClimate();
        COLD_CLIMATE.Type = TemperatureType.Cold;
        COLD_CLIMATE.BaseTemperature = 0f;
        COLD_CLIMATE.TemperaturePerSeason = new float[]{5f, 15f, 5f, -3f};

        HOT_CLIMATE = new BiomeClimate();
        HOT_CLIMATE.Type = TemperatureType.Hot;
        HOT_CLIMATE.BaseTemperature = 26f;
        HOT_CLIMATE.TemperaturePerSeason = new float[]{25f, 34f, 25f, 11f};

        TROPICAL_CLIMATE = new BiomeClimate();
        TROPICAL_CLIMATE.Type = TemperatureType.Hot;
        TROPICAL_CLIMATE.BaseTemperature = 27f;
        TROPICAL_CLIMATE.TemperaturePerSeason = new float[]{27f, 27f, 27f, 27f};
    }

    public static void Init(MinecraftServer server)
    {
        var world = server.getOverworld();
        Put(world, BiomeKeys.SNOWY_PLAINS.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.ICE_SPIKES.getValue(), COLD_CLIMATE);

        Put(world, BiomeKeys.DESERT.getValue(), HOT_CLIMATE);

        Put(world, BiomeKeys.TAIGA.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.SNOWY_TAIGA.getValue(), COLD_CLIMATE);

        Put(world, BiomeKeys.SAVANNA.getValue(), HOT_CLIMATE);
        Put(world, BiomeKeys.SAVANNA_PLATEAU.getValue(), HOT_CLIMATE);

        Put(world, BiomeKeys.JUNGLE.getValue(), TROPICAL_CLIMATE);
        Put(world, BiomeKeys.SPARSE_JUNGLE.getValue(), TROPICAL_CLIMATE);
        Put(world, BiomeKeys.BAMBOO_JUNGLE.getValue(), TROPICAL_CLIMATE);

        Put(world, BiomeKeys.BADLANDS.getValue(), HOT_CLIMATE);
        Put(world, BiomeKeys.ERODED_BADLANDS.getValue(), HOT_CLIMATE);
        Put(world, BiomeKeys.WOODED_BADLANDS.getValue(), HOT_CLIMATE);

        Put(world, BiomeKeys.SNOWY_SLOPES.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.FROZEN_PEAKS.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.JAGGED_PEAKS.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.STONY_PEAKS.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.FROZEN_OCEAN.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.DEEP_FROZEN_OCEAN.getValue(), COLD_CLIMATE);

        Put(world, BiomeKeys.NETHER_WASTES.getValue(), HOT_CLIMATE);
        Put(world, BiomeKeys.WARPED_FOREST.getValue(), HOT_CLIMATE);
        Put(world, BiomeKeys.CRIMSON_FOREST.getValue(), HOT_CLIMATE);
        Put(world, BiomeKeys.SOUL_SAND_VALLEY.getValue(), HOT_CLIMATE);
        Put(world, BiomeKeys.BASALT_DELTAS.getValue(), HOT_CLIMATE);

        Put(world, BiomeKeys.THE_END.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.END_HIGHLANDS.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.END_MIDLANDS.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.SMALL_END_ISLANDS.getValue(), COLD_CLIMATE);
        Put(world, BiomeKeys.END_BARRENS.getValue(), COLD_CLIMATE);
    }
}

//BiomeKeys.THE_VOID
//BiomeKeys.PLAINS
//BiomeKeys.SUNFLOWER_PLAINS
//BiomeKeys.SNOWY_PLAINS
//BiomeKeys.ICE_SPIKES
//BiomeKeys.DESERT
//BiomeKeys.SWAMP
//BiomeKeys.FOREST
//BiomeKeys.FLOWER_FOREST
//BiomeKeys.BIRCH_FOREST
//BiomeKeys.DARK_FOREST
//BiomeKeys.OLD_GROWTH_BIRCH_FOREST
//BiomeKeys.OLD_GROWTH_PINE_TAIGA
//BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA
//BiomeKeys.TAIGA
//BiomeKeys.SNOWY_TAIGA
//BiomeKeys.SAVANNA
//BiomeKeys.SAVANNA_PLATEAU
//BiomeKeys.WINDSWEPT_HILLS
//BiomeKeys.WINDSWEPT_GRAVELLY_HILLS
//BiomeKeys.WINDSWEPT_FOREST
//BiomeKeys.WINDSWEPT_SAVANNA
//BiomeKeys.JUNGLE
//BiomeKeys.SPARSE_JUNGLE
//BiomeKeys.BAMBOO_JUNGLE
//BiomeKeys.BADLANDS
//BiomeKeys.ERODED_BADLANDS
//BiomeKeys.WOODED_BADLANDS
//BiomeKeys.MEADOW
//BiomeKeys.GROVE
//BiomeKeys.SNOWY_SLOPES
//BiomeKeys.FROZEN_PEAKS;
//BiomeKeys.JAGGED_PEAKS
//BiomeKeys.STONY_PEAKS
//BiomeKeys.RIVER
//BiomeKeys.FROZEN_RIVER
//BiomeKeys.BEACH
//BiomeKeys.SNOWY_BEACH
//BiomeKeys.STONY_SHORE
//BiomeKeys.WARM_OCEAN
//BiomeKeys.LUKEWARM_OCEAN
//BiomeKeys.DEEP_LUKEWARM_OCEAN
//BiomeKeys.OCEAN
//BiomeKeys.DEEP_OCEAN
//BiomeKeys.COLD_OCEAN
//BiomeKeys.DEEP_COLD_OCEAN
//BiomeKeys.FROZEN_OCEAN
//BiomeKeys.DEEP_FROZEN_OCEAN
//BiomeKeys.MUSHROOM_FIELDS
//BiomeKeys.DRIPSTONE_CAVES
//BiomeKeys.LUSH_CAVES
//BiomeKeys.NETHER_WASTES
//BiomeKeys.WARPED_FOREST
//BiomeKeys.CRIMSON_FOREST
//BiomeKeys.SOUL_SAND_VALLEY
//BiomeKeys.BASALT_DELTAS
//BiomeKeys.THE_END
//BiomeKeys.END_HIGHLANDS
//BiomeKeys.END_MIDLANDS
//BiomeKeys.SMALL_END_ISLANDS
//BiomeKeys.END_BARRENS