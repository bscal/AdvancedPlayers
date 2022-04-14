package me.bscal.advancedplayer.client.renderers;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.entities.GhoulEntity;
import me.bscal.advancedplayer.client.models.GhoulModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.entity.GeoExampleEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

@Environment(EnvType.CLIENT)
public class GhoulRenderer extends GeoEntityRenderer<GhoulEntity>
{

	public GhoulRenderer(EntityRendererFactory.Context renderManager) {
		super(renderManager, new GhoulModel());
		this.addLayer(new GhoulEyeLayer(this));
	}

	@Environment(EnvType.CLIENT)
	public static class GhoulEyeLayer extends GeoLayerRenderer<GhoulEntity>
	{
		public static final Identifier TEXTURE = new Identifier(AdvancedPlayer.MOD_ID, "textures/entity/ghoul/ghoul_eyes.png");
		public static final Identifier MODEL = new Identifier(AdvancedPlayer.MOD_ID, "geo/ghoul.geo.json");

		public GhoulEyeLayer(IGeoRenderer<GhoulEntity> entityRendererIn)
		{
			super(entityRendererIn);
		}

		@Override
		public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, GhoulEntity entitylivingbaseIn, float limbSwing,
				float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
		{
			RenderLayer cameo =  RenderLayer.getEyes(TEXTURE);
			this.getRenderer().render(this.getEntityModel().getModel(MODEL), entitylivingbaseIn, partialTicks, cameo, matrixStackIn, bufferIn,
					bufferIn.getBuffer(cameo), 0xF00000, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1f);
		}
	}
}
