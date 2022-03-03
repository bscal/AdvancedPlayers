package me.bscal.advancedplayer.common.mechanics.ecs.components;

import com.artemis.Component;

public abstract class StackingComponent extends Component implements StackableComponent
{

	public byte Stacks;

	@Override
	public void OnGainStack()
	{
		Stacks++;
	}

	@Override
	public void OnLoseStack() { Stacks--; }

}