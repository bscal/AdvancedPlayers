package me.bscal.advancedplayer.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.mixin.client.SpriteAtlasTextureMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class PlayerStatusHud implements HudRenderCallback
{

    public final List<PlayerStatusRenderer> Renderers = new ArrayList<>();
    public int TextureWidth;
    public int TextureHeight;

    public PlayerStatusHud()
    {
        var data = (SpriteAtlasTextureMixin.Data) AdvancedPlayerClient.AtlasTextureData;
        TextureWidth = data.getWidth();
        TextureHeight = data.getHeight();

        Renderers.add(new ThirstHud());
        Renderers.add(new TemperatureWindow());
    }

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta)
    {
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;

        RenderSystem.setShaderTexture(0, AdvancedPlayerClient.AtlasTexture.getId());

        int x = 8;
        int y = client.getWindow().getScaledHeight() - 38;
        int iconSpacing = 20;
        //int x = client.getWindow().getScaledWidth() / 2 - X_OFFSET;
        //int y = client.getWindow().getScaledHeight() - Y_OFFSET;

        for (int i = 0; i < Renderers.size(); ++i)
        {
            Renderers.get(i).Render(matrixStack, tickDelta, client, x + i * iconSpacing, y, TextureWidth, TextureHeight);
        }

    }

    public interface PlayerStatusRenderer
    {
        void Render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, int x, int y, int textureWidth, int textureHeight);
    }
}
