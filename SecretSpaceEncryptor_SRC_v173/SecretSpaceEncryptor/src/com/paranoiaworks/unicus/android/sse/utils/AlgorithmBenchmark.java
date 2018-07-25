package com.paranoiaworks.unicus.android.sse.utils;

import java.security.GeneralSecurityException;
import java.util.Random;

import android.os.Handler;
import android.os.Message;

import com.paranoiaworks.unicus.android.sse.nativecode.EncryptorNC;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor.AlgorithmBean;

/**
 * Simple Benchmark for comparing algorithms speed
 * note: Tests A - more Garbage Collector interventions
 * 
 * @author Paranoia Works
 * @version 1.0.4
 */
public class AlgorithmBenchmark {
	
	public final static int BENCHMARK_SHOW_DIALOG = -10001;
	public final static int BENCHMARK_COMPLETED = -10002;
	public static final int BENCHMARK_APPEND_TEXT = -10003;
	public static final int BENCHMARK_APPEND_TEXT_RESOURCE = -10004;
	public static final int BENCHMARK_FLUSH_BUFFER = -10005;
	
	public static final int NATIVE_CODE_OFFSET = 100;
	
	//public final static String LOG_FILENAME = "benchmark.log";
	
	private final int FIRST_TEST_ITERATIONS = 20;
	private final int SECOND_TEST_ITERATIONS = 2;
	
	private final int FIRST_TEST_FILE_SIZE = 15360;
	private final int SECOND_TEST_FILE_SIZE = 262144;
	
	private AlgorithmBean ab;
	private Encryptor encryptor;
	private EncryptorNC encryptorNC;
	private Handler handler;
	private int multiplicator = 1;
	private boolean nativeCode = false;
	private byte[] iv;
	private byte[] key;
	
	private byte[] resultEnc = null;
	private byte[] resultDec = null;
	
	private long overAllScore = 0;
	
	private long firstTestEnc = -1;
	private long firstTestDec = -1;
	private long secondTestEnc = -1;
	private long secondTestDec = -1;
	
	private double firstTestEncRes;
	private double firstTestDecRes;
	private double secondTestEncRes;
	private double secondTestDecRes;
	
	public AlgorithmBenchmark(int algorithmCode)
	{
		this(algorithmCode, null);
	}
	
