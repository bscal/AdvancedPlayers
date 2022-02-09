package me.bscal.advancedplayer.common.mechanics.temperature;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.components.WetnessComponent;
import me.bscal.advancedplayer.common.mechanics.body.EntityBodyComponent;
import me.bscal.advancedplayer.common.mechanics.body.FloatBodyPart;
import me.bscal.seasons.api.SeasonAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
		float insulation = 0f;
		float windResistance = 0f;

		/*
			Body wants to be at NORMAL temperature.
			Body creates Work which increases temperature. Heat Production = M - W (We do not calculate M)
			Temperature goes down over time - Climate effects this
			If > NORMAL = your hot, < NORMAL = your cold.
			Heat Loss = R + C + E + K where:
			R = Radiation (heat loss or gain) between skin, clothing, or surfaces (includes sun). At rest, nude, in 21C environment, heat loss = 60%
			C = Convection (Air near body) natural - air moving from body, forced - wind. At rest = 18%
			E = Evaporation // TODO Current not used
			K = Conduction surfaces (blocks) // TODO Current not used
			There are other variables, but we don't need those.
		 */
		float m_BaseWork = 1.0f;
		BodyTemperature = CoreBodyValue + Work + m_BaseWork;
		OutSideTemperature = airTemperature + yTemperature + lightTemperature - (wind - windResistance);
		float diff = BodyTemperature - OutSideTemperature;
		ShiftType = TemperatureShiftType.TypeForTemp(OutSideTemperature);

		HeatLossRate = MathHelper.lerp(insulation, diff / 200, .0f);
		// Body moving towards the outside temperature. Not an expert at thermodynamics but this seems like a
		// decent system even though not 100% accurate
		CoreBodyValue = MathHelper.lerp(HeatLossRate, CoreBodyValue, OutSideTemperature);
		// Body trying to maintain stable temperature. This is a little simpler to do
		// then combined into the previous lerp because outside temp is usually always colder than you.
		float delta = MathHelper.lerp(humidity, .1f, .0f);
		CoreBodyValue = MathHelper.lerp(delta, CoreBodyValue, NORMAL);

		if (CoreBodyValue >= HOT) wetnessComponent.Wetness += 0.1f; // Sweat
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

	public static class TemperatureClothing
	{
		public static final Reference2ObjectOpenHashMap<Item, TemperatureClothingData> CLOTHING_MAP = new Reference2ObjectOpenHashMap<>();

		public static void LoadClothingMap()
		{
			CLOTHING_MAP.clear();
			CLOTHING_MAP.put(Items.LEATHER_BOOTS, new TemperatureClothingData(0.3f, 0.3f));
			CLOTHING_MAP.put(Items.LEATHER_LEGGINGS, new TemperatureClothingData(0.3f, 0.3f));
			CLOTHING_MAP.put(Items.LEATHER_CHESTPLATE, new TemperatureClothingData(0.3f, 0.3f));
			CLOTHING_MAP.put(Items.LEATHER_HELMET, new TemperatureClothingData(0.3f, 0.3f));
		}
	}

	public record TemperatureClothingData(float insulation, float windResistance)
	{
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
			return type == Cooling || type == Freezing;
		}

		public static boolean IsCooling(TemperatureShiftType type)
		{
			return type == Warming || type == Burning;
		}

		public static boolean IsBigDifference(TemperatureShiftType type)
		{
			return type == Freezing || type == Burning;
		}

	}

}
