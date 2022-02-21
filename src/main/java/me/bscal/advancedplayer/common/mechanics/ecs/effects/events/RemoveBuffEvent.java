package me.bscal.advancedplayer.common.mechanics.ecs.effects.events;

import com.artemis.Component;
import com.artemis.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.mostlyoriginal.api.event.common.Event;

public class RemoveBuffEvent implements Event
{

	public PlayerEntity Player;
	public Entity Entity;
	public Class<? extends Component> ComponentClass;
	public int Amount;

}
