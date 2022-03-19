package me.bscal.advancedplayer.common.mechanics.ecs;

import com.artemis.*;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.IntBag;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.componentTypes.StackableComponent;
import me.bscal.advancedplayer.common.mechanics.ecs.components.*;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.BleedSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.DebugSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.SyncSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.TemperatureSystem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class ECSManager
{

	public static final String SAVE_EXTENSION = ".bin";
	public static final float DELTA = 1f / 20f; // Minecraft runs 20 ticks per seconds, so I don't think there is a delta?
	public static final Identifier SYNC_CHANNEL = new Identifier(AdvancedPlayer.MOD_ID, "sync");
	public static final Identifier CREATE_CHANNEL = new Identifier(AdvancedPlayer.MOD_ID, "create");

	// *** Server Side ***
	public static World World;
	public static WorldSerializationManager SerializationManager;
	public static Reference2IntOpenHashMap<PlayerEntity> PlayerToEntityId;

	public static File SavePath;
	public static Archetype PlayerArchetype;

	// *** Client Side ***
	public static World ClientWorld;
	public static WorldSerializationManager ClientSerializationManager;
	public static Archetype ClientArchetype;
	public static int ClientEntityId = -1;
	public static EntitySubscription Subscription;

	// TODO
	public static Int2IntOpenHashMap NetworkIdToEntityId;

	/**
	 * Initializes the main world. This is updated server side, and all components should be created in here
	 */
	public static void InitServer(MinecraftServer server)
	{
		SerializationManager = new WorldSerializationManager();

		PlayerToEntityId = new Reference2IntOpenHashMap<>();

		WorldConfiguration worldConfig = new WorldConfigurationBuilder().with(SerializationManager, new TemperatureSystem(), new BleedSystem(),
				new DebugSystem(), new SyncSystem()).build();
		worldConfig.register("server", server);
		World = new World(worldConfig);
		World.setDelta(DELTA);

		var kryoSerializer = new KryoArtemisSerializer(World);
		kryoSerializer.register(Sync.AddContainer.class, new Sync.AddContainer());
		kryoSerializer.register(Sync.ClassContainer.class, new Sync.ClassContainer());
		kryoSerializer.register(Class.class, new Sync.ClassSerializer());
		SerializationManager.setSerializer(kryoSerializer);

		SavePath = new File(server.getSavePath(WorldSavePath.ROOT) + "/data/entities/");
		PlayerArchetype = new ArchetypeBuilder().add(Temperature.class, Wetness.class, Sync.class).build(World);

		AdvancedPlayer.LOGGER.info("Initialized ECSManager Server!");
	}

	/**
	 * Initializes a world for the client. Only purpose is to sync component data.
	 */
	public static void InitClient()
	{
		ClientSerializationManager = new WorldSerializationManager();
		WorldConfiguration worldConfig = new WorldConfigurationBuilder().with(ClientSerializationManager).build();
		ClientWorld = new World(worldConfig);
		ClientWorld.setDelta(DELTA);
		ClientWorld.getComponentManager().getTypeFactory().getTypeFor(Temperature.class);

		var kryoSerializer = new KryoArtemisSerializer(ClientWorld);
		kryoSerializer.register(Sync.AddContainer.class, new Sync.AddContainer());
		kryoSerializer.register(Sync.ClassContainer.class, new Sync.ClassContainer());
		kryoSerializer.register(Class.class, new Sync.ClassSerializer());
		ClientSerializationManager.setSerializer(kryoSerializer);

		ClientArchetype = new ArchetypeBuilder().add(Sync.class).build(ClientWorld);

		Subscription = ClientWorld.getAspectSubscriptionManager().get(Aspect.all(Sync.class));

		AdvancedPlayer.LOGGER.info("Initialized ECSManager Client!");
	}

	public static void Tick()
	{
		/*
			Not really sure what to put for as the delta. It wouldn't really make sense
			to use Minecraft's renderer's tickDelta. So Minecraft runs at 20 ticks per second
			the delta kind of already is there? Possible to just use 1f for 1 tick though too,
			which I do like.
		 */

		World.process();
	}

	public static Component AddComponent(int entityId, Class<? extends Component> clazz)
	{
		Entity entity = World.getEntity(entityId);
		Component component = entity.getComponent(clazz);
		if (component == null)
		{
			component = entity.edit().create(clazz);
			if (component instanceof StackableComponent stackable) stackable.OnGainStack();
		}
		return component;
	}

	public static Component AddComponent(PlayerEntity player, Class<? extends Component> clazz)
	{
		return AddComponent(PlayerToEntityId.getInt(player.getUuid()), clazz);
	}

	public static Component RemoveStack(int entityId, Class<? extends Component> clazz)
	{
		Entity entity = World.getEntity(entityId);
		Component component = entity.getComponent(clazz);
		if (component instanceof StackableComponent stackable)
		{
			stackable.OnLoseStack();
			if (stackable.IsEmpty()) entity.edit().remove(clazz);
		}
		else entity.edit().remove(clazz);
		return component;
	}

	public static Component RemoveStack(PlayerEntity player, Class<? extends Component> clazz)
	{
		return AddComponent(PlayerToEntityId.getInt(player.getUuid()), clazz);
	}

	public static void RemoveComponent(PlayerEntity player, Class<? extends Component> clazz)
	{
		Entity entity = World.getEntity(PlayerToEntityId.getInt(player.getUuid()));
		entity.edit().remove(clazz);
	}

	public static void LoadOrCreatePlayer(ServerPlayerEntity player)
	{
		int entityId = -1;

		File file = new File(SavePath, player.getUuid() + SAVE_EXTENSION);
		if (file.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(file);
				var savedData = SerializationManager.load(fis, SaveFileFormat.class);
				entityId = savedData.entities.get(0);
				fis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			entityId = World.create(PlayerArchetype);
		}
		Entity entity = World.getEntity(entityId);

		// Creates a RefPlayer Component. Is transient
		RefPlayer refPlayer = new RefPlayer();
		refPlayer.Player = player;
		entity.edit().add(refPlayer);

		PlayerToEntityId.put(player, entityId);

		var buffer = new PacketByteBuf(Unpooled.buffer(4));
		buffer.writeInt(player.getId());
		ServerPlayNetworking.send(player, CREATE_CHANNEL, buffer);
	}

	public static void SaveAndRemovePlayer(MinecraftServer server, PlayerEntity player)
	{
		int entityId = PlayerToEntityId.removeInt(player);

		IntBag entities = new IntBag(1);
		entities.add(entityId);

		File file = new File(SavePath, player.getUuid() + SAVE_EXTENSION);
		file.getParentFile().mkdirs();

		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			SerializationManager.save(fos, new SaveFileFormat(entities));
			fos.close();
		}
		catch (IOException e)
		{
			AdvancedPlayer.LOGGER.error("Could not save player");
			e.printStackTrace();
		}

		// Makes sure to set Player to null
		World.getEntity(entityId).getComponent(RefPlayer.class).Player = null;
		World.delete(entityId);
	}

	public static void SyncEntity(ServerPlayerEntity player)
	{
		long start = System.nanoTime();

		int entityId = PlayerToEntityId.removeInt(player);
		IntBag entities = new IntBag(1);
		entities.add(entityId);

		ByteBuf buf = Unpooled.buffer(256, 2048);
		PacketByteBuf packetBuf = new PacketByteBuf(buf);
		ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
		SerializationManager.save(bbos, new SaveFileFormat(entities));

		ServerPlayNetworking.send(player, SYNC_CHANNEL, packetBuf);

		long end = System.nanoTime() - start;
		AdvancedPlayer.LOGGER.info(String.format("Send Took: %dns, %dms, %.1fs", end, end / 1000000, end / 1000000000f));
		AdvancedPlayer.LOGGER.info(String.format("Sending Entity %d, Sizeof %d", entityId, buf.array().length));
	}

	public static void CreateUser(int networkId)
	{
		var entity = ClientWorld.createEntity(ClientArchetype);
		ClientEntityId = entity.getId();
		entity.getComponent(Sync.class).NetworkId = networkId;
		AdvancedPlayer.LOGGER.info(String.format("Created user on client, entityId %d, networkId %d", ClientEntityId, networkId));
	}

	/**
	 * TODO possibly create a client only system to handle this?
	 * and/or a Int2Int map NetworkId -> EntityId for faster look ups
	 */
	public static void ReadEntity(byte[] buffer)
	{
		long start = System.nanoTime();

		ByteBufferInput input = new ByteBufferInput(ByteBuffer.wrap(buffer));
		Sync serverSync = GetClientKyro().readObject(input, Sync.class);
		var entities = Subscription.getEntities();
		boolean found = false;
		for (int i = 0; i < entities.size(); i++)
		{
			Entity entity = ClientWorld.getEntity(i);
			Sync clientSync = entity.getComponent(Sync.class);
			if (clientSync.NetworkId == serverSync.NetworkId)
			{
				InitEntity(entity.edit(), serverSync.Components, serverSync.RemovedComponents);
				found = true;
				break;
			}
		}

		if (!found)
		{
			Entity entity = ClientWorld.createEntity(ClientArchetype);
			entity.getComponent(Sync.class).NetworkId = serverSync.NetworkId;
			InitEntity(entity.edit(), serverSync.Components, null);
		}

		long end = System.nanoTime() - start;
		AdvancedPlayer.LOGGER.info(
				String.format("Reading Entity %d. Adds %d, Removes %d. Took: %dns, %dms. ", serverSync.NetworkId, serverSync.AddedComponents.size(),
						serverSync.RemovedComponents.size(), end, end / 1000000));
		AdvancedPlayer.LOGGER.info("Was entity found? " + found);
	}

	public static void InitEntity(EntityEdit edit, List<Sync.AddContainer> components, Set<Sync.ClassContainer> removedComponents)
	{
		for (var added : components)
		{
			edit.add(added.Component);
		}

		if (removedComponents == null) return;

		for (var removed : removedComponents)
			edit.remove(removed.Clazz);
	}

	public static Kryo GetClientKyro()
	{
		return ((KryoArtemisSerializer) ClientSerializationManager.getSerializer()).getKryo();
	}

	public static Kryo GetServerKyro()
	{
		return ((KryoArtemisSerializer) SerializationManager.getSerializer()).getKryo();
	}

	public static void CleanupClient()
	{
		ClientWorld = null;
		ClientEntityId = -1;
	}

	public static Component GetClientComponent(Class<? extends Component> clazz)
	{
		var entity = ClientWorld.getEntity(ClientEntityId);
		if (entity == null) return null;
		return entity.getComponent(clazz);
	}

	public static boolean IsClient(int networkId)
	{
		if (MinecraftClient.getInstance().player == null) return false;
		return MinecraftClient.getInstance().player.getId() == networkId;
	}

	public static class MultiMap
	{
		private final Int2ObjectOpenHashMap<List<Component>> m_InternalMap;

		MultiMap()
		{
			m_InternalMap = new Int2ObjectOpenHashMap<>();
		}

		public List<Component> Get(int netId)
		{
			var list = m_InternalMap.get(netId);
			return (list == null) ? Collections.emptyList() : list;
		}

		public void Add(int netId, Component component)
		{
			var list = m_InternalMap.get(netId);
			if (list == null)
			{
				list = new ArrayList<>();
				m_InternalMap.put(netId, list);
			}
			list.add(component);
		}

		public void Remove(int netId)
		{
			m_InternalMap.remove(netId);
		}

	}

}
