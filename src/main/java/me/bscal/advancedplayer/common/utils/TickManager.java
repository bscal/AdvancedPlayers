package me.bscal.advancedplayer.common.utils;

import java.util.ArrayList;
import java.util.List;

public class TickManager
{

    public int Interval;
    public List<Tickable> TickEntries;
    private int m_CurrentIndex;
    private float m_ListProgress;
    private int m_NextCycleStart;

    public TickManager(int interval)
    {
        Interval = interval;
        TickEntries = new ArrayList<>(32);
    }

    public void Tick(int currentTick)
    {
        if (m_NextCycleStart <= currentTick)
        {
            m_CurrentIndex = 0;
            m_ListProgress = 0;
            m_NextCycleStart = currentTick + Interval;
        }

        m_ListProgress += TickEntries.size() / (float) Interval;
        int maxIndex = Math.min(TickEntries.size(), (int) Math.ceil(m_ListProgress));

        var it = TickEntries.listIterator(m_CurrentIndex);
        while (m_CurrentIndex++ < maxIndex)
        {
            it.next().Tick();
        }
    }

    public interface Tickable
    {
        void Tick();
    }

}
