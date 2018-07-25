package com.paranoiaworks.unicus.android.sse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;

import sse.org.bouncycastle.crypto.InvalidCipherTextException;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.provider.DocumentFile;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paranoiaworks.android.sse.interfaces.Lockable;
import com.paranoiaworks.unicus.android.sse.components.ImageToast;
import com.paranoiaworks.unicus.android.sse.components.PasswordDialog;
import com.paranoiaworks.unicus.android.sse.components.ScreenLockDialog;
import com.paranoiaworks.unicus.android.sse.components.SelectionDialog;
import com.paranoiaworks.unicus.android.sse.components.SettingsAppStartProtectionDialog;
import com.paranoiaworks.unicus.android.sse.components.SimpleHTMLDialog;
import com.paranoiaworks.unicus.android.sse.components.SimplePasswordDialog;
import com.paranoiaworks.unicus.android.sse.components.SimpleWaitDialog;
import com.paranoiaworks.unicus.android.sse.components.SteganogramSettingsDialog;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.dao.PasswordAttributes;
import com.paranoiaworks.unicus.android.sse.misc.CryptFile;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.DBHelper;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;
import com.paranoiaworks.unicus.android.sse.utils.TEChangeResolver;

import ext.com.nononsenseapps.filepicker.FilePickerActivity;

/**
 * Message Encryptor activity class
 * 
 * @author Paranoia Works
 * @version 1.1.3
 */
public class MessageEncActivity extends CryptActivity implements Lockable {
	
	private int encryptAlgorithmCode;
	private boolean hideInfoMessages;
	private boolean askOnLeave;
	private int finalEncoding;
	private int addWhiteSpace;
	private boolean lockOnPause = false;
	private File steganogramsDir;
	private File carrierImagesDir;
	private View toClipBoardDecButton;
	private View fromClipBoardDecButton;
	private View encryptButton;
	private View decryptButton;
	private View toClipBoardEncButton;
	private View fromClipBoardEncButton;
	private Button passwordButton;
	private Button helpButton;
	private Button fullScreenButton;
	private Button sendTextToAppButton;
	private Button moreOptionsButton;
	private Button cleanButton;
	private EditText encryptedEditText;
	private EditText decryptedEditText;
	private TextView statusBarTV;
	private ViewGroup rootLayout;
	private ViewGroup topContainer;
	private ViewGroup centralContainer;
	private ViewGroup bottomContainer;
	
	private PasswordDialog passwordDialog;
	private SelectionDialog moreDialog;
	private Dialog messageAskDialog;
	private ScreenLockDialog sld;
	private Dialog waitDialog;
	
	LayoutParams tempLayoutParams;
	
	public static final int MEA_MESSAGE_STATUSTEXT_SET = -2001;
	private static final int MEA_MESSAGE_MOREDIALOG = -2101;
	private static final int MEA_MESSAGE_MOREDIALOG_LOAD = -2102;
	private static final int MEA_MESSAGE_MOREDIALOG_DELETE = -2104;
	private static final int MEA_MESSAGE_MOREDIALOG_DELETE_CONFIRM = -2105;
	private static final int MEA_MESSAGE_MOREDIALOG_LOAD_FILE = -2202;
	private static final int MEA_MESSAGE_MOREDIALOG_DELETE_FILE = -2204;
	private static final int MEA_MESSAGE_MOREDIALOG_DELETE_CONFIRM_FILE = -2205;
	private static final int MEA_MESSAGE_MOREDIALOG_MAKE_STEGANOGRAM = -2301;
	private static final int MEA_MESSAGE_MOREDIALOG_DECRYPT_STEGANOGRAM = -2302;
	private static final int MEA_MESSAGE_MOREDIALOG_DELETE_STEGANOGRAM = -2303;
	private static final int MEA_UNIVERSALHANDLER_SHOW_WAITDIALOG = -2401;
	private static final int MEA_UNIVERSALHANDLER_HIDE_WAITDIALOG = -2402;
	private static final int MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG = -2403;
	private static final int MEA_UNIVERSALHANDLER_STEGANOGRAM_MAKE_OUTPUTREPORT = -2404;
	private static final int MEA_UNIVERSALHANDLER_STEGANOGRAM_DECRYPT_SETOUTPUT = -2405;
	private static final int MEA_HANDLE_CARRIER_PATH = 4001; // request code has to be > 0
	private static final int MEA_HANDLE_BROWSE_FOR_TEXTFILE = 4002;
	private static final int MEA_HANDLE_BROWSE_FOR_STEGANOGRAM = 4003;
	
	
	public static final String MEA_FILE_EXT = "txt";
	public static final String MEA_STEGANOGRAM_EXT = "jpg";
	
	public boolean currentEncTextUsed = true;
	private boolean fullScreen = false;
	private boolean sldVeto = false;
	private String lastLoadedFileName = null;
	private String predefinedScreenLockKey = null;
	private TEChangeResolver changeResolver;

		
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//this.resolveActivityPreferences("SC_MessageEnc");
    	super.onCreate(savedInstanceState);
    	this.setContentView(R.layout.la_messageenc);
    	this.setTitle(getResources().getString(R.string.common_app_messageEncryptor_name));
    	encryptAlgorithmCode = settingDataHolder.getItemAsInt("SC_MessageEnc", "SI_Algorithm");
    	hideInfoMessages = settingDataHolder.getItemAsBoolean("SC_MessageEnc", "SI_HideInfoMessage");
    	finalEncoding = settingDataHolder.getItemAsInt("SC_MessageEnc", "SI_FinalEncoding");
    	addWhiteSpace = settingDataHolder.getItemAsInt("SC_MessageEnc", "SI_AddWhiteSpace");
    	lockOnPause = settingDataHolder.getItemAsBoolean("SC_MessageEnc", "SI_LockScreenTE");
    	askOnLeave = settingDataHolder.getItemAsBoolean("SC_Common", "SI_AskIfReturnToMainPage");
    	
        //Intent - External password
    	final android.content.Intent intent = getIntent();
    	String predefinedPassword = null;
    	if (intent != null) 
    	{
    		predefinedPassword = intent.getStringExtra(PasswordVaultActivity.PWV_EXTRA_PASSWORD);
    		if(predefinedPassword != null)
    		{
    			String screenLockKey = intent.getStringExtra(PasswordVaultActivity.PWV_EXTRA_LOCKSCREEN_KEY);
    			
    			boolean pwvScreenLockOn = intent.getBooleanExtra(PasswordVaultActivity.PWV_EXTRA_LOCKSCREEN_ON, false);
    			if(pwvScreenLockOn) lockOnPause = true;   			  			
    			if(lockOnPause) predefinedScreenLockKey = screenLockKey; 
    			sldVeto = true; 			
    			encryptor = null;
    		}
    	}
    	
    	changeResolver = new TEChangeResolver();
    	
    	if(!this.isTablet()) this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	 
    	decryptedEditText = (EditText) findViewById(R.id.ME_decryptedEditText);
    	decryptedEditText.setBackgroundResource(R.drawable.d_green_border);
    	encryptedEditText = (EditText) findViewById(R.id.ME_encryptedEditText);
    	encryptedEditText.setBackgroundResource(R.drawable.d_red_border);
    	statusBarTV = (TextView) findViewById(R.id.ME_statusBarTextView);
    	topContainer = (ViewGroup) findViewById(R.id.ME_topContainer);
    	centralContainer = (ViewGroup) findViewById(R.id.ME_centralContainer);
    	bottomContainer = (ViewGroup) findViewById(R.id.ME_bottomContainer);
    	rootLayout = (ViewGroup) findViewById(R.id.ME_rootLayout);
    	
