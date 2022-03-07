package me.bscal.advancedplayer.common.mechanics.ecs.systems;

import com.artemis.ComponentMapper;
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

	private ComponentMapper<RefPlayer> PlayerReferences;
	private ComponentMapper<Sync> SyncPlayers;

	private Output Out;

	public SyncSystem()
	{
		super(null, 1);
	}

	@Override
	protected void initialize()
	{
		super.initialize();

		Out = new Output(256, 1024);
	}

	@Override
	protected void process(int entityId)
	{
		long start = System.nanoTime();

		var Player = (ServerPlayerEntity) PlayerReferences.get(entityId).Player;
		var Sync = SyncPlayers.get(entityId);

		Kryo kryo = ECSManager.GetServerKyro();
		kryo.writeObject(Out, Sync);

		var packetBuf = new PacketByteBuf(Unpooled.wrappedBuffer(Out.getBuffer()));
		ServerPlayNetworking.send(Player, ECSManager.SYNC_CHANNEL, packetBuf);

		Out.clear();
		Sync.Clear();

		long end = System.nanoTime() - start;
		AdvancedPlayer.LOGGER.info(String.format("Sending Entity %d. Sizeof: %d. Took: %dns, %dms", entityId, packetBuf.array().length, end, end / 1000000));
	}
}
