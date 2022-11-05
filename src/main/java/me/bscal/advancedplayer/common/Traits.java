package me.bscal.advancedplayer.common;

import me.bscal.advancedplayer.AdvancedPlayer;

import java.util.Objects;

public class Traits
{

    public static Traits FLU;

    public static void RegisterTraits()
    {
        FLU = Register("Flu", new TraitsInstance.FluInstance(20 * 30));
    }

    public String Name;
    public TraitsInstance DefaultInstance;

    public static Traits Register(String name, TraitsInstance defaultInstance)
    {
        Traits trait = new Traits();
        trait.Name = name;
        trait.DefaultInstance = defaultInstance;

        AdvancedPlayer.APPlayerManager.RegisterTrait(trait);
        return trait;
    }

    @Override
    public int hashCode()
    {
        return Name.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof Traits otherTrait)
        {
            return Objects.equals(this.Name, otherTrait.Name);
        }
        return false;
    }


}
