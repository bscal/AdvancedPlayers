package me.bscal.advancedplayer.mixin;

import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(SensorType.class) public interface SensorTypeAccessor
{
	@Invoker("<init>")
	static <U> SensorType createSensorType(Supplier<U> factory)
	{
		throw new UnsupportedOperationException();
	}

	@Invoker
	static <U extends Sensor<?>> SensorType<U> callRegister(String id, Supplier<U> factory)
	{
		throw new UnsupportedOperationException();
	}
}
