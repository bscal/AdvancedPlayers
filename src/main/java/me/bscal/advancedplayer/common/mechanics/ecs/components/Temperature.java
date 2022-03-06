package me.bscal.advancedplayer.common.mechanics.ecs.components;

import com.artemis.Component;
import me.bscal.advancedplayer.common.mechanics.ecs.ECSManager;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;

public class Temperature extends Component
{

	public float CoreBodyTemperature = TemperatureBody.NORMAL;
	public float Work;
	public float HeatLossRate;
	public float BodyTemperature;
	public float OutSideTemperature;
	public float Insulation;
	public float WindResistance;
	public TemperatureBody.TemperatureShiftType ShiftType;

}
