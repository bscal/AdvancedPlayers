package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.core.util.Assert;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WetnessComponent implements ComponentV3, AutoSyncedComponent
{

	public static final float MIN_WETNESS = 0.0f; // Dry
	public static final float MAX_WETNESS = 1.0f; // Soaking

	public float Wetness;
	private final LivingEntity m_Provider;

	public WetnessComponent(@NotNull LivingEntity provider)
	{
		Objects.requireNonNull(provider, "WetnessComponent Entity is null");
		m_Provider = provider;
	}

	public void SetValueClampedAndSync(float wetness)
	{
		Wetness = MathHelper.clamp(wetness, MIN_WETNESS, MAX_WETNESS);
		ComponentManager.WETNESS.sync(m_Provider);
	}

	@Override
	public void readFromNbt(NbtCompound tag)
	{
		Wetness = tag.getFloat("Wetness");
	}

	@Override
	public void writeToNbt(NbtCompound tag)
	{
		tag.putFloat("Wetness", Wetness);
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player)
	{
		return player == m_Provider;
	}


}
