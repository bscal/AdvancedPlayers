package me.bscal.advancedplayer.common.mechanics.ecs.effects.events;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.mostlyoriginal.api.event.common.Event;

public class PlayerDeathEvent implements Event
{

	public ServerPlayerEntity Player;
	public DamageSource DamageSource;
	public float DamageAmount;
	public boolean Cancel;

}
