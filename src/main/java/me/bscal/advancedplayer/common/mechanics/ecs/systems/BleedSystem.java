package me.bscal.advancedplayer.common.mechanics.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Bleed;
import me.bscal.advancedplayer.common.mechanics.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Sync;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

@All({ Bleed.class, RefPlayer.class, Sync.class }) public class BleedSystem extends IteratingSystem implements ServerPlayerEvents.AfterRespawn
{

	private ComponentMapper<Bleed> Bleeds;
	private ComponentMapper<RefPlayer> Players;
	private ComponentMapper<Sync> SyncPlayers;

	@Override
	protected void process(int entityId)
	{
		var Bleed = Bleeds.get(entityId);
		var Player = Players.get(entityId).Player;
		var Sync = SyncPlayers.get(entityId);

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

		if (Bleed.IsEmpty())
		{
			Bleeds.remove(entityId);
		}
	}

	private boolean IsSuccess(PlayerEntity p)
	{
		return p != null && !p.isCreative() && !p.isSpectator();
	}

	@Override
	public void removed(IntBag entities)
	{
		for (int i = 0; i < entities.size(); i++)
		{
			var sync = SyncPlayers.get(i);
		}
	}

	@Override
	public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive)
	{

	}

	@Override
	protected void initialize()
	{
		super.initialize();

		ServerPlayerEvents.AFTER_RESPAWN.register(this);
	}
}
