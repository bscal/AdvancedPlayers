package me.bscal.advancedplayer.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT) public class TemperatureWindow implements HudRenderCallback
{

	public static final int ICON_OFFSET = 16 / 2;

	private final Sprite Thermometer;

	public TemperatureWindow()
	{
		Thermometer = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_THERMOMETER);
	}

	public Identifier SetTemperature(float temperature)
	{
		if (temperature <= TemperatureBody.FREEZING) return AdvancedPlayerClient.TEXTURE_FREEZING;
		if (temperature <= TemperatureBody.COLD) return AdvancedPlayerClient.TEXTURE_COLD;
		if (temperature <= TemperatureBody.CHILLY) return AdvancedPlayerClient.TEXTURE_COOL;
		if (temperature >= TemperatureBody.EXTREMELY_HOT) return AdvancedPlayerClient.TEXTURE_BURNING;
		if (temperature >= TemperatureBody.HOT) return AdvancedPlayerClient.TEXTURE_HOT;
		if (temperature >= TemperatureBody.WARM) return AdvancedPlayerClient.TEXTURE_WARM;
		return AdvancedPlayerClient.TEXTURE_NORMAL;
	}

	@Override
	public void onHudRender(MatrixStack matrixStack, float tickDelta)
	{
		var client = MinecraftClient.getInstance();
		if (client.player == null) return;

		int x = client.getWindow().getScaledWidth() / 2 - ICON_OFFSET;
		int y = client.getWindow().getScaledHeight() / 2;

		RenderSystem.setShaderTexture(0, AdvancedPlayerClient.AtlasTexture.getId());

		InGameHud.drawSprite(matrixStack, x, y, 0, 16, 32, Thermometer);

		var temperatureBody = ComponentManager.BODY_TEMPERATURE.get(client.player);
		var spriteId = SetTemperature(temperatureBody.CoreBodyValue);
		InGameHud.drawSprite(matrixStack, x, y, 0, 16, 32, AdvancedPlayerClient.AtlasTexture.getSprite(spriteId));

		DrawChange(matrixStack, x, y, temperatureBody);
	}

	private void DrawChange(MatrixStack matrixStack, int x, int y, TemperatureBody temperatureBody)
	{
		float diff = temperatureBody.CoreBodyValue - temperatureBody.LastTemperature;
		boolean bigDiff = Math.abs(diff) > 1f;

		Sprite sprite;
		if (diff > .01f) sprite = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_UP_CARROT);
		else if (diff < -.01f) sprite = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_DOWN_CARROT);
		else return;

		RenderSystem.setShaderColor(1, 0, 0, 1);

		// matrix magic to scale without effecting position;
		y -= 4;
		double xx = x + 8;
		double yy = y + 8;
		matrixStack.push();
		matrixStack.translate(xx, yy, 0);
		matrixStack.scale(.5f, .5f, 0);
		matrixStack.translate(-xx - .5f, -yy, 0);
		InGameHud.drawSprite(matrixStack, x, y, 0, 16, 16, sprite);
		if (bigDiff) InGameHud.drawSprite(matrixStack, x, y - 6, 0, 16, 16, sprite);
		matrixStack.pop();
	}
}
