package me.bscal.advancedplayer;

import com.google.gson.Gson;
import me.bscal.advancedplayer.common.APPlayerManager;
import me.bscal.advancedplayer.common.commands.ServerCommands;
import me.bscal.advancedplayer.common.entities.EntityRegistry;
import me.bscal.advancedplayer.common.entities.GhoulEntity;
import me.bscal.advancedplayer.common.items.ItemRegistry;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBiomeRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class AdvancedPlayer implements ModInitializer
{

    public static final String MOD_ID = "advancedplayer";
    public static final Logger LOGGER = (Logger) LogManager.getLogger("AdvancedPlayer");
    public static Gson Gson;
    public static APPlayerManager APPlayerManager;
    private static boolean SeasonsEnabled;

    @Override
    public void onInitialize()
    {
        Gson = new Gson();
        Gson = Gson.newBuilder().setPrettyPrinting().create();

        SeasonsEnabled = FabricLoader.getInstance().isModLoaded("seasons");
        LOGGER.info("MCSeasons status: Loaded = " + SeasonsEnabled);
        LOGGER.setLevel(Level.ALL);

        ServerCommands.Init();
        ItemRegistry.Init();
        RegisterEntityAttributes();

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            APPlayerManager = new APPlayerManager(server);
            TemperatureBiomeRegistry.Init(server);
        });

        ServerPlayConnectionEvents.INIT.register((handler, sender) ->
        {
            APPlayerManager.LoadOrCreatePlayer(handler.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
        {
            APPlayerManager.SaveAndRemovePlayer(handler.player);
        });
        ServerTickEvents.END_SERVER_TICK.register(server ->
        {
            for (var apPlayer : APPlayerManager.PlayerList)
            {
                apPlayer.Update(server);
            }
        });
    }

    private void RegisterEntityAttributes()
    {
        FabricDefaultAttributeRegistry.register(EntityRegistry.GHOUL_ENTITY, GhoulEntity.CreateMobAttributes());
    }

    public static boolean IsUsingSeasons()
    {
        return SeasonsEnabled;
    }


}
