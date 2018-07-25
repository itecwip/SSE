package com.paranoiaworks.unicus.android.sse.utils;

/**
 * Simple Helper class for Time Measurement purposes
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class HowLong {

	private static long start = 0;
	
	public static void start_reset()
	{
		start = System.currentTimeMillis();
		//Log.v("HowLong:", "STARTED");
	}
	
	public static long getDuration()
	{
		long duration = System.currentTimeMillis() - start;
		//Log.v("Duration:", "" + duration);
		return duration;
	}
}
