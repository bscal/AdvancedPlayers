package me.bscal.advancedplayer.common.mechanics.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.mechanics.ecs.components.RefPlayer;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Sync;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Temperature;
import me.bscal.advancedplayer.common.mechanics.ecs.components.Wetness;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBiomeRegistry;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureClothing;
import me.bscal.seasons.api.SeasonAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
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

	@Override
	protected void process(int entityId)
	{
		var temperature = Temperatures.get(entityId);
		var player = Players.get(entityId).Player;
		var sync = SyncPlayers.get(entityId);

		var wetness = PlayerWetness.get(entityId);
		float wetnessValue = (wetness == null) ? 0 : wetness.Wetness;

		BlockPos pos = player.getBlockPos();
		RegistryEntry<Biome> biome = player.world.getBiome(pos);
		Identifier biomeId = SeasonAPI.getBiomeId(biome.value(), player.world);
		TemperatureBiomeRegistry.BiomeClimate climate = TemperatureBiomeRegistry.BiomesToClimateMap.get(biomeId);
		float airTemperature = climate.GetCurrentTemperature();
		float yTemperature = GetYTemperature(pos);
		float lightTemperature = GetLightTemperature(player.world.getLightLevel(LightType.SKY, pos));
		float humidity = 0.5f;
		float wind = 3f;
		TemperatureClothing.ClothingData clothingData = GetProviderClothingData(player);
		temperature.Insulation = clothingData.Insulation;
		temperature.WindResistance = clothingData.WindResistance;

		float m_BaseWork = 1.0f; // Players body always doing some work.
		temperature.BodyTemperature = temperature.CoreBodyTemperature + temperature.Work + m_BaseWork;
		temperature.OutSideTemperature = airTemperature + yTemperature + lightTemperature - (wind - temperature.WindResistance);
		float diff = temperature.BodyTemperature - temperature.OutSideTemperature;
		temperature.ShiftType = TemperatureBody.TemperatureShiftType.TypeForTemp(temperature.OutSideTemperature);

		// 100% insulation would mean you lose 0 heat, 0% you lose all the heat;
		temperature.HeatLossRate = MathHelper.lerp(temperature.Insulation, diff / 200, .0f);
		// Since Work is temporary
		temperature.Work = MathHelper.clamp(temperature.Work - temperature.HeatLossRate, 0, 10f);
		// Body moving towards the outside temperature. Not an expert at thermodynamics but this seems like a
		// decent system even though not 100% accurate
		temperature.CoreBodyTemperature = MathHelper.clamp(temperature.CoreBodyTemperature, TemperatureBody.MIN_COLD, TemperatureBody.MAX_HOT);
		temperature.CoreBodyTemperature = MathHelper.lerp(temperature.HeatLossRate, temperature.CoreBodyTemperature, temperature.OutSideTemperature);
		// 100% would not allow evaporation to take place. This does not matter if it is cold.
		float delta = TemperatureBody.TemperatureShiftType.IsWarming(temperature.ShiftType) ? MathHelper.lerp(humidity, .1f, .0f) : .1f;
		temperature.CoreBodyTemperature = MathHelper.lerp(delta, temperature.CoreBodyTemperature, TemperatureBody.NORMAL);

		sync.Add(temperature, Temperature.class);

		if (FabricLoader.getInstance().isDevelopmentEnvironment() && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
		{
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.clear();
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("CoreBodyTemperature = " + temperature.CoreBodyTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("BodyTemperature = " + temperature.BodyTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("Work = " + temperature.Work);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("outsideTemperature = " + temperature.OutSideTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("heatLossRate = " + temperature.HeatLossRate);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("TemperatureShiftType = " + temperature.ShiftType);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add(String.format("Wetness: Has %s, Value %.2f", wetnessValue > 0, wetnessValue));
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("season = " + SeasonAPI.getSeason());
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("airTemperature = " + airTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("yTemperature = " + yTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("lightTemperature = " + lightTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("diff = " + diff);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("delta = " + delta);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("biomeId = " + biomeId);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("climate = " + climate);
		}

	}

	public float GetYTemperature(BlockPos pos)
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

	public float GetLightTemperature(int lightLevel)
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
