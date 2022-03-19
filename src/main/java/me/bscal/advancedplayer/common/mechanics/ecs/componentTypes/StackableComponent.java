package me.bscal.advancedplayer.common.mechanics.ecs.componentTypes;

/**
 * A component which can internally handle multiple stacks of itself with different durations.
 * This component has a helper function OnNewStack() which is only used when trying to add this
 * component to an Entity which already has it.
 * <br>
 * It is up to the Component and System to implement how these "Stacks" are handled and removed.
 */
public interface StackableComponent
{

	void OnGainStack();

	void OnLoseStack();

	boolean IsEmpty();

}
