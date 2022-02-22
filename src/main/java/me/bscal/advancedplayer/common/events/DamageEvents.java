package me.bscal.advancedplayer.common.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;

public class DamageEvents
{
	public static final Event<BeforeReductionsCB> BeforeReductionEvent = EventFactory.createArrayBacked(BeforeReductionsCB.class, listeners -> (event) -> {
		for (var listener : listeners)
		{
			var result = listener.OnBeforeReductions(event);
			if (result != ActionResult.PASS) return result;
		}
		return ActionResult.PASS;
	});

	public interface BeforeReductionsCB
	{
		ActionResult OnBeforeReductions(BeforeReductions event);
	}

	public static final Event<ReceivedCB> ReceivedEvent = EventFactory.createArrayBacked(ReceivedCB.class, listeners -> (event) -> {
		for (var listener : listeners)
		{
			var result = listener.OnReceivedDamage(event);
			if (result != ActionResult.PASS) return result;
		}
		return ActionResult.PASS;
	});

	public interface ReceivedCB
	{
		ActionResult OnReceivedDamage(Received event);
	}

	public static final Event<BlockedCB> BlockedEvent = EventFactory.createArrayBacked(BlockedCB.class, listeners -> (event) -> {
		for (var listener : listeners)
		{
			var result = listener.OnBlocked(event);
			if (result != ActionResult.PASS) return result;
		}
		return ActionResult.PASS;
	});

	public interface BlockedCB
	{
		ActionResult OnBlocked(Blocked event);
	}

	public static class BeforeReductions
	{
		public DamageSource Source;
		public float Amount;
		public LivingEntity Entity;
		public boolean ShouldCancel;
	}

	public static class Received
	{
		public DamageSource Source;
		public float Amount;
		public LivingEntity Entity;
		public boolean ShouldCancel;
	}

	public static class Blocked
	{
		public DamageSource Source;
		public float AmountAfterBlocking;
		public float Amount;
		public LivingEntity Entity;
	}
}
