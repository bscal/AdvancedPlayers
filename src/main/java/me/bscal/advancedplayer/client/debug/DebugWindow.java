package me.bscal.advancedplayer.client.debug;

import com.google.common.base.Strings;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DebugWindow implements HudRenderCallback
{
    public boolean Show;
    public List<String> LeftDebugTxt = new ArrayList<>();
    public List<String> RightDebugTxt = new ArrayList<>();

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta)
    {
        if (MinecraftClient.getInstance().player == null) return;

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
        var ap = AdvancedPlayerClient.ClientAPPlayer;

        LeftDebugTxt.clear();
        LeftDebugTxt.add("BleedTicks = " + ap.BleedTicks);
        LeftDebugTxt.add("HeavyBleedTicks = " + ap.HeavyBleedTicks);
        LeftDebugTxt.add("LeftLegFracturedTicks = " + ap.LeftLegFracturedTicks);
        LeftDebugTxt.add("RightLegFracturedTicks = " + ap.RightLegFracturedTicks);
        LeftDebugTxt.add("LeftLegSplinted = " + ap.LeftLegSplinted);
        LeftDebugTxt.add("RightLegSplinted = " + ap.RightLegSplinted);
        LeftDebugTxt.add("Thirst = " + ap.Thirst);
        LeftDebugTxt.add("Hunger = " + ap.Hunger);
        LeftDebugTxt.add("Wetness = " + ap.Wetness);
        LeftDebugTxt.add("BodyTemperature = " + ap.BodyTemperature);
        LeftDebugTxt.add("OutsideTemperature = " + ap.OutsideTemperature);
        LeftDebugTxt.add("BiomeTemperature = " + ap.BiomeTemperature);
        LeftDebugTxt.add("HeightTemperature = " + ap.HeightTemperature);
        LeftDebugTxt.add("TempDelta = " + ap.TempDelta);

        for (int i = 0; i < LeftDebugTxt.size(); ++i)
        {
            String string = LeftDebugTxt.get(i);
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
        for (int i = 0; i < RightDebugTxt.size(); ++i)
        {
            String string = RightDebugTxt.get(i);
            if (Strings.isNullOrEmpty(string)) continue;
            int j = textRenderer.fontHeight;
            int k = textRenderer.getWidth(string);
            int l = MinecraftClient.getInstance().getWindow().getScaledWidth() - 2 - k;
            int m = 2 + j * i;
            DebugHud.fill(matrixStack, l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
            textRenderer.draw(matrixStack, string, (float) l, (float) m, 0xE0E0E0);
        }
    }
}
