package me.bscal.advancedplayer.client;

import me.bscal.advancedplayer.client.debug.DebugWindow;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT) public class AdvancedPlayerClient implements ClientModInitializer
{

	public static KeyBinding DebugTemperatureKeyBind;

	public static final DebugWindow TemperatureDebugWindow = new DebugWindow();

	@Override
	public void onInitializeClient()
	{
		DebugTemperatureKeyBind = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.advancedplayer.debug_temperature", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_COMMA, "category.advancedplayer.debug"));

		HudRenderCallback.EVENT.register(TemperatureDebugWindow);
	}
}
