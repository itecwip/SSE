package com.paranoiaworks.unicus.android.sse;

import java.io.File;
import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.components.HtmlAlertDialog;
import com.paranoiaworks.unicus.android.sse.components.SimpleHTMLDialog;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.DBHelper;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

/**
 * Application "Main Menu" activity class
 * 
 * @author Paranoia Works
 * @version 1.1.10
 */
public class MainActivity extends CryptActivity {

	private LinearLayout passwordVaultButton;
	private LinearLayout messageEncButton;
	private LinearLayout fileEncButton;
	private LinearLayout otherUtilsButton;
	private View settingsButton;
	private View helpButton;
	private View exitButton;
	private SimpleHTMLDialog simpleHTMLDialog;
	
	private LinearLayout containerAL;
	private LinearLayout containerBL;
	
	private static boolean readyForDestroy = false;
	
	
	public MainActivity()
	{		
		super();
	}
	
	/** Enable Main Activity for destroy */
	public static void setReadyForDestroy()
	{
		readyForDestroy = true;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		initApp();
		renderLayout();
		
	    try {
			HtmlAlertDialog whatsNewDialog = new HtmlAlertDialog(this, getResources().getString(R.string.alerts_dir) + 
					getResources().getString(R.string.whatsNewFile), getResources().getString(R.string.common_whatsNew));
			whatsNewDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}	    
    }
    
    /** Application Initialization */
    private void initApp()
    { 	
    	String importExportPath = settingDataHolder.getItem("SC_Common", "SI_ImportExportPath");
    	if(importExportPath.equals("???")) 
    	{
    		List<File> pathList = Helpers.getExtDirectories(this, false);
    		if(pathList != null && pathList.size() > 1 && pathList.get(1) != null)
    		{
    			settingDataHolder.addOrReplaceItem("SC_Common", "SI_ImportExportPath",
    				pathList.get(1).getAbsolutePath() + File.separator + getResources().getString(R.string.importExportDir));
    			settingDataHolder.save();
    		}
    	}		
    }   	
    	
