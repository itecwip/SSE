package com.paranoiaworks.unicus.android.sse.utils;

import sse.org.bouncycastle.crypto.BlockCipher;
import sse.org.bouncycastle.crypto.BufferedBlockCipher;
import sse.org.bouncycastle.crypto.CipherParameters;
import sse.org.bouncycastle.crypto.engines.AESFastEngine;
import sse.org.bouncycastle.crypto.engines.BlowfishEngine;
import sse.org.bouncycastle.crypto.engines.GOST28147Engine;
import sse.org.bouncycastle.crypto.engines.RC6Engine;
import sse.org.bouncycastle.crypto.engines.SerpentEngine;
import sse.org.bouncycastle.crypto.engines.Shacal2Engine;
import sse.org.bouncycastle.crypto.engines.ThreefishEngine;
import sse.org.bouncycastle.crypto.engines.TwofishEngine;
import sse.org.bouncycastle.crypto.modes.CBCBlockCipher;
import sse.org.bouncycastle.crypto.modes.EAXBlockCipher;
import sse.org.bouncycastle.crypto.modes.SICBlockCipher;
import sse.org.bouncycastle.crypto.paddings.BlockCipherPadding;
import sse.org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import sse.org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import sse.org.bouncycastle.crypto.params.AEADParameters;
import sse.org.bouncycastle.crypto.params.KeyParameter;
import sse.org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Cipher Provider with Bouncy Castle lightweight API
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.3
 */
public class CipherProvider {
	
	public static final int ALG_AES = 0;
	public static final int ALG_RC6 = 1;
	public static final int ALG_SERPENT = 2;
	public static final int ALG_BLOWFISH = 3;
	public static final int ALG_TWOFISH = 4;
	public static final int ALG_GOST28147 = 5;
	public static final int ALG_BLOWFISH448 = 6;
	public static final int ALG_THREEFISH = 7;
	public static final int ALG_SHACAL2 = 8;
	
	public static BufferedBlockCipher getBufferedBlockCipher(boolean forEncryption, byte[] iv, byte[] key, int algorithmCode) //CBC
	{
		return getBufferedBlockCipher(forEncryption, iv, key, algorithmCode, true);
	}
	
	public static BufferedBlockCipher getBufferedBlockCipher(boolean forEncryption, byte[] iv, byte[] key, int algorithmCode, boolean withPadding) //CBC
	{
		BufferedBlockCipher cipher = null;
  	    KeyParameter keyParam = new KeyParameter(key);
  	    CipherParameters params = new ParametersWithIV(keyParam, iv);
  	    BlockCipherPadding padding = new ISO10126d2Padding();	
  	    cipher = withPadding ? new PaddedBufferedBlockCipher(getBaseCBCCipher(algorithmCode), padding) : new BufferedBlockCipher(getBaseCBCCipher(algorithmCode));
		cipher.init(forEncryption, params);
		
		return cipher;
	}
	
	public static EAXBlockCipher getEAXCipher(boolean forEncryption, byte[] nonce, byte[] key, int algorithmCode)
	{
		EAXBlockCipher cipher = getBaseEAXCipher(algorithmCode);
		KeyParameter keyParam = new KeyParameter(key);
		int macSize = cipher.getBlockSize() * 8;
		if(macSize > 256) macSize = 256; // limit MAC size to 256
		CipherParameters params = new AEADParameters(keyParam, macSize, nonce);
		cipher.init(forEncryption, params);

		return cipher;
	}
	
