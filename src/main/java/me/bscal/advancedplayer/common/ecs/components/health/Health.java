package me.bscal.advancedplayer.common.ecs.components.health;

import com.artemis.Component;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;

public class Health extends Component
{

	public float HealthWithoutTempHealth;
	public float MaxHealthReductionProgress;
	public float TempHealth;
	public float MaxTempHealth;
	public final List<HealthRegenStatus> Regen = new ArrayList<>(3);

	public void AddRegen(HealthRegenStatus regen)
	{
		if (Regen.size() >= 3)
		{
			Regen.remove(0);
		}
		Regen.add(regen);
	}

	public static class HealthRegenStatus
	{
		public HealthRegen Regen;
		public int TicksRemaining;
		public int TickCount;
	}

	public static class HealthRegenStatusSerializer extends Serializer<HealthRegenStatus>
	{
		@Override
		public void write(Kryo kryo, Output output, HealthRegenStatus object)
		{
			output.writeInt(object.TicksRemaining, true);
			output.writeInt(object.TickCount, true);
			output.writeString(object.Regen.NameKey);
		}

		@Override
		public HealthRegenStatus read(Kryo kryo, Input input, Class<HealthRegenStatus> type)
		{
			int remaining = input.readInt(true);
			int count = input.readInt(true);
			String name = input.readString();

			var regenInstance = HealthRegen.NAME_TO_REGEN.get(name);
			if (regenInstance == null) return null;
			var status = new HealthRegenStatus();
			status.Regen = regenInstance;
			status.TicksRemaining = remaining;
			status.TickCount = count;
			return status;
		}
	}

	public record HealthRegen(String NameKey, String DescKey, float HpPerUpdate, float TicksPerUpdate, float DurationInTicks)
	{
		public static final Object2ObjectOpenHashMap<String, HealthRegen> NAME_TO_REGEN = new Object2ObjectOpenHashMap<>();
	}

}
