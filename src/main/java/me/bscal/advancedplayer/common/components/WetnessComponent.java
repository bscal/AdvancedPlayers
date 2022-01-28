package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class WetnessComponent implements FloatComponent, AutoSyncedComponent
{

	public static final float MIN_WETNESS = 0.0f; // Dry
	public static final float MAX_WETNESS = 1.0f; // Soaking

	private final LivingEntity m_Entity;
	private float m_Value;

	public WetnessComponent(@NotNull LivingEntity entity)
	{
		m_Entity = entity;
	}

	@Override
	public float GetValue()
	{
		return m_Value;
	}

	@Override
	public void SetValue(float wetness)
	{
		m_Value = MathHelper.clamp(wetness, MIN_WETNESS, MAX_WETNESS);
		ComponentManager.WETNESS.sync(m_Entity);
	}

	@Override
	public void readFromNbt(NbtCompound tag)
	{
		m_Value = tag.getFloat("Wetness");
	}

	@Override
	public void writeToNbt(NbtCompound tag)
	{
		tag.putFloat("Wetness", m_Value);
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player)
	{
		return player == m_Entity;
	}
}
