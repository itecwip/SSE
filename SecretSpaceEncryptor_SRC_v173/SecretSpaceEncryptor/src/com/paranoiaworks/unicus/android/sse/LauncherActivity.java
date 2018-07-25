package com.paranoiaworks.unicus.android.sse;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.paranoiaworks.unicus.android.sse.components.ImageToast;
import com.paranoiaworks.unicus.android.sse.components.SettingsAppStartProtectionDialog;
import com.paranoiaworks.unicus.android.sse.components.SimplePasswordDialog;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;
import com.paranoiaworks.unicus.android.sse.utils.DBHelper;

/**
 * Application Launcher
 * 
 * @author Paranoia Works
 * @version 1.1.0
 */
public class LauncherActivity extends CryptActivity  {
	
	private SimplePasswordDialog pd = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	DBHelper.updateAppStatus();
    	
    	boolean launchProtection = settingDataHolder.getItemAsBoolean("SC_Common", "SI_AppStartProtection");
    	
    	if(launchProtection) 
    	{
    		Handler pdHandler = new Handler() 
		    {
		        public void handleMessage(Message msg)  
		        {
		        	if (msg.what == SimplePasswordDialog.SPD_HANDLER_OK)
		        	{ 
		        		Object[] attachment = (Object[])msg.obj;	        		
		        		String password = (String)attachment[0];
		        		
						List<byte[]> passwordPackage = (List<byte[]>)settingDataHolder.getPersistentDataObject(SettingsAppStartProtectionDialog.PERSISTENT_DATA_OBJECT_LAUNCH_PASSWORD);
						
						if(passwordPackage != null) 
						{
							byte[] hashedPassword = SettingsAppStartProtectionDialog.getPasswordDerivative(password, passwordPackage.get(1));
							if(MessageDigest.isEqual(hashedPassword, passwordPackage.get(0)))
							{
								pd.cancel();
								launchApplication();
							}
							else
							{
				        		new ImageToast(LauncherActivity.this.getResources().getString(R.string.passwordDialog_invalidPassword), 
				        				ImageToast.TOAST_IMAGE_CANCEL, LauncherActivity.this).show();
				        		pd.show();
							}
						}
						else  {
							pd.cancel();
							launchApplication();
						}
		        	}
		        	else {
		        		finish();
		        	}
		        }
		    };
			pd = new SimplePasswordDialog(this, SimplePasswordDialog.SPD_MODE_ENTER_PASSWORD, pdHandler);
			pd.setHideOnly(true);
			pd.show();	
    	}
    	else {
    		launchApplication();
    	}
    }
	
	private void launchApplication()
	{
    	final Intent intent = getIntent();   
    	if(intent == null) {
    		runMainActivity();
    		return;
    	}  	
    	
    	final Bundle bundle = intent.getExtras();
    	final String action = intent.getAction();
        final String type = intent.getType();
        
        boolean allowedActions = (action != null && (
        			action.equals(Intent.ACTION_SEND) || 
        			action.equals(Intent.ACTION_SEND_MULTIPLE) ||
        			action.equals(Intent.ACTION_VIEW) ||
        			action.equals(Intent.ACTION_EDIT)));
		
    	if(allowedActions) 
    	{
    		List<CryptFileWrapper> externalFiles = null;
    		
        	Object rawData = null;   	
        	if(bundle != null) rawData = bundle.get(Intent.EXTRA_STREAM); 
    		if(intent.getData() != null || rawData != null)
    			externalFiles = FileEncActivity.getExternalFilesFromIntent(intent, this);
    		
    		if(externalFiles != null)
    		{
	    		android.net.Uri data = intent.getData();
	    		Bundle extras = intent.getExtras();
	
	    		Intent newIntent = new Intent(this, FileEncActivity.class);
	    		newIntent.setAction(action);
	    		if(type != null) newIntent.setType(type);
	    		newIntent.setData(data);
	    		if(extras != null) newIntent.putExtras(extras);
	    		startActivity(newIntent);
    		}
    		/*
    		else if(action.equals(Intent.ACTION_SEND) && type != null && type.equals("text/plain"))
    		{
	    		android.net.Uri data = intent.getData();
	    		Bundle extras = intent.getExtras();
    			
    			Intent newIntent = new Intent(this, MessageEncActivity.class);
	    		newIntent.setAction(action);
	    		if(type != null) newIntent.setType(type);
	    		newIntent.setData(data);
	    		if(extras != null) newIntent.putExtras(extras);
	    		startActivity(newIntent);
    		}
    		*/
    		else {
    			runMainActivity();
    		}
    	}  	
    	else {
    		runMainActivity();
    	}
	}
	
	private void runMainActivity()
	{
		Intent newIntent = new Intent(this, MainActivity.class);
        startActivity(newIntent);
	}
	
	@Override
	void processMessage() {
		
	}
}
