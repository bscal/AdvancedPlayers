package me.bscal.advancedplayer.common.mechanics.ecs.effects.events;

import net.minecraft.server.network.ServerPlayerEntity;
import net.mostlyoriginal.api.event.common.Event;

public class PlayerRespawnEvent implements Event
{

	public ServerPlayerEntity OldPlayer;
	public ServerPlayerEntity NewPlayer;
	public boolean Alive;

}
