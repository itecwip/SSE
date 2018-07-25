package com.paranoiaworks.sse.cmd;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.zip.DataFormatException;

import com.paranoiaworks.sse.CryptFile;
import com.paranoiaworks.sse.Encryptor;
import com.paranoiaworks.sse.Helpers;
import com.paranoiaworks.sse.MessageHandler;

/**
 * S.S.E. for PC - Command Line Main Class
 * 
 * @author Paranoia Works
 * @version 1.0.5
 */ 

public class SSEFileCmd {
	
	private static Map<String, Integer> algCodeMap = new HashMap<String, Integer>();
	
	private static ResourceBundle textBundle;
	
	static {		
		algCodeMap.put("aes", 0);
		algCodeMap.put("rc6", 1);
		algCodeMap.put("serpent", 2);
		algCodeMap.put("twofish", 4);
		algCodeMap.put("gost", 5);
		algCodeMap.put("blowfish", 6);
		algCodeMap.put("threefish", 7);
		algCodeMap.put("shacal2", 8);
	}

	 /** Main method */
	public static void main(String[] args) {
		
		Locale.setDefault(Locale.ENGLISH);
		textBundle = ResourceBundle.getBundle("com.paranoiaworks.sse.cmd.SSEFileCmd", Locale.getDefault());
		Encryptor.setLocale(Locale.getDefault());
		MessageHandler mh = new MessageHandler();
		
		mh.println(textBundle.getString("ssefilecmd.Welcome"));				
				
		boolean compress = true;
		String inputFilePath = null;		
		String password = null;
		int algorithmCode = -1;
		int errors = -1;
		CryptFile inputFile = null;
		
		while(true)
		{
			Parameters parameters;
			
			try {
				parameters = parseParameters(args);
			} catch (IllegalArgumentException e) {
				mh.println(e.getMessage());
				mh.println("");
				mh.println(textBundle.getString("ssefilecmd.HelpText"));
				return;
			}
			
			if(parameters.askMode) // Ask for Parameters mode
			{
				String input = null;
				
				Scanner scanner = new Scanner(System.in);
				
				while(inputFilePath == null)
				{
					mh.print(textBundle.getString("ssefilecmd.text.SetInputFile") + ": ");
					input = scanner.nextLine();
					File iTempFile = new File(Helpers.convertToCurrentFileSeparator(input));
					if(iTempFile.exists())
					{
						inputFilePath = iTempFile.getAbsolutePath();
						inputFile = new CryptFile(inputFilePath);
						break;
					}
					else mh.println(textBundle.getString("ssefilecmd.text.FileNotFound") + ": " + iTempFile.getAbsolutePath() + "\n");
				}
				
				while(password == null)
				{
					mh.print(textBundle.getString("ssefilecmd.text.SetPassword") + ": ");
					input = scanner.nextLine().trim();
	
					if(!input.equals(""))
					{
						password = input.trim();
						break;
					}
				}
				
				while(algorithmCode < 0 && !inputFile.isEncrypted())
				{
					mh.print(textBundle.getString("ssefilecmd.text.SetAlgorithm") + " (" + textBundle.getString("ssefilecmd.misc.AlgorithmList") + "): ");
					input = scanner.nextLine().trim();
					if(input.equals("")) input = "aes";
					algorithmCode = getAlgorithmCode(input);
	
					if(algorithmCode < 0)
					{
						mh.println(textBundle.getString("ssefilecmd.text.UnknownAlgorithm") + ": " + input + "\n");
					}
					else break;
				}		
			}
			else // Command Line Arguments mode
			{
				inputFilePath = parameters.inputFilePath;		
				password = parameters.password;
				algorithmCode = parameters.algorithmCode;
			}
			mh.println("");
			
			inputFile = new CryptFile(inputFilePath);
	
			errors = -1;
			try {
				Encryptor encryptor = new Encryptor(password, algorithmCode, true);
				
				// enc/dec process
				if(!inputFile.isEncrypted())
					errors = (int)encryptor.zipAndEncryptFile(inputFile, compress, mh, null);
				else
					errors = (int)encryptor.unzipAndDecryptFile(new CryptFile(inputFilePath), mh, null);
				
			} catch (InvalidParameterException e) {
				mh.println(e.getMessage());
				if(parameters.askMode) // invalid password
				{
					password = null;
					continue;
				}
			} catch (DataFormatException e) {
				mh.println(e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				mh.println(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				mh.println(e.getMessage());
			}
			break;
		}

		if(errors > -1)
		{
			if(errors == 0) mh.println("\n" + textBundle.getString("ssefilecmd.text.Completed") + ": OK");
			else mh.println("\n" + textBundle.getString("ssefilecmd.text.Completed") + ": " + errors + " " + textBundle.getString("ssefilecmd.text.Errors").toLowerCase());
		}
	}
	
	/** Convert Algorithm text representation code to Integer code */
	private static int getAlgorithmCode(String algorithmCodeString)
	{
		Integer code = algCodeMap.get(algorithmCodeString.toLowerCase());
		if(code == null) code = -1;
		return code;
	}
	
	/** Parse Input Arguments */
	private static Parameters parseParameters(String[]pars) throws IllegalArgumentException
	{
		List<String> parsList = new ArrayList<String>(Arrays.asList(pars));	
		Parameters parameters = new Parameters();
		if(parsList.contains("-a"))
		{
			parameters.askMode = true;
		}
		else
		{
			if(parsList.size() < 2) throw new IllegalArgumentException(textBundle.getString("ssefilecmd.text.NotEnoughArguments"));
			if(parsList.size() > 3) throw new IllegalArgumentException(textBundle.getString("ssefilecmd.text.TooManyArguments"));
			
			File inputFile = new File(Helpers.convertToCurrentFileSeparator(parsList.get(0)));
			if(!inputFile.exists()) throw new IllegalArgumentException(textBundle.getString("ssefilecmd.text.FileNotFound") + ": " + inputFile.getAbsolutePath());
			else parameters.inputFilePath = inputFile.getAbsolutePath();
			
			parameters.password = parsList.get(1);
			
			if(parsList.size() == 3)
			{
				parameters.algorithmCodeString = parsList.get(2);
				parameters.algorithmCode = getAlgorithmCode(parameters.algorithmCodeString);
				if(parameters.algorithmCode < 0) throw new IllegalArgumentException(textBundle.getString("ssefilecmd.text.UnknownAlgorithm") + ": " + parameters.algorithmCodeString);
			}
			else parameters.algorithmCode = 0;
		}
		
		return parameters;
	}
	
    public static class Parameters 
    {    
        public String inputFilePath;
        public String password;
        public String algorithmCodeString;
        public Integer algorithmCode;
        public boolean askMode = false;       
    }
}
