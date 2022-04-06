package me.bscal.advancedplayer.common.ecs;

import com.esotericsoftware.kryo.Kryo;
import me.bscal.advancedplayer.AdvancedPlayer;
import me.bscal.advancedplayer.common.food.MultiFood;
import net.minecraft.util.Identifier;

public abstract class ECSManager
{

	public static final String SAVE_EXTENSION = ".bin";
	public static final float DELTA = 1f / 20f; // Minecraft runs 20 ticks per seconds, so I don't think there is a delta?
	public static final Identifier CREATE_CHANNEL = new Identifier(AdvancedPlayer.MOD_ID, "create");
	public static final Identifier SYNC_CHANNEL = new Identifier(AdvancedPlayer.MOD_ID, "sync");


	public static void InitKryo(Kryo kryo)
	{
		kryo.setClassLoader(AdvancedPlayer.class.getClassLoader());
		kryo.register(MultiFood.class);
		kryo.register(MultiFood.Ingredient.class);
		kryo.register(MultiFood.FoodGroups.class);
		kryo.register(MultiFood.Cookable.class);
		kryo.register(MultiFood.Perishable.class);
	}

	public abstract Kryo GetKryo();

}
