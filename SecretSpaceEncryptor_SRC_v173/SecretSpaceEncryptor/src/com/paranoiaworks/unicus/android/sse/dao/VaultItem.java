package com.paranoiaworks.unicus.android.sse.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Leaf object of Password Vault object structure
 * Item Password Vault - code has similar structure as Vault.java
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.1
 * @related Vault.java, VaultFolder.java
 */
public class VaultItem implements Serializable, Comparable<VaultItem>{

	private static final long serialVersionUID = Vault.serialVersionUID;
	
	private String itemName = "";
	private String itemPassword = "";
	private String itemComment = "";
	private int colorCode = -1;
	private long dateCreated;
	private long dateModified = -1;
	private Map itemFutureMap;
	private int special = 0;
	private transient boolean selected = false;
	
	public static final int SPEC_GOBACKITEM = 1;
	public static final int SPEC_NEWITEM = 2;
	
	public VaultItem()
	{
		this.dateCreated = System.currentTimeMillis();
		this.itemFutureMap = new HashMap();
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName.trim();
	}

	public String getItemPassword() {
		return itemPassword;
	}

	public void setItemPassword(String itemPassword) {
		this.itemPassword = itemPassword.trim();
	}

	public String getItemComment() {
		return itemComment;
	}

	public void setItemComment(String itemComment) {
		this.itemComment = itemComment.trim();
	}

	public int getColorCode() {
		return colorCode;
	}

	public void setColorCode(int colorCode) {
		this.colorCode = colorCode;
	}

	public long getDateModified() {
		return dateModified;
	}

	public void setDateModified() {
		this.dateModified = System.currentTimeMillis();
	}
	
	public void setDateModified(Date date) {
		this.dateModified = date.getTime();
	}

	public Map getItemFutureMap() {
		return itemFutureMap;
	}

	public long getDateCreated() {
		return dateCreated;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getItemSecurityHash() {
		return Vault.getMD5Hash(this.getItemName() + Long.toString(dateCreated));
	}
	
	public boolean isSpecial() {
		if(special == 0) return false;
		return true;
	}
	
	public int getSpecialCode() 
	{
		return this.special;
	}
	
	public static VaultItem getSpecial(int code) 
	{
		return getSpecial(code, null);
	}
	
	/** Get "Go Back" or "New Item" item */
	public static VaultItem getSpecial(int code, Object tag) 
	{
		VaultItem specItem = null;
		switch (code) 
        {
	        case SPEC_GOBACKITEM: // "go to Folder Layout Item" (first in list of Items)
	        	specItem = new VaultItem(); 
	        	specItem.special = SPEC_GOBACKITEM;
	        	specItem.itemName = (String)tag;
	        	break;
	        case SPEC_NEWITEM:  // "create new item Item" (last in list of Items)
	        	specItem = new VaultItem(); 
	        	specItem.special = SPEC_NEWITEM;
	        	break;
	        default: return null;
        }
		return specItem;
	}
	
	public String toString() {
		return this.getItemName();
	}
	
	public int compareTo(VaultItem item)
	{
		int i = this.toString().compareToIgnoreCase(item.toString());
		if(i != 0) return i;
		if(this.dateCreated > item.dateCreated) return 1;
		else return -1;
	}
}
