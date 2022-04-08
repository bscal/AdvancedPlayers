package me.bscal.advancedplayer.common.utils;

import java.util.stream.LongStream;

public class Timer
{
	public long Last;
	public long[] PreviousTimings;
	private int m_PreviousCount;
	private long m_Start;

	public Timer()
	{
		this(1);
	}

	public Timer(int count)
	{
		PreviousTimings = new long[count];
	}

	public void Start()
	{
		m_Start = System.nanoTime();
	}

	public void Stop()
	{
		Last = System.nanoTime() - m_Start;

		if (PreviousTimings == null) return;
		PreviousTimings[m_PreviousCount] = Last;
		if (++m_PreviousCount >= PreviousTimings.length) m_PreviousCount = 0;
	}

	public double GetAverage()
	{
		return LongStream.of(PreviousTimings).average().getAsDouble();
	}

	public String GetPrettyPrint()
	{
		double avg = GetAverage();
		return String.format("[Timer] Took: %dns, %dms. Avg(%d): %.4fns, %.4fms",
				Last,
				Last / 1000000,
				PreviousTimings.length,
				avg,
				avg / 1000000);
	}

}
