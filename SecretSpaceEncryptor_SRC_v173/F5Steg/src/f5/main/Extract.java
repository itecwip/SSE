package f5.main;

import java.io.*;

import f5.crypt.*;
import f5.helpers.CoeffWrapper;
import f5.ortega.*;

public class Extract {
	private static byte[] carrier; // carrier data
	private static CoeffWrapper coeff; // dct values
	private static ByteArrayOutputStream baos; // output stream


	private static byte[] deZigZag = { 0, 1, 5, 6, 14, 15, 27, 28, 2, 4, 7, 13,
			16, 26, 29, 42, 3, 8, 12, 17, 25, 30, 41, 43, 9, 11, 18, 24, 31,
			40, 44, 53, 10, 19, 23, 32, 39, 45, 52, 54, 20, 22, 33, 38, 46, 51,
			55, 60, 21, 34, 37, 47, 50, 56, 59, 61, 35, 36, 48, 49, 57, 58, 62,
			63 };

	public static byte[] go(byte[] key, String sourceFilePath) throws Exception {
		FileInputStream fis = null;
		File f = new File(sourceFilePath);
		long fileSize = f.length();
		fis = new FileInputStream(f);
		
		return go(key, fis, fileSize);
	}
	
	public static byte[] go(byte[] key, InputStream is, long fileSize) throws Exception {
		InputStream fis = null;
		try {
			carrier = new byte[(int) fileSize];
			fis = is;
			baos = new ByteArrayOutputStream();
			fis.read(carrier);
			HuffmanDecode hd = new HuffmanDecode(carrier);
			//System.out.println("Huffman decoding starts");
			coeff = hd.decode();
			//System.out.println("Permutation starts");
			F5RandomPW random = new F5RandomPW(key);
			Permutation permutation = new Permutation(coeff.getSize(), random);
			//System.out.println(coeff.getSize() + " indices shuffled");
			int extractedByte = 0;
			int availableExtractedBits = 0;
			int extractedFileLength = 0;
			int nBytesExtracted = 0;
			int shuffledIndex = 0;
			int extractedBit;
			int i;
			//System.out.println("Extraction starts");
			// extract length information
			for (i = 0; availableExtractedBits < 32; i++) {
				shuffledIndex = permutation.getShuffled(i);
				if (shuffledIndex % 64 == 0)
					continue; // skip DC coefficients
				shuffledIndex = shuffledIndex - (shuffledIndex % 64)
						+ deZigZag[shuffledIndex % 64];
				if (coeff.getBit(shuffledIndex) < 0)
					continue; // skip zeroes
				else
					extractedBit = coeff.getBit(shuffledIndex);
				extractedFileLength |= extractedBit << availableExtractedBits++;
			}
			// remove pseudo random pad
			extractedFileLength ^= random.getNextByte();
			extractedFileLength ^= random.getNextByte() << 8;
			extractedFileLength ^= random.getNextByte() << 16;
			extractedFileLength ^= random.getNextByte() << 24;
			int k = extractedFileLength >> 24;
			k %= 32;
			int n = (1 << k) - 1;
			extractedFileLength &= 0x007fffff;
			//System.out.println("Length of embedded file: " + extractedFileLength + " bytes");
			availableExtractedBits = 0;
			if (n > 0) {
				int startOfN = i;
				int hash;
				//System.out.println("(1, " + n + ", " + k + ") code used");
				extractingLoop: do {
					// 1. read n places, and calculate k bits
					hash = 0;
					int code = 1;
					for (i = 0; code <= n; i++) {
						// check for pending end of coeff
						if (startOfN + i >= coeff.getSize())
							break extractingLoop;
						shuffledIndex = permutation.getShuffled(startOfN + i);
						if (shuffledIndex % 64 == 0)
							continue; // skip DC coefficients
						shuffledIndex = shuffledIndex - (shuffledIndex % 64)
								+ deZigZag[shuffledIndex % 64];
						if (coeff.getBit(shuffledIndex) < 0)
							continue; // skip zeroes
						else
							extractedBit = coeff.getBit(shuffledIndex);
						if (extractedBit == 1) {
							hash ^= code;
						}
						code++;
					}
					startOfN += i;
					// 2. write k bits bytewise
					for (i = 0; i < k; i++) {
						extractedByte |= ((hash >> i) & 1) << availableExtractedBits++;
						if (availableExtractedBits == 8) {
							// remove pseudo random pad
							extractedByte ^= random.getNextByte();
							baos.write((byte) extractedByte);
							extractedByte = 0;
							availableExtractedBits = 0;
							nBytesExtracted++;
							// check for pending end of embedded data
							if (nBytesExtracted == extractedFileLength)
								break extractingLoop;
						}
					}
				} while (true);
			} else {
				//System.out.println("Default code used");
				for (; i < coeff.getSize(); i++) {
					shuffledIndex = permutation.getShuffled(i);
					if (shuffledIndex % 64 == 0)
						continue; // skip DC coefficients
					shuffledIndex = shuffledIndex - (shuffledIndex % 64)
							+ deZigZag[shuffledIndex % 64];
					if (coeff.getBit(shuffledIndex) < 0)
						continue; // skip zeroes
					else
						extractedBit = coeff.getBit(shuffledIndex);
					extractedByte |= extractedBit << availableExtractedBits++;
					if (availableExtractedBits == 8) {
						// remove pseudo random pad
						extractedByte ^= random.getNextByte();
						baos.write((byte) extractedByte);
						extractedByte = 0;
						availableExtractedBits = 0;
						nBytesExtracted++;
						if (nBytesExtracted == extractedFileLength)
							break;
					}
				}
			}
			if (nBytesExtracted < extractedFileLength) {
				throw new IllegalStateException("1000::Incomplete file: only " + nBytesExtracted + " of " + extractedFileLength + " bytes extracted");
				//System.out.println("Incomplete file: only " + nBytesExtracted + " of " + extractedFileLength + " bytes extracted");
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				baos.flush();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return baos.toByteArray();
	}
}
