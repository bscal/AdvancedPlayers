package me.bscal.advancedplayer.common.mechanics.ecs.effects.events;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class HealEffectEvent
{

	public PlayerEntity ReceivingPlayer;
	public LivingEntity HealingEntity;
	public int Entity;
	public ItemStack ItemStack;
	public int HealType;
	public int HealStrength;

	public enum Types
	{
		Bleed(1),
		DeepBleed(2),
		Fracture(4);

		public final int Id;

		Types(int id)
		{
			Id = id;
		}

	}

}