    /** Prepare Main Menu Layout */
    private void renderLayout()
    {       	
        this.setContentView(R.layout.la_main);
        setTitle(getResources().getString(R.string.app_name_full));
        
        this.containerAL = (LinearLayout)this.findViewById(R.id.M_containerA);
        this.containerBL = (LinearLayout)this.findViewById(R.id.M_containerB);
        
        TextView text;
        ImageView image;
        
        this.passwordVaultButton = (LinearLayout)getLayoutInflater().inflate(R.layout.lc_square_button_icon, null);
        text = (TextView)passwordVaultButton.findViewById(R.id.text);
        text.setText(getResources().getString(R.string.main_passwordVaultButton));
        image = (ImageView)passwordVaultButton.findViewById(R.id.image);
        image.setImageResource(R.drawable.main_safe);
        passwordVaultButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
                Intent myIntent = new Intent(v.getContext(), PasswordVaultActivity.class);
                startActivityForResult(myIntent, EXIT_CASCADE);
		    }
	    });
        
        this.messageEncButton = (LinearLayout)getLayoutInflater().inflate(R.layout.lc_square_button_icon, null);
        text = (TextView)messageEncButton.findViewById(R.id.text);
        text.setText(getResources().getString(R.string.main_messageEncButton));
        image = (ImageView)messageEncButton.findViewById(R.id.image);
        image.setImageResource(R.drawable.main_text);
        messageEncButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
                Intent myIntent = new Intent(v.getContext(), MessageEncActivity.class);
                startActivityForResult(myIntent, EXIT_CASCADE);
		    }
	    });
        
        this.fileEncButton = (LinearLayout)getLayoutInflater().inflate(R.layout.lc_square_button_icon, null);
        text = (TextView)fileEncButton.findViewById(R.id.text);
        text.setText(getResources().getString(R.string.main_fileEncButton));
        image = (ImageView)fileEncButton.findViewById(R.id.image);
        image.setImageResource(R.drawable.main_file);
        fileEncButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
                Intent myIntent = new Intent(v.getContext(), FileEncActivity.class);
                startActivityForResult(myIntent, EXIT_CASCADE);
		    }
	    });
        
        this.otherUtilsButton = (LinearLayout)getLayoutInflater().inflate(R.layout.lc_square_button_icon, null);
        text = (TextView)otherUtilsButton.findViewById(R.id.text);
        text.setText(getResources().getString(R.string.main_otherUtils));
        image = (ImageView)otherUtilsButton.findViewById(R.id.image);
        image.setImageResource(R.drawable.main_utils);
        otherUtilsButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
                Intent myIntent = new Intent(v.getContext(), OtherUtilsActivity.class);
                startActivityForResult(myIntent, 0);
		    }
	    });
        
        setLayoutOrientation();
        
    	this.settingsButton = (View)this.findViewById(R.id.M_settingsButton);
	    this.settingsButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
                Intent myIntent = new Intent(v.getContext(), SettingsActivity.class);
                startActivityForResult(myIntent, 0);
		    }
	    }); 
	    
	    // Help Button
    	this.helpButton = (View)this.findViewById(R.id.M_helpButton);
	    this.helpButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	simpleHTMLDialog = new SimpleHTMLDialog(v);
		    	simpleHTMLDialog.addValue("SHOW_UPGRADE_FEATURES", StaticApp.SHOW_UPGRADE_FEATURES ? "1" : "0");
		    	simpleHTMLDialog.addValue("CN_VERSION", StaticApp.CN_VERSION ? "1" : "0");
		    	if(StaticApp.licenseLevel < 2) simpleHTMLDialog.loadURL(getResources().getString(R.string.helpLink_Main));
		    	else simpleHTMLDialog.loadURL(getResources().getString(R.string.helpLink_Main_Pro));
		    	simpleHTMLDialog.show();
		    }
	    });
    	
    	// Exit Application Button
    	this.exitButton = (View)this.findViewById(R.id.M_exitButton);
	    this.exitButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	ComponentProvider.getExitDialog(v).show();
		    }
	    });
    }
    
    void processMessage()
    {
    	
    }
    
    /** Solve differences between Portrait and Landscape orientation */ 
	private void setLayoutOrientation()
    {    	
    	int orientation = this.getResources().getConfiguration().orientation;
    	
        parametrizeSquareView(passwordVaultButton);
        parametrizeSquareView(messageEncButton);
        parametrizeSquareView(fileEncButton);
        parametrizeSquareView(otherUtilsButton);
        
        containerAL.removeAllViews();
        containerBL.removeAllViews();
    	
    	if(orientation == Configuration.ORIENTATION_PORTRAIT)
    	{   	
    		containerBL.setVisibility(View.VISIBLE);
    		containerAL.addView(passwordVaultButton);
            containerAL.addView(messageEncButton);
            containerBL.addView(fileEncButton);
            containerBL.addView(otherUtilsButton);
    	}
    	else if(orientation == Configuration.ORIENTATION_LANDSCAPE)
    	{
    		containerBL.setVisibility(View.GONE);
    		containerAL.addView(passwordVaultButton);
            containerAL.addView(messageEncButton);
            containerAL.addView(fileEncButton);
            containerAL.addView(otherUtilsButton);
    	}
    }
	
	/** Render Layout Helper */
	private void parametrizeSquareView(ViewGroup view)
	{
		float scaler = -1; int borderSize = -1;
		int orientation = this.getResources().getConfiguration().orientation;
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float width = pxToDp(dm.widthPixels);
		float height = pxToDp(dm.heightPixels);
		if(orientation == Configuration.ORIENTATION_PORTRAIT){
			scaler = 2;
			borderSize = dpToPx(10);
		} else {
			scaler = 4;
			borderSize = dpToPx(8);
		}
		int size = dpToPx((width - (10 * scaler)) / scaler);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size, 1.0f);
		params.setMargins(borderSize, borderSize, borderSize, borderSize);
		view.setLayoutParams(params);
		view.setFocusable(true);	
	}
    
	/** Part of the custom flow control */
    @Override
    protected void onStart()
    {
        switch(getRunningCode()) 
        {        
        	case RUNNING_PASSWORDVAULTACTIVITY:
        	{
                Intent myIntent = new Intent(this, PasswordVaultActivity.class);
                startActivityForResult(myIntent, EXIT_CASCADE);
        		break;
        	}    		
        	case RUNNING_MESSAGEENCACTIVITY:
        	{
                Intent myIntent = new Intent(this, MessageEncActivity.class);
                startActivityForResult(myIntent, EXIT_CASCADE);
        		break;
        	}
        	case RUNNING_FILEENCACTIVITY:
        	{
                Intent myIntent = new Intent(this, FileEncActivity.class);
                startActivityForResult(myIntent, EXIT_CASCADE);
        		break;
        	}
        	case RUNNING_SETTINGSACTIVITY:
        	{
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(myIntent, EXIT_CASCADE);
        		break;
        	}
        	case RUNNING_OTHERUTILS:
        	{
                Intent myIntent = new Intent(this, OtherUtilsActivity.class);
                startActivityForResult(myIntent, EXIT_CASCADE);
        		break;
        	}
        	default: 
            	break;
        }
    	super.onStart();
    }
    
    @Override
    public void onConfigurationChanged(Configuration c)
    {
    	setLayoutOrientation();
    	//drawApplicationReport();
    	super.onConfigurationChanged(c);
    }
     
    @Override
    public void onBackPressed()
    {
    	ComponentProvider.getExitDialog(this).show();
    }
    
    @Override
    public void onWindowFocusChanged(boolean b)
    {
    	//drawApplicationReport();
    	super.onWindowFocusChanged(b);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        if(StaticApp.licenseLevel < 2) setTitle(getResources().getString(R.string.app_name_full_free));
        else setTitle(getResources().getString(R.string.app_name_full_pro));
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) 
	{
		if (requestCode == EXIT_CASCADE && resultCode == EXIT_CASCADE) 
		{
			readyForDestroy = true;
			finish();
		}
		else if(resultCode == RESTART_PASSWORDVAULTACTIVITY)
		{
            Intent myIntent = new Intent(this, PasswordVaultActivity.class);
            startActivityForResult(myIntent, EXIT_CASCADE);
		}
	}
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	if (readyForDestroy)
    	{
    		// Wipeout application
    		try {
				DBHelper.killDB();
				android.os.Process.killProcess(android.os.Process.myPid());
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
}