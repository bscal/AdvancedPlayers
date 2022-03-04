package me.bscal.advancedplayer.common.utils;

import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.Unpooled;
import me.bscal.advancedplayer.common.mechanics.ecs.ECSManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class CachedBuffer
{

	public final Output Output;

	public CachedBuffer(int capacity, int maxCapacity)
	{
		Output = new Output(capacity, maxCapacity);
	}

	public void Send(ServerPlayerEntity player)
	{
		var buffer = Unpooled.wrappedBuffer(Output.getBuffer());
		var packet = new PacketByteBuf(buffer);
		ServerPlayNetworking.send(player, ECSManager.SYNC_CHANNEL, packet);
	}

}
