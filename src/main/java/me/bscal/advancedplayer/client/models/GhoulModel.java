package me.bscal.advancedplayer.client.models;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.entities.GhoulEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

@Environment(EnvType.CLIENT)
public class GhoulModel extends AnimatedGeoModel<GhoulEntity>
{
    @Override
    public Identifier getModelResource(GhoulEntity object)
    {
        return new Identifier(AdvancedPlayer.MOD_ID, "geo/ghoul.geo.json");
    }

    @Override
    public Identifier getTextureResource(GhoulEntity object)
    {
        return new Identifier(AdvancedPlayer.MOD_ID, "textures/entity/ghoul/ghoul.png");
    }

    @Override
    public Identifier getAnimationResource(GhoulEntity animatable)
    {
        return new Identifier(AdvancedPlayer.MOD_ID, "animations/ghoul.animation.json");
    }

}
