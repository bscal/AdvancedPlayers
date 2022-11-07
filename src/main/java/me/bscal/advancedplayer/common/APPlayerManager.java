package me.bscal.advancedplayer.common;

import com.mojang.serialization.Lifecycle;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class APPlayerManager
{

    public static final String SAVE_EXTENSION = ".bin";
    public static final String TRAITS_SAVE_FILE = "Traits.dat";

    public final Object2ObjectOpenHashMap<UUID, APPlayer> UUIDToPlayerMap;
    public final List<APPlayer> PlayerList;
    public final Object2ObjectOpenHashMap<String, Traits> TraitsRegister;
    public final Object2IntOpenHashMap<String> TraitNameToId;
    public int NextId;
    public final String SavePath;

    public APPlayerManager(MinecraftServer server)
    {
        UUIDToPlayerMap = new Object2ObjectOpenHashMap<>(10);
        PlayerList = new ArrayList<>(10);
        SavePath = server.getSavePath(WorldSavePath.ROOT) + "/" + AdvancedPlayer.MOD_ID + "/players/";
        TraitsRegister = new Object2ObjectOpenHashMap<>();
        TraitNameToId = new Object2IntOpenHashMap<>();
    }

    public void OnStart(MinecraftServer server)
    {
        Traits.RegisterTraits(this);
    }

    public void OnShutdown(MinecraftServer server)
    {
    }

    public Traits Register(String name, TraitsInstance defaultInstance)
    {
        Traits trait = new Traits();
        trait.Name = name;
        trait.Id = NextId++;
        trait.DefaultInstance = defaultInstance;

        TraitsRegister.put(trait.Name, trait);
        TraitNameToId.put(trait.Name, trait.Id);
        return trait;
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
                result = SerializationUtils.deserialize(fis);
                result.Player = serverPlayerEntity;
                // this should auto close
            } catch (IOException e)
            {
                AdvancedPlayer.LOGGER.error("Could not load player");
                e.printStackTrace();
            }
        }
        if (result == null)
            result = new APPlayer(serverPlayerEntity);
        AddAPPlayer(serverPlayerEntity, result);
        result.Sync();
        return result;
    }

    public void SaveAndRemovePlayer(ServerPlayerEntity serverPlayerEntity)
    {
        APPlayer player = RemoveAPPlayer(serverPlayerEntity.getUuid());
        if (player != null)
        {
            File file = new File(SavePath, serverPlayerEntity.getUuid() + SAVE_EXTENSION);
            file.getParentFile().mkdirs();
            try
            {
                FileOutputStream fos = new FileOutputStream(file);
                SerializationUtils.serialize(player, fos);
                // this should auto close
            } catch (IOException e)
            {
                AdvancedPlayer.LOGGER.error("Could not save player");
                e.printStackTrace();
            }
        }
    }
}
