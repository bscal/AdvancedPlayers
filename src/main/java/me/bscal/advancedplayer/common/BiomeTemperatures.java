package me.bscal.advancedplayer.common;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.seasons.api.SeasonAPI;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class BiomeTemperatures
{

    public static final float[] TEMPERATURE_DEFAULT = {0, 0, 0, 0};
    public static final float[] TEMPERATURE_COLD = {-5, -5, -5, -5};
    public static final float[] TEMPERATURE_HOT = {5, 5, 5, 5};
    public static final float[] TEMPERATURE_NETHER = {10, 10, 10, 10};

    public Object2ObjectOpenHashMap<Biome, Climate> Biome2Climate;

    public float MaxHeight = 100.0f;
    public float MinHeight = 0.0f;
    public float HeightModifier = 0.1f;

    public BiomeTemperatures()
    {
        Biome2Climate = new Object2ObjectOpenHashMap<Biome, Climate>();
    }

    public float CalculateTemperatureHeight(float y)
    {
        int temp;
        if (y >= MaxHeight)
            temp = (int) Math.floor((y - MaxHeight) * HeightModifier);
        else if (y <= MinHeight)
            temp = (int) Math.floor(Math.abs(y) * HeightModifier);
        else
            temp = 0;
        return temp;
    }

    public float GetTemperature(Biome biome)
    {
        Climate climate = Biome2Climate.get(biome);
        int seasonIndex = SeasonAPI.getSeason().ordinal();
        return climate.Temperatures[seasonIndex];
    }


    public void Register(ServerWorld world, Biome biome, int spring, int summer, int autumn, int winter)
    {
        Climate climate = new Climate();
        climate.Temperatures = new float[4];
        climate.Temperatures[0] = spring;
        climate.Temperatures[1] = summer;
        climate.Temperatures[2] = autumn;
        climate.Temperatures[3] = winter;
        Biome2Climate.put(biome, climate);
    }

    public void Register(ServerWorld world, Identifier biomeId, float[] temperatures)
    {
        var biome = world.getRegistryManager().get(Registry.BIOME_KEY).get(biomeId);
        if (biome == null) return;
        if (temperatures.length != 4)
        {
            AdvancedPlayer.LOGGER.error("Registering biome climate... temperatures array must equal 4.");
            return;
        }

        Climate climate = new Climate();
        climate.Temperatures = temperatures;
        Biome2Climate.put(biome, climate);
    }

    public void Init(ServerWorld world)
    {
        Climate climate = new Climate();
        climate.Temperatures = TEMPERATURE_DEFAULT;
        Biome2Climate.defaultReturnValue(climate);

        Register(world, BiomeKeys.SNOWY_PLAINS.getValue(), TEMPERATURE_COLD);
        Register(world, BiomeKeys.ICE_SPIKES.getValue(), TEMPERATURE_COLD);

        Register(world, BiomeKeys.DESERT.getValue(), TEMPERATURE_HOT);

        Register(world, BiomeKeys.TAIGA.getValue(), TEMPERATURE_COLD);
        Register(world, BiomeKeys.SNOWY_TAIGA.getValue(), TEMPERATURE_COLD);

        Register(world, BiomeKeys.SAVANNA.getValue(), TEMPERATURE_HOT);
        Register(world, BiomeKeys.SAVANNA_PLATEAU.getValue(), TEMPERATURE_HOT);

        Register(world, BiomeKeys.JUNGLE.getValue(), TEMPERATURE_HOT);
        Register(world, BiomeKeys.SPARSE_JUNGLE.getValue(), TEMPERATURE_HOT);
        Register(world, BiomeKeys.BAMBOO_JUNGLE.getValue(), TEMPERATURE_HOT);

        Register(world, BiomeKeys.BADLANDS.getValue(), TEMPERATURE_HOT);
        Register(world, BiomeKeys.ERODED_BADLANDS.getValue(), TEMPERATURE_HOT);
        Register(world, BiomeKeys.WOODED_BADLANDS.getValue(), TEMPERATURE_HOT);

        Register(world, BiomeKeys.SNOWY_SLOPES.getValue(), TEMPERATURE_COLD);
        Register(world, BiomeKeys.FROZEN_PEAKS.getValue(), TEMPERATURE_COLD);
        Register(world, BiomeKeys.JAGGED_PEAKS.getValue(), TEMPERATURE_COLD);
        Register(world, BiomeKeys.STONY_PEAKS.getValue(), TEMPERATURE_COLD);
        Register(world, BiomeKeys.FROZEN_OCEAN.getValue(), TEMPERATURE_COLD);
        Register(world, BiomeKeys.DEEP_FROZEN_OCEAN.getValue(), TEMPERATURE_COLD);

        // TODO
        Register(world, BiomeKeys.NETHER_WASTES.getValue(), TEMPERATURE_NETHER);
        Register(world, BiomeKeys.WARPED_FOREST.getValue(), TEMPERATURE_NETHER);
        Register(world, BiomeKeys.CRIMSON_FOREST.getValue(), TEMPERATURE_NETHER);
        Register(world, BiomeKeys.SOUL_SAND_VALLEY.getValue(), TEMPERATURE_NETHER);
        Register(world, BiomeKeys.BASALT_DELTAS.getValue(), TEMPERATURE_NETHER);

        // TODO
        Register(world, BiomeKeys.THE_END.getValue(), TEMPERATURE_NETHER);
        Register(world, BiomeKeys.END_HIGHLANDS.getValue(), TEMPERATURE_NETHER);
        Register(world, BiomeKeys.END_MIDLANDS.getValue(), TEMPERATURE_NETHER);
        Register(world, BiomeKeys.SMALL_END_ISLANDS.getValue(), TEMPERATURE_NETHER);
        Register(world, BiomeKeys.END_BARRENS.getValue(), TEMPERATURE_NETHER);
    }

    public static class Climate
    {
        float[] Temperatures;
    }

}
