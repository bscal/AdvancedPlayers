package me.bscal.advancedplayer.client.debug;

import com.google.common.base.Strings;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBiomeRegistry;
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
        LeftDebugTxt.clear();

        var ap = AdvancedPlayerClient.ClientAPPlayer;
        LeftDebugTxt.add("CoreBodyTemperature " + ap.CoreBodyTemperature);
        LeftDebugTxt.add("Work " + ap.Work);
        LeftDebugTxt.add("HeatLossRate " + ap.HeatLossRate);
        LeftDebugTxt.add("Delta " + ap.Delta);
        LeftDebugTxt.add("OutsideTemp " + ap.OutsideTemp);
        LeftDebugTxt.add("WindResistance " + ap.WindResistance);
        LeftDebugTxt.add("Insulation " + ap.Insulation);
        LeftDebugTxt.add("ShiftType " + ap.ShiftType);

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
