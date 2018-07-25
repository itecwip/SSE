package com.paranoiaworks.unicus.android.sse.utils;

import java.io.ByteArrayOutputStream;

import org.apache.shiro.util.SoftHashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Thumbnail Cache
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class BitmapCacherCompressed {
	
	SoftHashMap<String, byte[]> softMap = null;
	
	public BitmapCacherCompressed(int maxHardItems)
	{
		softMap = new SoftHashMap<String, byte[]>(maxHardItems);
	}
	
	public void putBitmap(String key, Bitmap bitmap) 
	{		
		if(softMap.containsKey(key)) return;
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);		
		//System.out.println("Size: " + bitmap.getByteCount() + " : " + baos.toByteArray().length);
		byte[] data = baos.toByteArray();		
		softMap.put(key, data);
	}
	
	public Bitmap getBitmap(String key)
	{		
		byte[] data = softMap.get(key);
		
		if(data != null) {
			return BitmapFactory.decodeByteArray(data, 0, data.length);		
		}
		else {
			return null;
		}
	}

}
