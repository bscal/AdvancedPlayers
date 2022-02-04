package me.bscal.advancedplayer.client.debug;

import com.google.common.base.Strings;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT) public class DebugWindow implements HudRenderCallback
{
	public boolean Show;
	public List<String> TemperatureDebugTextList = new ArrayList<>();

	@Override
	public void onHudRender(MatrixStack matrixStack, float tickDelta)
	{
		while (AdvancedPlayerClient.DebugTemperatureKeyBind.wasPressed())
		{
			Show = !Show;
		}
		if (!Show) return;

		var textRenderer = MinecraftClient.getInstance().textRenderer;
		for (int i = 0; i < TemperatureDebugTextList.size(); ++i)
		{
			String string = TemperatureDebugTextList.get(i);
			if (Strings.isNullOrEmpty(string)) continue;
			int j = textRenderer.fontHeight;
			int k = textRenderer.getWidth(string);
			int m = 2 + j * i;
			DebugHud.fill(matrixStack, 1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
			textRenderer.draw(matrixStack, string, 2.0f, (float) m, 0xE0E0E0);
		}
	}
}
