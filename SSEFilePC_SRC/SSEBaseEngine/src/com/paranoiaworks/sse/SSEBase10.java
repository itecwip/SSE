package com.paranoiaworks.sse;

import java.math.BigInteger;

/**
 * SSEBase10 Encoder/Decoder
 * 
 * @author Paranoia Works
 * @version 1.0.0
 */

public class SSEBase10 {

	private static final byte[] LEADING_BYTE = new byte[] {1};
	
	static public String encode(final byte[] bytes) {
		return new BigInteger(Helpers.concat(LEADING_BYTE, bytes)).toString();
	}
	
	static public byte[] decode(final String base10) {
		byte[] bytes = new BigInteger(base10).toByteArray();
		return Helpers.getSubarray(bytes, 1, bytes.length - 1);
	}
}
