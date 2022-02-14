package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class SanityComponent implements ComponentV3, AutoSyncedComponent
{

	public static final float DefaultSanity = 0.0f;

	public float Sanity;
	public float MinSanity, MaxSanity;
	private final PlayerEntity m_Provider;

	public SanityComponent(PlayerEntity player)
	{
		m_Provider = player;
		Sanity = DefaultSanity;
	}

	@Override
	public void readFromNbt(NbtCompound tag)
	{
		tag.putFloat("Sanity", Sanity);
		tag.putFloat("MinSanity", MinSanity);
		tag.putFloat("MaxSanity", MaxSanity);
	}

	@Override
	public void writeToNbt(NbtCompound tag)
	{
		Sanity = tag.getFloat("Sanity");
		MinSanity = tag.getFloat("MinSanity");
		MaxSanity = tag.getFloat("MaxSanity");
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player)
	{
		return m_Provider == player;
	}
}
