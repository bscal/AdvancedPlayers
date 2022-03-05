package me.bscal.advancedplayer.common.mechanics.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.ECSManager;
import me.bscal.advancedplayer.common.mechanics.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Sync;
import net.minecraft.server.network.ServerPlayerEntity;

@All({ RefPlayer.class, Sync.class }) public class SyncSystem extends IntervalIteratingSystem
{

	ComponentMapper<RefPlayer> PlayerReferences;
	ComponentMapper<Sync> SyncPlayers;

	public SyncSystem()
	{
		super(null, 1);
	}

	@Override
	protected void process(int entityId)
	{
		var Player = PlayerReferences.get(entityId).Player;
		var Sync = SyncPlayers.get(entityId);




		AdvancedPlayer.LOGGER.info("syncing " + entityId);
		ECSManager.SyncEntity((ServerPlayerEntity) PlayerReferences.get(entityId).Player);
	}
}
