package me.bscal.advancedplayer.common.utils;

import net.minecraft.entity.player.PlayerEntity;

public class HumanoidBody
{

	public PlayerEntity Player;
	public int[] PartEntityIds;

	public int GetHead() { return PartEntityIds[0]; }

}
