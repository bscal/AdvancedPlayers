package me.bscal.advancedplayer.common.components;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.mechanics.temperature.TemperatureBody;
import net.minecraft.util.Identifier;

public class ComponentManager implements EntityComponentInitializer
{

	public static final ComponentKey<TemperatureBody> BODY_TEMPERATURE = ComponentRegistryV3.INSTANCE.getOrCreate(
			new Identifier(AdvancedPlayer.MOD_ID, "body_temperature"), TemperatureBody.class);

	public static final ComponentKey<FloatComponent> WETNESS = ComponentRegistryV3.INSTANCE.getOrCreate(
			new Identifier(AdvancedPlayer.MOD_ID, "wetness"), FloatComponent.class);

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
	{
		registry.registerForPlayers(BODY_TEMPERATURE, TemperatureBody::new, RespawnCopyStrategy.INVENTORY);
		registry.registerForPlayers(WETNESS, WetnessComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
	}
}
