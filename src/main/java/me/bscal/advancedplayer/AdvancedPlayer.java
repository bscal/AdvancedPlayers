package me.bscal.advancedplayer;

import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.ArtemisEffectManager;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.events.PlayerCopy;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.events.PlayerDeath;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.events.PlayerRespawn;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ArtemisEffectManager.Init(server);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ArtemisEffectManager.LoadOrCreatePlayer(server, handler.player);

			LOGGER.info(handler.player.getDisplayName().asString() + " has connected");
			ComponentManager.BODY_TEMPERATURE.get(handler.player);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ArtemisEffectManager.SaveAndRemovePlayer(server, handler.player);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ArtemisEffectManager.Tick();
		});

		ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
			var event = new PlayerDeath();
			event.Player = player;
			event.DamageSource = damageSource;
			event.DamageAmount = damageAmount;
			ArtemisEffectManager.EventSystem.dispatch(event);
			return !event.Cancel;
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			var event = new PlayerCopy();
			event.OldPlayer = oldPlayer;
			event.NewPlayer = newPlayer;
			event.Alive = alive;
			ArtemisEffectManager.EventSystem.dispatch(event);
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			var event = new PlayerRespawn();
			event.OldPlayer = oldPlayer;
			event.NewPlayer = newPlayer;
			event.Alive = alive;
			ArtemisEffectManager.EventSystem.dispatch(event);
		});

	}

	public static boolean IsUsingSeasons()
	{
		return SEASONS_ENABLED;
	}
}
