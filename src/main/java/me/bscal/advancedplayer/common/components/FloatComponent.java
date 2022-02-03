package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public class FloatComponent implements ComponentV3
{
	public float Value;
	public float DefaultValue, MinValue, MaxValue;

	public FloatComponent()
	{
		this(0.0f, 0.0f, 1.0f);
	}

	public FloatComponent(float defaultValue, float minValue, float maxValue)
	{
		Value = defaultValue;
		DefaultValue = defaultValue;
		MinValue = minValue;
		MaxValue = maxValue;
	}

	public void SetValueClamped(float value)
	{
		Value = MathHelper.clamp(value, MinValue, MaxValue);
	}

	public void Reset()
	{
		Value = DefaultValue;
	}

	@Override
	public void readFromNbt(NbtCompound tag)
	{
		Value = tag.getFloat("Value");
	}

	@Override
	public void writeToNbt(NbtCompound tag)
	{
		tag.putFloat("Value", Value);
	}
}
