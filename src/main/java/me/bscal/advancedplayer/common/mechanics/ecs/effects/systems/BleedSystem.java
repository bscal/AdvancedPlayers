package me.bscal.advancedplayer.common.mechanics.ecs.effects.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.Bleed;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.RefPlayer;
import net.minecraft.entity.damage.DamageSource;

@All({ Bleed.class, RefPlayer.class }) public class BleedSystem extends IteratingSystem
{

	private ComponentMapper<Bleed> Bleeds;
	private ComponentMapper<RefPlayer> Players;

	@Override
	protected void process(int entityId)
	{
		var Bleed = Bleeds.get(entityId);
		float damage = Bleed.Damage * Bleed.Stacks;

		var Player = Players.get(entityId);
		Player.PlayerEntity.damage(DamageSource.GENERIC, damage);

		Bleed.Duration--;
		if (Bleed.Duration < 1) Bleeds.remove(entityId);
	}
}
