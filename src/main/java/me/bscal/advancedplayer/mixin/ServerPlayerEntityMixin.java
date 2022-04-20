package me.bscal.advancedplayer.mixin;

import me.bscal.advancedplayer.common.utils.ServerPlayerAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerAccess
{
    @Unique
    private int AP_EntityId;

    @Override
    public void SetAPEntityId(int id)
    {
        AP_EntityId = id;
    }

    @Override
    public int GetAPEntityId()
    {
        return AP_EntityId;
    }

}
