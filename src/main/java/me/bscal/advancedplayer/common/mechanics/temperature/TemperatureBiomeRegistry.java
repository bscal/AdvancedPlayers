package me.bscal.advancedplayer.common.mechanics.temperature;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.seasons.api.SeasonAPI;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class TemperatureBiomeRegistry
{

	public static final Object2ObjectOpenHashMap<Identifier, BiomeClimate> BiomesToClimateMap;

	public static final BiomeClimate DEFAULT_CLIMATE;
	public static final BiomeClimate COLD_CLIMATE;
	public static final BiomeClimate HOT_CLIMATE;
	public static final BiomeClimate TROPICAL_CLIMATE;

	public static BiomeClimate GetClimate(Biome biome, World world)
	{
		Identifier biomeId = world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
		return BiomesToClimateMap.get(biomeId);
	}

	public static BiomeClimate Register(Identifier biomeId, BiomeClimate climate)
	{
		BiomesToClimateMap.putIfAbsent(biomeId, climate);
		return climate;
	}

	public record BiomeClimate(TemperatureType type, float baseTemperature, ClimateTemperatures temperatures)
	{
	}

	public enum TemperatureType
	{
		None, Neutral, Hot, Cold
	}

	public static class ClimateTemperatures
	{
		private final float[] m_Temps;

		public ClimateTemperatures(float s0, float s1, float s2, float s3)
		{
			m_Temps = new float[] { s0, s1, s2, s3 };
		}

		public ClimateTemperatures(float s0s1, float s2s3)
		{
			m_Temps = new float[] { s0s1, s0s1, s2s3, s2s3 };
		}

		public float GetTemperature(int index)
		{
			return m_Temps[index];
		}

		public float GetTemperature()
		{
			return GetTemperature(GetInternalIndex());
		}

		@Override
		public String toString()
		{
			return String.format("ClimateTemperatures[s0=%.2f, s1=%.2f, s2=%.2f, s3=%.2f]", m_Temps[0], m_Temps[1], m_Temps[2], m_Temps[3]);
		}

		private static int GetInternalIndex()
		{
			return (AdvancedPlayer.IsUsingSeasons()) ? SeasonAPI.getSeasonId() : 0;
		}
	}

	static
	{
		BiomesToClimateMap = new Object2ObjectOpenHashMap<>();
		DEFAULT_CLIMATE = new BiomeClimate(TemperatureType.Neutral, 11f, new ClimateTemperatures(15f, 26f, 15f, 0f));
		BiomesToClimateMap.defaultReturnValue(DEFAULT_CLIMATE);

		COLD_CLIMATE = new BiomeClimate(TemperatureType.Cold, 0f, new ClimateTemperatures(5f, 15f, 5f, -3f));
		HOT_CLIMATE = new BiomeClimate(TemperatureType.Hot, 26f, new ClimateTemperatures(25f, 34f, 25f, 11f));
		TROPICAL_CLIMATE = new BiomeClimate(TemperatureType.Hot, 27f, new ClimateTemperatures(27f, 27f));

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
	}

}