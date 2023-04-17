package me.bscal.advancedplayer.common.entities;

import me.bscal.advancedplayer.AdvancedPlayer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public class EntityRegistry
{

	public static final EntityType<GhoulEntity> GHOUL_ENTITY = Register("ghoul",
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GhoulEntity::new).dimensions(EntityDimensions.fixed(1f, 1f)));

	public static <T extends Entity> EntityType<T> Register(String id, FabricEntityTypeBuilder<T> builder)
	{
		return Registry.register(Registries.ENTITY_TYPE, new Identifier(AdvancedPlayer.MOD_ID, id), builder.build());
	}

}
