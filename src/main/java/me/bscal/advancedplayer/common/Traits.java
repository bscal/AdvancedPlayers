package me.bscal.advancedplayer.common;

import me.bscal.advancedplayer.AdvancedPlayer;
import net.minecraft.util.math.random.Random;

import java.util.Objects;

public class Traits
{

    public static Traits FLU;
    public static final Random RANDOM = Random.create();

    public static void RegisterTraits(APPlayerManager pm)
    {
        FLU = pm.Register(new TraitsInstance.FluInstance("Flu",20 * 30));
        FLU.AppliedFunction = (player, instance) ->
        {
            boolean contains = player.Traits.get(FLU.Id);
            AdvancedPlayer.LOGGER.info(String.format("Applied %s, contains %s", instance.TraitsName, contains));
        };
        FLU.RemovedFunction = (player, instance) ->
        {
            boolean contains = player.Traits.get(FLU.Id);
            AdvancedPlayer.LOGGER.info(String.format("Removed %s, contains %s", instance.TraitsName, contains));
        };
        FLU.UpdateFunction = (player, instance) ->
        {
            if (RANDOM.nextInt(100) == 0)
            {
                AdvancedPlayer.LOGGER.info("Has flu!");
                int duration = ((TraitsInstance.FluInstance)instance).Duration;
                if (duration > 100)
                {
                    player.RemoveTrait(FLU);
                }
            }
        };
    }

    public String Name;
    public int Id;
    public TraitsInstance DefaultInstance;

    // Called once ever, when first applied
    public TraitFunc AppliedFunction;
    // Called on applied and every time player loads
    public TraitFunc StartFunction;
    // Called once ever, when removed.
    public TraitFunc RemovedFunction;
    // Called every tick
    public TraitFunc UpdateFunction;

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

    public interface TraitFunc
    {
        void Run(APPlayer player, TraitsInstance instance);
    }

}
