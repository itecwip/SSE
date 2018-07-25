package com.paranoiaworks.unicus.android.sse.misc;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

/**
 * Extended java.io.File Class
 * 
 * @author Paranoia Works
 * @version 1.0.1
 */
public class CryptFile extends File {
	
	public final static String ENC_FILE_HEADER_PREFIX = "SSE";
	public final static String ENC_FILE_EXTENSION = "enc";
	
	private boolean selected = false;
	private boolean encrypted = false;
	private boolean backDir = false;
	
	//+ Constructors
	public CryptFile(File file) {
		super(file.getAbsolutePath());
		processCommon();
	}
	
	public CryptFile(String path) {
		super(path);
		processCommon();
	}

	public CryptFile(URI uri) {
		super(uri);
		processCommon();
	}

	public CryptFile(File dir, String name) {
		super(dir, name);
		processCommon();
	}

	public CryptFile(String dirPath, String name) {
		super(dirPath, name);
		processCommon();
	}
	//- Constructors

	/** Is this file SSE encrypted file? */
	public boolean isEncrypted() {
		return encrypted;
	}
	
	/** Is this file Selected? (in File Encryptor) */
	public boolean isSelected() {
		return selected;
	}

	/** Set this file selected */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/** Means "parent file of current file list" (first item in the current file list in File Encryptor) */
	public boolean isBackDir() {
		return backDir;
	}

	public void setBackDir(boolean backDir) {
		this.backDir = backDir;
	}

	private void processCommon()
	{
		if(this.getName().endsWith("." + ENC_FILE_EXTENSION))
		{
			byte header[] = new byte[3];
			FileInputStream testStream = null;
			try {
				testStream = new FileInputStream(this);				
				testStream.read(header);
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				try {
					testStream.close();
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			if (new String(header).equalsIgnoreCase(ENC_FILE_HEADER_PREFIX)) this.encrypted = true;
		}
	}
	
	@Override 
	public String toString() {
		return this.getName();
	}
	
	@Override 
	public int compareTo(File anotherFile)
	{
		if (this.isBackDir()) return -1;
		if (((CryptFile)anotherFile).isBackDir()) return 1;
		
		boolean isThisFileDir = this.isDirectory() ? true : false;
		boolean isAnotherFileDir = anotherFile.isDirectory() ? true : false; 
		
		if (!isAnotherFileDir && isThisFileDir) return -1;
		if (isAnotherFileDir && !isThisFileDir) return 1;
	
		return this.getName().compareToIgnoreCase(anotherFile.getName());
	}			
}
