package me.bscal.advancedplayer.common.mechanics.ecs.effects.components;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class Bleed extends MultiComponent
{

	public int Duration;
	public int Damage;
	public IntArrayList Durations;

	@Override
	void OnNewStack()
	{
		Durations.add(Duration);
	}
}
