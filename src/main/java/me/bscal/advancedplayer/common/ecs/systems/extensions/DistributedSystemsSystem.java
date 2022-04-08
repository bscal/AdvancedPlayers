package me.bscal.advancedplayer.common.ecs.systems.extensions;

import com.artemis.BaseEntitySystem;
import com.artemis.BaseSystem;
import com.artemis.utils.IntBag;
import me.bscal.advancedplayer.common.utils.DistributedTickScheduler;

import java.util.ArrayList;
import java.util.List;

public class DistributedSystemsSystem extends BaseSystem
{

	DistributedTickScheduler Scheduler;
	List<BaseEntitySystem> Systems;
	List<DistributedTickScheduler.TickEntry> Entries;

	public DistributedSystemsSystem()
	{
		Scheduler = new DistributedTickScheduler();
		Systems = new ArrayList<>();
		Entries = new ArrayList<>();
	}

	@Override
	protected void processSystem()
	{
		//IntBag entities = subscription.getEntities();
		Scheduler.Tick(1);
	}

	public void AddSystem(BaseEntitySystem system, int interval)
	{
		Systems.add(system);
		var entry = Scheduler.RegisterRunnable(interval, system::process);
		Entries.add(entry);
	}


}
