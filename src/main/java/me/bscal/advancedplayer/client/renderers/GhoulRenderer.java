package me.bscal.advancedplayer.client.renderers;

import me.bscal.advancedplayer.common.entities.GhoulEntity;
import me.bscal.advancedplayer.client.models.GhoulModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class GhoulRenderer extends GeoEntityRenderer<GhoulEntity>
{

	public GhoulRenderer(EntityRendererFactory.Context renderManager) {
		super(renderManager, new GhoulModel());
	}
}
