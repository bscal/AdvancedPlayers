package me.bscal.advancedplayer.common.mechanics.ecs.components;

import com.artemis.Component;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Sync extends Component
{

	public int NetworkId;
	public Set<Class<? extends Component>> UniqueComponents = new ObjectOpenHashSet<>();
	public List<Component> ComponentInstances = new ArrayList<>();

	public void Add(Component component)
	{
		boolean added = UniqueComponents.add(component.getClass());
		if (added)
		{
			ComponentInstances.add(component);
		}
	}

}
