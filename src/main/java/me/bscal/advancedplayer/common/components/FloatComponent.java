package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public abstract class FloatComponent implements ComponentV3
{
	public float Value;
	public float DefaultValue;
	public float MinValue;
	public float MaxValue;

	public void SetDefaults()
	{
		DefaultValue = 0;
		Value = DefaultValue;
		MinValue = 0;
		MaxValue = 1;
	}

	@Override
	public void readFromNbt(NbtCompound tag)
	{
		Value = tag.getFloat("Value");
		MinValue = tag.getFloat("MinValue");
		MaxValue = tag.getFloat("MaxValue");
	}

	@Override
	public void writeToNbt(NbtCompound tag)
	{
		tag.putFloat("Value", Value);
		tag.putFloat("MinValue", MinValue);
		tag.putFloat("MaxValue", MaxValue);
	}

	public void SetClampedValue(float value)
	{
		Value = MathHelper.clamp(value, MinValue, MaxValue);
	}
}
