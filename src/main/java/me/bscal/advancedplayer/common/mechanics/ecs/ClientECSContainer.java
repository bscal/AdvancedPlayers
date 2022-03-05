package me.bscal.advancedplayer.common.mechanics.ecs;

import com.artemis.Component;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.List;

public class ClientECSContainer
{

	public final Object2ObjectOpenHashMap<Class<? extends Component>, Component> ComponentLookup;

	public ClientECSContainer()
	{
		ComponentLookup = new Object2ObjectOpenHashMap<>();
	}

}
