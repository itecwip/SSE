package com.paranoiaworks.unicus.android.sse;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.lambdaworks.crypto.SCrypt;
import com.paranoiaworks.unicus.android.sse.components.ImageToast;
import com.paranoiaworks.unicus.android.sse.components.PasswordGeneratorDialog;
import com.paranoiaworks.unicus.android.sse.components.SelectionDialog;
import com.paranoiaworks.unicus.android.sse.components.SimpleHTMLDialog;
import com.paranoiaworks.unicus.android.sse.components.SimpleWaitDialog;
import com.paranoiaworks.unicus.android.sse.components.VerboseWaitDialog;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.utils.AlgorithmBenchmark;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor.AlgorithmBean;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

/**
 * Other Utils activity class
 * 
 * @author Paranoia Works
 * @version 1.0.11
 */
public class OtherUtilsActivity extends CryptActivity {
	
	private View passwordGeneratorButton;
	private View clipboardCleanerButton;
	private View applicationReportButton;
	private View benchmarkButton;
	private View upgradeToProButton;
	private View proDownloadsButton;
	private Button helpButton;
	
	private VerboseWaitDialog verboseWaitDialog;
	private Dialog waitDialog;
	private SimpleHTMLDialog simpleHTMLDialog;
	
	private boolean nativeCodeDisabled;
	
	private static final int OU_MESSAGE_START_BENCHMARK = -5001;
	private static final int OU_SHOW_WAITDIALOG = -5002;
	private static final int OU_HIDE_WAITDIALOG = -5003;
	private static final int OU_CLIPBOARD_CLEANER_SHOW_REPORT = -5004;
	private static final int OU_MESSAGE_CLIPBOARD_CLEANER_CONFIRM_CLEAN = -5005;
	private static final int OU_MESSAGE_CLIPBOARD_CLEANER_SHOW_REPORT = -5006;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this.setContentView(R.layout.la_otherutils);
    	setTitle(getResources().getString(R.string.common_app_otherUtils_name));
    	nativeCodeDisabled = settingDataHolder.getItemAsBoolean("SC_Common", "SI_NativeCodeDisable");
    	
    	setLayoutOrientation();
    	
