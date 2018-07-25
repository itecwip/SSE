package ext;

public class Base64
{
    private static final byte STANDARD_ALPHABET[] = {
        65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 
        75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 
        85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 
        101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 
        111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 
        121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 
        56, 57, 43, 47
    };
    private static final byte STANDARD_DECODABET[] = {
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, 
        -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, 
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 
        -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, 
        -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 
        54, 55, 56, 57, 58, 59, 60, 61, -9, -9, 
        -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 
        25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 
        29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 
        49, 50, 51, -9, -9, -9, -9
    };
    private static final byte URL_SAFE_ALPHABET[] = {
        65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 
        75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 
        85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 
        101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 
        111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 
        121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 
        56, 57, 45, 95
    };
    private static final byte URL_SAFE_DECODABET[] = {
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, 
        -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, 
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 
        -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, 
        -9, -9, -9, -9, -9, 62, -1, -9, 52, 53, 
        54, 55, 56, 57, 58, 59, 60, 61, -9, -9, 
        -9, -9, -9, -9, -9, 0, 1, 2, 3, 4, 
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 
        25, -9, -9, -9, -9, 63, -9, 26, 27, 28, 
        29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 
        49, 50, 51, -9, -9, -9, -9
    };
    private boolean urlSafe;
    private byte ALPHABET[];
    private byte DECODABET[];
    private byte PADDING_CHAR;

    public Base64()
    {
        this(false);
    }

    public Base64(boolean urlSafe)
    {
        this.urlSafe = urlSafe;
        if(urlSafe)
        {
            ALPHABET = URL_SAFE_ALPHABET;
            DECODABET = URL_SAFE_DECODABET;
            PADDING_CHAR = 46;
        } else
        {
            ALPHABET = STANDARD_ALPHABET;
            DECODABET = STANDARD_DECODABET;
            PADDING_CHAR = 61;
        }
    }

    public boolean isUrlSafe()
    {
        return urlSafe;
    }

    private byte[] encode3to4(byte source[], int srcOffset, int numSigBytes, byte destination[], int destOffset)
    {
        int inBuff = (numSigBytes <= 0 ? 0 : (source[srcOffset] << 24) >>> 8) | (numSigBytes <= 1 ? 0 : (source[srcOffset + 1] << 24) >>> 16) | (numSigBytes <= 2 ? 0 : (source[srcOffset + 2] << 24) >>> 24);
        switch(numSigBytes)
        {
        case 3: // '\003'
            destination[destOffset] = ALPHABET[inBuff >>> 18];
            destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3f];
            destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 0x3f];
            destination[destOffset + 3] = ALPHABET[inBuff & 0x3f];
            return destination;

        case 2: // '\002'
            destination[destOffset] = ALPHABET[inBuff >>> 18];
            destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3f];
            destination[destOffset + 2] = ALPHABET[inBuff >>> 6 & 0x3f];
            destination[destOffset + 3] = PADDING_CHAR;
            return destination;

        case 1: // '\001'
            destination[destOffset] = ALPHABET[inBuff >>> 18];
            destination[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3f];
            destination[destOffset + 2] = PADDING_CHAR;
            destination[destOffset + 3] = PADDING_CHAR;
            return destination;
        }
        return destination;
    }

    public final byte[] encode(byte source[], int off, int len)
    {
        int len43 = (len * 4) / 3;
        byte outBuff[] = new byte[len43 + (len % 3 <= 0 ? 0 : 4)];
        int d = 0;
        int e = 0;
        int len2 = len - 2;
        int lineLength = 0;
        while(d < len2) 
        {
            encode3to4(source, d + off, 3, outBuff, e);
            lineLength += 4;
            d += 3;
            e += 4;
        }
        if(d < len)
        {
            encode3to4(source, d + off, len - d, outBuff, e);
            e += 4;
        }
        byte out[] = new byte[e];
        System.arraycopy(outBuff, 0, out, 0, e);
        return out;
    }

    public final byte[] encode(byte source[])
    {
        return encode(source, 0, source.length);
    }

    public final String encodeToString(byte source[])
    {
        return new String(encode(source));
    }

    private final int decode4to3(byte source[], int srcOffset, byte destination[], int destOffset)
    {
        if(source[srcOffset + 2] == PADDING_CHAR)
        {
            int outBuff = (DECODABET[source[srcOffset]] & 0xff) << 18 | (DECODABET[source[srcOffset + 1]] & 0xff) << 12;
            destination[destOffset] = (byte)(outBuff >>> 16);
            return 1;
        }
        if(source[srcOffset + 3] == PADDING_CHAR)
        {
            int outBuff = (DECODABET[source[srcOffset]] & 0xff) << 18 | (DECODABET[source[srcOffset + 1]] & 0xff) << 12 | (DECODABET[source[srcOffset + 2]] & 0xff) << 6;
            destination[destOffset] = (byte)(outBuff >>> 16);
            destination[destOffset + 1] = (byte)(outBuff >>> 8);
            return 2;
        } else
        {
            int outBuff = (DECODABET[source[srcOffset]] & 0xff) << 18 | (DECODABET[source[srcOffset + 1]] & 0xff) << 12 | (DECODABET[source[srcOffset + 2]] & 0xff) << 6 | DECODABET[source[srcOffset + 3]] & 0xff;
            destination[destOffset] = (byte)(outBuff >> 16);
            destination[destOffset + 1] = (byte)(outBuff >> 8);
            destination[destOffset + 2] = (byte)outBuff;
            return 3;
        }
    }

    public final byte[] decode(byte source[], int off, int len)
    {
        int len34 = (len * 3) / 4;
        byte outBuff[] = new byte[len34];
        int outBuffPosn = 0;
        byte b4[] = new byte[4];
        int b4Posn = 0;
        int i = 0;
        byte sbiCrop = 0;
        byte sbiDecode = 0;
        for(i = off; i < off + len; i++)
        {
            sbiCrop = (byte)(source[i] & 0x7f);
            sbiDecode = DECODABET[sbiCrop];
            if(sbiDecode < -5 || sbiDecode < -1)
            {
                continue;
            }
            b4[b4Posn++] = sbiCrop;
            if(b4Posn <= 3)
            {
                continue;
            }
            outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
            b4Posn = 0;
            if(sbiCrop == PADDING_CHAR)
            {
                break;
            }
        }

        byte out[] = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    }

    public final byte[] decode(byte source[])
    {
        return decode(source, 0, source.length);
    }

    public final byte[] decodeFromString(String s)
    {
        return decode(s.getBytes());
    }

}
