package me.bscal.advancedplayer.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.utils.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class ThirstHud implements PlayerStatusHud.PlayerStatusRenderer
{

    public static final int MAX_VALUE = 12;
    public static final int SHEET_WIDTH = 16;

    public Sprite WaterSkinSprites;

    public ThirstHud()
    {

    }

    @Override
    public void Render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, int xOffset, int x, int y, int textureWidth, int textureHeight)
    {
        var thirst = AdvancedPlayerClient.ClientAPPlayer.Thirst;
        float normalThirst = MathUtils.Normalize(thirst, -100, 100);

        int xx = xOffset + x + 5;
        int yy = y + 15;


        RenderSystem.setShaderTexture(0, AdvancedPlayerClient.TEXTURE_WATER);

        matrixStack.push();
        //WaterSkinSprites = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.TEXTURE_WATER);
        InGameHud.drawTexture(matrixStack, xx, yy, 0, 0, 16, 16, 208, 16);
        int textureXCoord = (int) (normalThirst * MAX_VALUE) * SHEET_WIDTH;
        if (textureXCoord > 0)
           InGameHud.drawTexture(matrixStack, xx, yy, textureXCoord, 0, 16, 16, 208, 16);
        matrixStack.pop();

    }
}

/**
 * Draws a textured rectangle from a region in a texture.
 *
 * <p>The width and height of the region are the same as
 * the dimensions of the rectangle.
 *
 * @param matrices the matrix stack used for rendering
 * @param u the left-most coordinate of the texture region
 * @param v the top-most coordinate of the texture region
 * @param x the X coordinate of the rectangle
 * @param y the Y coordinate of the rectangle
 * @param textureWidth the width of the entire texture
 * @param textureHeight the height of the entire texture
 * @param width the width of the rectangle
 * @param height the height of the rectangle
 */