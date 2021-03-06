package me.bscal.advancedplayer.common.ecs.componentTypes;

import com.artemis.Component;

public abstract class StackingDurationComponent extends Component implements StackableComponent
{

	public int Duration;
	public int DurationPerStack;

	@Override
	public void OnGainStack()
	{
		Duration += DurationPerStack;
	}
}
