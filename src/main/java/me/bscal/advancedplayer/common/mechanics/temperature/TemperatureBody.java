package me.bscal.advancedplayer.common.mechanics.temperature;

import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.components.WetnessComponent;
import me.bscal.advancedplayer.common.mechanics.body.EntityBodyComponent;
import me.bscal.advancedplayer.common.mechanics.body.FloatBodyPart;
import me.bscal.seasons.api.SeasonAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

public class TemperatureBody extends EntityBodyComponent
{

	public static final float MAX_HOT = 50.0f;
	public static final float EXTREMELY_HOT = 43.0f; // Human body limit
	public static final float VERY_HOT = 41.0f;
	public static final float HOT = 39.0f; // Fever (technically 38C, but this is better)
	public static final float WARM = 38.0f;
	public static final float NORMAL = 37.0f;
	public static final float CHILLY = 36.0f;
	public static final float COLD = 35.0f; // Hypothermia begins
	public static final float VERY_COLD = 33.0f;
	public static final float FREEZING = 31.0f; // Human body limit
	public static final float MIN_COLD = 25.0f;

	public static final int UPDATES_PER_TICK = 64 - 1; // Must be a power of 2

	public float Work; // Amount of additional work the play is doing. IE running
	public float HeatLossRate;
	public float BodyTemperature;
	public float OutSideTemperature;
	public float Insulation;
	public float WindResistance;
	public TemperatureShiftType ShiftType;
	private int m_UpdateCounter;

	public TemperatureBody(PlayerEntity player)
	{
		super(player, EntityBodyComponent.MAX_PARTS, NORMAL, MIN_COLD, MAX_HOT);
	}

	@Override
	public void serverTick()
	{
		// Updates every 64 ticks (3.2 secs)
		// I was looking into this, and I was not sure if Java's
		// irem instruction optimized to use AND, I presume it would,
		// but I just force it to use AND in case
		if ((m_UpdateCounter++ & UPDATES_PER_TICK) == 0)
		{
			UpdateTemperatures();
			super.serverTick(); // Checks if dirty and syncs
		}
	}

	public void AddWork(float amount)
	{
		Work += amount;
	}

