package com.paranoiaworks.unicus.android.sse.misc;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Cipher InputStream - Base Class
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public abstract class PWCipherInputStream extends FilterInputStream {
	
	protected byte[] iv;
	protected byte[] key;
	protected int algorithmCode;
	
	public PWCipherInputStream(final InputStream in, byte[] iv, byte[] key, int algorithmCode)
	{
		super(in);
        this.iv = iv;
        this.key = key;
        this.algorithmCode = algorithmCode;
	}
	
	@Override
    public int read() throws IOException {
    	boolean noUse = true;
    	if(noUse) throw new IOException("Bad data size PWCIS");
        return 0;
    }
}
