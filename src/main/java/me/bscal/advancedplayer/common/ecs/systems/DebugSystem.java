package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;

@All(RefPlayer.class) public class DebugSystem extends IteratingSystem
{
	@Override
	protected void process(int entityId)
	{
	}
}
