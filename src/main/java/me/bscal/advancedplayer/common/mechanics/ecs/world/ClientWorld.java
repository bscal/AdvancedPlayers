package me.bscal.advancedplayer.common.mechanics.ecs.world;

import com.artemis.Component;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Optional;

/**
 * Naive Map of Maps approach to handling ECS over network.
 * This allows clients to create entities and store their components.
 * This seems to be a simple lightweight approach to the problem, with the downside of not having ECS on the client.
 */
public class ClientWorld
{

	private final Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<Class<? extends Component>, Component>> m_InternalComponentMap;

	ClientWorld()
	{
		m_InternalComponentMap = new Int2ObjectOpenHashMap<>();
	}

	public void CreateEntity(int entityId)
	{
		if (!m_InternalComponentMap.containsKey(entityId))
		{
			m_InternalComponentMap.put(entityId, new Object2ObjectOpenHashMap<>());
		}
	}

	public void RemoveEntity(int entityId)
	{
		m_InternalComponentMap.remove(entityId);
	}

	public Optional<Object2ObjectOpenHashMap<Class<? extends Component>, Component>> GetEntityComponents(int entityId)
	{
		var container = m_InternalComponentMap.get(entityId);
		return (container == null) ? Optional.empty() : Optional.of(container);
	}

	public Object2ObjectOpenHashMap<Class<? extends Component>, Component> GetOrCreateEntityComponent(int entityId)
	{
		CreateEntity(entityId);
		return m_InternalComponentMap.get(entityId);
	}

	public void RemoveComponent(int entityId, Class<? extends Component> clazz)
	{
		var container = m_InternalComponentMap.get(entityId);
		if (container == null) return;
		container.remove(clazz);
	}

	public void AddComponent(int entityId, Component component)
	{
		var container = m_InternalComponentMap.get(entityId);
		if (container == null) return;
		container.put(component.getClass(), component);
	}

	public void AddAllComponents(int entityId, Component... components)
	{
		var container = m_InternalComponentMap.get(entityId);
		if (container == null) return;

		for (var component : components)
		{
			container.put(component.getClass(), component);
		}

	}

}
