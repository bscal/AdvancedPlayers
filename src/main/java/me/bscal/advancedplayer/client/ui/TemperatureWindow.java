package me.bscal.advancedplayer.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TemperatureWindow implements PlayerStatusHud.PlayerStatusRenderer
{

    public static final int ICON_OFFSET = 16 / 2;

    private final Sprite Thermometer;

    public TemperatureWindow()
    {
        Thermometer = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_THERMOMETER);
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

    private void DrawChange(MatrixStack matrixStack, int x, int y, TemperatureBody.TemperatureShiftType shiftType)
    {
        Sprite sprite;
        if (TemperatureBody.TemperatureShiftType.IsWarming(shiftType))
        {
            sprite = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_UP_CARROT);
            RenderSystem.setShaderColor(1, 0, 0, 1);
        }
        else if (TemperatureBody.TemperatureShiftType.IsCooling(shiftType))
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
        if (TemperatureBody.TemperatureShiftType.IsBigDifference(shiftType))
        {
            InGameHud.drawSprite(matrixStack, x, y - 6, 0, 16, 16, sprite);
        }
        matrixStack.pop();
    }

    @Override
    public void Render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, int xOffset, int x, int y, int textureWidth, int textureHeight)
    {
        var coreBodyTemperature = AdvancedPlayerClient.ClientAPPlayer.CoreBodyTemperature;
        var shiftType = AdvancedPlayerClient.ClientAPPlayer.ShiftType;

        int xx = xOffset + x + 5;
        InGameHud.drawSprite(matrixStack, xx, y, 0, 16, 32, Thermometer);

        var spriteId = SetTemperature(coreBodyTemperature);
        InGameHud.drawSprite(matrixStack, xx, y, 0, 16, 32, AdvancedPlayerClient.AtlasTexture.getSprite(spriteId));
        DrawChange(matrixStack, xx, y, shiftType);
    }
}
