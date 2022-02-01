package me.bscal.advancedplayer.common.mechanics.temperature;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.components.ComponentManager;
import me.bscal.advancedplayer.common.mechanics.body.EntityBodyComponent;
import me.bscal.advancedplayer.common.mechanics.body.FloatBodyPart;
import me.bscal.seasons.api.SeasonAPI;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
	public float WorkRecoverySpeed; // How fast Work degrades to 0
	protected final float m_DeltaToNormalTemperature; // The rate at which player's body goes to default temp
	protected int m_UpdateCounter;

	public TemperatureBody(PlayerEntity player)
	{
		super(player, EntityBodyComponent.MAX_PARTS, NORMAL, MIN_COLD, MAX_HOT);
		Work = 0;
		WorkRecoverySpeed = 0.1f;
		m_DeltaToNormalTemperature = 1.0f;
	}

	@Override
	public void serverTick()
	{
		TickWork();

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

	public void TickWork()
	{
		if (Work > 0)
			Work -= WorkRecoverySpeed;
		else
			Work = 0;
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

		float airTemperature = climate.temperatures().GetTemperature();

		float wind = 0f;
		float wetness = ComponentManager.WETNESS.get(m_Provider).Wetness;

		float insulation = 0f;
		float windResistance = 0f;

		float changeToNormal = MathHelper.lerp(m_DeltaToNormalTemperature, CoreBodyValue, NORMAL);
		float temperatureDelta = airTemperature - (Work + changeToNormal);
		float finalTemperatureDelta = temperatureDelta + (temperatureDelta * MathHelper.clamp(insulation, 0f, 1.0f));

		CoreBodyValue = finalTemperatureDelta;
		IsDirty = true;

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
													  	climate = %s""", CoreBodyValue, Work, airTemperature, wind, wetness, insulation,
					windResistance, changeToNormal, temperatureDelta, finalTemperatureDelta, biomeId, climate));
		}
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
