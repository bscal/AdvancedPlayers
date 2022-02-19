package me.bscal.advancedplayer.common.mechanics.ecs.effects.components;

import com.artemis.Component;

public abstract class StackingComponent extends Component implements StackableComponent
{

	public byte Stacks;

	@Override
	public void OnNewStack()
	{
		Stacks++;
	}

}
