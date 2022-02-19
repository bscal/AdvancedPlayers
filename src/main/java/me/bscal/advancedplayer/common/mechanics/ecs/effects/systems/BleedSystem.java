package me.bscal.advancedplayer.common.mechanics.ecs.effects.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.Bleed;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.events.PlayerDeath;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.mostlyoriginal.api.event.common.Subscribe;

@All({ Bleed.class, RefPlayer.class }) public class BleedSystem extends IteratingSystem
{

	private ComponentMapper<Bleed> Bleeds;
	private ComponentMapper<RefPlayer> Players;
	@Wire(name = "server") private MinecraftServer Server;

	@Override
	protected void process(int entityId)
	{
		var Bleed = Bleeds.get(entityId);
		var Player = Players.get(entityId).Player;

		if (!IsSuccess(Player)) return;
		if (!Player.isAlive())
		{
			Bleeds.remove(entityId);
			return;
		}

		var it = Bleed.Durations.iterator();
		while (it.hasNext())
		{
			int i = it.nextIndex();
			Player.damage(DamageSource.GENERIC, Bleed.Damage);

			int duration = Bleed.Durations.getInt(i) - 1;
			if (duration < 1) it.remove();
			else Bleed.Durations.set(i, duration);
		}

		if (Bleed.Durations.isEmpty())
		{
			Bleeds.remove(entityId);
		}
	}

	private boolean IsSuccess(PlayerEntity p)
	{
		return p != null && !p.isCreative() && !p.isSpectator();
	}

	@Subscribe
	public void OnPlayerDeath(PlayerDeath event)
	{
		AdvancedPlayer.LOGGER.info("*** TEST! ***");
	}
}
