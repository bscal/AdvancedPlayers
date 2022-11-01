package me.bscal.advancedplayer;

import com.google.gson.Gson;
import me.bscal.advancedplayer.common.APPlayerManager;
import me.bscal.advancedplayer.common.BiomeTemperatures;
import me.bscal.advancedplayer.common.ItemStackMixinInterface;
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
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class AdvancedPlayer implements ModInitializer
{

    public static final String MOD_ID = "advancedplayer";
    public static final Logger LOGGER = (Logger) LogManager.getLogger("AdvancedPlayer");
    public static Gson Gson;
    public static APPlayerManager APPlayerManager;
    public static BiomeTemperatures BiomeTemperatures;
    private static boolean SeasonsEnabled;
    public static MinecraftServer Server;

    public static long NextSpoilIncrement = 20 * 30;
    public static long NextSpoilTick;
    private static int NextSpoilCounter = (int)NextSpoilIncrement;

    public static final String KEY_ITEMSTACK_SPOIL = "SpoilDuration";
    public static final String KEY_ITEMSTACK_SPOIL_END = "SpoilEnd";
    public static final String KEY_ITEMSTACK_SPOIL_RATE = "SpoilRate";
    public static final String KEY_ITEMSTACK_IS_SPOILED = "SpoiledFood";

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

        BiomeTemperatures = new BiomeTemperatures();

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
        {
            Server = server;
            APPlayerManager = new APPlayerManager(server);
            TemperatureBiomeRegistry.Init(server);
            BiomeTemperatures.Init(server.getOverworld());
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
            int tick = server.getTicks();

            if (NextSpoilCounter++ == NextSpoilIncrement)
            {
                NextSpoilCounter = 0;
                NextSpoilTick = server.getOverworld().getTime() + NextSpoilIncrement;
                LOGGER.info("TICKED");
            }

            for (var apPlayer : APPlayerManager.PlayerList)
            {
                if (NextSpoilCounter == NextSpoilIncrement)
                {
                    var screen = apPlayer.Player.currentScreenHandler;
                    if (screen != null)
                    {
                        for (var slot : screen.slots)
                        {
                            var stack = slot.getStack();
                            ((ItemStackMixinInterface) (Object) stack).UpdateSpoilage(stack, apPlayer.Player.world.getTime());
                        }
                    }
                }
                apPlayer.Update(server, tick);
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
