package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.ecs.components.health.Health;

@All({Health.class, RefPlayer.class})
public class HealthSystem extends IteratingSystem
{

	private ComponentMapper<Health> m_HealthMapper;
	private ComponentMapper<RefPlayer> m_PlayerMapper;

	@Override
	protected void process(int entityId)
	{
		var health = m_HealthMapper.get(entityId);
		var player = m_PlayerMapper.get(entityId).Player;

		var iter = health.Regen.iterator();
		while (iter.hasNext())
		{
			var val = iter.next();
			if (val == null)
			{
				iter.remove();
				continue;
			}

			val.TicksRemaining -= 1;
			val.TickCount -= 1;

			if (val.TickCount <= 0)
			{
				val.TickCount += val.Regen.TicksPerUpdate();
				player.heal(val.Regen.HpPerUpdate());
			}

			if (val.TicksRemaining < 1) iter.remove();
		}
	}
}
