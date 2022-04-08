package me.bscal.advancedplayer.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Inspiration from HugsLib's tick scheduler a Rimworld modding library. Thought it could be a useful addition when I want to
 * scheduler runnables for mods or plugins more efficiently
 */
public class DistributedTickScheduler
{
	private static DistributedTickScheduler INSTANCE;

	public static DistributedTickScheduler Instance()
	{
		if (INSTANCE == null) INSTANCE = new DistributedTickScheduler();
		return INSTANCE;
	}

	private final List<TickerList> m_RunnableList;
	//private final Map<UUID, >
	//private final ObjectArrayFIFOQueue<TickEntry> m_UnregisterQueue;

	public DistributedTickScheduler()
	{
		m_RunnableList = new ArrayList<>();
		//m_UnregisterQueue = new ObjectArrayFIFOQueue<>(10);
	}

	public TickEntry RegisterRunnable(int interval, Runnable runnable)
	{
		Objects.requireNonNull(runnable, "runnable cannot be null!");
		if (interval <= 0) throw new IllegalArgumentException("interval is <= 0");

		TickEntry entry = new TickEntry(interval, runnable);
		TickerList list = GetOrCreateListRunnable(interval);
		list.Register(entry);
		return entry;
	}

	public TickerList GetOrCreateListRunnable(int interval)
	{
		for (TickerList runnableList : m_RunnableList)
		{
			if (runnableList.m_Interval == interval) return runnableList;
		}
		TickerList newRunnable = new TickerList(interval, this);
		m_RunnableList.add(newRunnable);
		return newRunnable;
	}

	public void Tick(int currentTick)
	{
		m_RunnableList.forEach(list -> list.Tick(currentTick));
	}

	private static class TickerList
	{
		public int NumCalls;
		private final int m_Interval;
		private final DistributedTickScheduler m_Scheduler;
		private final List<TickEntry> m_TickEntries;
		private int m_CurrentIndex;
		private float m_ListProgress;
		private int m_NextCycleStart;

		public TickerList(int interval, DistributedTickScheduler scheduler)
		{
			m_Interval = interval;
			m_Scheduler = scheduler;
			m_TickEntries = new ArrayList<>();
		}

		public int Size()
		{
			return m_TickEntries.size();
		}

		public void Tick(int currentTick)
		{
			NumCalls = 0;
			if (m_NextCycleStart <= currentTick)
			{
				m_CurrentIndex = 0;
				m_ListProgress = 0;
				m_NextCycleStart = currentTick + m_Interval;
			}

			m_ListProgress += m_TickEntries.size() / (float) m_Interval;
			int maxIndex = Math.min(m_TickEntries.size(), (int) Math.ceil(m_ListProgress));

			var it = m_TickEntries.listIterator(m_CurrentIndex);
			while (m_CurrentIndex < maxIndex)
			{
				m_CurrentIndex++;
				var entry = it.next();
				entry.Runnable.run();
				NumCalls++;
				if (entry.Canceled) it.remove();
			}
		}

		public void Register(TickEntry entry)
		{
			m_TickEntries.add(entry);
		}
	}

	public static class TickEntry
	{

		public final int Interval;
		public final Runnable Runnable;
		public boolean Canceled;

		public TickEntry(int interval, Runnable runnable)
		{
			Interval = interval;
			Runnable = runnable;
		}

	}

}
