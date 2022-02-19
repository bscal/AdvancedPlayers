package me.bscal.advancedplayer.common.mechanics.ecs.effects;

import com.artemis.*;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.IntBag;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.StackableComponent;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.systems.BleedSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.systems.DebugSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.mostlyoriginal.api.event.common.EventSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArtemisEffectManager
{

	public static World World;
	public static EventSystem EventSystem;
	public static final WorldSerializationManager SerializationManager = new WorldSerializationManager();
	public static final Reference2IntOpenHashMap<PlayerEntity> PlayerToEntityId = new Reference2IntOpenHashMap<>();

	public static final String SaveExtension = ".bin";
	public static File SavePath;

	public static Archetype Archetype;

	public static void Init(MinecraftServer server)
	{
		EventSystem = new EventSystem();
		WorldConfiguration worldConfig = new WorldConfigurationBuilder().with(SerializationManager, EventSystem, new BleedSystem(), new DebugSystem()).build();
		worldConfig.register("server", server);

		World = new World(worldConfig);

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
		World.setDelta(1f / 20f);
		World.process();
	}

	public static int GetPlayersEntityId(PlayerEntity player)
	{
		return PlayerToEntityId.getInt(player);
	}

	public static void AddComponent(PlayerEntity player, Component component)
	{
		Entity entity = World.getEntity(GetPlayersEntityId(player));
		entity.edit().add(component);
	}

	public static void AddComponent(int entityId, Component component)
	{
		Entity entity = World.getEntity(entityId);
		entity.edit().add(component);
	}

	public static void AddComponent(int entityId, Component component, boolean stack)
	{

	}

	public static Component AddEffect(PlayerEntity player, Class<? extends Component> componentClazz)
	{
		Entity entity = World.getEntity(GetPlayersEntityId(player));
		Component component = entity.getComponent(componentClazz);
		if (component == null) component = entity.edit().create(componentClazz);
		if (component instanceof StackableComponent stackableComponent)
		{
			stackableComponent.OnNewStack();
		}
		return component;
	}

	public static void AddStackToComponent(int entityId, Class<? extends Component> componentClazz)
	{
		Entity entity = World.getEntity(entityId);
		Component component = entity.getComponent(componentClazz);
		if (component instanceof StackableComponent stackableComponent)
		{
			stackableComponent.OnNewStack();
		}
	}

	public static void LoadOrCreatePlayer(MinecraftServer server, PlayerEntity player)
	{
		int entityId = -1;

		File file = new File(SavePath, player.getUuid() + SaveExtension);
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

		File file = new File(SavePath, player.getUuid() + SaveExtension);
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
