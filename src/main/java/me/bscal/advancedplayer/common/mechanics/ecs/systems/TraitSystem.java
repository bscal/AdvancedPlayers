package me.bscal.advancedplayer.common.mechanics.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Traits;

@All({ Traits.class, RefPlayer.class }) public class TraitSystem extends IteratingSystem
{

	private ComponentMapper<Traits> m_Traits;
	private ComponentMapper<RefPlayer> m_Players;

	@Override
	protected void process(int entityId)
	{
		var traits = m_Traits.get(entityId);
		var player = m_Players.get(entityId).Player;

		for (var trait : traits.Traits)
		{
			var cb = trait.TickFunction();
			if (cb != null) cb.OnTick(player, trait);
		}

	}

}
