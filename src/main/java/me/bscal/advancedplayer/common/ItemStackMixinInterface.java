package me.bscal.advancedplayer.common;

public interface ItemStackMixinInterface
{

    void InitSpoilage(long spoilage, long spoilageEnd, float rate);

    void SetSpoilageRate(float rate);

    long GetSpoilDuration();

    boolean IsFresh();

    long UpdateSpoilage(long time);

}
