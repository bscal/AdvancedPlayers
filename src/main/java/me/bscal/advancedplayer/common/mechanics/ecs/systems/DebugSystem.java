package me.bscal.advancedplayer.common.mechanics.ecs.systems;

import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.RefPlayer;

@All(RefPlayer.class) public class DebugSystem extends IteratingSystem
{
	@Override
	protected void process(int entityId)
	{
	}
}
