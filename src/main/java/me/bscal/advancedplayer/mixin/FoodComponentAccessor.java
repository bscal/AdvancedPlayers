package me.bscal.advancedplayer.mixin;

import net.minecraft.item.FoodComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodComponent.class)
public interface FoodComponentAccessor
{

		@Mutable
		@Accessor("hunger")
		void setHunger(int hunger);

}
