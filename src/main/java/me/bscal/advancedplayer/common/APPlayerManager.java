package me.bscal.advancedplayer.common;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class APPlayerManager
{

    public static final String SAVE_EXTENSION = ".bin";

    public final Object2ObjectOpenHashMap<UUID, APPlayer> UUIDToPlayerMap;
    public final List<APPlayer> PlayerList;
    public final String SavePath;

    public APPlayerManager(MinecraftServer server)
    {
        UUIDToPlayerMap = new Object2ObjectOpenHashMap<>(10);
        PlayerList = new ArrayList<>(10);
        SavePath = server.getSavePath(WorldSavePath.ROOT) + "/" + AdvancedPlayer.MOD_ID + "/players/";
    }

    public void AddAPPlayer(ServerPlayerEntity serverPlayer, APPlayer apPlayer)
    {
        if (UUIDToPlayerMap.putIfAbsent(serverPlayer.getUuid(), apPlayer) == null)
        {
            PlayerList.add(apPlayer);
        }
    }

    public APPlayer RemoveAPPlayer(UUID uuid)
    {
        var apPlayer = UUIDToPlayerMap.remove(uuid);
        if (apPlayer != null)
        {
            PlayerList.remove(apPlayer);
        }
        return apPlayer;
    }

    public APPlayer LoadOrCreatePlayer(ServerPlayerEntity serverPlayerEntity)
    {
        APPlayer result = UUIDToPlayerMap.get(serverPlayerEntity.getUuid());
        if (result != null) return result;

        File file = new File(SavePath, serverPlayerEntity.getUuid() + SAVE_EXTENSION);
        if (file.exists())
        {
            try
            {
                FileInputStream fis = new FileInputStream(file);
                byte[] data = fis.readAllBytes();
                PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(data));
                result = new APPlayer(serverPlayerEntity);
                result.Deserialize(buf);
                fis.close();
            } catch (IOException e)
            {
                AdvancedPlayer.LOGGER.error("Could not load player");
                e.printStackTrace();
            }
        }
        if (result == null)
            result = new APPlayer(serverPlayerEntity);
        result.Sync();
        AddAPPlayer(serverPlayerEntity, result);
        return result;
    }

    public void SaveAndRemovePlayer(ServerPlayerEntity serverPlayerEntity)
    {
        APPlayer player = RemoveAPPlayer(serverPlayerEntity.getUuid());
        if (player != null)
        {
            File file = new File(SavePath, serverPlayerEntity.getUuid() + SAVE_EXTENSION);
            file.getParentFile().mkdirs();
            PacketByteBuf buf = new PacketByteBuf(
                    PooledByteBufAllocator.DEFAULT.directBuffer(128, 1024));
            player.Serialize(buf);
            try
            {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(buf.getWrittenBytes());
                fos.close();
            } catch (IOException e)
            {
                AdvancedPlayer.LOGGER.error("Could not save player");
                e.printStackTrace();
            }
        }
    }
}
