package me.bscal.advancedplayer;

import com.esotericsoftware.kryo.Kryo;
import com.google.gson.Gson;
import me.bscal.advancedplayer.common.commands.ServerCommands;
import me.bscal.advancedplayer.common.ecs.ECSManager;
import me.bscal.advancedplayer.common.ecs.ECSManagerServer;
import me.bscal.advancedplayer.common.entities.EntityRegistry;
import me.bscal.advancedplayer.common.entities.GhoulEntity;
import me.bscal.advancedplayer.common.events.DamageEvents;
import me.bscal.advancedplayer.common.items.ItemRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.util.Optional;
import java.util.Random;

public class AdvancedPlayer implements ModInitializer
{

	public static final String MOD_ID = "advancedplayer";
	public static final Logger LOGGER = (Logger) LogManager.getLogger("AdvancedPlayer");

	public static Kryo Kryo;
	public static Gson Gson;
	public static ECSManagerServer ECSManagerServer;
	public static Random Random;

	private static boolean SeasonsEnabled;
	private static MinecraftServer Server;

	@Override
	public void onInitialize()
	{
		Kryo = new Kryo();
		ECSManager.InitKryo(Kryo);

		Gson = new Gson();
		Gson = Gson.newBuilder().setPrettyPrinting().create();

		Random = new Random();

		SeasonsEnabled = FabricLoader.getInstance().isModLoaded("seasons");
		LOGGER.info("MCSeasons status: Loaded = " + SeasonsEnabled);
		LOGGER.setLevel(Level.ALL);

		ServerCommands.Init();
		ItemRegistry.Init();
		RegisterEntityAttributes();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			Server = server;
			ECSManagerServer = new ECSManagerServer(server);
		});

		ServerPlayConnectionEvents.INIT.register((handler, sender) -> ECSManagerServer.LoadOrCreatePlayer(handler.player));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ECSManagerServer.SaveAndRemovePlayer(handler.player));
		ServerTickEvents.END_SERVER_TICK.register(server -> ECSManagerServer.Tick(server));
	}

	private void RegisterEntityAttributes()
	{
		FabricDefaultAttributeRegistry.register(EntityRegistry.GHOUL_ENTITY, GhoulEntity.CreateMobAttributes());
	}

	public static boolean IsUsingSeasons()
	{
		return SeasonsEnabled;
	}

	public static Optional<World> GetMainWorld()
	{
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) return Optional.ofNullable(MinecraftClient.getInstance().world);
		else return Optional.ofNullable(Server.getOverworld());
	}

	public static Kryo GetServerKryo()
	{
		return ECSManagerServer.GetKryo();
	}

	public static MinecraftServer GetServer()
	{
		return Server;
	}

}
