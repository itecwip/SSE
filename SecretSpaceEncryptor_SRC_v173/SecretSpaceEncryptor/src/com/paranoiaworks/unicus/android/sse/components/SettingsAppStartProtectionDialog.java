package com.paranoiaworks.unicus.android.sse.components;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import sse.org.bouncycastle.crypto.digests.SHA3Digest;
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
import android.widget.Toast;

import com.lambdaworks.crypto.SCrypt;
import com.paranoiaworks.android.sse.interfaces.SettingsCheckBoxCustom;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.config.ScryptParams;
import com.paranoiaworks.unicus.android.sse.dao.SettingDataHolder;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

/**
 * Custom CheckBox Dialog (extended settings)
 *
 * @author Paranoia Works
 */
public class SettingsAppStartProtectionDialog extends SecureDialog implements SettingsCheckBoxCustom {
	
	private Activity context;
	private int dialogMode = -1;
	
	private EditText passwordEditText1;
	private EditText passwordEditText2;
	private CheckBox passCB;
	private Button okButton;
	private Button cancelButton;
	private Object tag;
	private Handler handler;

	private boolean blockCancellation = false;
	private boolean buttonsBlock = false;
	
	public final static int SASPD_MODE_DISABLE_PROTECTION = 1;
	public final static int SASPD_MODE_ENABLE_PROTECTION = 2;
	
	public final static String PERSISTENT_DATA_OBJECT_LAUNCH_PASSWORD = "PERSISTENT_DATA_OBJECT_LAUNCH_PASSWORD";
	
	public SettingsAppStartProtectionDialog(View v, Handler handler) 
	{
		this((Activity)v.getContext(), handler);
	}	
	
	public SettingsAppStartProtectionDialog(Activity context, Handler handler) 
	{
		super(context);
		this.context = context;
		this.handler = handler;
	}
	
	public void setCustomTitle(String customTitle)
	{
		this.setTitle(customTitle);
	}	
	
	public void setTag(Object tag)
	{
		this.tag = tag;
	}	
	
	public Object getTag()
	{
		return this.tag;
	}
	
	@Override
	public void doOnCheck() {
		this.dialogMode = SASPD_MODE_ENABLE_PROTECTION;
		this.init();
		super.show();
	}

	@Override
	public void doOnUncheck() {
		this.dialogMode = SASPD_MODE_DISABLE_PROTECTION;
		this.init();	
		super.show();
	}
	
	@Override
	public void show(){
		// not used
	}
	
