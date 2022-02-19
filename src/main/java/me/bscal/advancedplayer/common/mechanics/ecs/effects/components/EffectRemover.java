package me.bscal.advancedplayer.common.mechanics.ecs.effects.components;

import com.artemis.Component;

import java.util.ArrayList;

public class EffectRemover extends Component
{
	public ArrayList<Class<? extends Component>> ClassesToRemove;
}
