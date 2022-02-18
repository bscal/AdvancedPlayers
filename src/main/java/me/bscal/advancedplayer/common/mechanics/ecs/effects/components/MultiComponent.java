package me.bscal.advancedplayer.common.mechanics.ecs.effects.components;

import com.artemis.Component;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * A component which can internally handle multiple stacks of itself with different durations.
 * This component has a helper function OnNewStack() which is only used when trying to add this
 * component to an Entity which already has it.
 *
 * It is up to the Component and System to implement how these "Stacks" are handled and removed.
 */
public abstract class MultiComponent extends Component
{

	abstract void OnNewStack();

}
