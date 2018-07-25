package com.paranoiaworks.unicus.android.sse.components;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.paranoiaworks.unicus.android.sse.R;

/**
 * Common Password Dialog
 * (enter password, set password)
 *
 * @author Paranoia Works
 * @version 1.0.1
 */
public class SimplePasswordDialog extends SecureDialog {
	
	private Activity context;
	private int dialogMode = -1;
	
	private EditText passwordEditText1;
	private EditText passwordEditText2;
	private EditText passwordOldPassword;
	private CheckBox passCB;
	private Button okButton;
	private Button cancelButton;
	private Object tag;
	private Handler handler;

	private boolean blockCancellation = false;
	private boolean buttonsBlock = false;
	private boolean hideOnly = false;
	
	public final static int SPD_MODE_ENTER_PASSWORD = 1;
	public final static int SPD_MODE_SET_PASSWORD = 2;
	public final static int SPD_HANDLER_OK = 100;
	public final static int SPD_HANDLER_CANCEL = 400;
	
	public SimplePasswordDialog(View v, int dialogMode, Handler handler) 
	{
		this((Activity)v.getContext(), dialogMode, handler);
	}	
	
	public SimplePasswordDialog(Activity context, int dialogMode, Handler handler) 
	{
		super(context);
		this.context = context;
		this.dialogMode = dialogMode;
		this.handler = handler;
		this.init();
	}
	
	public void setCustomTitle(String customTitle)
	{
		this.setTitle(customTitle);
	}	
	
	public void setHideOnly(boolean hideOnly)
	{
		this.hideOnly = hideOnly;
	}	
	
	public void setTag(Object tag)
	{
		this.tag = tag;
	}	
	
	public Object getTag()
	{
		return this.tag;
	}	
	
	private void init()
	{		
		this.setContentView(R.layout.lc_simple_password_dialog);
		this.setCanceledOnTouchOutside(false);

    	passwordEditText1 = (EditText)this.findViewById(R.id.SPD_passwordEditText1);
    	passwordEditText2 = (EditText)this.findViewById(R.id.SPD_passwordEditText2);
    	passwordOldPassword = (EditText)this.findViewById(R.id.SPD_passwordDialog_OldPassword);    	
    	cancelButton = (Button)this.findViewById(R.id.SPD_cancelButton);
    	okButton = (Button)this.findViewById(R.id.SPD_okButton);
    	passCB = (CheckBox)this.findViewById(R.id.SPD_passwordCheckBox);
    	
    	passwordEditText1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	passwordEditText1.setTransformationMethod(new PasswordTransformationMethod());
    	passwordEditText2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	passwordEditText2.setTransformationMethod(new PasswordTransformationMethod());
    	passwordOldPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	passwordOldPassword.setTransformationMethod(new PasswordTransformationMethod());

    	// prepare layout for mode
    	switch (dialogMode) 
        {        
        	case SPD_MODE_ENTER_PASSWORD:
        	{
        		passwordEditText1.setHint("");
        		passwordEditText2.setVisibility(EditText.GONE);
        		passwordOldPassword.setVisibility(EditText.GONE);
        		this.setTitle(context.getResources().getString(R.string.passwordDialog_title_enter));
        		break;
        	}    		
        	case SPD_MODE_SET_PASSWORD:
        	{
        		passwordOldPassword.setVisibility(EditText.GONE);
        		this.setTitle(context.getResources().getString(R.string.passwordDialog_title_set));  
        		break;
        	}
        	default:
        		throw new IllegalArgumentException("unknown mode");
        }
    	   	  	
    	if (dialogMode != SPD_MODE_ENTER_PASSWORD)
    	{
	    	passwordEditText1.addTextChangedListener((new TextWatcher()
	    	{
	            public void  afterTextChanged (Editable s)
	            {
	            }
	            public void  beforeTextChanged  (CharSequence s, int start, int count, int after)
	            {
	            }
	            public void onTextChanged  (CharSequence s, int start, int before, int count) 
	            {         
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
		    	
				String P1 = passwordEditText1.getText().toString().trim();
				String currentPassword = passwordOldPassword.getText().toString().trim(); // not used
				if (P1.equals(""))
				{
	        		new ImageToast(context.getResources().getString(R.string.passwordDialog_noPassword), 
	        				ImageToast.TOAST_IMAGE_CANCEL, context).show();
					return;
				}
				if (dialogMode != SPD_MODE_ENTER_PASSWORD && !P1.equals(passwordEditText2.getText().toString().trim()))
				{
	        		new ImageToast(context.getResources().getString(R.string.passwordDialog_passwordNotMatch), 
	        				ImageToast.TOAST_IMAGE_CANCEL, context).show();
					return;
				}
		    	buttonsBlock = true;	
		    	
		    	Object[] message = new Object[2];
		    	message[0] = P1;
		    	message[1] = tag;
				
		    	handler.sendMessage(Message.obtain(handler, SPD_HANDLER_OK, message));
		    	
		    	if(hideOnly) { 
		    		hide();
		    		buttonsBlock = false;
		    		passwordEditText1.setText("");
		    		passwordEditText2.setText("");
		    	}
		    	else cancel();
		    }
	    });
    	
    	
    	// Cancel Button
    	cancelButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	if(!blockCancellation) 
		    	{
		    		handler.sendMessage(Message.obtain(handler, SPD_HANDLER_CANCEL, tag));
		    		cancel();
		    	}
		    	else {
	        		new ImageToast(context.getResources().getString(R.string.passwordDialog_cannotCancel), 
	        				ImageToast.TOAST_IMAGE_CANCEL, context).show();
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
    	
    	this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}	
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
            return true;
        } else return super.onKeyDown(keyCode, event);
    }
}
