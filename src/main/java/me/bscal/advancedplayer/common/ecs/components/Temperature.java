package me.bscal.advancedplayer.common.ecs.components;

import com.artemis.Component;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import me.bscal.seasons.api.SeasonAPI;

public class Temperature extends Component
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
