package com.paranoiaworks.sse;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper - Obtain CheckCode + remove padding (decryption)
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.2
 */
public class CheckCodeParserInputStream extends FilterInputStream {

	private long bytesRead;
	private long dataSize;
	private boolean removePadding = false;
	private boolean buffered = false;
	private ByteArrayInputStream restOfFile = null;
	private byte[] checkCode;
	private int lastBSize;

    public CheckCodeParserInputStream(final InputStream in, long dataSize) {
        super(in);
        this.dataSize = dataSize;
    }
    
    public CheckCodeParserInputStream(final InputStream in, long dataSize, boolean removePadding) {
        super(in);
        this.dataSize = dataSize;
        this.removePadding = removePadding;
    }
	
	private CheckCodeParserInputStream(final InputStream in) {
        super(in);
    }
	
    public byte[] getCheckCode() throws IOException {

    	if(checkCode == null)
    	{ 		
    		byte[] bb = new byte[lastBSize];   		
    		while(this.read(bb) > 0);
    	}
    	
    	return checkCode;
    }

    @Override
    public int read() throws IOException {
        int r = in.read();
        if (r >= 0) {
            count(1);
        }
        return r;
    }
    
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
    	if(buffered) // read end of file from buffer
    	{
    		int r = restOfFile.read(b, off, len);
    		return r;
    	}
    	lastBSize = b.length;
    	
    	int minSize = 3 * lastBSize;
    	if(minSize < 768) minSize = 768;
    	
        if(dataSize - bytesRead < minSize)
        {
        	byte[] dataBuffer = new byte[3 * minSize];
        	int r = in.read(dataBuffer, 0, dataBuffer.length);
        	int paddingSize = removePadding ? (int)dataBuffer[r - 1] : 0; //for remove padding        	
        	if(paddingSize == -128) paddingSize = 128;       	
        	checkCode = Helpers.getSubarray(dataBuffer, r - 32 - paddingSize, 32);
        	dataBuffer = Helpers.getSubarray(dataBuffer, 0, r - 32 - paddingSize);

        	restOfFile = new ByteArrayInputStream(dataBuffer);
        	r = restOfFile.read(b, off, len);
        	buffered = true;
        	return r;
        }
        
    	int r = in.read(b, off, len);
        if (r >= 0) {
            count(r);
        }
        return r;
    }

    protected final void count(long read) {
        if (read != -1) {
            bytesRead += read;
        }
    }
}
