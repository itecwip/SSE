package com.paranoiaworks.unicus.android.sse.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part of Password Vault object structure
 * Folder Password Vault - code has similar structure as Vault.java
 * Keeps VaultItems objects
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.1
 * @related Vault.java, VaultItem.java
 */
public class VaultFolder implements Serializable, Comparable<VaultFolder> {

	private static final long serialVersionUID = Vault.serialVersionUID;
	
	public static final int VAULTFOLDER_ATTRIBUTE_POSITION = 201;
	
	private String folderName = "???";
	private String folderComment= "???";
	private int colorCode = -1;
	private List<VaultItem> items;
	private long dateCreated;
	private long dateModified = -1;
	private Map<Integer, Object> folderFutureMap;
	
	private transient boolean lockedDataChanges = false;
	
	public VaultFolder()
	{
		this.dateCreated = System.currentTimeMillis();
		this.items = new ArrayList<VaultItem>();
		this.folderFutureMap = new HashMap<Integer, Object>();  // for other attributes (future attributes)
	}
	
	public int getItemCount()
	{
		return items.size();
	}
	
	public boolean addItem (VaultItem vi)
	{
		if(lockedDataChanges) return false;
		lockedDataChanges = true;
		
		items.add(vi);
		
		lockedDataChanges = false;
		return true;
	}
	
	public boolean removeItemWithIndex(int i, String hashCode)
	{
		if(lockedDataChanges) return false;
		lockedDataChanges = true;
		
		VaultItem vi = getItemByIndex(i);
		
		if(!hashCode.equals(vi.getItemSecurityHash())) return false;
		
		items.remove(i);
		
		lockedDataChanges = false;
		return true;
	}
	
	public void notifyItemDataSetChanged()
	{
		Collections.sort(items);
	}
	
	public List<VaultItem> getItemList()
	{
		return items;
	}
	
	public VaultItem getItemByIndex(int i)
	{
		return items.get(i);
	}
	
	public String getFolderName() {
		return folderName;
	}
	
	public void setFolderName(String folderName) {
		this.folderName = folderName;
		setDateModified();
	}
	
	public String getFolderComment() {
		return folderComment;
	}
	
	public void setFolderComment(String folderComment) {
		this.folderComment = folderComment;
		setDateModified();
	}
	
	public int getColorCode() {
		return colorCode;
	}
	
	public void setColorCode(int colorCode) {
		this.colorCode = colorCode;
		setDateModified();
	}
	
	public long getDateCreated() {
		return dateCreated;
	}
	
	public long getDateModified() {
		return dateModified;
	}
	
	public void setDateModified() {
		this.dateModified = System.currentTimeMillis();
	}
	
	public String getFolderSecurityHash() {
		return Vault.getMD5Hash(this.getFolderName() + Long.toString(dateCreated));
	}
	
	public Object getAttribute(int attributeID)
	{
		return folderFutureMap.get(attributeID);
	}
	
	public void setAttribute(int attributeID, Object attribute)
	{
		folderFutureMap.put(attributeID, attribute);
		setDateModified();
	}
	
	public String toString() {
		return this.getFolderName();
	}
	
	public int compareTo(VaultFolder folder)
	{
		return this.toString().compareToIgnoreCase(folder.toString());
	}
}
