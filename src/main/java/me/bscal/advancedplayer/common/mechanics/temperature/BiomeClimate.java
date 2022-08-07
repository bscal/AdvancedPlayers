package me.bscal.advancedplayer.common.mechanics.temperature;

import me.bscal.advancedplayer.AdvancedPlayer;

public class BiomeClimate
{
    public TemperatureType Type;
    public float BaseTemperature;
    public float[] TemperaturePerSeason;

    public float GetCurrentTemperature()
    {
        return TemperaturePerSeason[(AdvancedPlayer.IsUsingSeasons()) ? 0 : 0];
    }

    @Override
    public String toString()
    {
        return String.format("%s, Base: %f.2f,0=%.2f 1=%.2f 2=%.2f 3=%.2f", Type, BaseTemperature, TemperaturePerSeason[0], TemperaturePerSeason[1],
                TemperaturePerSeason[2], TemperaturePerSeason[3]);
    }
}



