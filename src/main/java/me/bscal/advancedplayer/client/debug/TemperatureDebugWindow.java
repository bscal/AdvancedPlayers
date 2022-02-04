package me.bscal.advancedplayer.client.debug;

import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT) public class TemperatureDebugWindow implements HudRenderCallback
{
	public String Text;

	public boolean Show;

	@Override
	public void onHudRender(MatrixStack matrixStack, float tickDelta)
	{
		while (AdvancedPlayerClient.DebugTemperatureKeyBind.wasPressed())
		{
			Show = !Show;
		}

		if (Show) MinecraftClient.getInstance().textRenderer.draw(matrixStack, Text, 10, 10, 0xff);
	}
}
