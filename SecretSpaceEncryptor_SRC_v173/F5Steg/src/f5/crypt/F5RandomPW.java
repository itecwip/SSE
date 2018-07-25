package f5.crypt;

public class F5RandomPW {
	private DigestRandomGenerator drg = null;
	private byte[] buffer;
	private final int BUFFER_CAPACITY = 512;
	private int bufferCounter = BUFFER_CAPACITY;

	public F5RandomPW(byte[] key) {
		drg = new DigestRandomGenerator(new Blake2bDigest());
		drg.addSeedMaterial(key);
		buffer = new byte[BUFFER_CAPACITY];
	}

	public int getNextValue(int maxValue) {
		int retVal = getNextByte() | (getNextByte() << 8)
				| (getNextByte() << 16) | (getNextByte() << 24);
		retVal %= maxValue;
		if (retVal < 0)
			retVal += maxValue;
		return retVal;
	}

	public int getNextByte() {
		if (bufferCounter >= (BUFFER_CAPACITY - 1)) {
			drg.nextBytes(buffer);
			bufferCounter = 0;
		} else {
			++bufferCounter;
		}
		return buffer[bufferCounter];
	}
}
