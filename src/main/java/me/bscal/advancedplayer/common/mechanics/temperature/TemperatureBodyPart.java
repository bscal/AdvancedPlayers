package me.bscal.advancedplayer.common.mechanics.temperature;

import me.bscal.advancedplayer.common.mechanics.body.BodyPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class TemperatureBodyPart implements BodyPart
{
	public static final int SLOT_FOOT = 0;
	public static final int SLOT_LEG = 1;
	public static final int SLOT_CHEST = 2;
	public static final int SLOT_HELM = 3;

	public float Temperature;
	public float Insulation;
	public float WindResistance;
	public int[] SlotsForBodyPart;

	public void UpdateTemperature(float temperature, TemperatureBody body)
	{
		Temperature += temperature - body.Work;
	}

	public void ProcessClothing(PlayerEntity provider)
	{
		Insulation = 0;
		WindResistance = 0;
		for (int slotId : SlotsForBodyPart)
		{
			ItemStack item = provider.getInventory().getArmorStack(slotId);
			TemperatureClothing clothing = TemperatureBody.CLOTHING_MAP.get(item.getItem());
			if (clothing == null) return;
			Insulation += clothing.insulation();
			WindResistance += clothing.windResistance();
		}
	}

	@Override
	public void Reset()
	{
		Temperature = TemperatureBody.NORMAL;
		Insulation = 0;
		WindResistance = 0;
	}

	@Override
	public NbtCompound ToNbt()
	{
		return null;
	}

	@Override
	public void FromNbt(NbtCompound nbt)
	{

	}
}
