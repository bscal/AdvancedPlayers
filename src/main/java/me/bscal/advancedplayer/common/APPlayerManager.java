package me.bscal.advancedplayer.common;

import com.artemis.Entity;
import com.artemis.utils.IntBag;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.ecs.components.Sync;
import me.bscal.advancedplayer.common.utils.ServerPlayerAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class APPlayerManager
{

   public static final String SAVE_PATH = "";
   public static final String SAVE_EXTENSION = "";

    public void LoadOrCreatePlayer(ServerPlayerEntity serverPlayerEntity)
    {
        APPlayer result;
        File file = new File(SavePath, serverPlayerEntity.getUuid() + SAVE_EXTENSION);
        if (file.exists())
        {
            try
            {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = fis.readAllBytes();
                APP
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
