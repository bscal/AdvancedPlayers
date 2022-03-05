package me.bscal.advancedplayer.common.mechanics.ecs;

import com.artemis.*;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.IntBag;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.*;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.BleedSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.DebugSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.SyncSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.TemperatureSystem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.nio.ByteBuffer;

public final class ECSManager
{

	public static final String SAVE_EXTENSION = ".bin";
	public static final float DELTA = 1f / 20f; // Minecraft runs 20 ticks per seconds, so I don't think there is a delta?
	public static final Identifier SYNC_CHANNEL = new Identifier(AdvancedPlayer.MOD_ID, "sync");

	// *** Server Side ***
	public static World World;
	public static WorldSerializationManager SerializationManager;
	public static Reference2IntOpenHashMap<PlayerEntity> PlayerToEntityId;

	public static File SavePath;
	public static Archetype PlayerArchetype;

	// *** Client Side ***
	public static World ClientWorld;
	public static WorldSerializationManager ClientSerializationManager;

	/**
	 * A map to lookup components for entities that synced to the client.
	 * Key is the entityId from server. These values are never processed on the client.
	 */
	public static Int2ObjectOpenHashMap<ClientECSContainer> ClientComponents;

	/**
	 * Initializes the main world. This is updated server side, and all components should be created in here
	 */
	public static void Init(MinecraftServer server)
	{
		SerializationManager = new WorldSerializationManager();
		PlayerToEntityId = new Reference2IntOpenHashMap<>();

		WorldConfiguration worldConfig = new WorldConfigurationBuilder().with(SerializationManager, new TemperatureSystem(), new BleedSystem(),
				new DebugSystem(), new SyncSystem()).build();
		worldConfig.register("server", server);
		World = new World(worldConfig);
		World.setDelta(DELTA);
		SerializationManager.setSerializer(new KryoArtemisSerializer(World));

		SavePath = new File(server.getSavePath(WorldSavePath.ROOT) + "/data/entities/");
		PlayerArchetype = new ArchetypeBuilder().add(Temperature.class, Wetness.class, Sync.class).build(World);
	}

	/**
	 * Initializes a world for the client. Only purpose is to sync component data.
	 */
	public static void InitClient()
	{
		ClientComponents = new Int2ObjectOpenHashMap<>();
		ClientSerializationManager = new WorldSerializationManager();
		WorldConfiguration worldConfig = new WorldConfigurationBuilder().with(ClientSerializationManager).build();
		ClientWorld = new World(worldConfig);
		ClientWorld.setDelta(DELTA);
		ClientSerializationManager.setSerializer(new KryoArtemisSerializer(ClientWorld));
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

	public static int GetPlayersEntityId(PlayerEntity player)
	{
		return PlayerToEntityId.getInt(player);
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

	public static void LoadOrCreatePlayer(MinecraftServer server, PlayerEntity player)
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
		RefPlayer refPlayer = new RefPlayer();
		refPlayer.Player = player;
		entity.edit().add(refPlayer);
		PlayerToEntityId.put(player, entityId);
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
		int entityId = PlayerToEntityId.removeInt(player);
		IntBag entities = new IntBag(1);
		entities.add(entityId);

		long start = System.nanoTime();

		ByteBuf buf = Unpooled.buffer(256, 2048);
		PacketByteBuf packetBuf = new PacketByteBuf(buf);
		ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
		SerializationManager.save(bbos, new SaveFileFormat(entities));

/*		ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
		SerializationManager.save(baos, new SaveFileFormat(entities));
		//var bufferArray = baos.toByteArray();
		// TODO maybe look into optimizing this?
		// 1 sync per tick isnt that bad.
		// Maybe a pooled buffer?
		var buffer = Unpooled.buffer(baos.toByteArray().length);
		var packetBuffer = new PacketByteBuf(buffer);
		packetBuffer.writeByteArray(baos.toByteArray());*/
		//buffer.writeByteArray(bufferArray);

		long end = System.nanoTime() - start;
		AdvancedPlayer.LOGGER.info(String.format("Send Took: %dns, %dms, %.1fs", end, end / 1000000, end / 1000000000f));
		AdvancedPlayer.LOGGER.info(String.format("Sending Entity %d, Sizeof %d", entityId, buf.array().length));
		ServerPlayNetworking.send(player, SYNC_CHANNEL, packetBuf);
	}

	public static void ReadEntity(int entityId, byte[] buffer)
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		var savedData = ClientSerializationManager.load(bais, SaveFileFormat.class);
		AdvancedPlayer.LOGGER.info(String.format("Reading Entity %d", savedData.entities.get(0)));
	}

	public static void SyncComponent(ServerPlayerEntity player, Class<? extends Component> clazz)
	{
		Kryo kyro = GetServerKyro();

		var entityId = PlayerToEntityId.getInt(player.getUuid());
		var entity = World.getEntity(entityId);
		var component = entity.getComponent(clazz);
		if (component == null) return;

		ByteBufferOutput output = new ByteBufferOutput(256, 1024);
		output.writeInt(entityId);
		kyro.writeObject(output, component);
		PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.wrappedBuffer(output.getByteBuffer()));
		ServerPlayNetworking.send(player, new Identifier(AdvancedPlayer.MOD_ID, "sync_component"), packetBuf);
	}

	public static Component ReadComponent(int entityId, ByteBuffer buffer, Class<? extends Component> clazz)
	{
		Kryo kyro = GetClientKyro();

		var entity = ClientWorld.getEntity(entityId);
		ByteBufferInput input = new ByteBufferInput(buffer);
		var newComponent = kyro.readObject(input, clazz);
		entity.edit().add(newComponent);
		return newComponent;
	}

	private static Kryo GetClientKyro()
	{
		return ((KryoArtemisSerializer)ClientSerializationManager.getSerializer()).getKryo();
	}

	private static Kryo GetServerKyro()
	{
		return ((KryoArtemisSerializer)SerializationManager.getSerializer()).getKryo();
	}

	public ClientECSContainer GetOrCreateClientComponents(int entityId)
	{
		var container = ClientComponents.get(entityId);
		if (container == null)
		{
			container = new ClientECSContainer();
			ClientComponents.put(entityId, container);
		}
		return container;
	}



}
