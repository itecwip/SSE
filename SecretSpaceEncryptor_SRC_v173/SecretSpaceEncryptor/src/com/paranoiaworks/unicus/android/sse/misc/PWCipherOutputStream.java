package com.paranoiaworks.unicus.android.sse.misc;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Cipher OutputStream - Base Class
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public abstract class PWCipherOutputStream extends FilterOutputStream {
	
	protected byte[] iv;
	protected byte[] key;
	protected int algorithmCode;
	
	public PWCipherOutputStream(final OutputStream out, byte[] iv, byte[] key, int algorithmCode)
	{
		super(out);
        this.iv = iv;
        this.key = key;
        this.algorithmCode = algorithmCode;
	}
	
	abstract public void doFinal() throws IOException;
}
