package me.bscal.advancedplayer.common.utils;

import net.minecraft.client.util.math.MatrixStack;

public class AdvancedPlayerUtils
{

	public static float CelsiusToFahrenheit(float celsius)
	{
		return celsius * (9f / 5f) + 32f;
	}

	/**
	 * Utility function from https://github.com/Intro-Dev/Osmium
	 */
	public static void PositionAccurateScale(MatrixStack stack, float scale, int x, int y, int width, int height) {
		stack.translate((x + (width / 2f)), (y + (height / 2f)), 0);
		stack.scale(scale, scale, 0);
		stack.translate(-(x + (width / 2f)), -(y + (height / 2f)), 0);
	}
}