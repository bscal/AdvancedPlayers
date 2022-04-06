package me.bscal.advancedplayer.common.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class BiomeMap<V>
{

	private final Object2ObjectOpenHashMap<Identifier, V> m_IdToValue;
	private final Reference2ObjectOpenHashMap<Biome, V> m_BiomeToValue;

	public BiomeMap()
	{
		m_IdToValue = new Object2ObjectOpenHashMap<>();
		m_BiomeToValue = new Reference2ObjectOpenHashMap<>();
	}

	public void put(Identifier id, Biome b, V value)
	{
		m_IdToValue.put(id, value);
		m_BiomeToValue.put(b, value);
	}

	public void put(Identifier id, World world, V value)
	{
		var biome = world.getRegistryManager().get(Registry.BIOME_KEY).get(id);
		put(id, biome, value);
	}

	public void put(Biome biome, World world, V value)
	{
		var id = world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
		put(id, biome, value);
	}

	public V get(Identifier id)
	{
		return m_IdToValue.get(id);
	}

	public V get(Biome biome)
	{
		return m_BiomeToValue.get(biome);
	}


	public boolean contains(Identifier id)
	{
		return m_IdToValue.containsKey(id);
	}

	public boolean contains(Biome biome)
	{
		return m_BiomeToValue.containsKey(biome);
	}


}
