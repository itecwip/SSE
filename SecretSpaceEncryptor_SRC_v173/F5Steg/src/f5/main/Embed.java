package f5.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

import f5.helpers.RawImage;
import f5.james.JpegEncoder;

public class Embed {

	public static void go(byte[] key, byte[] data, RawImage rawImage, String outFilePath, int quality) throws Throwable 
	{
		FileOutputStream dataOut = null;
		JpegEncoder jpg;
		String comment = "";

		File outFile = new File(outFilePath);

		try {			
			dataOut = new FileOutputStream(outFile);

			jpg = new JpegEncoder(rawImage, quality, dataOut, comment);

			if (data == null)
				jpg.Compress();
			else
				jpg.Compress(new ByteArrayInputStream(data), key);

		} catch (Throwable t) {
			throw t;
		} finally {
			dataOut.close();
		}
	}
}
