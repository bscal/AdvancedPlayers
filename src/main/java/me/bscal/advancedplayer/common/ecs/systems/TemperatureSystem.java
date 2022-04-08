package me.bscal.advancedplayer.common.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.ecs.components.Sync;
import me.bscal.advancedplayer.common.ecs.components.Temperature;
import me.bscal.advancedplayer.common.ecs.components.Wetness;
import me.bscal.advancedplayer.common.mechanics.temperature.BiomeClimate;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBiomeRegistry;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureClothing;
import me.bscal.advancedplayer.common.utils.Timer;
import me.bscal.seasons.api.SeasonAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

@All({ RefPlayer.class, Temperature.class, Sync.class }) public class TemperatureSystem extends IteratingSystem
{

	ComponentMapper<Temperature> Temperatures;
	ComponentMapper<RefPlayer> Players;
	ComponentMapper<Sync> SyncPlayers;
	ComponentMapper<Wetness> PlayerWetness;

	public static Timer Timer = new Timer(20);

	@Override
	protected void process(int entityId)
	{
		Timer.Start();

		var temperature = Temperatures.get(entityId);
		var player = Players.get(entityId).Player;
		var sync = SyncPlayers.get(entityId);

		var wetness = PlayerWetness.get(entityId);
		float wetnessValue = (wetness == null) ? 0 : wetness.Wetness;

		// Cache temperature? doesnt really make sense to calculate temperature
		// TODO can we thread this? Is it worth?

		BlockPos pos = player.getBlockPos();
		RegistryEntry<Biome> biome = player.world.getBiome(pos);
		var season = SeasonAPI.getSeasonByBiome(biome.value());
		BiomeClimate climate = TemperatureBiomeRegistry.Get(biome.value());
		float airTemperature = climate.GetCurrentTemperature();
		float yTemperature = GetYTemperature(pos);
		float lightTemperature = GetLightTemperature(player.world.getLightLevel(LightType.SKY, pos));
		float humidity = 0.5f;
		float wind = 3f;

		TemperatureClothing.ClothingData clothingData = GetProviderClothingData(player);
		temperature.Insulation = clothingData.Insulation;
		temperature.WindResistance = clothingData.WindResistance;

		float m_BaseWork = 1.0f; // Players body always doing some work.
		float bodyTemp = temperature.CoreBodyTemperature + temperature.Work + m_BaseWork;

		temperature.OutsideTemp = airTemperature + yTemperature + lightTemperature - (wind - temperature.WindResistance);
		float diff = bodyTemp - temperature.OutsideTemp;
		temperature.ShiftType = TemperatureBody.TemperatureShiftType.TypeForTemp(temperature.OutsideTemp);

		// 100% insulation would mean you lose 0 heat, 0% you lose all the heat;
		temperature.HeatLossRate = MathHelper.lerp(temperature.Insulation, diff / 200, .0f);
		// Since Work is temporary
		temperature.Work = MathHelper.clamp(temperature.Work - temperature.HeatLossRate, 0, 10f);
		// Body moving towards the outside temperature. Not an expert at thermodynamics but this seems like a
		// decent system even though not 100% accurate
		temperature.CoreBodyTemperature = MathHelper.clamp(temperature.CoreBodyTemperature, TemperatureBody.MIN_COLD, TemperatureBody.MAX_HOT);
		temperature.CoreBodyTemperature = MathHelper.lerp(temperature.HeatLossRate, temperature.CoreBodyTemperature, temperature.OutsideTemp);
		// 100% would not allow evaporation to take place. This does not matter if it is cold.
		temperature.Delta = TemperatureBody.TemperatureShiftType.IsWarming(temperature.ShiftType) ? MathHelper.lerp(humidity, .1f, .0f) : .1f;
		temperature.CoreBodyTemperature = MathHelper.lerp(temperature.Delta, temperature.CoreBodyTemperature, TemperatureBody.NORMAL);

		sync.Add(temperature);

		Timer.Stop();
	}

	public static float GetYTemperature(BlockPos pos)
	{
		float y = pos.getY();
		if (y <= -32)
		{
			// 15-31C
			return ((-y) - 32) * 0.5f;
		}
		if (y >= 128)
		{
			// 320 max height = -57.6 | 41.6
			// 256 max gen h = -38.4 | -23.4
			// 128 start h = 0 | 15
			// Usually base temp is 15;
			return -((y - 128) * .3f);
		}
		return 0f;
	}

	public static float GetLightTemperature(int lightLevel)
	{
		return MathHelper.lerp(lightLevel / 15f, -4.5f, 4.5f);
	}

	public TemperatureClothing.ClothingData GetProviderClothingData(PlayerEntity player)
	{
		// TODO for now we hardcore getting armor of player
		TemperatureClothing.ClothingData data = new TemperatureClothing.ClothingData();
		player.getArmorItems().forEach(itemStack -> {
			var clothing = TemperatureClothing.CLOTHING_MAP.get(itemStack.getItem());
			if (clothing != null)
			{
				data.Insulation += clothing.Insulation;
				data.WindResistance += clothing.WindResistance;
			}
		});
		data.Insulation = MathHelper.clamp(data.Insulation, 0f, 1f);
		data.WindResistance = MathHelper.clamp(data.WindResistance, 0f, 1f);
		return data;
	}
}
