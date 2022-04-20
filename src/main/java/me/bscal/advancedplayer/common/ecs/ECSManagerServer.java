package me.bscal.advancedplayer.common.ecs;

import com.artemis.*;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.IntBag;
import com.esotericsoftware.kryo.Kryo;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ecs.components.*;
import me.bscal.advancedplayer.common.ecs.components.health.Health;
import me.bscal.advancedplayer.common.ecs.systems.*;
import me.bscal.advancedplayer.common.utils.ServerPlayerAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ECSManagerServer extends ECSManager
{
	public World World;
	public WorldSerializationManager SerializationManager;
	public Object2IntOpenHashMap<UUID> UUIDToEntityId;

	public File SavePath;
	public Archetype PlayerArchetype;

	public ECSManagerServer(MinecraftServer server)
	{
		SerializationManager = new WorldSerializationManager();
		UUIDToEntityId = new Object2IntOpenHashMap<>();
		UUIDToEntityId.defaultReturnValue(-1);

		WorldConfiguration worldConfig = new WorldConfigurationBuilder().with(
				SerializationManager,
				new TemperatureSystem(),
				new ThirstSystem(),
				new BleedSystem(),
				new DebugSystem(),
				new SyncSystem(),
				new HealthSystem()).build();

		worldConfig.register("server", server);
		World = new World(worldConfig);
		World.setDelta(DELTA);

		var kryoSerializer = new KryoArtemisSerializer(World);
		InitKryo(kryoSerializer.getKryo());
		SerializationManager.setSerializer(kryoSerializer);



		SavePath = new File(server.getSavePath(WorldSavePath.ROOT) + "/data/entities/");
		PlayerArchetype = new ArchetypeBuilder().add(
				Temperature.class,
				Wetness.class,
				Thirst.class,
				Health.class).build(World);

		AdvancedPlayer.LOGGER.info("Initialized ECSManager Server!");
	}

	public int GetEntity(UUID uuid)
	{
		return UUIDToEntityId.getInt(uuid);
	}

	public int GetEntity(net.minecraft.entity.Entity minecraftEntity) { return UUIDToEntityId.getInt(minecraftEntity.getUuid()); }

	public void CreateEntity(UUID uuid)
	{
		int entityId = World.create();
		UUIDToEntityId.put(uuid, entityId);
	}

	public void RemoveEntity(UUID uuid)
	{
		int entityId = UUIDToEntityId.removeInt(uuid);
		if (entityId >= 0) World.delete(entityId);
	}

	public void Tick(MinecraftServer server)
	{
		World.process();
	}

	@Override
	public Kryo GetKryo()
	{
		return ((KryoArtemisSerializer)SerializationManager.getSerializer()).getKryo();
	}

	public void LoadOrCreatePlayer(ServerPlayerEntity serverPlayerEntity)
	{
		int entityId = -1;

		File file = new File(SavePath, serverPlayerEntity.getUuid() + SAVE_EXTENSION);
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

		// Creates transient components
		RefPlayer refPlayer = new RefPlayer();
		refPlayer.Player = serverPlayerEntity;
		entity.edit().add(refPlayer);

		entity.edit().add(new Sync());

		((ServerPlayerAccess)serverPlayerEntity).SetAPEntityId(entityId);
		UUIDToEntityId.put(serverPlayerEntity.getUuid(), entityId);
	}

	public void SaveAndRemovePlayer(ServerPlayerEntity serverPlayerEntity)
	{
		int entityId = UUIDToEntityId.removeInt(serverPlayerEntity);

		IntBag entities = new IntBag(1);
		entities.add(entityId);

		File file = new File(SavePath, serverPlayerEntity.getUuid() + SAVE_EXTENSION);
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
}
