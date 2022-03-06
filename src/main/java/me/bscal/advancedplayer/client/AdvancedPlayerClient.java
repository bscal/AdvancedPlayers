package me.bscal.advancedplayer.client;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.client.debug.DebugWindow;
import me.bscal.advancedplayer.client.ui.TemperatureWindow;
import me.bscal.advancedplayer.common.mechanics.ecs.ECSManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.ClientSidePacketRegistryImpl;
import net.fabricmc.fabric.impl.networking.ServerSidePacketRegistryImpl;
import net.fabricmc.fabric.impl.screenhandler.client.ClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT) public class AdvancedPlayerClient implements ClientModInitializer
{
	public static KeyBinding DebugTemperatureKeyBind;
	public static final DebugWindow TemperatureDebugWindow = new DebugWindow();

	public static final Identifier TEXTURE_THERMOMETER = GetTextureId("thermometer");
	public static final Identifier TEXTURE_BURNING = GetTextureId("temperature_burning");
	public static final Identifier TEXTURE_HOT = GetTextureId("temperature_hot");
	public static final Identifier TEXTURE_WARM = GetTextureId("temperature_warm");
	public static final Identifier TEXTURE_NORMAL = GetTextureId("temperature_normal");
	public static final Identifier TEXTURE_COOL = GetTextureId("temperature_cool");
	public static final Identifier TEXTURE_COLD = GetTextureId("temperature_cold");
	public static final Identifier TEXTURE_FREEZING = GetTextureId("temperature_freezing");
	public static final Identifier TEXTURE_NEUTRAL = GetTextureId("neutral_line");
	public static final Identifier TEXTURE_UP_CARROT = GetTextureId("up_carrot");
	public static final Identifier TEXTURE_DOWN_CARROT = GetTextureId("down_carrot");

	public static SpriteAtlasTexture AtlasTexture;

	@Override
	public void onInitializeClient()
	{
		DebugTemperatureKeyBind = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.advancedplayer.debug_temperature", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, "category.advancedplayer.debug"));

		ClientPlayConnectionEvents.INIT.register((a, b) -> {
			ECSManager.InitClient();
		});

		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
			ECSManager.CleanupClient();
		}));

		ClientPlayNetworking.registerGlobalReceiver(ECSManager.SYNC_CHANNEL, (client, handler, buf, responseSender) -> {
			final byte[] buffer = new byte[buf.writerIndex()];
			buf.getBytes(0, buffer);
			client.execute(() -> ECSManager.ReadEntity(buffer));
		});

		ClientPlayNetworking.registerGlobalReceiver(ECSManager.CREATE_CHANNEL, ((client, handler, buf, responseSender) -> {
			final int networkId = buf.readInt();
			client.execute(() -> ECSManager.CreateUser(networkId));
		}));

		ClientTickEvents.END_WORLD_TICK.register((client -> {
			ECSManager.ClientWorld.process();
		}));

		HudRenderCallback.EVENT.register(TemperatureDebugWindow);

		// Create sprite sheet
		ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
			var rm = client.getResourceManager();
			var id = new Identifier(AdvancedPlayer.MOD_ID, "textures");
			AtlasTexture = new SpriteAtlasTexture(id);
			Stream<Identifier> textures = Stream.of(TEXTURE_BURNING, TEXTURE_HOT, TEXTURE_WARM, TEXTURE_NORMAL, TEXTURE_COOL, TEXTURE_COLD, TEXTURE_FREEZING,
					TEXTURE_THERMOMETER, TEXTURE_NEUTRAL, TEXTURE_DOWN_CARROT, TEXTURE_UP_CARROT);
			var data = AtlasTexture.stitch(rm, textures, client.getProfiler(), 0);
			AtlasTexture.upload(data);
			AtlasTexture.registerTexture(client.getTextureManager(), rm, id, (runnable) -> {
			});

			HudRenderCallback.EVENT.register(new TemperatureWindow());
		});
	}

	public static Identifier GetTextureId(String filename)
	{
		return new Identifier(AdvancedPlayer.MOD_ID, filename);
	}
}
