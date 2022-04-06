package me.bscal.advancedplayer.common.ecs;

import com.artemis.Component;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ECSManagerClient extends ECSManager
{

	public final Int2ObjectOpenHashMap<EntityContainer> IdToEntity;
	private final Kryo m_Kryo;

	public ECSManagerClient(Kryo kryo)
	{
		IdToEntity = new Int2ObjectOpenHashMap<>();
		m_Kryo = kryo;
	}

	public void EntityCreated(int netId, int entityId)
	{
		GetOrCreateContainer(netId);
	}

	public void EntityRemoved(int netId)
	{
		IdToEntity.remove(netId);
	}

	public EntityContainer GetOrCreateContainer(int netId)
	{
		var container = IdToEntity.get(netId);
		if (container == null)
		{
			container = new EntityContainer();
			IdToEntity.put(netId, container);
		}
		return container;
	}

	public Optional<Component> GetComponent(Entity entity, Class<?> clazz)
	{
		return GetComponent(entity.getId(), clazz);
	}

	public Optional<Component> GetComponent(int netId, Class<?> clazz)
	{
		var container = IdToEntity.get(netId);
		if (container == null) return Optional.empty();
		return container.GetComponent(clazz);
	}

	public void ComponentsAdd(int netId, List<Component> components)
	{
		var container = GetOrCreateContainer(netId);
		for (var component : components)
		{
			container.EntityComponents.put(component.getClass().getName(), component);
		}
	}

	public void ComponentAdd(int netId, Component component)
	{
		var container = GetOrCreateContainer(netId);
		container.EntityComponents.put(container.getClass().getName(), component);
	}

	public void ComponentRemove(int netId, List<Class<?>> clazzes)
	{
		var container = IdToEntity.get(netId);
		if (container == null) return;
		clazzes.forEach(container.EntityComponents::remove);
	}

	/**
	 * 	Hopefully thread safe only does some reading
	 */
	public void HandleSyncPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		final Input input = new Input();
		input.setBuffer(buf.getWrittenBytes());

		final int networkId = input.readInt();
		final int entityId = input.readInt();

		client.execute(() -> {
			long start = System.nanoTime();

			final ArrayList<Component> addedComponents = (ArrayList<Component>) m_Kryo.readObject(input, ArrayList.class);
			final ArrayList<Class<?>> removedComponents = (ArrayList<Class<?>>) m_Kryo.readObject(input, ArrayList.class);

			ComponentsAdd(networkId, addedComponents);
			ComponentRemove(networkId, removedComponents);
			input.close();

			long end = System.nanoTime() - start;
			AdvancedPlayer.LOGGER.info("Reading sync: {}ns, {}ms", end, end / 1000000);
		});
	}

	public static class EntityContainer
	{
		public final Object2ObjectOpenHashMap<String, Component> EntityComponents;

		public EntityContainer()
		{
			EntityComponents = new Object2ObjectOpenHashMap<>();
		}

		public Optional<Component> GetComponent(Class<?> clazz)
		{
			return Optional.ofNullable(EntityComponents.get(clazz.getName()));
		}
	}

	@Override
	public Kryo GetKryo()
	{
		return m_Kryo;
	}

}
