package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.ecs.components.Sync;
import me.bscal.advancedplayer.common.ecs.components.Thirst;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

@All({Thirst.class, RefPlayer.class, Sync.class})
public class ThirstSystem extends IntervalIteratingSystem implements ServerPlayerEvents.AfterRespawn
{
	public static final float MIN_THIRST = -100.0f;
	public static final float MAX_THIRST = 200.0f;
	public static final float DEFAULT_THIRST = 20.0f;
	public static final float DEFAULT_THIRST_DELTA = -.01f;

	private static final float INTERVAL = 5f;
	private static final float MSG_INTERVAL = (INTERVAL / 20f) * 60f * 5f; // sec -> min

	private static final Text THIRSTY_MSG = new TranslatableText("status.thirst.thirsty");

	private ComponentMapper<Thirst> m_ThirstMapper;
	private ComponentMapper<RefPlayer> m_PlayerMapper;
	private ComponentMapper<Sync> m_SyncMapper;

	private int m_Counter;

	public ThirstSystem()
	{
		super(null, INTERVAL);
		ServerPlayerEvents.AFTER_RESPAWN.register(this);
	}

	@Override
	protected void process(int entityId)
	{
		var thirst = m_ThirstMapper.get(entityId);

		thirst.Thirst = MathHelper.clamp(thirst.Thirst + thirst.ThirstDelta, MIN_THIRST, MAX_THIRST);

		var player = m_PlayerMapper.get(entityId).Player;
		if (thirst.Thirst == MIN_THIRST)
		{
			player.damage(DamageSource.GENERIC, 1);
		}
		if (thirst.Thirst < 0f && m_Counter++ >= MSG_INTERVAL)
		{
			m_Counter = 0;
			player.sendMessage(THIRSTY_MSG, false);
		}

		m_SyncMapper.get(entityId).Add(thirst);
	}

	@Override
	public void afterRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive)
	{
		int entity = AdvancedPlayer.ECSManagerServer.GetEntity(newPlayer);
		var thirst = m_ThirstMapper.get(entity);
		thirst.Thirst = DEFAULT_THIRST;
		thirst.ThirstDelta = DEFAULT_THIRST_DELTA;
	}
}
