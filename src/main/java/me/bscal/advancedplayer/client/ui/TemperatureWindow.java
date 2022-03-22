package me.bscal.advancedplayer.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.mechanics.ecs.ECSManager;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Temperature;
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

	@Override
	public void onHudRender(MatrixStack matrixStack, float tickDelta)
	{
		var client = MinecraftClient.getInstance();
		if (client.player == null) return;
		if (true) return;
		Temperature temperature = (Temperature) ECSManager.GetClientComponent(Temperature.class);
		if (temperature == null) return;

		/*
			Thermometer Drawing
		 */
		int x = client.getWindow().getScaledWidth() / 2 - 140;
		int y = client.getWindow().getScaledHeight() - 36;

		RenderSystem.setShaderTexture(0, AdvancedPlayerClient.AtlasTexture.getId());

		InGameHud.drawSprite(matrixStack, x, y, 0, 16, 32, Thermometer);

		var spriteId = SetTemperature(temperature.CoreBodyTemperature);
		InGameHud.drawSprite(matrixStack, x, y, 0, 16, 32, AdvancedPlayerClient.AtlasTexture.getSprite(spriteId));

		DrawChange(matrixStack, x, y, temperature);
	}

	private Identifier SetTemperature(float temperature)
	{
		if (temperature <= TemperatureBody.FREEZING) return AdvancedPlayerClient.TEXTURE_FREEZING;
		if (temperature <= TemperatureBody.COLD) return AdvancedPlayerClient.TEXTURE_COLD;
		if (temperature <= TemperatureBody.CHILLY) return AdvancedPlayerClient.TEXTURE_COOL;
		if (temperature >= TemperatureBody.EXTREMELY_HOT) return AdvancedPlayerClient.TEXTURE_BURNING;
		if (temperature >= TemperatureBody.HOT) return AdvancedPlayerClient.TEXTURE_HOT;
		if (temperature >= TemperatureBody.WARM) return AdvancedPlayerClient.TEXTURE_WARM;
		return AdvancedPlayerClient.TEXTURE_NORMAL;
	}

	private void DrawChange(MatrixStack matrixStack, int x, int y, Temperature temperature)
	{
		Sprite sprite;
		if (TemperatureBody.TemperatureShiftType.IsWarming(temperature.ShiftType))
		{
			sprite = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_UP_CARROT);
			RenderSystem.setShaderColor(1, 0, 0, 1);
		}
		else if (TemperatureBody.TemperatureShiftType.IsCooling(temperature.ShiftType))
		{
			sprite = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_DOWN_CARROT);
			RenderSystem.setShaderColor(0, 0, 1, 1);
		}
		else return;

		// matrix magic to scale without effecting position;
		double xx = x + 8;
		double yy = y;
		matrixStack.push();
		matrixStack.translate(xx, yy, 0);
		matrixStack.scale(.5f, .5f, 0);
		matrixStack.translate(-xx - .5f, -yy, 0);
		InGameHud.drawSprite(matrixStack, x, y, 0, 16, 16, sprite);
		if (TemperatureBody.TemperatureShiftType.IsBigDifference(temperature.ShiftType))
		{
			InGameHud.drawSprite(matrixStack, x, y - 6, 0, 16, 16, sprite);
		}
		matrixStack.pop();
	}
}
