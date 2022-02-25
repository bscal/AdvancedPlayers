package me.bscal.advancedplayer.common.mechanics.ecs.components;

import com.artemis.Component;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public abstract class StackingInstancedComponent extends Component implements StackableComponent
{

	public IntArrayList Durations;
	public int InstanceDuration;

	@Override
	public void OnGainStack()
	{
		Durations.add(InstanceDuration);
	}

	@Override
	public void OnLoseStack() { Durations.removeInt(0); }

	@Override
	public boolean IsEmpty() { return Durations.isEmpty(); }

}
