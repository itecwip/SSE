package com.paranoiaworks.unicus.android.sse.components;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.StaticApp;
import com.paranoiaworks.unicus.android.sse.adapters.BasicListAdapter;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.dao.SettingDataHolder;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.PasswordGenerator;

/**
 * Password Generator Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.7
 * @related PasswordGenerator.java
 */
public class PasswordGeneratorDialog extends SecureDialog {
	
    private static final List<String> charsetsList = new ArrayList<String>();
    private static final List<boolean[]> charsetsConfList = new ArrayList<boolean[]>();
    private static final List<Object> defaultSettings = new ArrayList<Object>();
    private static final int DEFAULT_LENGTH = 12;
	
	private SpinnerAdapter charsetSA;
	private Activity context;
    private Spinner charsetS;
    private EditText lengthET;
    private EditText passwordET;
    private EditText customCharSetET;
    private Button setButton;
    private Button toClipboardButton;
    private Button generateButton;
    private CheckBox excludeCB;
    private LinearLayout customCharSetContainer;
    
    private Integer messageCode = null;
    private boolean buttonLock = false;
    
    static
    {
    	{charsetsList.add("123"); boolean[] t = {false, false, true, false}; charsetsConfList.add(t);}
    	{charsetsList.add("abc"); boolean[] t = {true, false, false, false}; charsetsConfList.add(t);}
    	{charsetsList.add("ABC"); boolean[] t = {false, true, false, false}; charsetsConfList.add(t);}
    	{charsetsList.add("123 + abc"); boolean[] t = {true, false, true, false}; charsetsConfList.add(t);}
    	{charsetsList.add("123 + ABC"); boolean[] t = {false, true, true, false}; charsetsConfList.add(t);}
    	{charsetsList.add("abc + ABC"); boolean[] t = {true, true, false, false}; charsetsConfList.add(t);}
    	{charsetsList.add("123 + abc + ABC"); boolean[] t = {true, true, true, false}; charsetsConfList.add(t);}
    	{charsetsList.add("ASCII 33-126"); boolean[] t = {true, true, true, true}; charsetsConfList.add(t);}
    	
    	charsetsList.add(StaticApp.getContext().getResources().getString(R.string.passwordGeneratorDialog_customCharSet));
    	
    	defaultSettings.add(6);
    	defaultSettings.add(12);
    	defaultSettings.add(true);
    }
    
	public PasswordGeneratorDialog(View v) 
	{
		this((Activity)v.getContext());
	}	
	
	public PasswordGeneratorDialog(Activity context) 
	{
		super(context);
		this.context = context;
		this.init();
	}

	
	public PasswordGeneratorDialog(View v, int messageCode) 
	{
		this((Activity)v.getContext(), messageCode);
	}	
	
	public PasswordGeneratorDialog(Activity context, int messageCode) 
	{
		super(context);
		this.context = context;
		this.messageCode = messageCode;
		this.init();
	}
		
