package me.bscal.advancedplayer.common.utils;

public final class MathUtils
{
	private MathUtils() {}

	public static float Normalize(int value, int min, int max)
	{
		return ((float)value - (float)min) / ((float)max - (float)min);
	}

	public static float Normalize(float value, float min, float max)
	{
		return (value - min) / (max - min);
	}

}
