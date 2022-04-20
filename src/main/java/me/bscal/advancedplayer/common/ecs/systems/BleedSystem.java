package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ecs.components.Bleed;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.events.DamageEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

@All({ Bleed.class, RefPlayer.class }) public class BleedSystem extends IntervalIteratingSystem implements ServerPlayerEvents.AfterRespawn
{
	/**
	 * Processes every 60 ticks (3 secs)
	 */
	private static final int TIMESTEP = 100;
	private static final TranslatableText BLEED_START_TEXT = new TranslatableText("status.advancedplayer.bleed");
	private static final TranslatableText BLEED_BLEEDING_TEXT = new TranslatableText("status.advancedplayer.bleeding");
	private static final TranslatableText BLEED_INFECTED_TEXT = new TranslatableText("status.advandedplayer.bleed_infected");

	private ComponentMapper<Bleed> m_Bleeds;
	private ComponentMapper<RefPlayer> m_Players;

	public BleedSystem()
	{
		super(null, TIMESTEP);

		ServerPlayerEvents.AFTER_RESPAWN.register(this);
		DamageEvents.RECEIVED.register(this::OnReceivedDamage);
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
			var text = bleed.IsInfected ? BLEED_INFECTED_TEXT : BLEED_BLEEDING_TEXT;
			player.sendMessage(text.formatted(Formatting.RED), false);
		}
	}

	private boolean IsValid(PlayerEntity p)
	{
		return p.isAlive() && !p.isCreative() && !p.isSpectator();
	}

	@Override
	public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive)
	{
		var entityId = AdvancedPlayer.ECSManagerServer.GetEntity(oldPlayer);
		if (m_Bleeds.has(entityId))
			m_Bleeds.remove(entityId);
	}

	private ActionResult OnReceivedDamage(DamageSource damageSource, float v, LivingEntity livingEntity)
	{
		if (livingEntity instanceof PlayerEntity player)
		{
			int entity = AdvancedPlayer.ECSManagerServer.GetEntity(player.getUuid());
			if (damageSource instanceof EntityDamageSource && v > 1f && !m_Bleeds.has(entity))
			{
				int chance = AdvancedPlayer.Random.nextInt(20);
				if (chance == 0)
				{
					var bleed = m_Bleeds.create(entity);
					bleed.Damage = 1;
					bleed.Duration = 20 * 300;
					player.sendMessage(BLEED_START_TEXT.formatted(Formatting.RED), false);
				}
			}
		}
		return ActionResult.PASS;
	}

}