    	float textSizeMultiplier = 1.0F;
    	try {
			textSizeMultiplier = Integer.parseInt(settingDataHolder.getItemValueName("SC_MessageEnc", "SI_FontSizeME").split("::")[0]) / 100.0F;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	decryptedEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(decryptedEditText.getTextSize() * textSizeMultiplier));
    	encryptedEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(encryptedEditText.getTextSize() * textSizeMultiplier));
    	encryptedEditText.setTypeface(Typeface.MONOSPACE);
    	
        // Button - Copy Decrypted Message to system clipboard
        toClipBoardDecButton = (View) findViewById(R.id.ME_toClipBoardDecButton);
        toClipBoardDecButton.setOnClickListener(new OnClickListener() 
	    {
			@Override
		    public void onClick(View v) 
		    {
		    	Toast toast;
		    	String text = decryptedEditText.getText().toString().trim();
		    	if (text.equals(""))
		    	{
			    	toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.me_emptyInputArea), v);
			    	toast.show();
			    	return;
		    	}
		    	setToSystemClipboard(text);
		    	if(!hideInfoMessages)ComponentProvider.getImageToastInfo(getResources().getString(R.string.me_decryptedToClipboard), v).show();
		    }
	    }); 
        
        // Button - Paste text from clipboard to "decryptedEditText"
        fromClipBoardDecButton = (View) findViewById(R.id.ME_fromClipBoardDecButton);
        fromClipBoardDecButton.setOnClickListener(new OnClickListener() 
	    {
			@Override
		    public void onClick(View v) 
		    {	    	
		    	Toast toast;
		    	String text = getFromSystemClipboard();
		    	if (text.equals(""))
		    	{
			    	toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.me_emptyClipboard), v);
			    	toast.show();
			    	return;
		    	}		    	
		    	decryptedEditText.setText(text);
		    	decryptedEditText.setSelection(decryptedEditText.length());	    	
		    }
	    });
        
        // Button - Clean Textfield
        cleanButton = (Button) findViewById(R.id.ME_cleanButton);
        cleanButton.setOnClickListener(new OnClickListener() 
	    {
			@Override
		    public void onClick(View v) 
		    {	    		    	
		    	decryptedEditText.setText("");
		    	encryptedEditText.setText("");
		    	changeResolver.resetLastProcessed();
		    	currentEncTextUsed = true;
		    }
	    });
        
        // Button - Encrypt
        encryptButton = (View) findViewById(R.id.ME_encryptButton);
        encryptButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	Toast toast;
		    	String text = decryptedEditText.getText().toString().trim();
		    	if (text.equals(""))
		    	{
			    	toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.me_emptyEncTextArea), v);
			    	toast.show();
			    	return;
		    	}
		    	try {
		    		encryptedEditText.setText(encryptor.encryptStringVer2(text, finalEncoding, addWhiteSpace));
		    		encryptedEditText.setSelection(encryptedEditText.length());
		    		changeResolver.setLastProcessed(decryptedEditText.getText().toString(), encryptedEditText.getText().toString());
		    		currentEncTextUsed = false;
		    		setEncTextColorBlackOrBlue();
		    		setStatusBarText(getResources().getString(R.string.common_encryption_text) + " : " + encryptor.getEncryptAlgorithmComment() + " : OK");
				}  catch (Exception e) {
					setStatusBarText(getResources().getString(R.string.common_encryption_text) + " : " + e.getLocalizedMessage());
					e.printStackTrace();
				}
		    }
	    });
        
        // Button - Decrypt
        decryptButton = (View) findViewById(R.id.ME_decryptButton);
        decryptButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	final String text = encryptedEditText.getText().toString().trim();
		    	if (text.equals(""))
		    	{
			    	ComponentProvider.getImageToastKO(getResources().getString(R.string.me_emptyDecTextArea), v).show();
			    	return;
		    	}
		    	
	    		if(decryptedEditText.getText().toString().trim().length() > 0 && checkChange())
	    		{
			    	Handler decryptTextHandler = new Handler() 
				    {
				        public void handleMessage(Message msg)  
				        {
				        	if (msg.what == 1)
				        	{ 
				        		decryptText(text);
				        	}
				        }
				    };
				    ComponentProvider.getBaseQuestionDialog(
				    		v.getContext(), 
				    		getResources().getString(R.string.common_continue_text), 
				    		getResources().getString(R.string.common_replaceText_question), 
				    		decryptTextHandler, 
				    		false).show();
	    		}
	    		else {
	    			decryptText(text);
	    		}
		    }
	    });
        
        // Button - Copy Encrypted Message to system ClipBoard
        toClipBoardEncButton = (View) findViewById(R.id.ME_toClipBoardEncButton);
        toClipBoardEncButton.setOnClickListener(new OnClickListener() 
	    {
			@Override
		    public void onClick(View v) 
		    {
		    	Toast toast;
		    	String text = encryptedEditText.getText().toString().trim();
		    	if (text.equals(""))
		    	{
			    	toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.me_emptyInputArea), v);
			    	toast.show();
			    	return;
		    	}
		    	
		    	setToSystemClipboard(text);
		    	currentEncTextUsed = true;
		    	if(encryptedEditText.getTextColors().getDefaultColor() != Color.RED) setEncTextColorBlackOrBlue();
		    	if(!hideInfoMessages)ComponentProvider.getImageToastInfo(getResources().getString(R.string.me_encryptedToClipboard), v).show();
		    }
	    }); 
        
        // Button - Paste text from clipboard to "encryptedEditText"
        fromClipBoardEncButton = (View) findViewById(R.id.ME_fromClipBoardEncButton);
        fromClipBoardEncButton.setOnClickListener(new OnClickListener() 
	    {
			@Override
		    public void onClick(View v) 
		    {
		    	Toast toast;
		    	String text = getFromSystemClipboard();
		    	if (text.equals(""))
		    	{
			    	toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.me_emptyClipboard), v);
			    	toast.show();
			    	return;
		    	}	
		    	currentEncTextUsed = true;
		    	encryptedEditText.setText(text);
		    	encryptedEditText.setSelection(encryptedEditText.length());	    	
		    }
	    });
        
        
        // Button - Send Encrypted Text to chosen application
        sendTextToAppButton = (Button) findViewById(R.id.ME_sendTextToAppButton);
        sendTextToAppButton.setOnClickListener(new OnClickListener() 
	    {
			@Override
		    public void onClick(View v) 
		    {	    		    	
				try {
					Intent sendIntent = new Intent();
					String[] text = {encryptedEditText.getText().toString()};
					sendIntent.setAction(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_TEXT, text[0]);
					sendIntent.setType("text/plain");
					startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.me_sendToApplication)));
					currentEncTextUsed = true;
					if(encryptedEditText.getTextColors().getDefaultColor() != Color.RED) setEncTextColorBlackOrBlue();
				} catch (Exception e) {
					showErrorDialog(e);
				}
		    }
	    });
        
        // Button - Show "More menu"
        moreOptionsButton = (Button)findViewById(R.id.ME_moreOptionsButton);
        moreOptionsButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v)
		    {
		    	List<String> title = new ArrayList<String>();
		    	List<String> comment = new ArrayList<String>();
		    	List<Integer> icon = new ArrayList<Integer>();
		    	List<Object> tag = new ArrayList<Object>();
		    	
		    	title.add(getResources().getString(R.string.me_moreDialog_loadMessage));
		    	comment.add(null);
		    	icon.add(R.drawable.database);
		    	tag.add("me_moreDialog_loadMessage");
		    	title.add(getResources().getString(R.string.me_moreDialog_saveMessage));
		    	comment.add(null);
		    	icon.add(R.drawable.database);
		    	tag.add("me_moreDialog_saveMessage");
		    	title.add(getResources().getString(R.string.me_moreDialog_deleteMessage));
		    	comment.add(null);
		    	icon.add(R.drawable.database);
		    	tag.add("me_moreDialog_deleteMessage");
		    	
				File importExportDir = Helpers.getImportExportDir();            							    	
				if(importExportDir != null)
		    	{
			    	String dirHint = "(dir: " + importExportDir.getAbsolutePath() + File.separator + getResources().getString(R.string.textsSubdir) + ")";
					title.add(getResources().getString(R.string.me_moreDialog_loadMessageFile));
					comment.add(dirHint);
			    	icon.add(R.drawable.sdcard);
			    	tag.add("me_moreDialog_loadMessageFile");
			    	title.add(getResources().getString(R.string.me_moreDialog_saveMessageFile));
			    	comment.add(dirHint);
			    	icon.add(R.drawable.sdcard);
			    	tag.add("me_moreDialog_saveMessageFile");
			    	title.add(getResources().getString(R.string.me_moreDialog_deleteMessageFile));
			    	comment.add(dirHint);
			    	icon.add(R.drawable.sdcard);
			    	tag.add("me_moreDialog_deleteMessageFile");
			    	
			    	steganogramsDir = new File(importExportDir.getAbsolutePath() + File.separator + getResources().getString(R.string.steganogramsSubDir));   
					if(!steganogramsDir.exists()) steganogramsDir.mkdirs();
					String steganogramsDirHint = "(dir: " + steganogramsDir.getAbsolutePath() + ")";
					
				    String carrierImagePath = (String)settingDataHolder.getPersistentDataObject("STEGANOGRAM_CARRIER_IMAGE_FOLDER");	    
				    if(carrierImagePath != null)
				    	carrierImagesDir = new File(carrierImagePath);
				    if(carrierImagesDir == null || !carrierImagesDir.exists())
				    	carrierImagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			    	
					title.add(getResources().getString(R.string.me_moreDialog_importSteganogram));
			    	comment.add(steganogramsDirHint);
			    	icon.add(R.drawable.steganogram_import);
			    	tag.add("me_moreDialog_importSteganogram");		    	
			    	title.add(getResources().getString(R.string.me_moreDialog_exportSteganogram));
			    	comment.add(steganogramsDirHint);
			    	icon.add(R.drawable.steganogram_export);
			    	tag.add("me_moreDialog_exportSteganogram");
			    	title.add(getResources().getString(R.string.me_moreDialog_deleteSteganogram));
			    	comment.add(steganogramsDirHint);
			    	icon.add(R.drawable.steganogram_delete);
			    	tag.add("me_moreDialog_deleteSteganogram");
		    	}
		    	
		    	SelectionDialog moreDialog = new SelectionDialog(v, title, comment, icon, tag);	    	
		    	moreDialog.setMessageCode(MEA_MESSAGE_MOREDIALOG);
		    	if(moreDialog != null) moreDialog.show();
		    	
		    	if(importExportDir == null)
		    	{
			    	Dialog showMessageDialog = ComponentProvider.getShowMessageDialog(v, 
								getResources().getString(R.string.common_invalidPath_text), 
								getResources().getString(R.string.common_importExportPathInvalid)
								.replaceAll("<1>", Helpers.getImportExportPath()), 
								ComponentProvider.DRAWABLE_ICON_CANCEL);
					showMessageDialog.show();
		    	}
		    }
	    });
        
        // Button - Show "Help SimpleHTMLDialog"
        helpButton = (Button)findViewById(R.id.ME_helpButton);
        helpButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	SimpleHTMLDialog simpleHTMLDialog = new SimpleHTMLDialog(v);
		    	simpleHTMLDialog.loadURL(getResources().getString(R.string.helpLink_MessageEncryptor));
		    	simpleHTMLDialog.show();
		    }
	    });
        
        // Full Screen Button
        fullScreenButton = (Button) findViewById(R.id.ME_fullScreenButton);
        fullScreenButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		        Drawable img = null;
		        if(fullScreen) { 
		        	img = getResources().getDrawable(R.drawable.go_fullscreen_cropped);
		        	bottomContainer.setVisibility(ViewGroup.VISIBLE);
		        	encryptedEditText.setVisibility(ViewGroup.VISIBLE);
		        	centralContainer.setVisibility(ViewGroup.VISIBLE);
		        	
			    	decryptedEditText.setLayoutParams(tempLayoutParams);
			    	
			    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		        }
		        else {
		        	if(tempLayoutParams == null) tempLayoutParams = decryptedEditText.getLayoutParams();
		        	
		        	img = getResources().getDrawable(R.drawable.disable_fullscreen_cropped);
		        	bottomContainer.setVisibility(ViewGroup.GONE);
		        	encryptedEditText.setVisibility(ViewGroup.GONE);
		        	centralContainer.setVisibility(ViewGroup.GONE);
		        	
		        	RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		    		relativeParams.addRule(RelativeLayout.BELOW, topContainer.getId());
			    	decryptedEditText.setLayoutParams(relativeParams);
			    	
			    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		        }
		        fullScreenButton.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null); 
		        fullScreen = !fullScreen;
		    }
	    });
        
        // Button - Set Password
        passwordButton = (Button) findViewById(R.id.ME_passwordButton);
        passwordButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	passwordDialog = new PasswordDialog(v, PasswordDialog.PD_MODE_SET_PASSWORD);
		    	passwordDialog.setEncryptAlgorithmCode(encryptAlgorithmCode);
		    	passwordDialog.setCustomTitle(getResources().getString(R.string.passwordDialog_title_set_session));
		    	passwordDialog.show();
		    }
	    });
        
        decryptedEditText.addTextChangedListener((new TextWatcher()
    	{
            public void onTextChanged (CharSequence s, int start, int before, int count)  {            	
            }
            public void  beforeTextChanged (CharSequence s, int start, int count, int after) {        	
            }
            public void  afterTextChanged (Editable s) {
            	setTitleRight(decryptedEditText.getText().length() + " | " + encryptedEditText.getText().length());
            	checkChange();
            }
    	}));
        
	    encryptedEditText.addTextChangedListener(new TextWatcher() 
	    {
	    	@Override
	    	public void onTextChanged(CharSequence s, int start, int before, int count) {	    		
	    	}
	    	@Override
	    	public void beforeTextChanged(CharSequence s, int start, int count, int after) {	    		
	    	}
	    	@Override
	    	public void afterTextChanged(Editable s){
	    		if(encryptedEditText.getText().toString().trim().length() > 0)
	    		{
	    			sendTextToAppButton.setEnabled(true);
	    			sendTextToAppButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.send_text), null, null, null); 
	    		}
	    		else 
	    		{
	    			sendTextToAppButton.setEnabled(false);
	    			sendTextToAppButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.send_text_disabled), null, null, null);
	    		}
	    		
	    		setTitleRight(decryptedEditText.getText().length() + " | " + encryptedEditText.getText().length());
	    		checkChange();
	    	} 
        });
        
        // "On Start Set Password" 
        if(predefinedPassword == null)
        	passwordDialog = new PasswordDialog(this, PasswordDialog.PD_MODE_SET_PASSWORD);
        else {
        	passwordDialog = new PasswordDialog(this, PasswordDialog.PD_MODE_SET_PASSWORD, predefinedPassword);
        	predefinedPassword = null;
        }
        passwordDialog.setEncryptAlgorithmCode(encryptAlgorithmCode);
        passwordDialog.setCustomTitle(getResources().getString(R.string.passwordDialog_title_set_session));
        passwordDialog.show();
        
        // OnClick on EncryptedEditText
        encryptedEditText.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v) {
            	//encryptedEditText.setSelection(encryptedEditText.length());
            }
        });
    }
    
    /** Handle Message */
    void processMessage()
    {
        ActivityMessage am = getMessage();
        if (am == null) return;
        
        int messageCode = am.getMessageCode();    
        switch (messageCode) 
        {        
        	case CryptActivity.COMMON_MESSAGE_SET_ENCRYPTOR:
            	this.passwordAttributes = (PasswordAttributes)((List)am.getAttachement()).get(0);
            	this.encryptor = (Encryptor)((List)am.getAttachement()).get(1);
            	Drawable bd = getResources().getDrawable(this.passwordAttributes.getDrawableID());
    	        passwordButton.setBackgroundDrawable(bd);
    	        this.resetMessage();
            	break;
            	
        	case MessageEncActivity.MEA_MESSAGE_STATUSTEXT_SET:
            	setStatusBarText(am.getMainMessage());
            	if(encryptedEditText.getTextColors().getDefaultColor() != Color.RED) setEncTextColorBlackOrBlue();
    	        this.resetMessage();
            	break;
            	
           	case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DECRYPT_STEGANOGRAM:
        	{
        		String mm = (String)am.getMainMessage();

				if(mm.equals("me_browseForFile_*?")) 
				{
        			Intent intent = new Intent();
					intent.setType("image/jpeg");
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setAction(Intent.ACTION_GET_CONTENT);
					
					startActivityForResult(intent, MEA_HANDLE_BROWSE_FOR_STEGANOGRAM);
				}
				else 
				{
					decryptSteganogram(mm, null);
				}
        	}
    	    this.resetMessage();
            break;
            	
        	case MessageEncActivity.MEA_HANDLE_CARRIER_PATH:
        	{
        		CryptFileWrapper chosenFile = (CryptFileWrapper)am.getAttachement();
        		if(chosenFile != null)
        		{
        	    	if(chosenFile.getMode() == CryptFileWrapper.MODE_FILE) {
	        			settingDataHolder.addOrReplacePersistentDataObject("STEGANOGRAM_CARRIER_IMAGE_FOLDER", chosenFile.getParentFile().getAbsolutePath());
	        	    	settingDataHolder.save();
        	    	}
        			
	        		try {
						SteganogramSettingsDialog ssd = new SteganogramSettingsDialog(this, chosenFile, MEA_MESSAGE_MOREDIALOG_MAKE_STEGANOGRAM);
						ssd.show();
					} catch (IllegalStateException e) {
						showErrorDialog(getResources().getString(R.string.common_invalidImageFile));
					} catch (Throwable e) {
						showErrorDialog(e);
					}
        		}
        		else {
        			showErrorDialog(getResources().getString(R.string.common_invalidImageFile));
        		}
        	}
    	    this.resetMessage();
            break;
        	
           	case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_MAKE_STEGANOGRAM:
        	{
		    	final String inputText = decryptedEditText.getText().toString().trim();
        		
        		List<Object> parameters = (List)am.getAttachement();
        		final CryptFileWrapper carrierImage = (CryptFileWrapper)parameters.get(0);
        		final double imageScale = (Double)parameters.get(1); 
        		final int jpegQuality = (Integer)parameters.get(2); 
        		
        		//System.out.println(carrierPath + " : " + outputImageWidth + " : " + outputImageHeight + " : " + jpegQuality);
    
    			waitDialog = new SimpleWaitDialog(this);
    			new Thread (new Runnable() 
    			{
    				public void run() 
    				{
    					PowerManager.WakeLock wakeLock;
    					PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    					wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ME_MAKE_STEGANOGRAM");
    					
    					wakeLock.acquire();
    					universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_SHOW_WAITDIALOG));
    					
    					//+ Process
                		try {
	                		String imageName = carrierImage.getName();
	                		if(imageName.indexOf(".") < 0) imageName = imageName + ".img";
                			String outputFilePath = steganogramsDir.getAbsolutePath() + System.getProperty("file.separator") + imageName;
	                		File outFile = new File(outputFilePath);
	                		int i = 0;
	                		while (outFile.exists() || i == 0) 
	                		{
	                			outFile = new File(outputFilePath.substring(0,
	                					outputFilePath.lastIndexOf("."))
	                					+ ".f5" + (i != 0 ? "(" + i + ")" : "") + ".jpg");
	                			++i;
	                		}   
                		
                			encryptor.exportTextToSteganogram(inputText, carrierImage, outFile, jpegQuality, imageScale);
                			
                			List<String> outputReport = new ArrayList<String>();
                			outputReport.add(outFile.getAbsolutePath());
                			outputReport.add(encryptor.getEncryptAlgorithmComment());
                			universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_STEGANOGRAM_MAKE_OUTPUTREPORT, outputReport));
                			
                		} catch (Exception e) {
        					String[] message = e.getMessage().split("::");
        					if(message[0].equals("1000")) {
        						universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG, getResources().getString(R.string.me_steganogramInsufficientCapacity)));
        					} 
        					else {
        						e.printStackTrace();
        						universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG, Helpers.getShortenedStackTrace(e, 1)));
        					}
        				} catch (Throwable t) {
        					universalHandler.sendMessage(Message.obtain(universalHandler, 
        							MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG, 
        							getResources().getString(R.string.common_insufficientMemory) + " (Java Max Heap: " + Helpers.getMaxHeapSizeInMB() + " MB)<br/><small>" + getResources().getString(R.string.me_steganogramInsufficientMemoryWhatToDo) + "</small>"));
        				} finally {
        					System.gc();
        				}
                		//- Process
    					
            			universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_HIDE_WAITDIALOG));
            			wakeLock.release();
    				}
    			}).start();
        	}
    	    this.resetMessage();
            break;
  
        	case MessageEncActivity.MEA_MESSAGE_MOREDIALOG:
        	{
            	String mainMessage = (String)am.getAttachement();
        		if(mainMessage.equals("me_moreDialog_saveMessage"))
            	{
            		if (encryptedEditText != null && !encryptedEditText.getText().toString().equals(""))
            		{
            			messageAskDialog = ComponentProvider.getMessageSetNameDialog(this, encryptedEditText.getText().toString().trim(), lastLoadedFileName);
            			messageAskDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            			messageAskDialog.show();
            		}
            		else
            			ComponentProvider.getImageToast(
            					this.getResources().getString(R.string.me_emptyDecTextArea02), 
            					ImageToast.TOAST_IMAGE_CANCEL, this).show();
            	} 
            	else if (mainMessage.equals("me_moreDialog_loadMessage")) 
            	{
            		List<Object> messageData = DBHelper.getMessageNamesAndData();
            		List<String> itemList = messageData != null ? (List<String>)messageData.get(0) : null;            		
            		
            		if (itemList == null || itemList.size() < 1)
            		{
            			ComponentProvider.getImageToast(
            					this.getResources().getString(R.string.me_moreDialog_noMessagesInDB), 
            					ImageToast.TOAST_IMAGE_CANCEL, this).show();
            			return;
            		}
            		
            		List<String> messageComments = Helpers.getMessageCommentsList(
            				(List<Long>)messageData.get(1), 
            				(List<Long>)messageData.get(2), 
            				getResources().getConfiguration().locale);
            		
            		moreDialog = new SelectionDialog(this, 
            				itemList, 
            				messageComments, 
            				null, null, 
            				getResources().getString(R.string.me_moreDialog_loadMessage));
            		moreDialog.setMessageCode(MEA_MESSAGE_MOREDIALOG_LOAD);
            		
    		    	if (moreDialog != null) moreDialog.show();
            	}
            	else if (mainMessage.equals("me_moreDialog_deleteMessage")) 
            	{
            		List<Object> messageData = DBHelper.getMessageNamesAndData();
            		List<String> itemList = messageData != null  ? (List<String>)messageData.get(0) : null;  
            		
            		if (itemList == null || itemList.size() < 1)
            		{
            			ComponentProvider.getImageToast(
            					this.getResources().getString(R.string.me_moreDialog_noMessagesInDB), 
            					ImageToast.TOAST_IMAGE_CANCEL, this).show();
            			return;
            		}
            		
            		List<String> messageComments = Helpers.getMessageCommentsList(
            				(List<Long>)messageData.get(1), 
            				(List<Long>)messageData.get(2), 
            				getResources().getConfiguration().locale);
            		
            		moreDialog = new SelectionDialog(this, 
            				itemList, 
            				messageComments,
            				null, null, 
            				getResources().getString(R.string.me_moreDialog_deleteMessage));
            		moreDialog.setMessageCode(MEA_MESSAGE_MOREDIALOG_DELETE);
            		
    		    	if (moreDialog != null) moreDialog.show();
            	}
            	else if(mainMessage.equals("me_moreDialog_saveMessageFile"))
            	{
            		if (encryptedEditText != null && !encryptedEditText.getText().toString().equals(""))
            		{
            			messageAskDialog = ComponentProvider.getMessageSetFileNameDialog(this, encryptedEditText.getText().toString().trim(), lastLoadedFileName);
            			messageAskDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            			messageAskDialog.show();
            		}
            		else
            			ComponentProvider.getImageToast(
            					this.getResources().getString(R.string.me_emptyDecTextArea02), 
            					ImageToast.TOAST_IMAGE_CANCEL, this).show();
            	} 
            	else if (mainMessage.equals("me_moreDialog_loadMessageFile")) 
            	{
            		List<String> fileList = null;
            		
            		File dir = new File(Helpers.getImportExportDir().getAbsolutePath() + File.separator + getResources().getString(R.string.textsSubdir));   
            		String[] fileArray = dir.list(Helpers.getOnlyExtFilenameFilter(MEA_FILE_EXT));         		
            		if(fileArray != null) fileList = new ArrayList(Arrays.asList(fileArray));
            		else fileList = new ArrayList<String>();
            		
            		Collections.sort(fileList);
            		
            		List<String> fileComments = Helpers.getFileCommentsList(
            				fileList, 
            				dir.getAbsolutePath(), 
            				getResources().getConfiguration().locale, -1);
            		
            		List<Object> keys = new ArrayList<Object>(fileList.size());
            		for(int i = 0; i < fileList.size(); ++i) {
            			keys.add(fileList.get(i));
            		}
            		
            		List<Integer> itemType = new ArrayList<Integer>();
            		for(int i = 0; i < fileList.size(); ++i) {
            			itemType.add(SelectionDialog.ITEMTYPE_NORMAL);
            		}
            		
            		List<Integer> icons = new ArrayList<Integer>();
            			
            		
            		if (android.os.Build.VERSION.SDK_INT >= 19) {
	            		fileList.add(0, getResources().getString(R.string.common_browseForFile));
	            		fileComments.add(0, "(" + getResources().getString(R.string.common_openFilePicker) + ")");
	            		itemType.add(0, SelectionDialog.ITEMTYPE_HIGHLIGHTED);
	            		keys.add(0, "me_browseForFile_*?");  
	            		icons.add(R.drawable.diskdir);
            		}        		
            		
            		moreDialog = new SelectionDialog(this, 
            				fileList, 
            				fileComments, 
            				icons, 
            				keys, 
            				getResources().getString(R.string.me_moreDialog_loadMessageFile,
            				true));
            		moreDialog.setMessageCode(MEA_MESSAGE_MOREDIALOG_LOAD_FILE);
            		moreDialog.setItemTypes(itemType);
            		moreDialog.deferredInit();
            		
    		    	if (moreDialog != null) moreDialog.show();
            	}
            	else if (mainMessage.equals("me_moreDialog_deleteMessageFile")) 
            	{
            		List<String> fileList = null;
            		File dir = new File(Helpers.getImportExportDir().getAbsolutePath() + File.separator + getResources().getString(R.string.textsSubdir));   
            		String[] fileArray = dir.list(Helpers.getOnlyExtFilenameFilter(MEA_FILE_EXT));
            		if(fileArray != null ) fileList = Arrays.asList(fileArray);
            		
            		if (fileList == null || fileList.size() < 1)
            		{
            			ComponentProvider.getImageToast(
            					this.getResources().getString(R.string.common_noFiles_text), 
            					ImageToast.TOAST_IMAGE_CANCEL, this).show();
            			return;
            		}
            		Collections.sort(fileList);
            		
            		List<String> fileComments = Helpers.getFileCommentsList(
            				fileList, 
            				dir.getAbsolutePath(), 
            				getResources().getConfiguration().locale, -1);
            		
            		moreDialog = new SelectionDialog(this, 
            				fileList, 
            				fileComments, null, null, 
            				getResources().getString(R.string.me_moreDialog_deleteMessageFile));
            		moreDialog.setMessageCode(MEA_MESSAGE_MOREDIALOG_DELETE_FILE);
            		
    		    	if (moreDialog != null) moreDialog.show();
            	}
            	else if(mainMessage.equals("me_moreDialog_importSteganogram"))
            	{  
            		List<String> fileList = null;
            		String[] fileArray = steganogramsDir.list(Helpers.getOnlyExtFilenameFilter(MEA_STEGANOGRAM_EXT));         		
            		if(fileArray != null) fileList = new ArrayList(Arrays.asList(fileArray));
            		else fileList = new ArrayList<String>();
            		
            		Collections.sort(fileList);
            		
            		List<String> fileComments = Helpers.getFileCommentsList(
            				fileList, 
            				steganogramsDir.getAbsolutePath(), 
            				getResources().getConfiguration().locale, -1);
            		
            		List<Object> keys = new ArrayList<Object>(fileList.size());
            		for(int i = 0; i < fileList.size(); ++i) {
            			keys.add(fileList.get(i));
            		}
            		
            		List<Integer> itemType = new ArrayList<Integer>();
            		for(int i = 0; i < fileList.size(); ++i) {
            			itemType.add(SelectionDialog.ITEMTYPE_NORMAL);
            		}
            		
            		List<Integer> icons = new ArrayList<Integer>();
            			
            		if (android.os.Build.VERSION.SDK_INT >= 19) {
	            		fileList.add(0, getResources().getString(R.string.common_browseForFile));
	            		fileComments.add(0, "(" + getResources().getString(R.string.common_openFilePicker) + ")");
	            		itemType.add(0, SelectionDialog.ITEMTYPE_HIGHLIGHTED);
	            		keys.add(0, "me_browseForFile_*?"); 
	            		icons.add(R.drawable.diskdir);
            		}
            		
            		moreDialog = new SelectionDialog(this, 
            				fileList, 
            				fileComments, 
            				icons, 
            				keys, 
            				getResources().getString(R.string.me_moreDialog_importSteganogram),
            				true);
            		moreDialog.setMessageCode(MEA_MESSAGE_MOREDIALOG_DECRYPT_STEGANOGRAM);
            		moreDialog.setItemTypes(itemType);
            		moreDialog.deferredInit();
            		
    		    	if (moreDialog != null) moreDialog.show();
            	} 
            	else if(mainMessage.equals("me_moreDialog_exportSteganogram"))
            	{
    		    	String text = decryptedEditText.getText().toString().trim();
    		    	if (text.equals(""))
    		    	{
    		    		ComponentProvider.getImageToast(getResources().getString(R.string.me_emptyEncTextArea), ComponentProvider.DRAWABLE_ICON_CANCEL, this).show();
    			    	return;
    		    	}
            		
            		if(android.os.Build.VERSION.SDK_INT < 12) {
						Dialog enterFilePath = ComponentProvider.getTextSetDialog(this,
								getResources().getString(R.string.me_selectCarrierImage), 
								carrierImagesDir.getAbsolutePath(), 
								MEA_HANDLE_CARRIER_PATH, 
								null
								);
						enterFilePath.show();
            		}
            		else if (android.os.Build.VERSION.SDK_INT < 19) {
	            		Intent intent = new Intent(this, FilePickerActivity.class);
	                    intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
	                    intent.putExtra(FilePickerActivity.EXTRA_START_PATH, carrierImagesDir.getAbsolutePath());
	                    intent.putExtra(FilePickerActivity.EXTRA_EXTENSIONS_FILTER, "png;jpg;bmp;");
	                    intent.putExtra(FilePickerActivity.EXTRA_CUSTOM_TITLE, this.getResources().getString(R.string.me_selectCarrierImage));
	                    
	                    startActivityForResult(intent, MEA_HANDLE_CARRIER_PATH);
            		}
            		else {
            			ComponentProvider.getImageToast(
            					this.getResources().getString(R.string.me_selectCarrierImage), 
            					ImageToast.TOAST_IMAGE_INFO, this).show();
            			
            			Intent intent = new Intent();
						intent.setType("image/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						intent.setAction(Intent.ACTION_GET_CONTENT);
						
						startActivityForResult(intent, MEA_HANDLE_CARRIER_PATH);
            		}
            	} 
            	else if(mainMessage.equals("me_moreDialog_deleteSteganogram"))
            	{
            		List<String> fileList = null;
            		String[] fileArray = steganogramsDir.list(Helpers.getOnlyExtFilenameFilter(MEA_STEGANOGRAM_EXT));         		
            		if(fileArray != null ) fileList = Arrays.asList(fileArray);
            		
            		if (fileList == null || fileList.size() < 1)
            		{
            			ComponentProvider.getImageToast(
            					this.getResources().getString(R.string.common_noFiles_text) 
            						+ " (" + MEA_STEGANOGRAM_EXT + "): <br/>" + steganogramsDir.getAbsolutePath(), 
            					ImageToast.TOAST_IMAGE_CANCEL, this).show();
            			return;
            		}
            		Collections.sort(fileList);
            		
            		List<String> fileComments = Helpers.getFileCommentsList(
            				fileList, 
            				steganogramsDir.getAbsolutePath(), 
            				getResources().getConfiguration().locale, -1);
            		
            		moreDialog = new SelectionDialog(this, 
            				fileList, 
            				fileComments, null, null, 
            				getResources().getString(R.string.me_moreDialog_deleteSteganogram));
            		moreDialog.setMessageCode(MEA_MESSAGE_MOREDIALOG_DELETE_STEGANOGRAM);
            		
    		    	if (moreDialog != null) moreDialog.show();
            	} 
        	}
        	this.resetMessage();
        	break;
	            
	        case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_LOAD:       	
				try {
					currentEncTextUsed = true;
					List messageData = new ArrayList<Object>();
					encryptedEditText.setText(DBHelper.getMessage(am.getMainMessage(), messageData));
					lastLoadedFileName = am.getMainMessage();
					setStatusBarText(Helpers.capitalizeFirstLetter(getResources().getString(R.string.me_moreDialog_loadMessage).toLowerCase()) + " : " + am.getMainMessage() + " : OK");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
	        	this.resetMessage();
	            break;
	        
	        case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_LOAD_FILE:
				try {
					String mm = am.getMainMessage();
					
					if(mm.equals("me_browseForFile_*?")) {
            			Intent intent = new Intent();
						intent.setType("text/*");
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						intent.setAction(Intent.ACTION_GET_CONTENT);
						
						startActivityForResult(intent, MEA_HANDLE_BROWSE_FOR_TEXTFILE);
					}
					else {
						File file = new File(Helpers.getImportExportDir().getAbsolutePath() + File.separator + 
								getResources().getString(R.string.textsSubdir) + File.separator + mm);
						String text = Helpers.loadStringFromFile(file);
						currentEncTextUsed = true;
						encryptedEditText.setText(text);
						lastLoadedFileName = Helpers.removeExt(mm, MEA_FILE_EXT);
						setStatusBarText(Helpers.capitalizeFirstLetter(getResources().getString(R.string.me_moreDialog_loadMessageFile).toLowerCase()) + " : " + mm + " : OK");
					}
				} catch (Exception e) {
					ComponentProvider.getShowMessageDialog(this, "Error", e.getLocalizedMessage(), ComponentProvider.DRAWABLE_ICON_CANCEL);
					e.printStackTrace();
				}
	        	this.resetMessage();
	            break;
	            
	        case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DELETE:
				ComponentProvider.getBaseQuestionDialog(this,
						this.getResources().getString(R.string.me_moreDialog_deleteMessage_dialogTitle),
						this.getResources().getString(R.string.common_question_delete)
						.replaceAll("<1>", am.getMainMessage()), am.getMainMessage(), MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DELETE_CONFIRM
						).show();
	        	this.resetMessage();
	            break;
	            
	        case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DELETE_CONFIRM:
				if(am.getAttachement().equals(new Integer(1)))
				{
					DBHelper.deleteMessage(am.getMainMessage());
					ComponentProvider.getImageToast(this.getResources().getString
							(R.string.common_question_delete_confirm), ImageToast.TOAST_IMAGE_OK, this
							).show();
					setStatusBarText(Helpers.capitalizeFirstLetter(getResources().getString(R.string.me_moreDialog_deleteMessage).toLowerCase()) + " : " + am.getMainMessage() + " : OK");
				}	        	
	        	this.resetMessage();
	            break;
	            
	        case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DELETE_FILE:
	        {
				File file = new File(Helpers.getImportExportDir().getAbsolutePath() + File.separator + 
						getResources().getString(R.string.textsSubdir) + File.separator + am.getMainMessage());
	        	
	        	ComponentProvider.getBaseQuestionDialog(this,
						this.getResources().getString(R.string.me_moreDialog_deleteMessage_dialogTitle),
						this.getResources().getString(R.string.common_question_delete)
						.replaceAll("<1>", file.getName()), file.getName(), MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DELETE_CONFIRM_FILE,
						file).show();
	        }
	        this.resetMessage();
	        break;
	            
	        case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DELETE_STEGANOGRAM:
	        {
				File file = new File(steganogramsDir + File.separator + am.getMainMessage());
	        	
	        	ComponentProvider.getBaseQuestionDialog(this,
						this.getResources().getString(R.string.fe_deleteFile_dialogTitle),
						this.getResources().getString(R.string.common_question_delete)
						.replaceAll("<1>", file.getName()), file.getName(), MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DELETE_CONFIRM_FILE,
						file).show();
	        }
	        this.resetMessage();
	        break;
	            
	        case MessageEncActivity.MEA_MESSAGE_MOREDIALOG_DELETE_CONFIRM_FILE:
	        {	
				if(am.getAttachement().equals(new Integer(1)))
				{
					File fileForDelete = (File)am.getAttachement2();
					boolean deleted = fileForDelete.delete();
					if(deleted)
					{
						ComponentProvider.getImageToast(this.getResources().getString
								(R.string.common_question_delete_confirm), ImageToast.TOAST_IMAGE_OK, this
								).show();
						setStatusBarText(Helpers.capitalizeFirstLetter(getResources().getString(R.string.me_moreDialog_deleteMessageFile).toLowerCase()) + " : " + fileForDelete.getName() + " : OK");
					}
					else
					{
						ComponentProvider.getImageToast(this.getResources().getString
								(R.string.common_fileFailedToDelete).replaceAll("<1>", fileForDelete.getName()), ImageToast.TOAST_IMAGE_CANCEL, this
								).show();
					}
				}	
	        }
	        this.resetMessage();
	        break;
	            
        	case COMMON_MESSAGE_CONFIRM_EXIT:
				if(am.getAttachement() == null || am.getAttachement().equals(new Integer(1)))
				{
		    		setRunningCode(0);
		    		String mm = am.getMainMessage();
		    		if(mm != null) {
			            setResult(Integer.parseInt(mm));
			    	}
		    		finish();
				}
        		break;
        		
        	case EXIT_CASCADE:
        	{	
		    	setRunningCode(0);
		    	setResult(EXIT_CASCADE);
		    	finish();
			}
        	break;

            default: 
            	break;
        }
    }
    
    private void setStatusBarText(String text)
    {
    	statusBarTV.setText(Helpers.getFormatedTime(System.currentTimeMillis(), getResources().getConfiguration().locale) + " | " + text);
    }
    
    private boolean checkChange()
    {
    	if(changeResolver.checkChange(decryptedEditText.getText().toString(), encryptedEditText.getText().toString())) {
    		setEncTextColorRed();
    		return true;
    	}
    	else {
    		setEncTextColorBlackOrBlue();
    		return false;
    	}
	}		

    private void setEncTextColorRed()
    {
    	encryptedEditText.setTextColor(Color.RED);
    }
    
    private void setEncTextColorBlackOrBlue()
    {
    	if(currentEncTextUsed)
    		encryptedEditText.setTextColor(Color.BLACK);
    	else
    		encryptedEditText.setTextColor(Color.BLUE);
    }
    
    private void decryptSteganogram(String stegName, CryptFileWrapper stegCFW)
    {
		final CryptFileWrapper stegFile = stegCFW != null ? stegCFW : new CryptFileWrapper(new CryptFile(steganogramsDir + File.separator + stegName));

		waitDialog = new SimpleWaitDialog(this);
		new Thread (new Runnable() 
		{
			public void run() 
			{
				PowerManager.WakeLock wakeLock;
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ME_DECRYPT_STEGANOGRAM");
				
				wakeLock.acquire();
				universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_SHOW_WAITDIALOG));
				
				//+ Process
				try {
					String outputText = encryptor.importTextFromSteganogram(stegFile);
					
        			List<String> outputList = new ArrayList<String>();
        			outputList.add(outputText);
        			outputList.add(stegFile.getName());
        			outputList.add(encryptor.getDecryptAlgorithmComment());
					
					universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_STEGANOGRAM_DECRYPT_SETOUTPUT, outputList));
					
				} catch (IllegalStateException e) {
					universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG, 
							getResources().getString(R.string.common_invalidPasswordOrSteganogram)));
				} catch (ArrayIndexOutOfBoundsException e) {
					universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG, 
							getResources().getString(R.string.common_invalidPasswordOrSteganogram)));
				} catch (Exception e) {
					universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG, 
							Helpers.getShortenedStackTrace(e, 1)));
					e.printStackTrace();
				} catch (Throwable t) {
					universalHandler.sendMessage(Message.obtain(universalHandler, 
							MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG, 
							getResources().getString(R.string.common_insufficientMemory) + " (Java Max Heap: " + Helpers.getMaxHeapSizeInMB() + " MB)<br/><small>" + getResources().getString(R.string.me_steganogramInsufficientMemoryWhatToDo) + "</small>"));
				} finally {
					System.gc();
				}
        		//- Process
				
    			universalHandler.sendMessage(Message.obtain(universalHandler, MEA_UNIVERSALHANDLER_HIDE_WAITDIALOG));
    			wakeLock.release();
			}
		}).start();
    }
    
    private void decryptText(String text)
    {
    	Toast toast;
    	try {
    		decryptedEditText.setText(encryptor.decryptString(text));
			decryptedEditText.setSelection(decryptedEditText.length());
			changeResolver.setLastProcessed(decryptedEditText.getText().toString(), encryptedEditText.getText().toString());
			setEncTextColorBlackOrBlue();
			setStatusBarText(getResources().getString(R.string.common_decryption_text) + " : " + encryptor.getDecryptAlgorithmComment() + " : OK");
		} catch (DataFormatException e) {
	    	toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.me_decryptChecksumError), this);
	    	toast.show();
	    	setStatusBarText(getResources().getString(R.string.common_decryption_text) + " : " + getResources().getString(R.string.me_decryptChecksumError));
		} catch (InvalidCipherTextException e) {
	    	toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.me_decryptChecksumError), this);
	    	toast.show();
	    	setStatusBarText(getResources().getString(R.string.common_decryption_text) + " : " + getResources().getString(R.string.me_decryptChecksumError));
		} catch (NoSuchAlgorithmException e) {
			toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.common_unknownAlgorithm_text), this);
			toast.show();
			setStatusBarText(getResources().getString(R.string.common_decryption_text) + " : " + getResources().getString(R.string.common_unknownAlgorithm_text));
		} catch (Exception e) {
	    	toast = ComponentProvider.getImageToastKO(getResources().getString(R.string.me_decryptError), this);
	    	toast.show();
	    	setStatusBarText(getResources().getString(R.string.common_decryption_text) + " : " + getResources().getString(R.string.me_decryptError));
		}
    }

    @Override
    protected void onStart ()
    {
        setRunningCode(RUNNING_MESSAGEENCACTIVITY);
    	super.onStart();
    }
    
    @Override
    public void onBackPressed()
    {
		if(askOnLeave)
		{
    		ComponentProvider.getBaseQuestionDialog(this, 
					getResources().getString(R.string.common_returnToMainMenuTitle),  
    				getResources().getString(R.string.common_question_leave)
    				.replaceAll("<1>", getResources().getString(R.string.common_app_messageEncryptor_name)), 
    				null, 
    				COMMON_MESSAGE_CONFIRM_EXIT
    				).show();
		}
		else setMessage(new ActivityMessage(COMMON_MESSAGE_CONFIRM_EXIT, null));
    }
    
    @Override
    public void onWindowFocusChanged(boolean b)
    {
    	if(this.encryptor == null)
    	{
    		setRunningCode(0);
    		this.finish();
    	}
    	super.onWindowFocusChanged(b);
    }
    
    /** Lock Screen */
    private void showScreenLockDialog()
    {
    	if(predefinedScreenLockKey == null)
    		sld = new ScreenLockDialog(this, encryptor.getKeyHash());
    	else
    		sld = new ScreenLockDialog(this, predefinedScreenLockKey);
        sld.show();
    }
    
    @Override
    protected void onPause() 
    {
        super.onPause();
        
        if(sld != null && sld.getActiveFlag()) return;
        
        if(!Helpers.isScreenOn(this) && lockOnPause && encryptor != null)
        {
        	doOnLock();
        	showScreenLockDialog();
        }
    }
	
    @Override
    protected void onResume()
    {
        super.onResume();
        
        if(sldVeto) {
        	sldVeto = false;
        	return;
        }
        
        if(sld != null && sld.getActiveFlag()) return;

        if(lockOnPause && encryptor != null)
        {
        	doOnLock();
        	showScreenLockDialog();
        }
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {		
		if (requestCode == MEA_HANDLE_CARRIER_PATH) 
		{
			sldVeto = true;
			if(resultCode == RESULT_OK)
			{
				if(android.os.Build.VERSION.SDK_INT < 19) {
					String selectedFilePath = new File(resultData.getData().getPath()).getAbsolutePath();
					this.setMessage(new ActivityMessage(MEA_HANDLE_CARRIER_PATH, null, new CryptFileWrapper(new CryptFile(selectedFilePath))));
				}
				else {
					final List<Uri> uris = Helpers.extractUriFromIntent(resultData);
					
					if(uris.size() > 0) 
					{
						try {
							String selectedFilePath = Helpers.getRealPathFromUriExtended(this, uris.get(0));
							if(selectedFilePath != null) 
							{
								CryptFile testFile = new CryptFile(selectedFilePath);
								if(testFile.exists() && testFile.isFile() && testFile.length() > 0) 
								{
									this.setMessage(new ActivityMessage(MEA_HANDLE_CARRIER_PATH, null, new CryptFileWrapper(testFile)));
									return;
								}
							}
						} catch (Exception e) {
							//e.printStackTrace();
						}
						
						try {
							DocumentFile dFile = DocumentFile.fromSingleUri(this, uris.get(0));
							if(dFile.exists() && dFile.isFile()) {
								this.setMessage(new ActivityMessage(MEA_HANDLE_CARRIER_PATH, null, new CryptFileWrapper(dFile, this)));
							}
							else {
								throw new IllegalStateException();
							}
						} catch (Exception e) {
							this.setMessage(new ActivityMessage(MEA_HANDLE_CARRIER_PATH, null, null));
						}
					}
				}
			}		
		}
		else if (requestCode == MEA_HANDLE_BROWSE_FOR_TEXTFILE) 
		{
			sldVeto = true;
			if(resultCode == RESULT_OK)
			{
				CryptFileWrapper chosenFile = Helpers.getCFWFromChooserIntent(this, resultData);
				if(chosenFile != null) {

					try {
						String text = Helpers.loadStringFromFile(chosenFile);
						currentEncTextUsed = true;
						encryptedEditText.setText(text);
						setStatusBarText(Helpers.capitalizeFirstLetter(getResources().getString(R.string.me_moreDialog_loadMessageFile).toLowerCase()) + " : " + chosenFile.getName() + " : OK");
					}  catch (Exception e) {
						showErrorDialog(getResources().getString(R.string.common_invalidFile));
					}
				}
				else {
					showErrorDialog(getResources().getString(R.string.common_invalidPath_text));
				}
			}
		}
		else if (requestCode == MEA_HANDLE_BROWSE_FOR_STEGANOGRAM) 
		{
			sldVeto = true;
			if(resultCode == RESULT_OK)
			{
				CryptFileWrapper chosenFile = Helpers.getCFWFromChooserIntent(this, resultData);
				if(chosenFile != null) {
					decryptSteganogram(null, chosenFile);
				}
				else {
					showErrorDialog(getResources().getString(R.string.common_invalidPath_text));
				}
			}
		}
	}
    
    //+ Lockable    
	public void doOnLock() {
		rootLayout.setVisibility(ViewGroup.GONE);
	}
	
	public void doOnUnlock() {
		rootLayout.setVisibility(ViewGroup.VISIBLE);
	}   
    //- Lockable
    
    @SuppressWarnings("deprecation")
	private void setToSystemClipboard(String text)
    {
    	ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    	ClipMan.setText(text);
    }
    
    @SuppressWarnings("deprecation")
	private String getFromSystemClipboard()
    {
    	ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    	String text = null;
    	try {
			text = ClipMan.getText().toString().trim();
		} catch (Exception e) {
			// Empty clipboard (Android 3.0+)
		}
    	if(text == null) text = "";
    	return text;
    }
    
    // Handler for miscellaneous background activities
    Handler universalHandler = new Handler() 
    {
        public void handleMessage(Message msg)  
        {    	
        	if (msg.what == MEA_UNIVERSALHANDLER_SHOW_WAITDIALOG)
        	{ 
        		if(waitDialog != null) waitDialog.show();
        		return;
        	}
        	if (msg.what == MEA_UNIVERSALHANDLER_HIDE_WAITDIALOG)
        	{ 
        		if(waitDialog != null) waitDialog.cancel();     		
        		return;
        	}
        	if (msg.what == MEA_UNIVERSALHANDLER_SHOW_ERRORDIALOG)
        	{ 
        		showErrorDialog((String)msg.obj);    		
        		return;
        	}
        	if (msg.what == MEA_UNIVERSALHANDLER_STEGANOGRAM_DECRYPT_SETOUTPUT)
        	{ 
        		List outputReport = (List)msg.obj; 
        		String iFileName = (String)outputReport.get(1);
        		String decAlgComment = (String)outputReport.get(2);
        		
        		decryptedEditText.setText((String)outputReport.get(0));
        		
        		encryptedEditText.setText("");
        		setStatusBarText(getResources().getString(R.string.common_decryption_text) + " : " + decAlgComment + " + F5 : " + iFileName + " : OK");	
        		
        		return;
        	}
        	if (msg.what == MEA_UNIVERSALHANDLER_STEGANOGRAM_MAKE_OUTPUTREPORT)
        	{ 
        		List outputReport = (List)msg.obj; 
        		File of = new File((String)outputReport.get(0));
        		String encAlgComment = (String)outputReport.get(1);
        		
        		setStatusBarText(getResources().getString(R.string.common_encryption_text) + " : " + encAlgComment + " + F5 : " + of.getName() + " : OK");	
        		
    			StringBuffer report = new StringBuffer();
    			report.append(getResources().getString(R.string.common_algorithm_text) + ": <b>" + encAlgComment + " + F5</b><br/><br/>");
    			report.append(getResources().getString(R.string.fe_report_outputFile).replaceAll("<1>", of.getName()) + "<br/>");
    			report.append("<small>(" + of.getParentFile().getAbsolutePath() + ")</small>");
        		
        		ComponentProvider.getShowMessageDialog(MessageEncActivity.this, 
    					getResources().getString(R.string.fe_report_enc_title), 
    					report.toString(), 
    					ComponentProvider.DRAWABLE_ICON_OK).show();
        		
        		return;
        	}
        }
    };
}
