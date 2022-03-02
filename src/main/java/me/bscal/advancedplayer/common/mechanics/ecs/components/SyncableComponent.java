package me.bscal.advancedplayer.common.mechanics.ecs.components;

import net.minecraft.network.PacketByteBuf;

public interface SyncableComponent
{

	void Write(PacketByteBuf buffer);

	void Read(PacketByteBuf buffer);

}
