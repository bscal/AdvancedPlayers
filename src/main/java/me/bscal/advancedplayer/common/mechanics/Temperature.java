package me.bscal.advancedplayer.common.mechanics;

import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;

public class Temperature
{

    public float CoreBodyTemperature = TemperatureBody.NORMAL;
    public float Work;
    public float HeatLossRate;
    public float Delta;
    public float OutsideTemp;
    public float Insulation;
    public float WindResistance;
    public TemperatureBody.TemperatureShiftType ShiftType;

}
