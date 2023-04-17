package me.bscal.advancedplayer.client.models;

import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.entities.GhoulEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

@Environment(EnvType.CLIENT)
public class GhoulModel extends DefaultedEntityGeoModel<GhoulEntity>
{
    public GhoulModel()
    {
        super(new Identifier(AdvancedPlayer.MOD_ID, "ghoul"));
    }

    @Override
    public Identifier getModelResource(GhoulEntity object)
    {
        return new Identifier(AdvancedPlayer.MOD_ID, "geo/ghoul_gecko.geo.json");
    }

    @Override
    public Identifier getTextureResource(GhoulEntity object)
    {
        return new Identifier(AdvancedPlayer.MOD_ID, "textures/entity/ghoul/ghoul_gecko.png");
    }

    @Override
    public Identifier getAnimationResource(GhoulEntity animatable)
    {
        return new Identifier(AdvancedPlayer.MOD_ID, "animations/ghoul.animation.json");
    }

}
