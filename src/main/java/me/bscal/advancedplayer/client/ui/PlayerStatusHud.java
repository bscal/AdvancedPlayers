package me.bscal.advancedplayer.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.mixin.client.SpriteAtlasTextureMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

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

        int x = client.getWindow().getScaledWidth() / 2 - 91 - 29 - 44;
        int y = client.getWindow().getScaledHeight() - 34;
        int iconSpacing = 20;
        //int x = client.getWindow().getScaledWidth() / 2 - X_OFFSET;
        //int y = client.getWindow().getScaledHeight() - Y_OFFSET;


/*        for(n = 0; n < 9; ++n) {
            o = i - 90 + n * 20 + 2;
            p = this.scaledHeight - 16 - 3;
            this.renderHotbarItem(o, p, tickDelta, playerEntity, (ItemStack)playerEntity.getInventory().main.get(n), m++);
        }*/

        for (int i = 0; i < Renderers.size(); ++i)
        {
            int xOffset = i * iconSpacing;
            Renderers.get(i).Render(matrixStack, tickDelta, client, xOffset, x, y, TextureWidth, TextureHeight);
        }

    }

    public interface PlayerStatusRenderer
    {
        void Render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, int xOffset, int x, int y, int textureWidth, int textureHeight);
    }
}
