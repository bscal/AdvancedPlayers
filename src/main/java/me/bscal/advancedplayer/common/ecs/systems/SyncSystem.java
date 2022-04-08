package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.All;
import com.artemis.systems.IntervalEntityProcessingSystem;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.Unpooled;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.ecs.ECSManagerServer;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.ecs.components.Sync;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

@All({ RefPlayer.class, Sync.class }) public class SyncSystem extends IntervalEntityProcessingSystem
{
	private ComponentMapper<RefPlayer> m_PlayerReferences;
	private ComponentMapper<Sync> m_SyncPlayers;

	private Output m_Output;
	private int m_ShouldShrinkCount;

	public SyncSystem()
	{
		super(null, 1);
	}

	@Override
	protected void initialize()
	{
		super.initialize();
		m_Output = new Output(256, 2048);
	}

	@Override
	protected void process(Entity e)
	{
		long start = System.nanoTime();

		int entityId = e.getId();
		var player = (ServerPlayerEntity) m_PlayerReferences.get(entityId).Player;
		var sync = m_SyncPlayers.get(entityId);

		// Handles the removing of components
		for (var type : sync.Components)
		{
			var component = e.getComponent(type);
			if (component == null)
			{
				sync.Remove(type);
			}
		}

		// If no components need to be synced don't send
		if (sync.IsEmpty()) return;

		m_Output.writeInt(player.getId());
		m_Output.writeInt(entityId);

		var kryo = AdvancedPlayer.ECSManagerServer.GetKryo();
		kryo.writeObject(m_Output, sync.ComponentsToAdd);
		kryo.writeObject(m_Output, sync.ComponentsToRemove);

		m_Output.flush();

		var packetBuf = new PacketByteBuf(Unpooled.wrappedBuffer(m_Output.getBuffer()));
		ServerPlayNetworking.send(player, ECSManagerServer.SYNC_CHANNEL, packetBuf);

		m_Output.clear();
		sync.Clear();

		long end = System.nanoTime() - start;
		AdvancedPlayer.LOGGER.info(String.format("Sending Entity %d. Sizeof: %d. Took: %dns, %dms", entityId, packetBuf.array().length, end, end / 1000000));
	}
}