	private void init()
	{		
		this.setContentView(R.layout.lc_settings_app_start_protection_dialog);
		this.setCancelable(false);
		this.setCanceledOnTouchOutside(false);

    	passwordEditText1 = (EditText)this.findViewById(R.id.SASPD_passwordEditText1);
    	passwordEditText2 = (EditText)this.findViewById(R.id.SASPD_passwordEditText2);   	
    	cancelButton = (Button)this.findViewById(R.id.SASPD_cancelButton);
    	okButton = (Button)this.findViewById(R.id.SASPD_okButton);
    	passCB = (CheckBox)this.findViewById(R.id.SASPD_passwordCheckBox);
    	
    	passwordEditText1.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	passwordEditText1.setTransformationMethod(new PasswordTransformationMethod());
    	passwordEditText2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	passwordEditText2.setTransformationMethod(new PasswordTransformationMethod());

    	// prepare layout for mode
    	switch (dialogMode) 
        {        
        	case SASPD_MODE_DISABLE_PROTECTION:
        	{
        		passwordEditText1.setHint("");
        		passwordEditText2.setVisibility(EditText.GONE);
        		this.setTitle(Helpers.capitalizeFirstLetter(context.getResources().getString(R.string.passwordDialog_oldPasswordHint)));
        		break;
        	}    		
        	case SASPD_MODE_ENABLE_PROTECTION:
        	{
        		this.setTitle(context.getResources().getString(R.string.passwordDialog_title_set));  
        		break;
        	}
        	default:
        		throw new IllegalArgumentException("unknown mode");
        }
    	   	  	
    	if (dialogMode != SASPD_MODE_DISABLE_PROTECTION)
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
		    	
		    	SettingDataHolder sdh = SettingDataHolder.getInstance();
		    	
				String P1 = passwordEditText1.getText().toString().trim();
				if(P1.equals(""))
				{
	        		new ImageToast(context.getResources().getString(R.string.passwordDialog_noPassword), 
	        				ImageToast.TOAST_IMAGE_CANCEL, context).show();
					return;
				}
				if(dialogMode == SASPD_MODE_ENABLE_PROTECTION)
				{
	        		if(!P1.equals(passwordEditText2.getText().toString().trim()))
	        		{
		        		new ImageToast(context.getResources().getString(R.string.passwordDialog_passwordNotMatch), 
		        				ImageToast.TOAST_IMAGE_CANCEL, context).show();
						return;
	        		}
				}
				else if((dialogMode == SASPD_MODE_DISABLE_PROTECTION))
				{
					List<byte[]> passwordPackage = new ArrayList<byte[]>();
					passwordPackage = (List<byte[]>)sdh.getPersistentDataObject(PERSISTENT_DATA_OBJECT_LAUNCH_PASSWORD);
					
					if(passwordPackage != null) 
					{
						byte[] hashedPassword = getPasswordDerivative(P1, passwordPackage.get(1));
						if(!MessageDigest.isEqual(hashedPassword, passwordPackage.get(0)))
						{
							passwordEditText1.setText("");
							new ImageToast(context.getResources().getString(R.string.passwordDialog_invalidCurrentPassword), 
			        				ImageToast.TOAST_IMAGE_CANCEL, context).show();
							return;
						}
					}
				}
				
		    	buttonsBlock = true;
		    	
		    	if(dialogMode == SASPD_MODE_ENABLE_PROTECTION) 
		    	{
		    		byte[] salt = Encryptor.getRandomBA(64);
		    		byte[] hashedPassword = getPasswordDerivative(P1, salt);
		    		
		    		List<byte[]> passwordPackage = new ArrayList<byte[]>();
		    		passwordPackage.add(hashedPassword);
		    		passwordPackage.add(salt);
		    		
		    		sdh.addOrReplacePersistentDataObject(PERSISTENT_DATA_OBJECT_LAUNCH_PASSWORD, passwordPackage);
		    		sdh.save();
		    	}
		    	else if(dialogMode == SASPD_MODE_DISABLE_PROTECTION)
		    	{
		    		//sdh.addOrReplacePersistentDataObject(PERSISTENT_DATA_OBJECT_LAUNCH_PASSWORD, null);
		    		//sdh.save();
		    	}
		    	
		    	Object[] message = new Object[2];
		    	message[0] = P1;
		    	message[1] = tag;
				
		    	handler.sendMessage(Message.obtain(handler, SettingsCheckBoxCustom.OK, message));
		    	cancel();
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
		    		handler.sendMessage(Message.obtain(handler, SettingsCheckBoxCustom.CANCEL, tag));
		    		cancel();
		    	}
		    	else {
	        		Toast.makeText(context, context.getResources().getString(R.string.passwordDialog_cannotCancel), 
	        				Toast.LENGTH_SHORT).show();
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
    	        	passwordEditText2.setText(passwordEditText1.getText().toString());
    	        	if(passwordEditText1.length() > 0) passwordEditText1.setSelection(passwordEditText1.length());
    	        } else {
    	        	passwordEditText2.setFocusable(true);
    	        	passwordEditText2.setFocusableInTouchMode(true);
    	        	passwordEditText2.setEnabled(true);
    	        	passwordEditText1.setTransformationMethod(new PasswordTransformationMethod());
    	        	passwordEditText1.setSelection(passwordEditText1.length());
    	        }
    	    }
    	});
    	
    	this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
	
	/** Get password derivative */
    public static byte[] getPasswordDerivative(String password, byte[] salt)
    {
    	password = convertToCodePoints(password.trim());
    	
    	ScryptParams sp = ScryptParams.getParameters(ScryptParams.APP_CODE_AUTHENTICATION, 2);   	
    	int dkLen = 64;   	
    	
    	byte[] output = null;
    	try {
			output = SCrypt.scrypt(Encryptor.getSHA3Hash(password.getBytes(), 512), salt, sp.getN(), sp.getR(), sp.getP(), dkLen);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
    	
    	return output;
    }
	
    /** Convert text to "char + unicode int representation string" */
    private static String convertToCodePoints(String text)
    {
    	StringBuffer codePointsText = new StringBuffer();
    	for(int i = 0; i < text.length(); ++i)
    	{
    		int unicode = text.codePointAt(i);
    		if(unicode > 126 || unicode < 32)
    		{
	    		codePointsText.append(Integer.toString(unicode));
    		}
    		else
    		{
    			codePointsText.append(text.charAt(i));
    		}
    	}
    	
    	return codePointsText.toString();
    }
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
            return true;
        } else return super.onKeyDown(keyCode, event);
    }
}
