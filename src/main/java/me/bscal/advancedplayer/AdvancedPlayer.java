package me.bscal.advancedplayer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ReferenceResolver;
import com.google.gson.Gson;
import me.bscal.advancedplayer.common.commands.ServerCommands;
import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.food.MultiFood;
import me.bscal.advancedplayer.common.items.ItemRegistry;
import me.bscal.advancedplayer.common.mechanics.ecs.ECSManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class AdvancedPlayer implements ModInitializer
{

	public static final String MOD_ID = "advancedplayer";
	public static final Logger LOGGER = (Logger) LogManager.getLogger("AdvancedPlayer");

	public static Kryo Kryo;
	public static Gson Gson;

	private static boolean SeasonsEnabled;
	private static MinecraftServer Server;

	@Override
	public void onInitialize()
	{
		Kryo = new Kryo();
		// When using 2 different Kryo instances class instances can be messed up.
		// I believe this fixes it.
		Kryo.setClassLoader(AdvancedPlayer.class.getClassLoader());
		Kryo.register(MultiFood.class);
		Kryo.register(MultiFood.Ingredient.class);
		Kryo.register(MultiFood.FoodGroups.class);
		Kryo.register(MultiFood.Cookable.class);
		Kryo.register(MultiFood.Perishable.class);


		Gson = new Gson();
		Gson = Gson.newBuilder().setPrettyPrinting().create();



		SeasonsEnabled = FabricLoader.getInstance().isModLoaded("seasons");
		LOGGER.info("MCSeasons status: Loaded = " + SeasonsEnabled);
		LOGGER.setLevel(Level.ALL);

		ServerCommands.Init();
		ItemRegistry.Init();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Server = server;
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
