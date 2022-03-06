package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class PlayerStatsComponent implements ComponentV3, AutoSyncedComponent, ServerTickingComponent
{

	public static final float MIN_VALUE = 0;
	public static final float DEFAULT_MAX_VALUE = 20f;

	public static final float DEFAULT_COMFORT = 20f;
	public static final float DEFAULT_SANITY = 20f;
	public static final float DEFAULT_WETNESS = 0f;

	public float Comfort;
	public float MaxComfort;
	public float Sanity;
	public float MaxSanity;
	public float Wetness;
	public float MaxWetness;

	public boolean IsDirty;

	private final PlayerEntity m_Provider;

	public PlayerStatsComponent(PlayerEntity player)
	{
		m_Provider = player;
		SetAllToDefault();
		IsDirty = true;
	}

	public void SetAllToDefault()
	{
		Comfort = DEFAULT_COMFORT;
		MaxComfort = DEFAULT_MAX_VALUE;
		Sanity = DEFAULT_SANITY;
		MaxSanity = DEFAULT_MAX_VALUE;
		Wetness = DEFAULT_WETNESS;
		MaxWetness = DEFAULT_MAX_VALUE;
	}

	public void SetComfortClampedAndSynced(float comfort)
	{
		Comfort = MathHelper.clamp(comfort, MIN_VALUE, MaxComfort);
		IsDirty = true;
	}

	public void SetSanityClampedAndSynced(float sanity)
	{
		Sanity = MathHelper.clamp(sanity, MIN_VALUE, MaxSanity);
		IsDirty = true;
	}

	public void SetWetnessClampedAndSynced(float wetness)
	{
		Wetness = MathHelper.clamp(wetness, MIN_VALUE, MaxWetness);
		IsDirty = true;
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player)
	{
		return m_Provider == player;
	}

	@Override
	public void readFromNbt(NbtCompound tag)
	{
		Comfort = tag.getFloat("Comfort");
		MaxComfort = tag.getFloat("MaxComfort");
		Sanity = tag.getFloat("Sanity");
		MaxSanity = tag.getFloat("MaxSanity");
		Wetness = tag.getFloat("Wetness");
		MaxWetness = tag.getFloat("MaxWetness");
	}

	@Override
	public void writeToNbt(NbtCompound tag)
	{
		tag.putFloat("Comfort", Comfort);
		tag.putFloat("MaxComfort", MaxComfort);
		tag.putFloat("Sanity", Sanity);
		tag.putFloat("MaxSanity", MaxSanity);
		tag.putFloat("Wetness", Wetness);
		tag.putFloat("MaxWetness", MaxWetness);
	}

	@Override
	public void serverTick()
	{
		if (IsDirty)
		{
			IsDirty = false;
			//ComponentManager.PLAYER_STATS.sync(m_Provider);
		}
	}
}
