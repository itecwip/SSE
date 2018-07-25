package com.paranoiaworks.unicus.android.sse.misc;

/**
 * Extended Interuption Encryptor Exception
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class ExtendedInterruptedException extends InterruptedException {

	private static final long serialVersionUID = 1;
	private Object attachment;

	public ExtendedInterruptedException(String  message)
	{
		super(message);
	}
	
	public ExtendedInterruptedException(String  message, Object attachment)
	{
		super(message);
		this.attachment = attachment;
	}
	
	public Object getAttachement()
	{
		return attachment;
	}
}
