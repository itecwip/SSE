package com.paranoiaworks.unicus.android.sse.misc;

import java.io.IOException;
import java.io.InputStream;

import sse.org.bouncycastle.crypto.BufferedBlockCipher;

import com.paranoiaworks.unicus.android.sse.utils.CipherProvider;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

/**
 * Cipher InputStream - Platform Independent
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class CipherInputStreamPI extends PWCipherInputStream {
	
	byte[] lastIv = null;
	
	public CipherInputStreamPI(final InputStream in, byte[] iv, byte[] key, int algorithmCode) {
        super(in, iv, key, algorithmCode);
    }
	
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException 
    {
    	if(b.length % iv.length != 0) throw new IOException("Bad data size CISPI");
    	
    	BufferedBlockCipher cipher = CipherProvider.getBufferedBlockCipher(false, lastIv == null ? iv : lastIv, key, algorithmCode, false);
    	
    	byte[] inputBuffer = new byte[b.length];
    	int r = in.read(inputBuffer, off, len);
    	lastIv = Helpers.getSubarray(inputBuffer, inputBuffer.length - iv.length, iv.length);

    	int bytesProcessed = cipher.processBytes(inputBuffer, 0, inputBuffer.length, b, 0);
    	try {
			cipher.doFinal(b, bytesProcessed);
		} catch (Exception e) {
			throw new IOException("DoFinal Failed CISPI");
		} 

        return r;
    }

}