        // Password Generator Button
        passwordGeneratorButton = (View)findViewById(R.id.OU_PasswordGenerator);
        passwordGeneratorButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	new PasswordGeneratorDialog(v).show();
		    }
	    });
        
        // Clipboard Cleaner Button
        clipboardCleanerButton = (View)findViewById(R.id.OU_ClipboardCleaner);
        clipboardCleanerButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v)
		    {
	    		ComponentProvider.getBaseQuestionDialog(v, 
						getResources().getString(R.string.ou_clipboardCleaner),  
	    				getResources().getString(R.string.ou_clipboardCleanQuestion), 
	    				null, 
	    				OU_MESSAGE_CLIPBOARD_CLEANER_CONFIRM_CLEAN
	    				).show();  	
		    }
	    });
    	
        // Application Report Button
        applicationReportButton = (View)findViewById(R.id.OU_ApplicationReport);
        applicationReportButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	verboseWaitDialog = new VerboseWaitDialog(v);
		    	verboseWaitDialog.appendText(getApplicationReport());
		    	verboseWaitDialog.show();
		    }
	    });
    	
        // Benchmark Button
        benchmarkButton = (View)findViewById(R.id.OU_AlgorithmBenchmark);
        benchmarkButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	List<String> itemList = new ArrayList<String>();
		    	List<Object> keyList = new ArrayList<Object>();
		    	List<String> commentList = new ArrayList<String>();
		    	
				Map<Integer, AlgorithmBean> abMap = null;
				try {
					Encryptor tempE = new Encryptor("...");
					if(!nativeCodeDisabled)tempE.enableNativeCodeEngine();
					abMap = tempE.getAvailableAlgorithms();
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
				TreeSet<Integer> keySet = new TreeSet<Integer>(abMap.keySet());
			    Iterator<Integer> ks = keySet.iterator();
		    	
		    	while (ks.hasNext())
		    	{
			    	int key = ks.next();
			    	if(key == 3) continue;
			    	if((key == 7 || key == 8) && StaticApp.licenseLevel < 2) continue;
		    		AlgorithmBean ab = abMap.get(key);
		    		itemList.add(ab.getShortComment());
		    		commentList.add("(platform independent)");
		    		keyList.add(Integer.toString(ab.getInnerCode()));
		    		if(ab.isNativeCodeAvailable())
		    		{
			    		itemList.add(ab.getShortComment());
			    		commentList.add("(native code)");
			    		keyList.add(Integer.toString(ab.getInnerCode() + AlgorithmBenchmark.NATIVE_CODE_OFFSET));
		    		}
		    	}	    	

		    	SelectionDialog benchmarkAlgorithmChooseDialog = new SelectionDialog(v, 
		    			itemList, 
        				commentList, null, 
        				keyList, 
        				getResources().getString(R.string.common_algorithm_text) + " " + getResources().getString(R.string.common_benchmark_text));
		    	benchmarkAlgorithmChooseDialog.setMessageCode(OU_MESSAGE_START_BENCHMARK);
		    	
		    	benchmarkAlgorithmChooseDialog.show();		    	
		    }
	    });
        
	    // Help Button
    	this.helpButton = (Button)this.findViewById(R.id.OU_helpButton);
	    this.helpButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	simpleHTMLDialog = new SimpleHTMLDialog(v);
		    	simpleHTMLDialog.loadURL(getResources().getString(R.string.helpLink_OtherUtils));
		    	simpleHTMLDialog.show();
		    }
	    });
    }
    
    private void setLayoutOrientation()
    {    	
    	LinearLayout mainWrapper = (LinearLayout) this.findViewById(R.id.MainWrapper);
    	ViewGroup topWrapper = (ViewGroup) this.findViewById(R.id.TopWrapper);
    	ViewGroup bottomWrapper = (ViewGroup) this.findViewById(R.id.BottomWrapper);
    	
    	Helpers.setLayoutOrientationA(mainWrapper, topWrapper, bottomWrapper);
    }
    
	void processMessage()
    {
        ActivityMessage am = getMessage();
        if (am == null) return;
        
        int messageCode = am.getMessageCode();    
        switch (messageCode) 
        {   
		    case OU_MESSAGE_START_BENCHMARK:
		    {
		    	verboseWaitDialog = new VerboseWaitDialog(this);
		    	verboseWaitDialog.hideButton();
		    	final int value = Integer.parseInt(am.getMainMessage());
		    	Thread benchmarkThread = new Thread (new Runnable() 
				{
					public void run() 
					{
						PowerManager.WakeLock wakeLock;
						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
						wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "S_BENCHMARK");
						
						wakeLock.acquire();
						AlgorithmBenchmark ab = new AlgorithmBenchmark(value, handler); 			    	
				    	ab.startBenchmark();
				    	wakeLock.release();
					}
				});
				benchmarkThread.setPriority(Thread.MAX_PRIORITY);
				benchmarkThread.start(); 
		    	
		    	this.resetMessage();
		        break;
		    }
		    
		    case OU_MESSAGE_CLIPBOARD_CLEANER_CONFIRM_CLEAN:
		    {
				if(am.getAttachement().equals(new Integer(1)))
				{
					final ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			    	
			    	waitDialog = new SimpleWaitDialog(this);
	        		Thread ccThread = new Thread (new Runnable() 
	        		{
	        			public void run() 
	        			{
	        				PowerManager.WakeLock wakeLock;
	        				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        				wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CLEAN_CLIPBOARD");
	        					
	        				wakeLock.acquire();
	        				handler.sendMessage(Message.obtain(handler, OU_SHOW_WAITDIALOG));
	        									
	        				for (int i = 0; i < 25; ++i)
	        				{
	        					try {
	        						clipBoard.setText(Integer.toString(i));
	        						Thread.sleep(200);
	        					} catch (Exception e) {
	        						e.printStackTrace();
	        					}
	        				}      				
	        				clipBoard.setText(null);
		            			
		            		handler.sendMessage(Message.obtain(handler, OU_HIDE_WAITDIALOG));
		            		handler.sendMessage(Message.obtain(handler, OU_CLIPBOARD_CLEANER_SHOW_REPORT));
		            		wakeLock.release();
	        			}
	        		});
	        		ccThread.setPriority(Thread.MIN_PRIORITY);
	        		ccThread.start();     		
				}
		    	this.resetMessage();
		        break;
		    }
		    
		    case OU_MESSAGE_CLIPBOARD_CLEANER_SHOW_REPORT:
		    {
		    	new ImageToast(getResources().getString(R.string.ou_clipboardCleaned), ImageToast.TOAST_IMAGE_OK, this).show();
		    	this.resetMessage();
		        break;
		    }
	        
		    default: 
	        	break;
        }
    }
	
    /** Application report */
    private String getApplicationReport()
    {
    	StringBuilder reportText = new StringBuilder();
    	reportText.append(getResources().getString
				(R.string.main_report_title));
    	reportText.append((getResources().getString
				(R.string.main_firstStartup_text)) + " " + asb.getFirstRunString() + "<br/>");
    	reportText.append((getResources().getString
    			(R.string.main_lastStartup_text)) + " " + asb.getLastRunString() + "<br/>");
		reportText.append((getResources().getString
				(R.string.main_numberOfStartups_text)) + " " + Integer.toString(asb.getNumberOfRuns()) + "<br/>");
		reportText.append((getResources().getString
				(R.string.main_dbIntegrity_text)) + " " + asb.isChecksumOkText() + "<br/>");
		
		try {	
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			reportText.append("<br/><b>App: </b>");	
			reportText.append(pInfo.versionName + "  (" + pInfo.versionCode + ")<br/>");
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
		try {
			reportText.append("<br/><b>System:</b><br/>");
			reportText.append("Arch.: " + System.getProperty("os.arch") + "<br/>");
			reportText.append("API: " + android.os.Build.VERSION.SDK_INT + "<br/>");
			reportText.append("Max Heap Size: " + Helpers.getMaxHeapSizeInMB() + " MB<br/>");
			reportText.append("Display Density: " + Math.round(getResources().getDisplayMetrics().density * 100) / 100f + "<br/>");
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		reportText.append(getResources().getString(R.string.main_AvailableAlgorithmsText));	
		
		Map<Integer, AlgorithmBean> abMap = null;
		try {
			Encryptor tempE = new Encryptor("...");
			if(!nativeCodeDisabled)tempE.enableNativeCodeEngine();
			abMap = tempE.getAvailableAlgorithms();
		} catch (Exception e) {
			e.printStackTrace();
		} 
			
		TreeSet<Integer> keySet = new TreeSet<Integer>(abMap.keySet());
	    Iterator<Integer> ks = keySet.iterator();
	    	
	    while (ks.hasNext())
	    {
	    	int key = ks.next();
	    	if(key == 3) continue;
	    	if((key == 7 || key == 8) && StaticApp.licenseLevel < 2) continue;
	    	AlgorithmBean ab = abMap.get(key);
	    	reportText.append("<br/>" + ab.getComment());
	    	if(ab.isNativeCodeAvailable()) reportText.append(" N.C.");
	    }
	    reportText.append("<br/>------------<br/>");
	    reportText.append("scrypt: " + (SCrypt.isNativeCodeAvailable() ? "N.C." : "P.I."));
	    	    	
	    return reportText.toString();
    }
	
    @Override
    protected void onStart ()
    {
        setRunningCode(RUNNING_OTHERUTILS);
    	super.onStart();
    }
	
    @Override
    public void onConfigurationChanged(Configuration c)
    {
    	setLayoutOrientation();
    	super.onConfigurationChanged(c);
    }
    
    @Override
    public void onBackPressed()
    {
		setRunningCode(0);
		finish();
    }
    
    Handler handler = new Handler() 
    {
    	StringBuffer textBuffer = new StringBuffer();
    	
    	public void handleMessage(Message msg)  
        {	
    		if (msg.what == OU_SHOW_WAITDIALOG)
        	{ 
    			if(waitDialog != null) waitDialog.show(); 
        		return;
        	}
        	if (msg.what == OU_HIDE_WAITDIALOG)
        	{ 
        		if(waitDialog != null) waitDialog.cancel(); 
        		waitDialog = null;
        		return;
        	}
        	if (msg.what == OU_CLIPBOARD_CLEANER_SHOW_REPORT)
        	{ 
        		setMessage(new ActivityMessage(OU_MESSAGE_CLIPBOARD_CLEANER_SHOW_REPORT, null, msg.obj)); 
        		return;
        	}
    		
    		if (msg.what == AlgorithmBenchmark.BENCHMARK_APPEND_TEXT)
        	{ 
        		textBuffer.append((String)msg.obj);
        		return;
        	}
        	if (msg.what == AlgorithmBenchmark.BENCHMARK_APPEND_TEXT_RESOURCE)
        	{ 
        		textBuffer.append(getStringResource((String)msg.obj));
        		return;
        	}
        	if (msg.what == AlgorithmBenchmark.BENCHMARK_FLUSH_BUFFER)
        	{ 
        		verboseWaitDialog.appendText(textBuffer.toString());
        		textBuffer = new StringBuffer();
        		return;
        	}
        	if (msg.what == AlgorithmBenchmark.BENCHMARK_SHOW_DIALOG)
        	{ 
        		verboseWaitDialog.show();
        		return;
        	}
        	if (msg.what == AlgorithmBenchmark.BENCHMARK_COMPLETED)
        	{ 
        		//verboseWaitDialog.appendText(getStringResource("ou_benchmark_log") + ": " + Helpers.getImportExportDir() + "/" + AlgorithmBenchmark.LOG_FILENAME);
        		verboseWaitDialog.showButton();
        		return;
        	}
        }
    };
}
