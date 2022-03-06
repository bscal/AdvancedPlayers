package me.bscal.advancedplayer;

import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.mechanics.ecs.ECSManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class AdvancedPlayer implements ModInitializer
{

	public static final String MOD_ID = "advancedplayer";
	public static final Logger LOGGER = (Logger) LogManager.getLogger("AdvancedPlayer");

	private static boolean SeasonsEnabled;

	@Override
	public void onInitialize()
	{
		SeasonsEnabled = FabricLoader.getInstance().isModLoaded("seasons");
		LOGGER.info("MCSeasons status: Loaded = " + SeasonsEnabled);
		LOGGER.setLevel(Level.ALL);

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ECSManager.Init(server);
		});

		ServerPlayConnectionEvents.INIT.register((handler, sender) -> {
			ECSManager.LoadOrCreatePlayer(handler.player);

			LOGGER.info(handler.player.getDisplayName().asString() + " has connected");
			//ComponentManager.BODY_TEMPERATURE.get(handler.player);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ECSManager.SaveAndRemovePlayer(server, handler.player);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ECSManager.Tick();
		});
	}

	public static boolean IsUsingSeasons()
	{
		return SeasonsEnabled;
	}

}
