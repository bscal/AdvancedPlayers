package me.bscal.advancedplayer.common.mechanics;

import net.minecraft.nbt.NbtCompound;

public interface Effect
{

	NbtCompound ToNbt();

	void FromNbt(NbtCompound nbt);

}
