package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.All;
import com.artemis.systems.EntityProcessingSystem;
import me.bscal.advancedplayer.common.ecs.components.EffectRemover;

@All(EffectRemover.class) public class EffectHealingSystem extends EntityProcessingSystem
{

	private ComponentMapper<EffectRemover> Removers;

	@Override
	protected void process(Entity e)
	{
		var remover = Removers.get(e);
		remover.ClassesToRemove.forEach(clazz -> {
			var component = e.getComponent(clazz);
			if (component == null) return;
			e.edit().remove(clazz);
		});
		Removers.remove(e);
	}
}
