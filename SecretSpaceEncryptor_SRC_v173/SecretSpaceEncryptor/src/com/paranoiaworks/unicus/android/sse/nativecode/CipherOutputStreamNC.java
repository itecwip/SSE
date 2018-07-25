package com.paranoiaworks.unicus.android.sse.nativecode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

import com.paranoiaworks.unicus.android.sse.misc.PWCipherOutputStream;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

/**
 * Cipher OuputStream - Native Code
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class CipherOutputStreamNC extends PWCipherOutputStream {	

	private EncryptorNC encryptorNC;
	
	byte[] lastIv = null;
	
	final int BUFFER_SIZE = 196608;
	private ByteBuffer writeBuffer = null;
	private int bufferedBytesCounter = 0;
	
	
	public CipherOutputStreamNC(final OutputStream out, byte[] iv, byte[] key, int algorithmCode) {
        super(out, iv, key, algorithmCode);
        this.encryptorNC = new EncryptorNC();
        writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    }
	
	@Override
	public void write(int b) throws IOException {
		byte [] singleByte = {(byte) b};
		write(singleByte, 0, 1);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
    @Override
	public void write(byte[] b, int off, int len) throws IOException {
    	if(len < 1) return;
    	
    	if(getBufferFreeSpace() <= len) // write to stream
    	{
    		writeBuffer.put(b, off, getBufferFreeSpace());
    		
    		if(lastIv == null) lastIv = iv;
    		byte[] writeBufferData = writeBuffer.array();
    		boolean ok = encryptorNC.encryptByteArray(lastIv, key, writeBufferData, algorithmCode);   		
    		if(!ok) throw new IOException("Unexpected Error COSNC");
    		lastIv = Helpers.getSubarray(writeBufferData, writeBufferData.length - iv.length, iv.length);
    		out.write(writeBufferData, 0, BUFFER_SIZE);
    		
    		writeBuffer.clear();
    		
    		if(len - getBufferFreeSpace() != 0) {
	    		writeBuffer.put(Helpers.getSubarray(b, off + getBufferFreeSpace(), len - getBufferFreeSpace()));
	    		bufferedBytesCounter = len - getBufferFreeSpace();
    		} else {
    			bufferedBytesCounter = 0;
    		}
    	}
    	else // put data to buffer
    	{
    		writeBuffer.put(b, off, len);
    		countBufferedBytes(len);
    	} 
    }
    
    public void doFinal() throws IOException, InvalidParameterException
    {  	
    	byte[] data = Helpers.getSubarray(writeBuffer.array(), 0, bufferedBytesCounter);
    	
    	if(lastIv == null) lastIv = iv;
    	data = encryptorNC.encryptByteArrayWithPadding(lastIv, key, data, algorithmCode);
    	if(data == null) throw new InvalidParameterException("Unexpected Error COSNC LB");
    	out.write(data, 0, data.length);
    	
		flush();
    }
    
    private void countBufferedBytes(long count) {
        if (count > 0) {
        	bufferedBytesCounter += count;
        }
    }
    
    protected int getBufferFreeSpace(){
    	return BUFFER_SIZE - bufferedBytesCounter;
    }
}
