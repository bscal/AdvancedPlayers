package me.bscal.advancedplayer.common.mechanics.body;

import net.minecraft.nbt.NbtCompound;

public interface BodyPart
{

	void Reset();

	NbtCompound ToNbt();

	void FromNbt(NbtCompound nbt);
}
