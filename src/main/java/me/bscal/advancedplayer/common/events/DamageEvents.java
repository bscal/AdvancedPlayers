package me.bscal.advancedplayer.common.events;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.mostlyoriginal.api.event.common.Event;

public class DamageEvents
{

	public static class BeforeReductions implements Event
	{
		public DamageSource Source;
		public float Amount;
		public LivingEntity Entity;
		public boolean ShouldCancel;
	}

	public static class Received implements Event
	{
		public DamageSource Source;
		public float Amount;
		public LivingEntity Entity;
		public boolean ShouldCancel;
	}

	public static class Blocked implements Event
	{
		public DamageSource Source;
		public float Amount;
		public LivingEntity Entity;
	}

	public static class ModifyBlock implements Event
	{
		Blocked BlockedEvent;
	}

}
