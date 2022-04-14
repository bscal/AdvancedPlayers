package me.bscal.advancedplayer.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(MemoryModuleType.class) public interface MemoryModuleTypeAccessor
{
	@Invoker("<init>")
	static <U> MemoryModuleType createMemoryModuleType(Optional<Codec<U>> codec)
	{
		throw new UnsupportedOperationException();
	}

	@Invoker
	static <U> MemoryModuleType<U> callRegister(String id, Codec<U> codec)
	{
		throw new UnsupportedOperationException();
	}

	@Invoker
	static <U> MemoryModuleType<U> callRegister(String id)
	{
		throw new UnsupportedOperationException();
	}
}
