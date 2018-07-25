package com.paranoiaworks.unicus.android.sse.misc;

import sse.org.bouncycastle.crypto.digests.SHA3Digest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

import com.paranoiaworks.unicus.android.sse.StaticApp;

/**
 * Additional Entropy Provider
 * 
 * @author Paranoia Works
 * @version 1.0.0
 */
public class ExtendedEntropyProvider implements SensorEventListener {
	
	private SensorManager mSensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	
	private double acc0 = 0;
	private double acc1 = 0;
	private double acc2 = 0;
	private double mg0 = 0;
	private double mg1 = 0;
	private double mg2 = 0;
	private long iterations = 0;
	
	public static byte[] getSystemStateDataDigested() 
	{			
		return getSHA3Hash(getSystemStateData().getBytes(), 256);
	}
	
	public static String getSystemStateData()
	{
		StringBuffer systemVariables = new StringBuffer();
		
		systemVariables.append(String.valueOf(System.currentTimeMillis()));
		systemVariables.append(" ");
		
		try {
			if(android.os.Build.VERSION.SDK_INT >= 17)
			{
				systemVariables.append(SystemClock.elapsedRealtimeNanos());
				systemVariables.append(" ");
			}
		} catch (Throwable e) {
			// N/A
		}
		
		try {
			systemVariables.append(SystemClock.uptimeMillis());
			systemVariables.append(" ");
		} catch (Throwable e) {
			// N/A
		}
		
		try {
			systemVariables.append(SystemClock.currentThreadTimeMillis());
			systemVariables.append(" ");
		} catch (Throwable e) {
			// N/A
		}
		
		try {
			systemVariables.append(Runtime.getRuntime().freeMemory());
			systemVariables.append(" ");
		} catch (Throwable e) {
			// N/A
		}
		
		try {
			systemVariables.append(android.os.Process.myTid());
			systemVariables.append(" ");
		} catch (Throwable e) {
			// N/A
		}
		
		try {
			systemVariables.append(android.os.Process.getElapsedCpuTime());
			systemVariables.append(" ");
		} catch (Throwable e) {
			// N/A
		}
				
		return systemVariables.toString();
	}
	
	public ExtendedEntropyProvider() 
	{		
		Context context = StaticApp.getContext();
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if(mSensorManager != null)
		{
			accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		}
	}	
	
	public void startCollectors() 
	{	        	   
		if(accelerometer != null)
			mSensorManager.registerListener(ExtendedEntropyProvider.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		if(magnetometer != null)
			mSensorManager.registerListener(ExtendedEntropyProvider.this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void stopCollectors() 
	{	        	   
		mSensorManager.unregisterListener(this);
	}
	
	public byte[] getActualDataDigested() 
	{			
		return getSHA3Hash(getActualData().getBytes(), 512);
	}
	
	public String getActualData() 
	{			
		String output = iterations + " " + acc0 + " " + acc1 + " " + acc2  + " " + mg0  + " " + mg1  + " " + mg2;
		return output;
	}
	
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) 
    {
    	// N/A
    }
 
    @Override
    public void onSensorChanged(SensorEvent event) 
    {
        try {
			++iterations;
        	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				acc0 += event.values[0];
				acc1 += event.values[1];
				acc2 += event.values[2];
				
				Log.d("---", "ACC x = " + event.values[0] + "\nACC y = " + event.values[1] + "\nACC z = " + event.values[2]);
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mg0 += event.values[0];
				mg1 += event.values[1];
				mg2 += event.values[2];
				       	
				Log.d("---", "MF x = " + event.values[0] + "\nMF y = " + event.values[1] + "\nMF z = " + event.values[2]);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
    }
    
    public static byte[] getSHA3Hash(byte[] data, int bits)
    {
    	byte[] hash = new byte[bits / 8];
    	SHA3Digest digester = new SHA3Digest(bits);
    	digester.update(data, 0, data.length);
    	digester.doFinal(hash, 0);
    	return hash;
    }
    
	// + Test
	/*
	final ExtendedEntropyProvider eep = new ExtendedEntropyProvider();
	eep.startCollectors();
	  	
	Thread collectorThread = new Thread (new Runnable() {
		public void run() {			        	   
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			eep.stopCollectors();
			System.out.println("DATA P: " + eep.getActualData());
			System.out.println("DATA D: " + Helpers.byteArrayToHexString(eep.getActualDataDigested()));
			
       }
    });
	
	collectorThread.start();
	*/
	// - Test
}
