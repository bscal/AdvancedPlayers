package me.bscal.advancedplayer.common.mechanics.temperature;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.bscal.advancedplayer.client.AdvancedPlayerClient;
import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.mechanics.body.EntityBodyComponent;
import me.bscal.advancedplayer.common.mechanics.body.FloatBodyPart;
import me.bscal.seasons.api.SeasonAPI;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
	public float BaseWork;
	protected int m_UpdateCounter;

	public TemperatureBody(PlayerEntity player)
	{
		super(player, EntityBodyComponent.MAX_PARTS, NORMAL, MIN_COLD, MAX_HOT);
		Work = 0;
		BaseWork = 1.0f;
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

		float airTemperature = climate.temperatures().GetCurrentTemperature();
		float yTemperature = GetYTemperature(pos);
		float lightTemperature = GetLightTemperature(m_Provider.world.getLightLevel(LightType.SKY, pos));

		float humidity = 0f;
		float wind = 0f; // TODO
		float wetness = ComponentManager.WETNESS.get(m_Provider).Wetness;

		float insulation = 0f; // TODO Clothing
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
		float bodyTemperature = CoreBodyValue + Work + BaseWork;
		float outsideTemperature = airTemperature + yTemperature;
		float diff = bodyTemperature - outsideTemperature;
		float heatLossRate = diff / 200;
		//float changeToNormal = MathHelper.lerp(heatLossRate, bodyTemperature, NORMAL);
		CoreBodyValue -= heatLossRate;
		IsDirty = true;

		if (FabricLoader.getInstance().isDevelopmentEnvironment() && m_Provider.world.isClient && AdvancedPlayerClient.ShowTemperatureDebug)
		{
			AdvancedPlayerClient.TemperatureDebugWindow.Text = String.format("");
		}
	}

	public float CalculateHeatLoss(float temperature, float humidity, float wetness, float wind, float insulation, float windResistence)
	{
		return 0;
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
		// Light Level 0 - 15
		//TODO
		return 0f;
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
	}

	@Override
	public void readFromNbt(NbtCompound nbt)
	{
		super.readFromNbt(nbt);
		Work = nbt.getFloat("Work");
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

}
