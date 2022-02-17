package me.bscal.advancedplayer.common.mechanics.ecs.effects.systems;

import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.RefPlayer;

@All(RefPlayer.class)
public class DebugSystem extends IteratingSystem
{
	@Override
	protected void process(int entityId)
	{
		AdvancedPlayer.LOGGER.info("Printing is a test for " + entityId);
	}
}
