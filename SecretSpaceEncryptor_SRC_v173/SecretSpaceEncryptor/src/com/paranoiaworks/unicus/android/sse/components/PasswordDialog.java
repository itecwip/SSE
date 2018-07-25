package com.paranoiaworks.unicus.android.sse.components;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.dao.PasswordAttributes;
import com.paranoiaworks.unicus.android.sse.dao.SettingDataHolder;
import com.paranoiaworks.unicus.android.sse.misc.ProgressBarToken;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor;
import com.paranoiaworks.unicus.android.sse.utils.SSElog;

/**
 * Common Password Dialog for SSE
 * (enter password, set password, change password implemented)
 * 
 * @author Paranoia Works
 * @version 1.0.12
 * @related PasswordAttributes.java (strengthMeasure)
 */
public class PasswordDialog extends SecureDialog {
	
	private Activity context;
	private int dialogMode = -1;
	
	private EditText passwordEditText1;
	private EditText passwordEditText2;
	private EditText passwordOldPassword;
	private ImageView strengthMeasure;
	private TextView pet;
	private CheckBox passCB;
	private Button okButton;
	private Button cancelButton;
	
	private String currentKeyHash;
	private int currentAlgorithmCode = -1;
	private int encryptAlgorithmCode = 0;
	private String parentMessage;
	private Dialog waitDialog;
	private Dialog deferredDialog;
	private ProgressBarToken progressBarToken;
	private boolean hideWaitDialogOnFinish = true;
	private boolean exiting = false;
	private boolean unicodeAllowed = false;
	private boolean blockCancellation = false;
	private boolean buttonsBlock = false;
	private String predefinedPassword;
	private Object attachment;
	
	public final static int PD_MODE_ENTER_PASSWORD = 1;
	public final static int PD_MODE_SET_PASSWORD = 2;
	public final static int PD_MODE_CHANGE_PASSWORD = 3;
	
	public final static int PD_HANDLER_TOASTMESSAGE_OK = -1001;
	public final static int PD_HANDLER_TOASTMESSAGE_KO = -1002;
	public final static int PD_HANDLER_UNICODE_ENABLE = -2001;
	
	public PasswordDialog(View v, int dialogMode) 
	{
		this((Activity)v.getContext(), dialogMode);
	}	
	
	public PasswordDialog(Activity context, int dialogMode) 
	{
		super(context);
		this.context = context;
		this.dialogMode = dialogMode;
		this.init();
	}
	
	public PasswordDialog(View v, int dialogMode, String predefinedPassword) 
	{
		this((Activity)v.getContext(), dialogMode, predefinedPassword);
	}	
	
	public PasswordDialog(Activity context, int dialogMode, String predefinedPassword) 
	{
		super(context);
		this.context = context;
		this.dialogMode = dialogMode;
		this.predefinedPassword = predefinedPassword.trim();
		this.init();
	}
	
	@Override
	public void show()
	{
		super.show();
		if(deferredDialog != null) deferredDialog.show();
	}
	
	public void setEncryptAlgorithmCode(int encryptAlgorithmCode)
	{
		this.encryptAlgorithmCode = encryptAlgorithmCode;
	}
	
	public void setCurrentDecryptSpec(String hash, int currentAlgorithmCode)
	{
		this.currentKeyHash = hash;
		this.currentAlgorithmCode = currentAlgorithmCode;
	}
	
	public void setParentMessage(String parentMessage)
	{
		this.parentMessage = parentMessage;
	}
	
	public void setAttachment(Object attachment)
	{
		this.attachment = attachment;
	}
	
	// if processing password could take more time
	public void setWaitDialog(Dialog waitDialog, boolean hideWaitDialogOnFinish)
	{
		this.waitDialog = waitDialog;
		this.hideWaitDialogOnFinish = hideWaitDialogOnFinish;
	}
	
	public void setWaitDialog(ProgressBarToken progressBarToken, boolean hideWaitDialogOnFinish)
	{
		this.progressBarToken = progressBarToken;
		this.waitDialog = progressBarToken.getDialog();
		this.hideWaitDialogOnFinish = hideWaitDialogOnFinish;
	}
	
	public void setBlockCancellation(boolean block)
	{
		this.blockCancellation = block;
		this.setCancelable(!block);
	}
	
	public int getDialogMode()
	{
		return dialogMode;
	}
	
