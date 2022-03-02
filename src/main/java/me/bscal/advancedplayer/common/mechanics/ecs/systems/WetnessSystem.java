package me.bscal.advancedplayer.common.mechanics.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.common.mechanics.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Temperature;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Wetness;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.MathHelper;

@All({ RefPlayer.class }) public class WetnessSystem extends IteratingSystem
{

	public static final float WATER_WETNESS = 10f;
	public static final float SWEAT_WETNESS = 1f;
	public static final float EVAPORATION = .5f;

	ComponentMapper<RefPlayer> Players;
	ComponentMapper<Wetness> Wetnesses;
	ComponentMapper<Temperature> Temperatures;

	@Override
	protected void process(int entityId)
	{
		var player = Players.get(entityId).Player;

		float wetnessFromWater = 0f;

		var feetBlockState = player.world.getBlockState(player.getBlockPos());
		if (feetBlockState.isOf(Blocks.WATER)) wetnessFromWater += WATER_WETNESS;

		var eyeBlockState = player.world.getBlockState(player.getBlockPos().up());
		if (eyeBlockState.isOf(Blocks.WATER)) wetnessFromWater += WATER_WETNESS;

		if (Temperatures.has(entityId))
		{
			var temperature = Temperatures.get(entityId);
			if (temperature.CoreBodyTemperature >= TemperatureBody.HOT) wetnessFromWater += SWEAT_WETNESS;
		}

		if (Wetnesses.has(entityId))
		{
			var wetness = Wetnesses.get(entityId);
			wetness.Wetness = MathHelper.clamp(wetness.Wetness + wetnessFromWater - EVAPORATION, 0f, 100f);
			if (wetness.Wetness < 0) Wetnesses.remove(entityId);
		}
		else if (wetnessFromWater > 0) Wetnesses.create(entityId);
	}
}
