package me.bscal.advancedplayer.common.mechanics.temperature;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.body.EntityBody;
import me.bscal.advancedplayer.common.mechanics.body.FloatBodyPart;
import me.bscal.seasons.api.SeasonAPI;
import me.bscal.seasons.common.seasons.SeasonState;
import me.bscal.seasons.common.seasons.SeasonType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

public class TemperatureBody extends EntityBody
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

	public static final Reference2ObjectOpenHashMap<Item, TemperatureClothing> CLOTHING_MAP = new Reference2ObjectOpenHashMap<>();

	public static void LoadClothingMap()
	{
		CLOTHING_MAP.clear();
		CLOTHING_MAP.put(Items.LEATHER_BOOTS, new TemperatureClothing(0.3f, 0.3f));
		CLOTHING_MAP.put(Items.LEATHER_LEGGINGS, new TemperatureClothing(0.3f, 0.3f));
		CLOTHING_MAP.put(Items.LEATHER_CHESTPLATE, new TemperatureClothing(0.3f, 0.3f));
		CLOTHING_MAP.put(Items.LEATHER_HELMET, new TemperatureClothing(0.3f, 0.3f));
	}

	protected float m_Work;
	protected float m_WorkRecoverySpeed;

	public TemperatureBody(PlayerEntity player)
	{
		super(player, MIN_COLD, MAX_HOT, (bodyPartMap) -> {
			// I wonder if the compilers or java optimizes this into a putAll or something?
			bodyPartMap.put("Head", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 1.5f));
			bodyPartMap.put("Chest", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 1.0f));
			bodyPartMap.put("LArm", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, .5f));
			bodyPartMap.put("RArm", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, .5f));
			bodyPartMap.put("Groin", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 1.0f));
			bodyPartMap.put("LLeg", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, .75f));
			bodyPartMap.put("RLeg", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, .75f));
			bodyPartMap.put("LFoot", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 0.5f));
			bodyPartMap.put("RFoot", new FloatBodyPart(NORMAL, MIN_COLD, MAX_HOT, 0.5f));
		});
		m_Work = 0;
		m_WorkRecoverySpeed = 0.1f;
		SetCoreBodyValue(NORMAL);
	}

	@Override
	public void ResetBody()
	{
		super.ResetBody();
		SetCoreBodyValue(NORMAL);
	}

	int m_Counter = 0;

	@Override
	public void serverTick()
	{
		if (m_Work > 0)
			m_Work -= m_WorkRecoverySpeed;
		else
			m_Work = 0;

		// Did not want to calculate temperature every tick
		// Updates every 64 ticks (~3 secs)
		if ((m_Counter++ % 64) == 0)
		{
			UpdateTemperatures();
			super.serverTick();
		}
	}

	public void AddWork(float amount)
	{
		m_Work += amount;
	}

	public float CalculateAirTemperature()
	{
		float temperature = 0f;

		BlockPos pos = m_Provider.getBlockPos();
		Biome biome = m_Provider.world.getBiome(pos);
		Identifier biomeId = SeasonAPI.getBiomeId(biome, m_Provider.world);
		SeasonState season = SeasonAPI.getSeason(biomeId);
		SeasonType seasonType = SeasonAPI.getSeasonType(biomeId);

		return temperature;
	}

	public void UpdateTemperatures()
	{
		BlockPos pos = m_Provider.getBlockPos();
		Biome biome = m_Provider.world.getBiome(pos);
		Identifier biomeId = SeasonAPI.getBiomeId(biome, m_Provider.world);
		TemperatureBiomeRegistry.BiomeClimate climate = TemperatureBiomeRegistry.BiomesToClimateMap.get(biomeId);
		float airTemperature = climate.temperatures().GetTemperature();
		float windModifier = 0f;
		float wetnessModifier = 0f;

		float insulation = 0f;
		float windResistance = 0f;

		float delta = 1.0f;
		float changeToNormal = MathHelper.lerp(delta, m_CoreBodyValue, NORMAL);
		float temperatureDelta = airTemperature - (m_Work + changeToNormal);
		float finalTemperatureDelta = temperatureDelta + (temperatureDelta * MathHelper.clamp(insulation, 0f, 1.0f));

		if (FabricLoader.getInstance().isDevelopmentEnvironment())
		{
			AdvancedPlayer.LOGGER.info(String.format("""
													              
													 +--------+ Temperature +--------+
													  	CoreBodyValue = %.2f
													  	Work = %.2f
													  	airTemperature = %.2f
													  	windModifier = %.2f
													  	wetnessModifier = %.2f
													  	insulation = %.2f
													  	windResistance = %.2f,
													  	changeToNormal = %.2f
													  	temperatureDelta = %.2f
													  	finalTemperatureDelta = %.2f
													  	biome = %s
													  	climate = %s""", m_CoreBodyValue, m_Work, airTemperature, windModifier, wetnessModifier, insulation,
					windResistance, changeToNormal, temperatureDelta, finalTemperatureDelta, biomeId, climate));
		}

		SetCoreBodyValue(finalTemperatureDelta);

		//m_PartsMap.forEach((s, bodyPart) -> ((TemperatureBodyPart) bodyPart).UpdateTemperature(this, airTemperature, windModifier, wetnessModifier));

	}
}