	public void setCustomTitle(String customTitle)
	{
		this.setTitle(customTitle);
	}
	
	
	private void init()
	{		
		this.setContentView(R.layout.lc_password_dialog);
		this.setCanceledOnTouchOutside(false);
		unicodeAllowed = SettingDataHolder.getInstance().getItemAsBoolean("SC_Common", "SI_AllowUnicodePasswords");

    	passwordEditText1 = (EditText)this.findViewById(R.id.PD_passwordEditText1);
    	passwordEditText2 = (EditText)this.findViewById(R.id.PD_passwordEditText2);
    	passwordOldPassword = (EditText)this.findViewById(R.id.PD_passwordDialog_OldPassword);    	
    	strengthMeasure = (ImageView)this.findViewById(R.id.PD_strengthView);
        pet = (TextView)this.findViewById(R.id.PD_strengthText);
    	cancelButton = (Button)this.findViewById(R.id.PD_cancelButton);
    	okButton = (Button)this.findViewById(R.id.PD_okButton);
    	passCB = (CheckBox)this.findViewById(R.id.PD_passwordCheckBox);
    	
    	//passwordEditText1.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_CLASS_TEXT); //like android:password="true"
    	passwordEditText1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	passwordEditText1.setTransformationMethod(new PasswordTransformationMethod());
    	passwordEditText2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	passwordEditText2.setTransformationMethod(new PasswordTransformationMethod());
    	passwordOldPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	passwordOldPassword.setTransformationMethod(new PasswordTransformationMethod());
    	
    	if(!unicodeAllowed)
    	{
    		passwordEditText1.setFilters(new InputFilter[] { filter });
    		passwordEditText2.setFilters(new InputFilter[] { filter });
    		passwordOldPassword.setFilters(new InputFilter[] { filter });
    	}
    	else
    	{
    		strengthMeasure.setVisibility(ImageView.GONE);
    		pet.setVisibility(TextView.GONE);
    	}
    	
    	// prepare layout for mode
    	switch (dialogMode) 
        {        
        	case PD_MODE_ENTER_PASSWORD:
        	{
        		passwordEditText1.setHint("");
        		passwordEditText2.setVisibility(EditText.GONE);
        		passwordOldPassword.setVisibility(EditText.GONE);
        		strengthMeasure.setVisibility(ImageView.GONE);
        		pet.setVisibility(TextView.GONE);
        		this.setTitle(context.getResources().getString(R.string.passwordDialog_title_enter));
        		break;
        	}    		
        	case PD_MODE_SET_PASSWORD:
        	{
        		passwordOldPassword.setVisibility(EditText.GONE);
        		this.setTitle(context.getResources().getString(R.string.passwordDialog_title_set));  
        		break;
        	}
        	case PD_MODE_CHANGE_PASSWORD:
        	{
        		this.setTitle(context.getResources().getString(R.string.passwordDialog_title_change));
        		passwordEditText1.setHint(context.getResources().getString(R.string.passwordDialog_newPasswordHint)); 
        		break;
        	}
        	default:
        		throw new IllegalArgumentException("unknown mode");
        }
    	   	  	
    	if (dialogMode != PD_MODE_ENTER_PASSWORD)
    	{
	    	passwordEditText1.addTextChangedListener((new TextWatcher()
	    	{
	            public void  afterTextChanged (Editable s)
	            {
	            	if(!unicodeAllowed && !passwordEditText1.getText().toString().matches("\\A\\p{ASCII}*\\z")) {
	            		Resources r = context.getResources();
	            		ComponentProvider.getShowMessageDialog(context, 
	            				r.getString(R.string.passwordDialog_title_incorrectCharacter), 
	            				r.getString(R.string.passwordDialog_title_incorrectCharacter) + ":<br/>" + r.getString(R.string.passwordDialog_doNotPastePassword), 
	            				ComponentProvider.DRAWABLE_ICON_CANCEL).show();
	            	}
	            }
	            public void  beforeTextChanged  (CharSequence s, int start, int count, int after)
	            {
	            }
	            public void onTextChanged  (CharSequence s, int start, int before, int count) 
	            {
	                strengthMeasure.setImageResource(PasswordAttributes.getSMImageID(PasswordAttributes.checkPasswordStrengthWeight(s.toString())));
	                pet.setText(PasswordAttributes.getCommentID(PasswordAttributes.checkPasswordStrengthWeight(s.toString())));            
	            	if(passCB.isChecked()) passwordEditText2.setText(passwordEditText1.getText());
	            }
	    	}));
    	}

    	
    	// OK Button
    	okButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	if(buttonsBlock) return;
		    	buttonsBlock = true;
		    	
		    	Thread executor = new Thread (new Runnable() {
		            public void run() {
		            	ActivityMessage am = null;
		            	try {
		            		am = okButtonExecute();
		            		if (am == null)
		            		{
		            			okButtonExecuteHandler.sendMessage(Message.obtain(okButtonExecuteHandler, -201, null));
		            			return;
		            		}
						} catch (Exception e) {
							e.printStackTrace();
							okButtonExecuteHandler.sendMessage(Message.obtain(okButtonExecuteHandler, -400, e));
						}
						okButtonExecuteHandler.sendMessage(Message.obtain(okButtonExecuteHandler, -100, am));
		            }
		    	});
		    	executor.start();
		    }
	    });
    	
    	
    	// Cancel Button
    	cancelButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	if(!blockCancellation) cancel();
		    	else {
	        		Toast tt = new ImageToast(context.getResources().getString(R.string.passwordDialog_cannotCancel), ImageToast.TOAST_IMAGE_CANCEL, context);
	        		tt.setDuration(Toast.LENGTH_SHORT);
	        		tt.show();
		    	}
		    }
	    });

    	
    	// CheckBox Show Password
    	passCB.setText("  " + context.getResources().getString(R.string.passwordDialog_showPassword));
    	passCB.setOnCheckedChangeListener(new OnCheckedChangeListener()
    	{
    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    	    {
    	        if (isChecked)
    	        {
    	        	passwordEditText2.setFocusable(false);
    	        	passwordEditText2.setFocusableInTouchMode(false);
    	        	passwordEditText2.setEnabled(false);
    	        	passwordEditText1.setTransformationMethod(null);
    	        	passwordOldPassword.setTransformationMethod(null);
    	        	passwordEditText2.setText(passwordEditText1.getText().toString());
    	        	if(passwordEditText1.length() > 0) passwordEditText1.setSelection(passwordEditText1.length());
    	        	if(passwordOldPassword.length() > 0) passwordOldPassword.setSelection(passwordOldPassword.length());
    	        } else {
    	        	passwordEditText2.setFocusable(true);
    	        	passwordEditText2.setFocusableInTouchMode(true);
    	        	passwordEditText2.setEnabled(true);
    	        	passwordEditText1.setTransformationMethod(new PasswordTransformationMethod());
    	        	passwordEditText1.setSelection(passwordEditText1.length());
    	        	passwordOldPassword.setTransformationMethod(new PasswordTransformationMethod());
    	        	passwordOldPassword.setSelection(passwordOldPassword.length());
    	        }

    	    }
    	});
    	
    	if(predefinedPassword != null) {
    		passwordEditText1.setText(predefinedPassword);
    		passCB.setChecked(true); 		
    	}
	}
	
	/** OK Button - process required action */
	private ActivityMessage okButtonExecute() throws Exception
	{
		String P1 = passwordEditText1.getText().toString().trim();
		String currentPassword = passwordOldPassword.getText().toString().trim();
		if (P1.equals("") || (dialogMode == PD_MODE_CHANGE_PASSWORD && currentPassword.equals("")))
		{
			okButtonExecuteHandler.sendMessage(Message.obtain(
					okButtonExecuteHandler, 
					PD_HANDLER_TOASTMESSAGE_KO, 
					R.string.passwordDialog_noPassword));
			return null;
		}
		if (dialogMode == PD_MODE_CHANGE_PASSWORD && P1.equals(currentPassword))
		{
			okButtonExecuteHandler.sendMessage(Message.obtain(
					okButtonExecuteHandler, 
					PD_HANDLER_TOASTMESSAGE_KO, 
					R.string.passwordDialog_newPasswordSameAsCurrent));
			return null;
		}
		if (dialogMode != PD_MODE_ENTER_PASSWORD && !P1.equals(passwordEditText2.getText().toString().trim()))
		{
			okButtonExecuteHandler.sendMessage(Message.obtain(
					okButtonExecuteHandler, 
					PD_HANDLER_TOASTMESSAGE_KO, 
					R.string.passwordDialog_passwordNotMatch));
			return null;
		}	
		
		okButtonExecuteHandler.sendMessage(Message.obtain(okButtonExecuteHandler, -200, null));
		if (dialogMode == PD_MODE_CHANGE_PASSWORD)
		{
			if(currentKeyHash == null || currentAlgorithmCode == -1) 
				throw new IllegalArgumentException("change password mode needs currentKeyHash");
			String testKeyHash = (new Encryptor(currentPassword, currentAlgorithmCode, unicodeAllowed)).getKeyHash();
			
			if(!currentKeyHash.equals(testKeyHash))
			{
    			okButtonExecuteHandler.sendMessage(Message.obtain(
    					okButtonExecuteHandler, 
    					PD_HANDLER_TOASTMESSAGE_KO, 
    					R.string.passwordDialog_invalidCurrentPassword));
    			return null;
			}
		}

		final List returnList = new ArrayList();
		returnList.add(new PasswordAttributes(P1));
		returnList.add(new Encryptor(P1, encryptAlgorithmCode, unicodeAllowed));
		
		return new ActivityMessage(CryptActivity.COMMON_MESSAGE_SET_ENCRYPTOR, parentMessage, returnList, attachment);
	}
	
	Handler miscHandler = new Handler() 
    {
        public void handleMessage(Message msg)  
        {
        	if (msg.what == PD_HANDLER_UNICODE_ENABLE)
        	{ 
        		ActivityMessage am = (ActivityMessage)msg.obj;
        		
        		if(am.getAttachement().equals(new Integer(1)))
        		{
        			passwordEditText1.setText("");
        			passwordEditText2.setText("");
        			passwordOldPassword.setText("");
        			SettingDataHolder sdh = SettingDataHolder.getInstance();
        			sdh.addOrReplaceItem("SC_Common", "SI_AllowUnicodePasswords", "true");
        			sdh.save();
        			unicodeAllowed = true; 
        			passwordEditText1.setFilters( new InputFilter[] { new InputFilter.LengthFilter(1024) } );
        			passwordEditText2.setFilters( new InputFilter[] { new InputFilter.LengthFilter(1024) } );
        			passwordOldPassword.setFilters( new InputFilter[] { new InputFilter.LengthFilter(1024) } );      			     			
        		}
        	}   	
        }
    };
	
    Handler okButtonExecuteHandler = new Handler() 
    {
        public void handleMessage(Message msg)  
        {
        	if (msg.what == PD_HANDLER_TOASTMESSAGE_KO)
        	{ 
        		Toast tt = new ImageToast(context.getResources().getString((Integer)msg.obj), ImageToast.TOAST_IMAGE_CANCEL, context);
        		tt.setDuration(Toast.LENGTH_SHORT);
        		tt.show();
        		return;
        	}
        	if (msg.what == -200)
        	{            	
        		if (progressBarToken != null) progressBarToken.getProgressHandler().sendMessage(
        				Message.obtain(progressBarToken.getProgressHandler(), -1000, null));
        		showWaitDialogIfExists();
        		return;
        	}
        	if (msg.what == -201)
        	{            	
        		hideWaitDialogIfExists();
        		buttonsBlock = false;
        		return;
        	}
        	if (msg.what == -100)
        	{            	
        		exiting = true;
        		CryptActivity ca = (CryptActivity)context;
        		ca.setMessage((ActivityMessage)msg.obj);
        		if(hideWaitDialogOnFinish)
        		{
        			hideWaitDialogIfExists();
        		}
        		buttonsBlock = false;
        		cancel();
        		return;
        	}
        	if (msg.what == -400)
        	{     		
        		Exception e;
        		e = (Exception)msg.obj;
        		Toast tt = new ImageToast(e.getMessage(), ImageToast.TOAST_IMAGE_CANCEL, context);
        		tt.setDuration(Toast.LENGTH_SHORT);
        		tt.show();
        		e.printStackTrace();
        		enableAllComponent();
        		hideWaitDialogIfExists();
        		buttonsBlock = false;
        	}
        }
    };
	
	// Only ASCII 32...126 allowed
    InputFilter filter = new InputFilter()
	{
	    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) 
	    { 
	    	if (source.length() < 1) return null;
	    	char last = source.charAt(source.length() - 1);
        	if(last > 126 || last < 32) 
        	{
        		deferredDialog = ComponentProvider.getBaseQuestionDialog(context, miscHandler,
						context.getResources().getString(R.string.passwordDialog_title_incorrectCharacter), 
						context.getResources().getString(R.string.passwordDialog_incorrectCharacterReport),
						"",
						PD_HANDLER_UNICODE_ENABLE,
						null
    			);
        		
				if(PasswordDialog.this.isShowing()) deferredDialog.show();
				if(predefinedPassword != null) return "";
        		return source.subSequence(0, source.length() - 1);
        	}
        	return null;
	    }  
	};
	
	private void showWaitDialogIfExists()
	{
		if(waitDialog != null) waitDialog.show();
		else disableAllComponent();
	}
	
	private void hideWaitDialogIfExists()
	{
		if(waitDialog != null) waitDialog.cancel();
		else if (!exiting) enableAllComponent();
	}
	
	private void enableAllComponent()
	{
		this.setCancelable(true);
    	cancelButton.setEnabled(true);
    	okButton.setEnabled(true);
	}
	
	private void disableAllComponent()
	{
    	this.setCancelable(false);
    	cancelButton.setEnabled(false);
    	okButton.setEnabled(false);
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
            return true;
        } else return super.onKeyDown(keyCode, event);
    }
}
