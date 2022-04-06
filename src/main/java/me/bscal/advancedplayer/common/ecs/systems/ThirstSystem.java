package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import me.bscal.advancedplayer.common.ecs.components.Thirst;

@All({Thirst.class})
public class ThirstSystem extends IntervalIteratingSystem
{

	private ComponentMapper<Thirst> ThirstMapper;

	public ThirstSystem()
	{
		super(null, 5);
	}

	@Override
	protected void process(int entityId)
	{
		var thirst = ThirstMapper.get(entityId);

		thirst.Thirst -= thirst.ThirstDelta;
	}
}
