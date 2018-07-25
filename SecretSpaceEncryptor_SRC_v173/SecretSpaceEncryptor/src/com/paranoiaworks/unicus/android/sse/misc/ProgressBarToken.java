package com.paranoiaworks.unicus.android.sse.misc;

import java.util.List;

import android.app.Dialog;
import android.os.Handler;

import com.paranoiaworks.unicus.android.sse.components.DualProgressDialog;

/**
 * Helper object for communication between ProgressBar and executor Thread
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.3
 */
public class ProgressBarToken {
	
	private DualProgressDialog dialog;
	private Dialog cancelDialog;
	private Handler progressHandler;
	private int increment;
	
	private boolean encryptAllToOneFile = false;
	private String customFileName = null;
	private CryptFileWrapper customOutputDirectoryEncrypted = null;
	private CryptFileWrapper customOutputDirectoryDecrypted = null;		
	private List<CryptFileWrapper> includedFiles = null;
	private int numberOfFile = -1;	
	
	public boolean getEncryptAllToOneFile()
	{
		return encryptAllToOneFile;
	}
	
	public String getCustomFileName()
	{
		return customFileName;
	}
	
	public CryptFileWrapper getCustomOutputDirectoryEncrypted() {
		return customOutputDirectoryEncrypted;
	}

	public CryptFileWrapper getCustomOutputDirectoryDecrypted() {
		return customOutputDirectoryDecrypted;
	}
	
	public List<CryptFileWrapper> getIncludedFiles()
	{
		return includedFiles;
	}
	
	public int getNumberOfFiles()
	{
		return numberOfFile;
	}
	
	public void setEncryptAllToOneFile(boolean toOneFile)
	{
		this.encryptAllToOneFile = toOneFile;
	}
	
	public void setCustomFileName(String fileName)
	{
		this.customFileName = fileName;
	}
	
	public void setCustomOutputDirectoryEncrypted(
			CryptFileWrapper customOutputDirectoryEncrypted) {
		this.customOutputDirectoryEncrypted = customOutputDirectoryEncrypted;
	}

	public void setCustomOutputDirectoryDecrypted(
			CryptFileWrapper customOutputDirectoryDecrypted) {
		this.customOutputDirectoryDecrypted = customOutputDirectoryDecrypted;
	}
	
	public void setIncludedFiles(List<CryptFileWrapper> files)
	{
		this.includedFiles = files;
	}
	
	public void setNumberOfFiles(int number)
	{
		this.numberOfFile = number;
	}
	
	public DualProgressDialog getDialog() {
		return dialog;
	}
	
	public void setDialog(DualProgressDialog dialog) {
		this.dialog = dialog;
	}	

	public Dialog getCancelDialog() {
		return cancelDialog;
	}

	public void setCancelDialog(Dialog cancelDialog) {
		this.cancelDialog = cancelDialog;
	}

	public int getIncrement() {
		return increment;
	}
	
	public void setIncrement(int increment) {
		this.increment = increment;
	}
	
	public Handler getProgressHandler() {
		return progressHandler;
	}
	
	public void setProgressHandler(Handler progressHandler) {
		this.progressHandler = progressHandler;
	}
}
