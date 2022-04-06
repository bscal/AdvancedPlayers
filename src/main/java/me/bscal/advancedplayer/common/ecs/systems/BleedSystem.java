package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ecs.components.Bleed;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@All({ Bleed.class, RefPlayer.class }) public class BleedSystem extends IntervalIteratingSystem implements ServerPlayerEvents.AfterRespawn
{
	/**
	 * Processes every 60 ticks (3 secs)
	 */
	private static final int TIMESTEP = 60;

	private ComponentMapper<Bleed> m_Bleeds;
	private ComponentMapper<RefPlayer> m_Players;

	public BleedSystem()
	{
		super(null, TIMESTEP);
	}

	@Override
	protected void process(int entityId)
	{
		var bleed = m_Bleeds.get(entityId);
		var player = m_Players.get(entityId).Player;

		bleed.Duration -= TIMESTEP;
		if (bleed.Duration < 1 || bleed.IsBandaged)
		{
			m_Bleeds.remove(entityId);
			return;
		}

		if (IsValid(player))
		{
			player.damage(DamageSource.GENERIC, bleed.Damage);
			player.sendMessage(GenerateText(player, bleed), false);
		}
	}

	private Text GenerateText(PlayerEntity player, Bleed bleed)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("You seem to be bleeding");
		if (bleed.IsInfected) sb.append(" and the wounds is festering");
		sb.append("...");

		LiteralText text = new LiteralText(sb.toString());
		text.formatted(Formatting.RED);
		return text;
	}

	private boolean IsValid(PlayerEntity p)
	{
		return p.isAlive() && !p.isCreative() && !p.isSpectator();
	}

	@Override
	public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive)
	{
		var entityId = AdvancedPlayer.ECSManagerServer.GetEntity(oldPlayer);
		m_Bleeds.remove(entityId);
	}

	@Override
	protected void initialize()
	{
		super.initialize();

		ServerPlayerEvents.AFTER_RESPAWN.register(this);
	}
}
