package com.paranoiaworks.sse;


/**
 * scrypt KDF parameters (N, r, p) for Application/Format Version
 * 
 * @author Paranoia Works
 * @version 1.0.0
 */ 
public class ScryptParams {

	private ScryptParams(){}
	
	private int n = -1;
	private int r = -1;
	private int p = -1;
	
	public static final int APP_CODE_PASSWORD_VAULT = 1;
	public static final int APP_CODE_TEXT_ENCRYPTOR = 2;
	public static final int APP_CODE_FILE_ENCRYPTOR = 3;
	
	public int getN() {
		return n;
	}
	public int getR() {
		return r;
	}
	public int getP() {
		return p;
	}
	
	public static ScryptParams getDefaultParameters()
	{
		ScryptParams sp = new ScryptParams();
		sp.n = 2048;
		sp.r = 8;
		sp.p = 5;
		return sp;
	}
	
	public static ScryptParams createCustomParameters(int n, int r, int p)
	{
		ScryptParams sp = new ScryptParams();
		sp.n = n;
		sp.r = r;
		sp.p = p;
		return sp;
	}
	
	public static ScryptParams getParameters(int applicationCode, int formatVersion) throws IllegalArgumentException
	{
		ScryptParams sp = new ScryptParams();
		
		switch(applicationCode) 
        {    
	        case APP_CODE_PASSWORD_VAULT:
	    	{
	    		if(formatVersion == 2)
	    		{
	    			sp.n = 2048;
	    			sp.r = 8;
	    			sp.p = 5;
	    		}
	    		else throw new IllegalArgumentException("Incorrect Format Version PWV: " + formatVersion);
	    	}
	    	break; 
	    	
	        case APP_CODE_TEXT_ENCRYPTOR:
	    	{
	    		if(formatVersion == 2)
	    		{
	    			sp.n = 2048;
	    			sp.r = 8;
	    			sp.p = 5;
	    		}
	    		else throw new IllegalArgumentException("Incorrect Format Version TE: " + formatVersion);
	    	}
	    	break;
	    	
	        case APP_CODE_FILE_ENCRYPTOR:
	    	{
	    		if(formatVersion == 2)
	    		{
	    			sp.n = 2048;
	    			sp.r = 8;
	    			sp.p = 10;
	    		}
	    		else throw new IllegalArgumentException("Incorrect Format Version FE: " + formatVersion);
	    	}
	    	break; 
	    	
	        default: throw new IllegalArgumentException("Incorrect Application Code: " + applicationCode);
        }	
		
		return sp;
	}
}
