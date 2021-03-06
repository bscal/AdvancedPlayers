package me.bscal.advancedplayer.client.ui;

import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.ecs.components.Thirst;
import me.bscal.advancedplayer.common.utils.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;

public class ThirstHud implements PlayerStatusHud.PlayerStatusRenderer
{

	public static final int MAX_VALUE = 12;
	public static final int SHEET_WIDTH = 16;

	public final Sprite WaterSkinSprites;

	public ThirstHud()
	{
		WaterSkinSprites = AdvancedPlayerClient.AtlasTexture.getSprite(AdvancedPlayerClient.GetUiTexture("water_skin_sprites"));
	}

	@Override
	public void Render(MatrixStack matrixStack, float tickDelta, MinecraftClient client, int x, int y, int textureWidth, int textureHeight)
	{
		var thirst = AdvancedPlayerClient.ECSManagerClient.GetComponentTyped(client.player, Thirst.class);
		float thirstValue = thirst.map(value -> value.Thirst).orElse(0F);
		float normalThirst = MathUtils.Normalize(thirstValue, -100f, 200f);

		int xx = x + 5;
		int yy = y + 15;
		InGameHud.drawTexture(matrixStack, xx, yy, WaterSkinSprites.getX(), WaterSkinSprites.getY(), 16, 16, textureWidth, textureHeight);
		int xOffset = (int)(normalThirst * MAX_VALUE) * SHEET_WIDTH;
		if (xOffset > 0)
			InGameHud.drawTexture(matrixStack, xx, yy, WaterSkinSprites.getX() + xOffset, WaterSkinSprites.getY(), 16, 16, textureWidth, textureHeight);
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