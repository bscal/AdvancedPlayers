package me.bscal.advancedplayer.common.events;

import me.bscal.advancedplayer.common.utils.FloatReference;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;

public class DamageEvents
{

    public static final Event<TryAttack> TRY_ATTACK =
            EventFactory.createArrayBacked(TryAttack.class, (entity, target) -> ActionResult.PASS,
                    listeners -> (entity, target) ->
                    {
                        for (var listener : listeners)
                        {
                            var result = listener.TryAttack(entity, target);
                            if (result != ActionResult.PASS) return result;
                        }
                        return ActionResult.PASS;
                    }
            );

    public interface TryAttack
    {
        ActionResult TryAttack(LivingEntity entity, LivingEntity target);
    }

    public static final Event<BeforeReductions> BEFORE_REDUCTIONS =
            EventFactory.createArrayBacked(BeforeReductions.class, listeners -> (src, amount, entity) ->
            {
                for (var listener : listeners)
                {
                    var result = listener.OnBeforeReductions(src, amount, entity);
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            });

    public interface BeforeReductions
    {
        ActionResult OnBeforeReductions(DamageSource source, float amount, LivingEntity entity);
    }

    public static final Event<Received> RECEIVED =
            EventFactory.createArrayBacked(Received.class, listeners -> (src, amount, entity) ->
            {
                for (var listener : listeners)
                {
                    var result = listener.OnReceivedDamage(src, amount, entity);
                    if (result != ActionResult.PASS) return result;
                }
                return ActionResult.PASS;
            });

    public interface Received
    {
        ActionResult OnReceivedDamage(DamageSource source, float amount, LivingEntity entity);
    }

    public static final Event<Blocked> BLOCKED =
            EventFactory.createArrayBacked(Blocked.class,
                    (src, amountAfterBlock, amount, entity) -> ActionResult.PASS,
                    listeners -> (src, amountAfterBlock, amount, entity) ->
                    {
                        for (var listener : listeners)
                        {
                            var result = listener.OnBlocked(src, amountAfterBlock, amount, entity);
                            if (result != ActionResult.PASS) return result;
                        }
                        return ActionResult.PASS;
                    });

    public interface Blocked
    {
        ActionResult OnBlocked(DamageSource source, float amount, FloatReference amountAfterBlocking, LivingEntity entity);
    }
}
