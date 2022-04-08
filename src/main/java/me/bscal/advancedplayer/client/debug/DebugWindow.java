package me.bscal.advancedplayer.client.debug;

import com.google.common.base.Strings;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.ecs.components.Temperature;
import me.bscal.advancedplayer.common.ecs.systems.TemperatureSystem;
import me.bscal.advancedplayer.common.mechanics.temperature.BiomeClimate;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBiomeRegistry;
import me.bscal.seasons.api.SeasonAPI;
import me.bscal.seasons.api.SeasonAPIUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT) public class DebugWindow implements HudRenderCallback
{
	public boolean Show;
	public List<String> LeftDebugTxt = new ArrayList<>();
	public List<String> RightDebugTxt = new ArrayList<>();

	private ClientPlayerEntity m_Player;

	@Override
	public void onHudRender(MatrixStack matrixStack, float tickDelta)
	{
		m_Player = MinecraftClient.getInstance().player;
		if (m_Player == null) return;

		while (AdvancedPlayerClient.DebugTemperatureKeyBind.wasPressed())
		{
			Show = !Show;
		}
		if (!Show) return;
		
		var textRenderer = MinecraftClient.getInstance().textRenderer;
		RenderTextTopLeft(textRenderer, matrixStack);
		RenderTextTopRight(textRenderer, matrixStack);
	}

	private void RenderTextTopLeft(TextRenderer textRenderer, MatrixStack matrixStack)
	{
		LeftDebugTxt.clear();
		var optional = AdvancedPlayerClient.ECSManagerClient.GetComponentTyped(m_Player, Temperature.class);
		optional.ifPresent(temperature -> {
			LeftDebugTxt.add("CoreBodyTemperature " + temperature.CoreBodyTemperature);
			LeftDebugTxt.add("Work " +temperature.Work);
			LeftDebugTxt.add("HeatLossRate " +temperature.HeatLossRate);
			LeftDebugTxt.add("Delta " +temperature.Delta);
			LeftDebugTxt.add("OutsideTemp " +temperature.OutsideTemp);
			LeftDebugTxt.add("WindResistance " +temperature.WindResistance);
			LeftDebugTxt.add("Insulation " +temperature.Insulation);
			LeftDebugTxt.add("ShiftType " +temperature.ShiftType);
		});
		if (AdvancedPlayer.IsUsingSeasons())
		{
			var season = SeasonAPI.getSeason();
			var biome = SeasonAPIUtils.getBiomeFromEntity(m_Player);
			var climate = TemperatureBiomeRegistry.Get(biome);
			LeftDebugTxt.add("Season " + season);
			LeftDebugTxt.add("Climate " + climate);
		}

		for (int i = 0; i < LeftDebugTxt.size(); ++i)
		{
			String string = LeftDebugTxt.get(i);
			if (Strings.isNullOrEmpty(string)) continue;
			int j = textRenderer.fontHeight;
			int k = textRenderer.getWidth(string);
			int m = 2 + j * i;
			DebugHud.fill(matrixStack, 1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
			textRenderer.draw(matrixStack, string, 2.0f, (float) m, 0xE0E0E0);
		}
	}


	private void RenderTextTopRight(TextRenderer textRenderer, MatrixStack matrixStack)
	{
		RightDebugTxt.clear();
		if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER)
		{
			RightDebugTxt.add(TemperatureSystem.Timer.GetPrettyPrint());
		}

		for (int i = 0; i < RightDebugTxt.size(); ++i)
		{
			String string = RightDebugTxt.get(i);
			if (Strings.isNullOrEmpty(string)) continue;
			int j = textRenderer.fontHeight;
			int k = textRenderer.getWidth(string);
			int l = MinecraftClient.getInstance().getWindow().getScaledWidth() - 2 - k;
			int m = 2 + j * i;
			DebugHud.fill(matrixStack, l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
			textRenderer.draw(matrixStack, string, (float)l, (float)m, 0xE0E0E0);
		}
	}
}
