package com.paranoiaworks.unicus.android.sse.config;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.paranoiaworks.unicus.android.sse.StaticApp;
import com.paranoiaworks.unicus.android.sse.dao.ApplicationStatusBean;
import com.paranoiaworks.unicus.android.sse.dao.SettingDataHolder;
import com.paranoiaworks.unicus.android.sse.utils.DBHelper;

/**
 * Helper - Update historical settings data to the current version + other updates
 * 
 * @author Paranoia Works
 * @version 1.0.2
 */ 
public class SettingsUpdater {

	public static void update(SettingDataHolder shd)
	{
		boolean changed = false;
		ApplicationStatusBean asb = DBHelper.getAppStatus();
		
		//+ Blowfish-256 -> Blowfish-448		
		try {
			int encryptAlgorithmCode = shd.getItemAsInt("SC_PasswordVault", "SI_Algorithm");
			if(encryptAlgorithmCode == 3)
			{
				shd.addOrReplaceItem("SC_PasswordVault", "SI_Algorithm", Integer.toString(6));
				changed = true;
			}		
			encryptAlgorithmCode = shd.getItemAsInt("SC_MessageEnc", "SI_Algorithm");
			if(encryptAlgorithmCode == 3)
			{
				shd.addOrReplaceItem("SC_MessageEnc", "SI_Algorithm", Integer.toString(6));
				changed = true;
			}	
			encryptAlgorithmCode = shd.getItemAsInt("SC_FileEnc", "SI_Algorithm");
			if(encryptAlgorithmCode == 3)
			{
				shd.addOrReplaceItem("SC_FileEnc", "SI_Algorithm", Integer.toString(6));
				changed = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			
		//-
		
		//+ Activate UNICODE password support for chosen languages
		try {
			Locale locale = StaticApp.getContext().getResources().getConfiguration().locale;
			
			if(asb.getNumberOfRuns() == 1)
			{
				String languageISO3 = locale.getISO3Language().toLowerCase();
				Set<String> langCodesSet = new HashSet<String>();
				langCodesSet.add("zho");
				langCodesSet.add("jpn");
				langCodesSet.add("kor");
				langCodesSet.add("tha");
				langCodesSet.add("rus");
				langCodesSet.add("ukr");
				langCodesSet.add("heb");
				langCodesSet.add("ell");
				langCodesSet.add("ara");
				langCodesSet.add("hin");
				
				if(langCodesSet.contains(languageISO3))
				{
					shd.addOrReplaceItem("SC_Common", "SI_AllowUnicodePasswords", Boolean.toString(true));
					changed = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			
		//-
		
		//+ Activate screen shot taking protection on Android 5+
		try {
			if(asb.getNumberOfRuns() == 1 && android.os.Build.VERSION.SDK_INT >= 21)
			{
				shd.addOrReplaceItem("SC_Common", "SI_PreventScreenshots", Boolean.toString(true));
				changed = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}			
		//-
		
		if(changed) shd.save();
	}
}
