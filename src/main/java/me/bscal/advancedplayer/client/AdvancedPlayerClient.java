package me.bscal.advancedplayer.client;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.client.debug.DebugWindow;
import me.bscal.advancedplayer.client.ui.PlayerStatusHud;
import me.bscal.advancedplayer.common.ecs.ECSManagerClient;
import me.bscal.advancedplayer.common.ecs.ECSManagerServer;
import me.bscal.advancedplayer.common.entities.EntityRegistry;
import me.bscal.advancedplayer.client.renderers.GhoulRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT) public class AdvancedPlayerClient implements ClientModInitializer
{
	public static KeyBinding DebugTemperatureKeyBind;

	public static final DebugWindow DEBUG_WINDOW = new DebugWindow();
	public static final Identifier TEXTURE_THERMOMETER = GetUiTexture("thermometer");
	public static final Identifier TEXTURE_BURNING = GetUiTexture("temperature_burning");
	public static final Identifier TEXTURE_HOT = GetUiTexture("temperature_hot");
	public static final Identifier TEXTURE_WARM = GetUiTexture("temperature_warm");
	public static final Identifier TEXTURE_NORMAL = GetUiTexture("temperature_normal");
	public static final Identifier TEXTURE_COOL = GetUiTexture("temperature_cool");
	public static final Identifier TEXTURE_COLD = GetUiTexture("temperature_cold");
	public static final Identifier TEXTURE_FREEZING = GetUiTexture("temperature_freezing");
	public static final Identifier TEXTURE_NEUTRAL = GetUiTexture("neutral_line");
	public static final Identifier TEXTURE_UP_CARROT = GetUiTexture("up_carrot");
	public static final Identifier TEXTURE_DOWN_CARROT = GetUiTexture("down_carrot");

	public static SpriteAtlasTexture AtlasTexture;
	public static SpriteAtlasTexture.Data AtlasTextureData;
	public static ECSManagerClient ECSManagerClient;

	@Override
	public void onInitializeClient()
	{
		DebugTemperatureKeyBind = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.advancedplayer.debug_temperature", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, "category.advancedplayer.debug"));

		EntityRendererRegistry.register(EntityRegistry.GHOUL_ENTITY, GhoulRenderer::new);

		ClientPlayConnectionEvents.INIT.register((a, b) -> {
			ECSManagerClient = new ECSManagerClient(AdvancedPlayer.Kryo);
		});

		ClientPlayNetworking.registerGlobalReceiver(ECSManagerServer.SYNC_CHANNEL, (client, handler, buf, responseSender) -> {
			if (ECSManagerClient != null)
				ECSManagerClient.HandleSyncPacket(client, handler, buf, responseSender);
		});

		HudRenderCallback.EVENT.register(DEBUG_WINDOW);

		// Create sprite sheet
		ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
			var rm = client.getResourceManager();
			var id = new Identifier(AdvancedPlayer.MOD_ID, "textures");
			AtlasTexture = new SpriteAtlasTexture(id);
			Stream<Identifier> textures = Stream.of(
					TEXTURE_BURNING,
					TEXTURE_HOT,
					TEXTURE_WARM,
					TEXTURE_NORMAL,
					TEXTURE_COOL,
					TEXTURE_COLD,
					TEXTURE_FREEZING,
					TEXTURE_THERMOMETER,
					TEXTURE_NEUTRAL,
					TEXTURE_DOWN_CARROT,
					TEXTURE_UP_CARROT,
					GetUiTexture("water_skin_sprites"));
			AtlasTextureData = AtlasTexture.stitch(rm, textures, client.getProfiler(), 0);
			AtlasTexture.upload(AtlasTextureData);
			AtlasTexture.registerTexture(client.getTextureManager(), rm, id, (runnable) -> {
			});
			// Registered later so textures are loaded.
			HudRenderCallback.EVENT.register(new PlayerStatusHud());
		});
	}

	public static Identifier GetUiTexture(String filename)
	{
		return new Identifier(AdvancedPlayer.MOD_ID,  "ui/" + filename);
	}
}
