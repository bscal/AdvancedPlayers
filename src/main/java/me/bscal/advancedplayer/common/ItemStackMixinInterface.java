package me.bscal.advancedplayer.common;

import net.minecraft.nbt.NbtCompound;

public interface ItemStackMixinInterface
{

    void InitSpoilage(long spoilage, long spoilageEnd, float rate);

    void SetSpoiled(NbtCompound nbt);

    void SetSpoilageRate(float rate);

    long GetTicksTillSpoiled();

    boolean IsFresh();

}
