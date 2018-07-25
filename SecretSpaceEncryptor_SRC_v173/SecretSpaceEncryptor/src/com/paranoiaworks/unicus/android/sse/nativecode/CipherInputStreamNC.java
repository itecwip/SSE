package com.paranoiaworks.unicus.android.sse.nativecode;

import java.io.IOException;
import java.io.InputStream;

import com.paranoiaworks.unicus.android.sse.misc.PWCipherInputStream;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

/**
 * Cipher InputStream - Native Code
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class CipherInputStreamNC extends PWCipherInputStream {

	private EncryptorNC encryptorNC;
	byte[] lastIv = null;
	
	public CipherInputStreamNC(final InputStream in, byte[] iv, byte[] key, int algorithmCode) {
        super(in, iv, key, algorithmCode);
        this.encryptorNC = new EncryptorNC();
    }
	
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
    	if(b.length % iv.length != 0) throw new IOException("Bad data size CISNC");
    	
    	boolean ok = false;
    	int r = in.read(b, off, len);
    	byte[] lastIvTemp = Helpers.getSubarray(b, b.length - iv.length, iv.length);
    	
    	if(lastIv == null) ok = encryptorNC.decryptByteArray(iv, key, b, algorithmCode);
		else ok = encryptorNC.decryptByteArray(lastIv, key, b, algorithmCode);
    	
    	if(!ok) throw new IOException("Unexpected Error CISNC");
		
		lastIv = lastIvTemp;

        return r;
    }
}
