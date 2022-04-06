package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.common.ecs.components.Bleed;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;

@All({ RefPlayer.class }) public class PlayerEffectSystem extends IteratingSystem
{

	private ComponentMapper<Bleed> Bleeds;
	private ComponentMapper<RefPlayer> Players;

	@Override
	protected void process(int entityId)
	{
		var Player = Players.get(entityId).Player;
	}
}
