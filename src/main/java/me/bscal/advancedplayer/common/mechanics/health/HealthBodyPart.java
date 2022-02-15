package me.bscal.advancedplayer.common.mechanics.health;

import me.bscal.advancedplayer.common.mechanics.Effect;
import me.bscal.advancedplayer.common.mechanics.body.BodyPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class HealthBodyPart implements BodyPart
{

	public float Health;
	public List<Effect> Effects;

	public HealthBodyPart()
	{
		Reset();
	}

	@Override
	public void Reset()
	{
		Health = 20;
		Effects = new ArrayList<>();
	}

	@Override
	public NbtCompound ToNbt()
	{
		NbtCompound nbt = new NbtCompound();
		nbt.putFloat("Health", Health);
		NbtList list = new NbtList();
		for (var effect : Effects)
		{
			list.add(effect.ToNbt());
		}
		nbt.put("Effects", list);
		return nbt;
	}

	@Override
	public void FromNbt(NbtCompound nbt)
	{
		Health = nbt.getFloat("Health");
		NbtList list = nbt.getList("Effects", NbtElement.COMPOUND_TYPE);
		for (var ele : list)
		{
			NbtCompound effectNbt = (NbtCompound) ele;
			String className = effectNbt.getString("ClassName");
			try
			{
				Effect effectInstance = (Effect) Class.forName(className).getConstructor().newInstance();
				effectInstance.FromNbt(effectNbt);
			}
			catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
	}
}