	public AlgorithmBenchmark(int algorithmCode, Handler handler)
	{
		try {
			if(algorithmCode >= NATIVE_CODE_OFFSET) { // native code
				this.encryptor = new Encryptor("AlgorithmBenchmarkPassword", algorithmCode - NATIVE_CODE_OFFSET);
				this.ab = encryptor.getAvailableAlgorithms().get(algorithmCode - NATIVE_CODE_OFFSET);
				this.encryptorNC = new EncryptorNC();
				this.nativeCode = true;
				this.iv = getPseudoRandomBytes(ab.getBlockSize() / 8);
				this.key = getPseudoRandomBytes(ab.getKeySize() / 8);
			}
			else {
				this.encryptor = new Encryptor("AlgorithmBenchmarkPassword", algorithmCode);
			}
			
			this.handler = handler;
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}
	
	public void startBenchmark()
	{
		try {
			go();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void go() throws InterruptedException
	{
		HowLong.start_reset();
		if(handler != null)
		{
			handler.sendMessage(Message.obtain(handler, BENCHMARK_SHOW_DIALOG));
			appentText("<u><b>");
			appentText("ou_benchmark_benchmark" , true);
			appentText(": " + encryptor.getEncryptAlgorithmComment());
			if(nativeCode) {
				appentText(" ");
				appentText("ou_benchmark_nc", true);
			}
			appentText("</b></u><br/>...<br/>");
		}
		
		appentText("ou_benchmark_init_text", true);appentText(": "); flushBuffer();
		byte[] initBytesFirstTest = getPseudoRandomBytes(FIRST_TEST_FILE_SIZE);
		byte[] initBytesSecondTest = getPseudoRandomBytes(SECOND_TEST_FILE_SIZE);
		
		Thread.sleep(300);
		while(true)
        {
    		HowLong.start_reset();
    		resultEnc = initBytesSecondTest;
    		for(int i = 0; i < SECOND_TEST_ITERATIONS * multiplicator; ++i)
    		{
    			resultEnc = encryptor.encrypt(resultEnc, false);
    		}
    		if(HowLong.getDuration() > 3000) break;
    		multiplicator *= 2;
        }
		multiplicator *= (nativeCode ? 2 : 1); // native code more cycles for better precision
        appentText("<b>OK</b><br/>"); flushBuffer();
		
        //Start A ENC
        appentText("ou_benchmark_encryption", true); appentText(" A: "); flushBuffer();
        Thread.sleep(500);
		HowLong.start_reset();
		resultEnc = initBytesFirstTest;
		for(int i = 0; i < FIRST_TEST_ITERATIONS * multiplicator; ++i)
		{
			if(nativeCode)
				resultEnc = encryptorNC.encryptByteArrayWithPadding(iv, key, resultEnc, ab.getInnerCode());
			else
				resultEnc = encryptor.encrypt(resultEnc, false);
		}
		firstTestEnc = HowLong.getDuration();
		timeToSpeed();
		appentText("<b>" + numSpeedToString(firstTestEncRes) + "</b><br/>"); flushBuffer();
		
		 
		//Start A DEC
		appentText("ou_benchmark_decryption", true); appentText(" A: "); flushBuffer();
		Thread.sleep(500);
		HowLong.start_reset();
		resultDec = resultEnc;
		for(int i = 0; i < FIRST_TEST_ITERATIONS * multiplicator; ++i)
		{
			if(nativeCode)
				encryptorNC.decryptByteArray(iv, key, resultDec, ab.getInnerCode());
			else
			{
				try {
					resultDec = encryptor.decrypt(resultDec, false);
				} catch (Exception e) {}
			}
		}
		firstTestDec = HowLong.getDuration();
		timeToSpeed();
		appentText("<b>" + numSpeedToString(firstTestDecRes) + "</b><br/>"); flushBuffer();
			
		
		//Start B ENC
		appentText("ou_benchmark_encryption", true); appentText(" B: "); flushBuffer();
		Thread.sleep(500);
		HowLong.start_reset();
		resultEnc = initBytesSecondTest;
		for(int i = 0; i < SECOND_TEST_ITERATIONS * multiplicator; ++i)
		{
			if(nativeCode)
				resultEnc = encryptorNC.encryptByteArrayWithPadding(iv, key, resultEnc, ab.getInnerCode());
			else
				resultEnc = encryptor.encrypt(resultEnc, false);
		}
		secondTestEnc = HowLong.getDuration();
		timeToSpeed();
		appentText("<b>" + numSpeedToString(secondTestEncRes) + "</b><br/>"); flushBuffer();
		
		
		//Start B DEC
		appentText("ou_benchmark_decryption", true); appentText(" B: "); flushBuffer();
		Thread.sleep(500);
		HowLong.start_reset();
		resultDec = resultEnc;
		for(int i = 0; i < SECOND_TEST_ITERATIONS * multiplicator; ++i)
		{
			boolean ok = false;
			if(nativeCode)
				ok = encryptorNC.decryptByteArray(iv, key, resultDec, ab.getInnerCode());
			else
			{
				try {
					resultDec = encryptor.decrypt(resultDec, false);
				} catch (Exception e) {}
			} 
		}
		secondTestDec = HowLong.getDuration(); flushBuffer();
		
		timeToSpeed();
		appentText("<b>" + numSpeedToString(secondTestDecRes) + "</b><br/>"); flushBuffer();
		appentText("...<br/><b>");
		appentText("ou_benchmark_overallScore_text", true);
		appentText(": " + overAllScore);
		appentText(" ");
		appentText("ou_benchmark_points_text", true);
		appentText("</b><<br/>");
		appentText("ou_benchmark_io_notincluded", true);
		appentText("<br/>"); flushBuffer();

		// Log to File
		/*
		SSElog.l("BENCHMARK", ("******").getBytes(),LOG_FILENAME, true, false, true);
		SSElog.l("BENCHMARK", ((encryptor.getEncryptAlgorithmComment()) + (nativeCode ? " N.C." : "")).getBytes(), LOG_FILENAME, true, false, true);
		SSElog.l("BENCHMARK", ("firstTestEnc: " + numSpeedToString(firstTestEncRes)).getBytes(), LOG_FILENAME, true, false, true);
		SSElog.l("BENCHMARK", ("firstTestDec: " + numSpeedToString(firstTestDecRes)).getBytes(), LOG_FILENAME, true, false, true);
		SSElog.l("BENCHMARK", ("secondTestEnc: " + numSpeedToString(secondTestEncRes)).getBytes(), LOG_FILENAME, true, false, true);
		SSElog.l("BENCHMARK", ("secondTestDec: " + numSpeedToString(secondTestDecRes)).getBytes(),LOG_FILENAME, true, false, true);
		SSElog.l("BENCHMARK", ("overAllScore: " + overAllScore + " points").getBytes(), LOG_FILENAME, true, false, true);
		SSElog.l("BENCHMARK", ("").getBytes(),LOG_FILENAME, true, false, true);
		*/
		
		if(handler != null)
			handler.sendMessage(Message.obtain(handler, BENCHMARK_COMPLETED));
	}
	
	private byte[] getPseudoRandomBytes(int size)
	{
		byte[] bytes = new byte[size];
		Random rand = new Random(0);
		rand.nextBytes(bytes);
		return bytes;
	}
	
	private void timeToSpeed()
	{
		if(firstTestEnc > -1) firstTestEncRes = ((1.0 * FIRST_TEST_FILE_SIZE * FIRST_TEST_ITERATIONS * multiplicator) / firstTestEnc * 1000);
		if(firstTestDec > -1) firstTestDecRes = ((1.0 * FIRST_TEST_FILE_SIZE * FIRST_TEST_ITERATIONS * multiplicator) / firstTestDec * 1000);
		if(secondTestEnc > -1) secondTestEncRes = ((1.0 * SECOND_TEST_FILE_SIZE * SECOND_TEST_ITERATIONS * multiplicator) / secondTestEnc * 1000);
		if(secondTestDec > -1) secondTestDecRes = ((1.0 * SECOND_TEST_FILE_SIZE * SECOND_TEST_ITERATIONS * multiplicator) / secondTestDec * 1000);
		overAllScore = (long)((firstTestEncRes + firstTestDecRes + secondTestEncRes + secondTestDecRes) / 4 / 1024);
	}
	
	private void appentText(String text)
	{
		appentText(text, false);
	}
	
	private void appentText(String text, boolean fromRes)
	{
		if(handler != null) 
		{
			if(fromRes)handler.sendMessage(Message.obtain(handler, BENCHMARK_APPEND_TEXT_RESOURCE , text));
			else handler.sendMessage(Message.obtain(handler, BENCHMARK_APPEND_TEXT , text));
		}		
	}
	
	private void flushBuffer()
	{
		if(handler != null)handler.sendMessage(Message.obtain(handler, BENCHMARK_FLUSH_BUFFER)); 
	}
	
	private String numSpeedToString(double speed)
	{
		return Helpers.getFormatedFileSize((long)speed) + "/s";	
	}
}
