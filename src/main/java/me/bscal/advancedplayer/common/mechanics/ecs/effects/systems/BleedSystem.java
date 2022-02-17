package me.bscal.advancedplayer.common.mechanics.ecs.effects.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.Bleed;
import me.bscal.advancedplayer.common.mechanics.ecs.effects.components.RefPlayer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@All({ Bleed.class, RefPlayer.class }) public class BleedSystem extends IteratingSystem
{

	private ComponentMapper<Bleed> Bleeds;
	private ComponentMapper<RefPlayer> Players;
	@Wire(name = "server")
	private MinecraftServer Server;

	@Override
	protected void process(int entityId)
	{
		var Bleed = Bleeds.get(entityId);
		var Player = Server.getPlayerManager().getPlayer(Players.get(entityId).PlayerUuid);

		if (!IsSuccess(Player)) return;
		if (!Player.isAlive())
		{
			Bleeds.remove(entityId);
			return;
		}

		float damage = Bleed.Damage * Bleed.Stacks;
		Player.damage(DamageSource.GENERIC, damage);

		Bleed.Duration--;
		if (Bleed.Duration < 1) Bleeds.remove(entityId);
	}

	private boolean IsSuccess(ServerPlayerEntity p)
	{
		return p != null && !p.isCreative() && !p.isSpectator();
	}
}
