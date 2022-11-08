package me.bscal.advancedplayer.common;

import me.bscal.advancedplayer.AdvancedPlayer;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class Traits
{

    public static Traits FLU;

    public static void RegisterTraits(APPlayerManager pm)
    {
        FLU = pm.Register("Flu", new TraitsInstance.FluInstance("Flu",20 * 30));
    }

    public String Name;
    public int Id;
    public TraitsInstance DefaultInstance;

    public TraitUpdateFunc UpdateFunction;

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

    public interface TraitUpdateFunc
    {
        void OnUpdate(APPlayer player, TraitsInstance instance);
    }

}
