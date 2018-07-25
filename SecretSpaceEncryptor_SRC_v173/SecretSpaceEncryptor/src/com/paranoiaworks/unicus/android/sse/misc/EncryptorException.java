package com.paranoiaworks.unicus.android.sse.misc;

/**
 * Encryptor Exception
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class EncryptorException extends Exception {

	private static final long serialVersionUID = 1;
	private Throwable cause;

	public EncryptorException(String  message)
	{
		super(message);
	}

	public EncryptorException(String  message, Throwable cause)
	{
		super(message);
		this.cause = cause;
	}

	public Throwable getCause()
	{
		return cause;
	}
}
