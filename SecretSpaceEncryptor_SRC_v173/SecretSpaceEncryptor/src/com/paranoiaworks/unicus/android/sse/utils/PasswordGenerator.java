package com.paranoiaworks.unicus.android.sse.utils;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.paranoiaworks.unicus.android.sse.misc.ExtendedEntropyProvider;

import sse.org.bouncycastle.crypto.digests.SkeinDigest;
import sse.org.bouncycastle.crypto.prng.ThreadedSeedGenerator;

/**
 * Password Generator
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.3
 */
public class PasswordGenerator {

	private String charSet = "";
	private int[] substituteCache = new int[3];
	private int substituteCounter;
	private byte[] externalEntropy;
	private boolean customCharSet = false;

	private String lowerAlphaChars;
	private String upperAlphaChars;
	private String numberChars;
	private String specCharChars;
	private List<String> charSetList = new ArrayList<String>();
	
	
	public PasswordGenerator(boolean lowerAlpha, boolean upperAlpha, boolean number, boolean specChar, boolean removeMisspelling)
	{	
		if(removeMisspelling)
		{
			// 0O'`1l|I
			this.lowerAlphaChars = "abcdefghijkmnopqrstuvwxyz";
			this.upperAlphaChars = "ABCDEFGHJKLMNPQRSTUVWXYZ";
			this.numberChars = "23456789";
			this.specCharChars = "!\"#$%&()*+,-./:;<=>?@[\\]^_{}~";
		}
		else
		{
			this.lowerAlphaChars = "abcdefghijklmnopqrstuvwxyz";
			this.upperAlphaChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			this.numberChars = "0123456789";
			this.specCharChars = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
		}
		
		if(lowerAlpha) {
			charSet += lowerAlphaChars;
			charSetList.add(lowerAlphaChars);
		}
		if(upperAlpha) {
			charSet += upperAlphaChars;
			charSetList.add(upperAlphaChars);
		}
		if(number) {
			charSet += numberChars;
			charSetList.add(numberChars);
		}
		if(specChar) {
			charSet += specCharChars;
			charSetList.add(specCharChars);
		}
	}
	
	public PasswordGenerator(String customCharset)
	{	
		charSet = customCharset;
		if(charSet.trim().length() < 1) charSet = "?";
		customCharSet = true;
	}
	
	public byte[] getExternalEntropy() {
		return externalEntropy;
	}

	public void setExternalEntropy(byte[] externalEntropy) {
		this.externalEntropy = externalEntropy;
	}
	
	public String getNewPassword(int length)
	{	
		if(length < 4) length = 4;
		if(length > 128) length = 128;
		substituteCounter = 0;
		StringBuffer password = new StringBuffer(length);
		byte[] randomBytesBuffer = getRandomBA(length);		
		
		for(int i = 0; i < length; ++i)
		{
			password.append(Helpers.getCharFromChosenCharset(charSet, randomBytesBuffer[i]));
		}		
		
		String output = null;
		
		if(customCharSet ) output = password.toString();
		else output = balancePassword(password.toString());
		
		return output;
	}
	
	/** Get Random bytes */
	private byte[] getRandomBA(int length)
	{	
		int seedLength = length < 64 ? 64 : length;
		seedLength *= 2;
		ThreadedSeedGenerator tsg = new ThreadedSeedGenerator();
		byte[] tsgOutput = tsg.generateSeed(seedLength, true);		
		byte[] systemOutput = ExtendedEntropyProvider.getSystemStateDataDigested();
		
		byte[] seed = Helpers.concat(tsgOutput, systemOutput);
		byte[] output = null;
		
		try {
			SecureRandom rand = new SecureRandom();
			rand.setSeed(seed);
			byte[]randomNum = new byte[seedLength];
			rand.nextBytes(randomNum);		
			
			int skeinLength = (length + 3) * 8;
			output = getSkeinHash(randomNum, skeinLength);
			
			if(externalEntropy != null)
				output = Helpers.xorit(output, getSkeinHash(externalEntropy, skeinLength));
			
			substituteCache[0] = (output[length] + 128);
			substituteCache[1] = (output[length + 1] + 128);
			substituteCache[2] = (output[length + 2] + 128);

		} catch (Exception e1) {
			e1.printStackTrace();
		}  

		return Helpers.getSubarray(output, 0, length);
	}
	
	private String balancePassword(String password)
	{
		int zeroCounter = 4;
		TreeMap<Integer, Integer> sortMap = new TreeMap<Integer, Integer>();
		
		while(zeroCounter > 0)
		{	
			if(sortMap.size() > 0)
			{
				String max = charSetList.get(sortMap.get(sortMap.lastKey()));
				String min = charSetList.get(sortMap.get(sortMap.firstKey()));
				String replacement = Character.toString(min.charAt(substituteCache[substituteCounter] % min.length()));
				if(replacement.equals("$") || replacement.equals("\\")) replacement = Matcher.quoteReplacement(replacement);
				password = password.replaceFirst("[" + Pattern.quote(max) + "]", replacement);
				++substituteCounter;
			}
			
			sortMap.clear();
			zeroCounter = 0;
			for(int i = 0; i < charSetList.size(); ++i)
			{
				int count = Helpers.regexGetCountOf(password, "[" + Pattern.quote(charSetList.get(i)) + "]");
				if(count == 0) ++zeroCounter;
				sortMap.put(count, i);		
			}			
		}
		return password;
	}
	
	/** Get Skein of Byte Array */
    public static byte[] getSkeinHash(byte[] data, int outputSizeBits)
    {
    	byte[] hash = new byte[outputSizeBits / 8];
    	SkeinDigest digester = new SkeinDigest(SkeinDigest.SKEIN_1024, outputSizeBits);
    	digester.update(data, 0, data.length);
    	digester.doFinal(hash, 0);
    	return hash;
    }
}
