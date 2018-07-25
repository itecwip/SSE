package com.paranoiaworks.unicus.android.sse;

import android.app.Application;
import android.content.Context;

/**
 * "Static Context" provider 
 * 
 * @author Paranoia Works
 * @version 1.0.4
 */
public class StaticApp extends Application {

    private static Context mContext;
    
    public static final boolean SHOW_UPGRADE_FEATURES = false;
    public static final boolean CN_VERSION = false;
    public static int licenseLevel = 0; // 0 - unknown

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
    
	/** Get String Resource dynamically by Identifier */
	public static String getStringResource(String name)
    {
    	String resText = null;
    	int resID = getStringResID(name);
    	if(resID > 1) resText = mContext.getResources().getString(resID);
    	else resText = name;
    	
    	return resText;
    }
	
    private static int getStringResID(String name)
    {
    	return mContext.getResources().getIdentifier(name, "string", mContext.getPackageName());
    }
    
    /** Convert DP to PX */
    public static int dpToPx(float dp)
    {
    	float scale = mContext.getResources().getDisplayMetrics().density;
    	return (int)(dp * scale + 0.5f);
    }
    
    /** Convert PX to DP */
    public float pxToDp(int px)
    {
    	float scale = mContext.getResources().getDisplayMetrics().density;
    	return ((float)px - 0.5f) / scale;
    }
}

