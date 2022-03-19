package me.bscal.advancedplayer.common.mechanics.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalIteratingSystem;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.Unpooled;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.ECSManager;
import me.bscal.advancedplayer.common.mechanics.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Sync;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

@All({ RefPlayer.class, Sync.class }) public class SyncSystem extends IntervalIteratingSystem
{

	private ComponentMapper<RefPlayer> m_PlayerReferences;
	private ComponentMapper<Sync> m_SyncPlayers;

	private Output m_Output;

	public SyncSystem()
	{
		super(null, 1);
	}

	@Override
	protected void initialize()
	{
		super.initialize();

		m_Output = new Output(256, 1024);
	}

	@Override
	protected void process(int entityId)
	{
		long start = System.nanoTime();

		var player = (ServerPlayerEntity) m_PlayerReferences.get(entityId).Player;
		var sync = m_SyncPlayers.get(entityId);

		Kryo kryo = ECSManager.GetServerKyro();
		kryo.writeObject(m_Output, sync);

		var packetBuf = new PacketByteBuf(Unpooled.wrappedBuffer(m_Output.getBuffer()));
		ServerPlayNetworking.send(player, ECSManager.SYNC_CHANNEL, packetBuf);

		m_Output.clear();
		sync.Clear();

		long end = System.nanoTime() - start;
		AdvancedPlayer.LOGGER.info(String.format("Sending Entity %d. Sizeof: %d. Took: %dns, %dms", entityId, packetBuf.array().length, end, end / 1000000));
	}

	// NOTE wrote this too see how it would work
	protected void testProcess(Entity entity)
	{
		var sync = m_SyncPlayers.get(entity.getId());

		ECSManager.MultiMap map = null;
		var componentsToAdd = map.Get(sync.NetworkId);

		for (var component : componentsToAdd)
			entity.edit().add(component);

		componentsToAdd.clear();
	}
}
