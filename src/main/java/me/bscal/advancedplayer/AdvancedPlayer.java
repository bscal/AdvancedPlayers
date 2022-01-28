package me.bscal.advancedplayer;

import me.bscal.advancedplayer.common.components.ComponentManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancedPlayer implements ModInitializer
{

	public static final String MOD_ID = "advancedplayer";
	public static final Logger LOGGER = LogManager.getLogger("AdvancedPlayer");

	private static boolean SEASONS_ENABLED;

	@Override
	public void onInitialize()
	{
		SEASONS_ENABLED = FabricLoader.getInstance().isModLoaded("seasons");
		LOGGER.info("MCSeasons status: Loaded = " + SEASONS_ENABLED);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
		{
			LOGGER.info(handler.player.getDisplayName().asString() + " has connected");
			ComponentManager.BODY_TEMPERATURE.get(handler.player);
		});
	}

	public static boolean IsUsingSeasons()
	{
		return SEASONS_ENABLED;
	}
}
