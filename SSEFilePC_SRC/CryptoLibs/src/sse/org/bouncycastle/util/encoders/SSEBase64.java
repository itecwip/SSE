package sse.org.bouncycastle.util.encoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SSEBase64
{
    private static final Encoder encoder = new SSEBase64Encoder();
    
    public static byte[] encode(
        byte[]    data)
    {
        ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        
        try
        {
            encoder.encode(data, 0, data.length, bOut);
        }
        catch (Exception e)
        {
            throw new EncoderException("exception encoding base64 data: " + e.getMessage(), e);
        }
        
        return bOut.toByteArray();
    }

    public static int encode(
        byte[]                data,
        OutputStream    out)
        throws IOException
    {
        return encoder.encode(data, 0, data.length, out);
    }
    
    public static byte[] decode(
        byte[]    data)
    {
    	if(data.length % 4 != 0) {
        	String paddingS = "";
        	for(int i = 0; i < 4 - (data.length % 4); ++i) paddingS += (char)SSEBase64Encoder.padding;
        			
        	try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
				outputStream.write(data);
				outputStream.write(paddingS.getBytes());
				data = outputStream.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    	
    	ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        
        try
        {
            encoder.decode(data, 0, data.length, bOut);
        }
        catch (Exception e)
        {
            throw new DecoderException("exception decoding base64 string: " + e.getMessage(), e);
        }
        
        return bOut.toByteArray();
    }
    
    public static int decode(
        byte[]                data,
        OutputStream    out)
        throws IOException
    {
        return encoder.decode(data, 0, data.length, out);
    }
    

    public static byte[] decode(
        String    data)
    {
        ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        
        try
        {
            encoder.decode(data, bOut);
        }
        catch (Exception e)
        {
            throw new DecoderException("exception decoding base64 string: " + e.getMessage(), e);
        }
        
        return bOut.toByteArray();
    }
    
    public static int decode(
        String                data,
        OutputStream    out)
        throws IOException
    {
        return encoder.decode(data, out);
    }
}
