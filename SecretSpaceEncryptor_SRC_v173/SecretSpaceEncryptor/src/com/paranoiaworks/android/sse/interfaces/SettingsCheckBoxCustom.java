package com.paranoiaworks.android.sse.interfaces;

/** Custom CheckBox Interface (used in app's settings)
 * 
 * @author for Paranoia Works
 * @version 1.0.0
 */ 
public interface SettingsCheckBoxCustom {

	public final static int OK = 100;
	public final static int CANCEL = 400;
	
	public void doOnCheck();	
	public void doOnUncheck();	
}
