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

    //private final Sprite Thermometer;

    public TemperatureWindow()
    {
        //Thermometer = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_THERMOMETER);
    }

    private Identifier SetTemperature(float temperature)
    {
        if (temperature <= -15) return AdvancedPlayerClient.TEXTURE_FREEZING;
        if (temperature <= -10) return AdvancedPlayerClient.TEXTURE_COLD;
        if (temperature <= -5) return AdvancedPlayerClient.TEXTURE_COOL;
        if (temperature >= 15) return AdvancedPlayerClient.TEXTURE_BURNING;
        if (temperature >= 10) return AdvancedPlayerClient.TEXTURE_HOT;
        if (temperature >= 5) return AdvancedPlayerClient.TEXTURE_WARM;
        return AdvancedPlayerClient.TEXTURE_NORMAL;
    }

    private void DrawChange(MatrixStack matrixStack, int x, int y, float outsideTemperature)
    {
        Sprite sprite;
        if (outsideTemperature > 0)
        {
            //sprite = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_UP_CARROT);
            RenderSystem.setShaderColor(1, 0, 0, 1);
        }
        else if (outsideTemperature < 0)
        {
            //sprite = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_DOWN_CARROT);
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
        //InGameHud.drawSprite(matrixStack, x, y, 0, 16, 16, sprite);
        if (Math.abs(outsideTemperature) >= 5)
        {
            //InGameHud.drawSprite(matrixStack, x, y - 6, 0, 16, 16, sprite);
        }
        matrixStack.pop();
    }

    @Override
    public void Render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, int xOffset, int x, int y, int textureWidth, int textureHeight)
    {
        //RenderSystem.setShaderTexture(0, AdvancedPlayerClient.AtlasTexture.getId());
        var coreBodyTemperature = AdvancedPlayerClient.ClientAPPlayer.BodyTemperature;
        var outsideTemperature = AdvancedPlayerClient.ClientAPPlayer.OutsideTemperature;

        int xx = xOffset + x + 5;
        float u = 0;
        float v = 0;
        int w = 16;
        int h = 32;

        matrixStack.push();

        RenderSystem.setShaderTexture(0, AdvancedPlayerClient.TEXTURE_THERMOMETER);
        InGameHud.drawTexture(matrixStack, xx, y, u, v, w, h, w, h);

        var spriteId = SetTemperature(coreBodyTemperature);
        RenderSystem.setShaderTexture(0, spriteId);
        InGameHud.drawTexture(matrixStack, xx, y, u, v, w, h, w, h);
        //DrawChange(matrixStack, xx, y, outsideTemperature);
        matrixStack.pop();
    }
}