	public static SICBlockCipher getCTRCipher(boolean forEncryption, byte[] nonce, byte[] key, int algorithmCode)
	{
		SICBlockCipher cipher = getBaseCTRCipher(algorithmCode);
		KeyParameter keyParam = new KeyParameter(key);
		CipherParameters params = new ParametersWithIV(keyParam, nonce);
		cipher.init(forEncryption, params);

		return cipher;
	}
	
	
	private static EAXBlockCipher getBaseEAXCipher(int algorithmCode)
	{
		EAXBlockCipher baseCipher = null;
		switch (algorithmCode)
        {        	
        	case 0: 
        	{
        		baseCipher = new EAXBlockCipher(new AESFastEngine());
            	break;
        	}
        	case 1: 
        	{
        		baseCipher = new EAXBlockCipher(new RC6Engine());
            	break;
        	}
        	case 2: 
        	{
        		baseCipher = new EAXBlockCipher(new SerpentEngine());
            	break;
        	}
        	case 3: 
        	{
        		baseCipher = new EAXBlockCipher(new BlowfishEngine());
            	break;
        	}
        	case 4: 
        	{
        		baseCipher = new EAXBlockCipher(new TwofishEngine());
            	break;
        	}
        	case 5: 
        	{
        		baseCipher = new EAXBlockCipher(new GOST28147Engine());
            	break;
        	}
        	case 6:
        	{
        		baseCipher = new EAXBlockCipher(new BlowfishEngine());
            	break;
        	}
        	case 7:
        	{
        		baseCipher = new EAXBlockCipher(new ThreefishEngine(ThreefishEngine.BLOCKSIZE_1024));
            	break;
        	}
        	case 8:
        	{
        		baseCipher = new EAXBlockCipher(new Shacal2Engine());
            	break;
        	}
            
        	default: 
            	break;
        }
		
		return baseCipher;
	}
	
	private static SICBlockCipher getBaseCTRCipher(int algorithmCode)
	{
		SICBlockCipher baseCipher = null;
		switch (algorithmCode)
        {        	
        	case 0: 
        	{
        		baseCipher = new SICBlockCipher(new AESFastEngine());
            	break;
        	}
        	case 1: 
        	{
        		baseCipher = new SICBlockCipher(new RC6Engine());
            	break;
        	}
        	case 2: 
        	{
        		baseCipher = new SICBlockCipher(new SerpentEngine());
            	break;
        	}
        	case 3: 
        	{
        		baseCipher = new SICBlockCipher(new BlowfishEngine());
            	break;
        	}
        	case 4: 
        	{
        		baseCipher = new SICBlockCipher(new TwofishEngine());
            	break;
        	}
        	case 5: 
        	{
        		baseCipher = new SICBlockCipher(new GOST28147Engine());
            	break;
        	}
        	case 6:
        	{
        		baseCipher = new SICBlockCipher(new BlowfishEngine());
            	break;
        	}
        	case 7:
        	{
        		baseCipher = new SICBlockCipher(new ThreefishEngine(ThreefishEngine.BLOCKSIZE_1024));
            	break;
        	}
        	case 8:
        	{
        		baseCipher = new SICBlockCipher(new Shacal2Engine());
            	break;
        	}
            
        	default: 
            	break;
        }
		
		return baseCipher;
	}
	
	private static BlockCipher getBaseCBCCipher(int algorithmCode)
	{
		BlockCipher baseCipher = null;
		switch (algorithmCode)
        {        	
        	case 0: 
        	{
        		baseCipher = new CBCBlockCipher(new AESFastEngine());
            	break;
        	}
        	case 1: 
        	{
        		baseCipher = new CBCBlockCipher(new RC6Engine());
            	break;
        	}
        	case 2: 
        	{
        		baseCipher = new CBCBlockCipher(new SerpentEngine());
            	break;
        	}
        	case 3: 
        	{
        		baseCipher = new CBCBlockCipher(new BlowfishEngine());
            	break;
        	}
        	case 4: 
        	{
        		baseCipher = new CBCBlockCipher(new TwofishEngine());
            	break;
        	}
        	case 5: 
        	{
        		baseCipher = new CBCBlockCipher(new GOST28147Engine());
            	break;
        	}
        	case 6:
        	{
        		baseCipher = new CBCBlockCipher(new BlowfishEngine());
            	break;
        	}
        	case 7:
        	{
        		baseCipher = new CBCBlockCipher(new ThreefishEngine(ThreefishEngine.BLOCKSIZE_1024));
            	break;
        	}
        	case 8:
        	{
        		baseCipher = new CBCBlockCipher(new Shacal2Engine());
            	break;
        	}
            
        	default: 
            	break;
        }
		
		return baseCipher;
	}
}
