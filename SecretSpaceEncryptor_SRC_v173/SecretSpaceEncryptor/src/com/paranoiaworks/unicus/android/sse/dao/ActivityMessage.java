package com.paranoiaworks.unicus.android.sse.dao;

/**
 * Part of "Activity messaging system"
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.1
 * @related CryptActivity.java and all child classes (methods - getMessage, setMessage, resetMessage, processMessage)
 */
public class ActivityMessage {

	private int messageCode;
	private String mainMessage;
	private Object attachement;
	private Object attachement2;
	
	public ActivityMessage (int messageCode, String mainMessage)
	{
		this.messageCode = messageCode;
		this.mainMessage = mainMessage;
	}
	
	public ActivityMessage (int messageCode, String mainMessage, Object attachement)
	{
		this.messageCode = messageCode;
		this.mainMessage = mainMessage;
		this.attachement = attachement;
	}
	
	public ActivityMessage (int messageCode, String mainMessage, Object attachement, Object attachement2)
	{
		this.messageCode = messageCode;
		this.mainMessage = mainMessage;
		this.attachement = attachement;
		this.attachement2 = attachement2;
	}

	public int getMessageCode() {
		return messageCode;
	}

	public String getMainMessage() {
		return mainMessage;
	}

	public Object getAttachement() {
		return attachement;
	}
	
	public Object getAttachement2() {
		return attachement2;
	}
}
