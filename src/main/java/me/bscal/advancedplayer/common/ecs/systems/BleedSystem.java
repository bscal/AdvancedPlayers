package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ecs.components.Bleed;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.events.DamageEvents;
import me.bscal.advancedplayer.common.utils.ServerPlayerAccess;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

@All({ Bleed.class, RefPlayer.class }) public class BleedSystem extends IntervalIteratingSystem implements ServerPlayerEvents.AfterRespawn
{
	/**
	 * Processes every 60 ticks (3 secs)
	 */
	private static final int TIMESTEP = 100;
	private static final MutableText BLEED_START_TEXT = Text.translatable("status.advancedplayer.bleed");
	private static final MutableText BLEED_BLEEDING_TEXT = Text.translatable("status.advancedplayer.bleeding");
	private static final MutableText BLEED_INFECTED_TEXT = Text.translatable("status.advandedplayer.bleed_infected");

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
		if (livingEntity instanceof ServerPlayerEntity player)
		{
			int entityId = ((ServerPlayerAccess)player).GetAPEntityId();
			if (damageSource instanceof EntityDamageSource && v > 1f && !m_Bleeds.has(entityId))
			{
				int chance = AdvancedPlayer.Random.nextInt(20);
				if (chance == 0)
				{
					var bleed = m_Bleeds.create(entityId);
					bleed.Damage = 1;
					bleed.Duration = 20 * 300;
					player.sendMessage(BLEED_START_TEXT.formatted(Formatting.RED), false);
				}
			}
		}
		return ActionResult.PASS;
	}

}