	private void init()
	{	
		final SettingDataHolder sdh = SettingDataHolder.getInstance();
			
		this.setContentView(R.layout.lc_passwordgenerator_dialog);
		this.setCanceledOnTouchOutside(false);
		this.setTitle(context.getResources().getString(R.string.passwordGeneratorDialog_passwordGenerator_text));
		charsetSA = new BasicListAdapter(context, charsetsList);
		charsetS = (Spinner)findViewById(R.id.PWGD_charsetSpinner);
		charsetS.setAdapter(charsetSA);
		setButton = (Button)findViewById(R.id.PWGD_setButton);
		toClipboardButton = (Button)findViewById(R.id.PWGD_toClipboardButton);
		generateButton = (Button)findViewById(R.id.PWGD_generateButton);
	    lengthET = (EditText)findViewById(R.id.PWGD_length);
	    passwordET = (EditText)findViewById(R.id.PWGD_passwordField);
	    customCharSetET = (EditText)findViewById(R.id.PWGD_customCharSetET);
	    excludeCB = (CheckBox)findViewById(R.id.PWGD_excludeCheckBox);
	    customCharSetContainer = (LinearLayout)findViewById(R.id.PWGD_customCharSetContainer);
	    
	    if(this.messageCode == null) setButton.setVisibility(Button.GONE);
	    else toClipboardButton .setVisibility(Button.GONE);
	    
	    passwordET.setTransformationMethod(null);
	    excludeCB.setText(Html.fromHtml(context.getResources().getString(R.string.passwordGeneratorDialog_excludeCharacters)));
	    
	    List<Object> savedSettings = (List)sdh.getPersistentDataObject("PASSWORD_GENERATOR_SETTINGS");	    
	    List<Object> settings = savedSettings != null ? savedSettings : defaultSettings;
	    
	    charsetS.setSelection((Integer)settings.get(0));
	    lengthET.setText("");
	    lengthET.append(Integer.toString((Integer)settings.get(1)));
	    excludeCB.setChecked((Boolean)settings.get(2));
	    if(settings.size() > 3) customCharSetET.setText((String)settings.get(3));
	    
    	this.setOnCancelListener(new DialogInterface.OnCancelListener() {
    		@Override
    		public void onCancel (DialogInterface dialogInterface) {
    			saveCurrentSettting(sdh);
    		}
    	});
	    
	    generate();
	    
	    generateButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	generate();
		    }
	    });
	    
	    toClipboardButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	setToSystemClipboard(passwordET.getText().toString().trim());
		    	ComponentProvider.getShowMessageDialog(
		    			context, 
		    			context.getResources().getString(R.string.common_copyToClipboard_text),
		    			context.getResources().getString(R.string.common_passwordCopiedToClipboard_text) + "<br/><br/>" + context.getResources().getString(R.string.common_copyToClipboardWarning),
		    			ComponentProvider.DRAWABLE_ICON_INFO_BLUE).show();
		    	return;
		    }
	    });
	    
	    setButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	buttonLock = true;
		    	if(messageCode != null)
        		{
	        		CryptActivity ca = (CryptActivity)context;
	        		ca.setMessage(new ActivityMessage(messageCode, passwordET.getText().toString().trim(), null));
        		}
		    	
        		cancel(); 
        		return;
		    }
	    });
	    
	    charsetS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() 
	    {
	    	@Override
	    	public void onItemSelected(AdapterView adapter, View v, int i, long lng) 
	    	{              
	    		if(i != 8) {
	    			customCharSetContainer.setVisibility(View.GONE);
	    			excludeCB.setVisibility(View.VISIBLE);
	    		}
	    		else {
	    			customCharSetContainer.setVisibility(View.VISIBLE);
	    			excludeCB.setVisibility(View.GONE);
	    		}
	        }

	        @Override
	        public void onNothingSelected(AdapterView arg0) {
	        	// N/A
	        }
	    });  
	    
    	this.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {		
		        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
		        	return true;
		        }
		        return false;
			}
		});  
	}
	
	private void generate()
	{
    	if(buttonLock) return;
    	buttonLock = true;
    	
    	int position = charsetS.getSelectedItemPosition();
    	String lenS = lengthET.getText().toString().trim();
    	int length = lenS.length() > 0 ? Integer.parseInt(lenS) : DEFAULT_LENGTH;
    	if(length > 99) length = 99;
    	if(length < 4) length = 4;
    	lengthET.setText("");
    	lengthET.append(Integer.toString(length));
    	PasswordGenerator pg = null;
    	if(position != 8) {
        	boolean[] conf = charsetsConfList.get(position);
    		pg = new PasswordGenerator(conf[0], conf[1], conf[2], conf[3], excludeCB.isChecked());
    	}
    	else {
    		String customCharSet = customCharSetET.getText().toString().trim().replaceAll("(.)(?=.*\\1)", "");
    		customCharSetET.setText(customCharSet);    		
    		pg = new PasswordGenerator(customCharSet);
    	}
	    String password = pg.getNewPassword(length);
	    passwordET.setText(password);
    	int textLength = passwordET.getText().length();
    	passwordET.setSelection(textLength, textLength);
	    
	    buttonLock = false;
	}
	
	private void saveCurrentSettting(SettingDataHolder sdh)
	{
    	List<Object> settingsObject = new ArrayList<Object>();
    	settingsObject.add(charsetS.getSelectedItemPosition());
    	String lenS = lengthET.getText().toString().trim();
    	settingsObject.add(lenS.length() > 0 ? Integer.parseInt(lenS) : DEFAULT_LENGTH);
    	settingsObject.add(excludeCB.isChecked());
    	settingsObject.add(customCharSetET.getText().toString().trim());
    	
    	sdh.addOrReplacePersistentDataObject("PASSWORD_GENERATOR_SETTINGS", settingsObject);
    	sdh.save();
	}
	
    @SuppressWarnings("deprecation")
	private void setToSystemClipboard(String text)
    {
    	ClipboardManager ClipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    	ClipMan.setText(text);
    }
}
