package me.bscal.advancedplayer.common.ecs.components;

import com.artemis.Component;
import com.artemis.ComponentType;
import com.artemis.annotations.Transient;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A components used to handle syncing other components with the client.
 * Has some utility methods. Does not get saved.
 * TODO maybe look at performance of these classes?
 */
@Transient public class Sync extends Component
{

	/**
	 * Server and client entityId's are not the same.
	 * Uses Minecraft's ServerPlayerEntity.getId() as the NetworkId
	 */
	public int NetworkId;

	/**
	 * List of AddContainers which hold the components instance, and components class name.
	 */
	public List<Component> ComponentsToAdd = new ArrayList<>();

	/**
	 * A set to keep track of what classes are removed. Sent to client to remove any classes.
	 */
	public List<Class<?>> ComponentsToRemove = new ArrayList<>();

	/**
	 * A Set to keep track of what classes are added.
	 * This isn't sent to client, just keeps track of added classes.
	 */
	transient public Set<Class<? extends Component>> Components = new ObjectOpenHashSet<>();

	/**
	 * A utility method for adding a component. Some safety checks, and creates the containers.
	 */
	public void Add(Component c)
	{
		Components.add(c.getClass());
		if (!ComponentsToAdd.contains(c))
			ComponentsToAdd.add(c);
	}

	/**
	 * A utility method for removing a component.
	 */
	public void Remove(Class<? extends Component> clazz)
	{
		Components.remove(clazz);
		if (!ComponentsToRemove.contains(clazz))
			ComponentsToRemove.add(clazz);

	}

	public void Clear()
	{
		ComponentsToAdd.clear();
		ComponentsToRemove.clear();
	}

	public boolean IsEmpty()
	{
		return ComponentsToAdd.isEmpty() && ComponentsToRemove.isEmpty();
	}

}
