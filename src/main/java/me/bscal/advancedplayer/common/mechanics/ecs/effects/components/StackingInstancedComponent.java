package me.bscal.advancedplayer.common.mechanics.ecs.effects.components;

import com.artemis.Component;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public abstract class StackingInstancedComponent extends Component implements StackableComponent
{

	public IntArrayList Durations;
	public int InstanceDuration;

	@Override
	public void OnNewStack()
	{
		Durations.add(InstanceDuration);
	}
}
