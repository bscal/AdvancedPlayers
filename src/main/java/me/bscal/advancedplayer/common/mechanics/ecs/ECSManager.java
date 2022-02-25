package me.bscal.advancedplayer.common.mechanics.ecs;

import com.artemis.*;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.IntBag;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.StackableComponent;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.BleedSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.systems.DebugSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ECSManager
{

	public static final String SAVE_EXTENSION = ".bin";
	public static final float DELTA = 1f / 20f; // Minecraft runs 20 ticks per seconds, so I don't think there is a delta?

	public static World World;
	public static WorldSerializationManager SerializationManager;
	public static Reference2IntOpenHashMap<PlayerEntity> PlayerToEntityId;
	public static File SavePath;
	public static Archetype Archetype;

	public static void Init(MinecraftServer server)
	{
		SerializationManager = new WorldSerializationManager();
		PlayerToEntityId = new Reference2IntOpenHashMap<>();
		WorldConfiguration worldConfig = new WorldConfigurationBuilder().with(SerializationManager, new BleedSystem(), new DebugSystem()).build();
		worldConfig.register("server", server);

		World = new World(worldConfig);
		World.setDelta(DELTA);

		SerializationManager.setSerializer(new KryoArtemisSerializer(World));

		SavePath = new File(server.getSavePath(WorldSavePath.ROOT) + "/data/entities/");

		Archetype = new ArchetypeBuilder().add(RefPlayer.class).build(World);
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
			entityId = World.create(Archetype);
		}
		Entity entity = World.getEntity(entityId);
		entity.getComponent(RefPlayer.class).Player = player;
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

		World.delete(entityId);
	}

}
