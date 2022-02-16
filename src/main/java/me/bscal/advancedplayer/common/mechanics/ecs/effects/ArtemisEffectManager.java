package me.bscal.advancedplayer.common.mechanics.ecs.effects;

import com.artemis.*;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.IntBag;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.RefPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

import java.io.*;

public class ArtemisEffectManager
{

	public static World World;
	public static final WorldSerializationManager SerializationManager = new WorldSerializationManager();
	public static final Reference2IntOpenHashMap<PlayerEntity> PlayerToEntityId = new Reference2IntOpenHashMap<>();

	public static final String SaveExtension = ".bin";
	public static String SavePath;

	public static void Init()
	{
		WorldConfiguration worldConfig = new WorldConfigurationBuilder().with(SerializationManager).build();

		World = new World(worldConfig);

		SerializationManager.setSerializer(new KryoArtemisSerializer(World));

		SavePath = WorldSavePath.ROOT + "/data/entities/";
	}

	public static void Tick()
	{
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

	public static void LoadPlayer(ServerWorld serverWorld, PlayerEntity player)
	{
		int entityId = -1;
		File file = new File( SavePath + player.getUuid() + SaveExtension);
		if (file.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(file);
				var savedData = SerializationManager.load(fis, SaveFileFormat.class);
				fis.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		Archetype archetype = new ArchetypeBuilder().add(RefPlayer.class).build(World);
		entityId = World.create(archetype);

		Entity entity = World.getEntity(entityId);
		entity.getComponent(RefPlayer.class).PlayerEntity = player;

		PlayerToEntityId.put(player, entityId);
		
	}

	public static void SaveAndRemovePlayer(PlayerEntity player)
	{
		int entityId = PlayerToEntityId.removeInt(player);

		IntBag entities = new IntBag(1);
		entities.add(entityId);

		File file = new File( SavePath + player.getUuid() + SaveExtension);
		file.mkdirs();

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
