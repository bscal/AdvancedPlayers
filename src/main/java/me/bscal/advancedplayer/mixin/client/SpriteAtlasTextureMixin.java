package me.bscal.advancedplayer.mixin.client;

import net.minecraft.client.texture.SpriteAtlasTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteAtlasTexture.class)
public class SpriteAtlasTextureMixin
{

	@Mixin(SpriteAtlasTexture.Data.class)
	public interface Data
	{
		@Accessor("width")
		int getWidth();

		@Accessor("height")
		int getHeight();
	}

}
