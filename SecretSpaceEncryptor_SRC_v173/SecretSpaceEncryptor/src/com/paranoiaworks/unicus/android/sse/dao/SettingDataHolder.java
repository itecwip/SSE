package com.paranoiaworks.unicus.android.sse.dao;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;

import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.config.SettingsUpdater;
import com.paranoiaworks.unicus.android.sse.utils.DBHelper;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor;

/**
 * Keeps Application Settings
 * 
 * @author Paranoia Works
 * @version 1.2.3
 * @related DB table BLOB_REP(ID = SETTINGS), data.xml
 */
public class SettingDataHolder implements Serializable{

	private static final long serialVersionUID = 10L;	
	private static final String SDH_DBPREFIX = "SETTINGS";
		
	private long dateCreated;
	private Map<String, String> data = new HashMap<String, String>();
	private Map<String, Object> persistentDataObjectMap;
	private transient Map<String, Object> sessionDataObjectMap;
	
	private SettingDataHolder() 
	{
		this.dateCreated = System.currentTimeMillis();
	}	 

	/** Singleton data holder */
	private static class PseudoSingletonHolder 
	{ 
		public static SettingDataHolder INSTANCE = null;
		
		public static void init()
		{
			SettingDataHolder sdhTemp = loadSDH(); // try to load from DB
			if(sdhTemp != null)
				INSTANCE = sdhTemp;
			else
				INSTANCE = new SettingDataHolder(); // if nothing in DB, create the new one
			
			INSTANCE.loadDefaultValues();
			
			SettingsUpdater.update(INSTANCE);
		} 	
	}
	 
	/** Create SettingDataHolder object */
	public static void initHolder()
	{
		if(PseudoSingletonHolder.INSTANCE == null) PseudoSingletonHolder.init();
	}
	
	/** Get only instance of the SettingDataHolder */
	public static SettingDataHolder getInstance()
	{
		if(PseudoSingletonHolder.INSTANCE == null) PseudoSingletonHolder.init();
		return PseudoSingletonHolder.INSTANCE;
	}
	
	/** Get data when SettingDataHolder object has been created */
	public long getDateCreated() {
		return dateCreated;
	}
	
	/** Get number of stored Settings Items */
	public int getItemsCount() {
		return data.size();
	}
	
	/** Get Item by Category and Item names - as String value */
	public String getItem(String categoryName, String itemName)
	{
		String itemCode = categoryName + ":" + itemName;
		return data.get(itemCode);
	}
	
	/** Get Item by Category and Item names - as int value */
	public int getItemAsInt(String categoryName, String itemName)
	{
		return Integer.parseInt(getItem(categoryName, itemName));
	}
	
	/** Get Item by Category and Item names - as boolean value */
	public boolean getItemAsBoolean(String categoryName, String itemName)
	{
		return Boolean.parseBoolean(getItem(categoryName, itemName));
	}
	
	/** Add or Replace Item identified by Category and Item names */
	public void addOrReplaceItem(String categoryName, String itemName, String item)
	{
		String itemCode = categoryName + ":" + itemName;
		String test = data.get(itemCode);
		if(test != null) data.remove(itemCode);
		data.put(itemCode, item);
	}
	
	/** Get Item Value NAME (for selector) */
	public String getItemValueName(String categoryName, String itemName)
	{
		return getItemValueNames(categoryName, itemName).get(
				Integer.parseInt(getItem(categoryName, itemName)));
	}
	
	/** Get Item Value Names List */
	public static List<String> getItemValueNames(String categoryName, String itemName)
	{
		List<String> names = new ArrayList<String>();
		Context cn = DBHelper.getContext();
		Resources res = cn.getResources();
    	String[] settingsItemsTemp = res.getStringArray(R.array.settings_items); 
    	
        for(String settingItem : settingsItemsTemp) 
        { 
        	String[] parAndDir = settingItem.split("!!");
        	String[] parameters = parAndDir[0].split("\\|\\|");
        	if(!parameters[0].equals(categoryName) || !parameters[1].equals(itemName)) continue;	
        	names = Arrays.asList(parameters[4].split("\\|"));
        }
        return names;
	}
	
	/** Get Persistent Data Object */
	public Object getPersistentDataObject(String key)
	{
		if(persistentDataObjectMap == null ) createPersistentDataObjectMap();
		return persistentDataObjectMap.get(key);
	}
	
	/** Add or Replace Persistent Data Object */
	public void addOrReplacePersistentDataObject(String key, Object dataObject)
	{
		if(persistentDataObjectMap == null ) createPersistentDataObjectMap();
		Object test = persistentDataObjectMap.get(key);
		if(test != null) persistentDataObjectMap.remove(key);
		persistentDataObjectMap.put(key, dataObject);
	}
	
	/** Get Session Data Object */
	public Object getSessionDataObject(String key)
	{
		if(sessionDataObjectMap == null ) createSessionDataObjectMap();
		return sessionDataObjectMap.get(key);
	}
	
	/** Add or Replace Session Data Object */
	public void addOrReplaceSessionDataObject(String key, Object dataObject)
	{
		if(sessionDataObjectMap == null ) createSessionDataObjectMap();
		Object test = sessionDataObjectMap.get(key);
		if(test != null) sessionDataObjectMap.remove(key);
		sessionDataObjectMap.put(key, dataObject);
	}
	
	private void createSessionDataObjectMap()
	{
		sessionDataObjectMap = Collections.synchronizedMap(new HashMap<String, Object>());
	}
	
	private void createPersistentDataObjectMap()
	{
		persistentDataObjectMap = Collections.synchronizedMap(new HashMap<String, Object>());
	}
		
	/** Get default values for the newly created SettingDataHolder object or Update Current (from data.xml) */
	private void loadDefaultValues()
	{
		Context cn = DBHelper.getContext();
		Resources res = cn.getResources();
    	String[] settingsItemsTemp = res.getStringArray(R.array.settings_items);
    	
    	String itemCode = null;
    	String[] parameters = null;
    	String defaultValue = null;
    	
        for(String settingItem : settingsItemsTemp) 
        { 
        	String[] parAndDir = settingItem.split("!!");
        	parameters = parAndDir[0].split("\\|\\|");
        	itemCode = parameters[0] + ":" + parameters[1];
        	defaultValue = parameters[parameters.length - 1];
    		
    		if(data.get(itemCode) != null) continue;
    		data.put(itemCode, defaultValue);   	
        } 
	}
	
	/** Load SettingDataHolder object from DB*/
	private static SettingDataHolder loadSDH() 
	{
		try {
			byte[] sdh = DBHelper.getBlobData(SDH_DBPREFIX);
			if(sdh != null) return (SettingDataHolder)Encryptor.unzipObject(sdh, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** Save SettingDataHolder object to DB*/
	public synchronized void save() 
	{
    	byte[] shd;
		try {
			shd = Encryptor.zipObject(getInstance(), null);
			DBHelper.insertUpdateBlobData(SDH_DBPREFIX, shd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
