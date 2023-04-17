package me.bscal.advancedplayer.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.mixin.client.SpriteAtlasTextureAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class PlayerStatusHud implements HudRenderCallback
{

    public final List<PlayerStatusRenderer> Renderers = new ArrayList<>();

    public PlayerStatusHud()
    {
        Renderers.add(new ThirstHud());
        Renderers.add(new TemperatureWindow());
    }

    @Override
    public void onHudRender(MatrixStack matrixStack, float tickDelta)
    {
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;


        //SpriteAtlasTextureAccessor accessor = (SpriteAtlasTextureAccessor)AdvancedPlayerClient.AtlasTexture;
        //int width = accessor.callGetWidth();
        //int height = accessor.callGetHeight();
        int x = client.getWindow().getScaledWidth() / 2 - 91 - 29 - 44;
        int y = client.getWindow().getScaledHeight() - 34;
        int iconSpacing = 20;

        for (int i = 0; i < Renderers.size(); ++i)
        {
            int xOffset = i * iconSpacing;
            Renderers.get(i).Render(matrixStack, tickDelta, client, xOffset, x, y, 0, 0);
        }

    }

    public interface PlayerStatusRenderer
    {
        void Render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, int xOffset, int x, int y, int textureWidth, int textureHeight);
    }
}
