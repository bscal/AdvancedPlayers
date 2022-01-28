package me.bscal.advancedplayer.common.mechanics.body;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public class FloatBodyPart implements BodyPart
{

	private float m_Value;
	private float m_Default, m_Min, m_Max;
	private float m_Weight;

	public FloatBodyPart(float value, float min, float max, float weight)
	{
		m_Value = MathHelper.clamp(value, min, max);
		m_Default = m_Value;
		m_Min = min;
		m_Max = max;
		m_Weight = weight;
	}

	public float GetValue()
	{
		return m_Value;
	}

	public void SetValue(float value)
	{
		m_Value = MathHelper.clamp(value, m_Min, m_Max);
	}

	@Override
	public void Reset()
	{
		m_Value = m_Default;
	}

	public float GetWeight()
	{
		return m_Weight;
	}

	@Override
	public NbtCompound ToNbt()
	{
		var bodyPart = new NbtCompound();
		bodyPart.putFloat("Value", m_Value);
		return bodyPart;
	}

	@Override
	public void FromNbt(NbtCompound nbt)
	{
		m_Value = nbt.getFloat("Value");
	}
}
