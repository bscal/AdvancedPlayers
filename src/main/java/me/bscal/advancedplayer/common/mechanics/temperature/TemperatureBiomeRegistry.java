package me.bscal.advancedplayer.common.mechanics.temperature;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

public class TemperatureBiomeRegistry
{

	public static final Map<String, BiomeClimate> BiomesToClimateMap;

	public static final float[] TemperateTemperatures = new float[] {};

	public record BiomeClimate(float[] temperatures)
	{
		public static float[] OfTropical(float wet, float dry)
		{
			return new float[] { wet, wet, dry, dry };
		}
	}

	static
	{
		BiomesToClimateMap = new Object2ObjectOpenHashMap<>();
	}

}
