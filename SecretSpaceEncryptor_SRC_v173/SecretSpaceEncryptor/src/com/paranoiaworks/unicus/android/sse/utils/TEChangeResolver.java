package com.paranoiaworks.unicus.android.sse.utils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Used for changes detection in unenc/enc text fields
 * (when one of the text fields (unenc/enc) is changed, so the data in the other field does not correspond to the actual value)
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */ 

public class TEChangeResolver {

	private byte[] lastProcessedHash = null;
	long lastProcessedSizeUnenc = -1;
	long lastProcessedSizeEnc = -1;
	
    public void setLastProcessed(String unenc, String enc)
    {
    	lastProcessedSizeUnenc = unenc.length();
    	lastProcessedSizeEnc = enc.length();
		try {
			lastProcessedHash = Encryptor.getSHA256Hash(unenc.getBytes("UTF8"));
			lastProcessedHash = Helpers.concat(lastProcessedHash, Encryptor.getSHA256Hash(enc.getBytes("UTF8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    public void resetLastProcessed()
    {
    	lastProcessedSizeUnenc = -1;
    	lastProcessedSizeEnc = -1;
		lastProcessedHash = null;
    }
    
    public boolean checkChange(String unenc, String enc)
    {
    	boolean change = false;
    	
    	int currentSizeUnenc = unenc.length();
    	int currentSizeEnc = enc.length();
    	
    	if(lastProcessedHash == null || currentSizeUnenc != lastProcessedSizeUnenc || currentSizeEnc != lastProcessedSizeEnc)
    	{
    		change = true;
    	}
    	else
    	{
    		byte[] currentHashDec = null;
    		byte[] currentHashEnc = null;
    		try {
				currentHashDec = Encryptor.getSHA256Hash(unenc.getBytes("UTF8"));	
				currentHashEnc = Encryptor.getSHA256Hash(enc.getBytes("UTF8"));
			} catch (UnsupportedEncodingException e) {
				return true;
			}	
    		
    		byte[] currentHash = Helpers.concat(currentHashDec, currentHashEnc);
    		
    		if(!Arrays.equals(currentHash, lastProcessedHash))
    			change = true;
    		else
    			change = false;
    	}
    	
    	return change;
    }
}