	public void UpdateTemperatures()
	{
		BlockPos pos = m_Provider.getBlockPos();
		Biome biome = m_Provider.world.getBiome(pos);
		Identifier biomeId = SeasonAPI.getBiomeId(biome, m_Provider.world);
		TemperatureBiomeRegistry.BiomeClimate climate = TemperatureBiomeRegistry.BiomesToClimateMap.get(biomeId);
		float airTemperature = climate.GetCurrentTemperature();
		float yTemperature = GetYTemperature(pos);
		float lightTemperature = GetLightTemperature(m_Provider.world.getLightLevel(LightType.SKY, pos));
		float humidity = 0.5f;
		float wind = 3f;
		WetnessComponent wetnessComponent = ComponentManager.WETNESS.get(m_Provider);
		float wetness = wetnessComponent.Wetness;
		TemperatureClothing.ClothingData clothingData = GetProviderClothingData();
		Insulation = clothingData.Insulation;
		WindResistance = clothingData.WindResistance;

		float m_BaseWork = 1.0f; // Players body always doing some work.
		BodyTemperature = CoreBodyValue + Work + m_BaseWork;
		OutSideTemperature = airTemperature + yTemperature + lightTemperature - (wind - WindResistance);
		float diff = BodyTemperature - OutSideTemperature;
		ShiftType = TemperatureShiftType.TypeForTemp(OutSideTemperature);

		// 100% insulation would mean you lose 0 heat, 0% you lose all the heat;
		HeatLossRate = MathHelper.lerp(Insulation, diff / 200, .0f);
		// Since Work is temporary
		Work = MathHelper.clamp(Work - HeatLossRate, 0, 10f);
		// Body moving towards the outside temperature. Not an expert at thermodynamics but this seems like a
		// decent system even though not 100% accurate
		CoreBodyValue = MathHelper.lerp(HeatLossRate, CoreBodyValue, OutSideTemperature);
		// 100% would not allow evaporation to take place. This does not matter if it is cold.
		float delta = TemperatureShiftType.IsWarming(ShiftType) ? MathHelper.lerp(humidity, .1f, .0f) : .1f;
		CoreBodyValue = MathHelper.lerp(delta, CoreBodyValue, NORMAL);

		// Sweat
		if (CoreBodyValue >= HOT) wetnessComponent.Wetness += 0.1f;
		else if (wetnessComponent.Wetness > 0) wetnessComponent.Wetness -= 0.1f;

		IsDirty = true;

		if (FabricLoader.getInstance().isDevelopmentEnvironment() && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
		{
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.clear();
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("CoreBodyValue = " + CoreBodyValue);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("BodyTemperature = " + BodyTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("Work = " + Work);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("outsideTemperature = " + OutSideTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("airTemperature = " + airTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("yTemperature = " + yTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("lightTemperature = " + lightTemperature);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("diff = " + diff);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("delta = " + delta);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("heatLossRate = " + HeatLossRate);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("wetness = " + wetness);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("TemperatureShiftType = " + ShiftType);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("biomeId = " + biomeId);
			AdvancedPlayerClient.TemperatureDebugWindow.TemperatureDebugTextList.add("season = " + SeasonAPI.getSeason(biomeId));
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

	public TemperatureClothing.ClothingData GetProviderClothingData()
	{
		// TODO for now we hardcore getting armor of player
		TemperatureClothing.ClothingData data = new TemperatureClothing.ClothingData();
		m_Provider.getArmorItems().forEach(itemStack -> {
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

	@Override
	public void PopulateBodyMap()
	{
		BodyParts[EntityBodyComponent.HEAD] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 1.5f);
		BodyParts[EntityBodyComponent.CHEST] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 1.0f);
		BodyParts[EntityBodyComponent.LEFT_ARM] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, .5f);
		BodyParts[EntityBodyComponent.RIGHT_ARM] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, .5f);
		BodyParts[EntityBodyComponent.GROIN] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 1.0f);
		BodyParts[EntityBodyComponent.LEFT_LEG] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, .75f);
		BodyParts[EntityBodyComponent.RIGHT_LEG] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, .75f);
		BodyParts[EntityBodyComponent.LEFT_FOOT] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 0.5f);
		BodyParts[EntityBodyComponent.RIGHT_FOOT] = new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 0.5f);
	}

	@Override
	public void writeToNbt(NbtCompound nbt)
	{
		super.writeToNbt(nbt);
		nbt.putFloat("Work", Work);
		nbt.putFloat("HeatLossRate", HeatLossRate);
		nbt.putFloat("BodyTemperature", BodyTemperature);
		nbt.putFloat("OutSideTemperature", OutSideTemperature);
		nbt.putInt("ShiftType", ShiftType.ordinal());
	}

	@Override
	public void readFromNbt(NbtCompound nbt)
	{
		super.readFromNbt(nbt);
		Work = nbt.getFloat("Work");
		HeatLossRate = nbt.getFloat("HeatLossRate");
		BodyTemperature = nbt.getFloat("BodyTemperature");
		OutSideTemperature = nbt.getFloat("OutSideTemperature");
		ShiftType = TemperatureShiftType.values()[nbt.getInt("ShiftType")];
	}

	public enum TemperatureShiftType
	{
		Normal(TemperatureBiomeRegistry.EVEN_BODY_TEMP), Cooling(TemperatureBiomeRegistry.COOLING_BODY_TEMP), Warming(
			TemperatureBiomeRegistry.WARMING_BODY_TEMP), Freezing(TemperatureBiomeRegistry.FREEZING_BODY_TEMP), Burning(
			TemperatureBiomeRegistry.BURNING_BODY_TEMP);

		public final float Temperature;

		TemperatureShiftType(float temperature)
		{
			Temperature = temperature;
		}

		public static TemperatureShiftType TypeForTemp(float temperature)
		{
			if (temperature < Freezing.Temperature) return Freezing;
			else if (temperature < Cooling.Temperature) return Cooling;
			else if (temperature > Burning.Temperature) return Burning;
			else if (temperature > Warming.Temperature) return Warming;
			return Normal;
		}

		public static boolean IsWarming(TemperatureShiftType type)
		{
			return type == Warming || type == Burning;
		}

		public static boolean IsCooling(TemperatureShiftType type)
		{
			return type == Cooling || type == Freezing;
		}

		public static boolean IsBigDifference(TemperatureShiftType type)
		{
			return type == Freezing || type == Burning;
		}

	}

}

/*
	Saved this for reference.
			Body wants to be at NORMAL temperature.
			Body creates Work which increases temperature. Heat Production = M - W (We do not calculate M)
			Temperature goes down over time - Climate effects this
			If > NORMAL = your hot, < NORMAL = your cold.
			Heat Loss = R + C + E + K where:
			R = Radiation (heat loss or gain) between skin, clothing, or surfaces (includes sun). At rest, nude, in 21C environment, heat loss = 60%
			C = Convection (Air near body) natural - air moving from body, forced - wind. At rest = 18%
			E = Evaporation
			K = Conduction surfaces (blocks)
			There are other variables, but we don't need those.
 */