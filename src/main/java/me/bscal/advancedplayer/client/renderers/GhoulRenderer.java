package me.bscal.advancedplayer.client.renderers;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.client.models.GhoulModel;
import me.bscal.advancedplayer.common.entities.GhoulEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;


@Environment(EnvType.CLIENT)
public class GhoulRenderer extends GeoEntityRenderer<GhoulEntity>
{
    public GhoulRenderer(EntityRendererFactory.Context renderManager)
    {
        super(renderManager, new GhoulModel());
        addRenderLayer(new GhoulEyeLayer(this));
    }

    @Environment(EnvType.CLIENT)
    public static class GhoulEyeLayer extends GeoRenderLayer<GhoulEntity>
    {
        public static final Identifier TEXTURE = new Identifier(AdvancedPlayer.MOD_ID, "textures/entity/ghoul/ghoul_eyes.png");
        public static final Identifier MODEL = new Identifier(AdvancedPlayer.MOD_ID, "geo/ghoul.geo.json");

        public GhoulEyeLayer(GeoRenderer<GhoulEntity> entityRendererIn)
        {
            super(entityRendererIn);
        }

        @Override
        public void render(MatrixStack poseStack, GhoulEntity animatable, BakedGeoModel bakedModel, RenderLayer renderType,
                           VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay)
        {
            RenderLayer cameo =  RenderLayer.getEyes(TEXTURE);
            this.getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource,
                    animatable, cameo, bufferSource.getBuffer(cameo),
                    partialTick, 0xF00000, OverlayTexture.DEFAULT_UV,
                    1f, 1f, 1f, 1f);
        }
    }
}
