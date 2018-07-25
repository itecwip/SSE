package com.paranoiaworks.unicus.android.sse.components;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.paranoiaworks.android.sse.interfaces.Lockable;
import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.StaticApp;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.dao.SettingDataHolder;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor;

/** Screen Lock Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.6
 */ 
public class ScreenLockDialog extends SecureDialog {
	
	private Context appContext;
	private Activity callingActivity;
	
	private Button unlockButton;
	private Button leaveButton;
	private EditText passwordET;
	private TextView alertTV;
	private CheckBox showPasswordCB;
	
	private boolean leave = false;
	private boolean active = true;
	private boolean unicodeAllowed = false;
	
	private String decKeyHash = null;

	public ScreenLockDialog(View v, String decKeyHash) 
	{
		this((Activity)v.getContext(), decKeyHash);
	}	
	
	public ScreenLockDialog(Activity activity, String decKeyHash) 
	{
		//super(StaticApp.getContext());
		//this.appContext = StaticApp.getContext();
		super(activity);
		this.appContext = activity;
		this.callingActivity = activity;
		this.decKeyHash = decKeyHash;
		this.init();
		this.implementOnClicks();
	}
	
	private void init()
	{		
		this.setContentView(R.layout.lc_screenlock_dialog);
		this.setTitle(appContext.getResources().getString(R.string.common_locked_text));
		this.setCancelable(false);
		this.setCanceledOnTouchOutside(false);
		
		unicodeAllowed = SettingDataHolder.getInstance().getItemAsBoolean("SC_Common", "SI_AllowUnicodePasswords");
		
		unlockButton = (Button)this.findViewById(R.id.SLD_unlockButton);
		leaveButton = (Button)this.findViewById(R.id.SLD_leaveButton);
		passwordET = (EditText)this.findViewById(R.id.SLD_passwordEditText);
		showPasswordCB = (CheckBox)this.findViewById(R.id.SLD_showPasswordCheckBox);
		alertTV = (TextView)this.findViewById(R.id.SLD_alertTV);
		
		passwordET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		passwordET.setTransformationMethod(new PasswordTransformationMethod());
		
    	if(!unicodeAllowed) passwordET.setFilters(new InputFilter[] { filter });
    	
    	showPasswordCB.setOnCheckedChangeListener(new OnCheckedChangeListener()
    	{
    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    	    {
    	        if (isChecked)
    	        {
    	        	passwordET.setTransformationMethod(null);
    	        	if(passwordET.length() > 0) passwordET.setSelection(passwordET.length());
    	        } else {
    	        	passwordET.setTransformationMethod(new PasswordTransformationMethod());
    	        	if(passwordET.length() > 0) passwordET.setSelection(passwordET.length());
    	        }
    	    }
    	});
    	
		passwordET.addTextChangedListener((new TextWatcher()
    	{
            public void  afterTextChanged (Editable s) {
            }
            public void  beforeTextChanged (CharSequence s, int start, int count, int after) {
            	alertTV.setVisibility(View.GONE);
            	alertTV.setText("");
            }
            public void onTextChanged (CharSequence s, int start, int before, int count)  {
            }
    	}));
    	
    	Window window = this.getWindow();
    	//WindowManager.LayoutParams wlp = window.getAttributes();
    	//wlp.gravity = Gravity.TOP;
    	//window.setAttributes(wlp);
    	window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    	//window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	}
	
	private void implementOnClicks()
	{		
		unlockButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	String currentPassword = passwordET.getText().toString().trim();
		    	String testKeyHash = null;
		    	try {
					testKeyHash = (new Encryptor(currentPassword, 0, unicodeAllowed)).getKeyHash();
				} catch (Exception e) {
					e.printStackTrace();
				}
		    	
				if(decKeyHash.equals(testKeyHash))
				{		    	
					Lockable lockable = (Lockable)callingActivity;
					lockable.doOnUnlock();
			    	active = false;
			    	cancel();
				}
				else
				{
	        		passwordET.setText("");
	        		alertTV.setVisibility(View.VISIBLE);
		    		alertTV.setText(appContext.getResources().getString(R.string.passwordDialog_invalidPassword));
				}
		    }
	    });
		
		leaveButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	leave = true;
		    	active = false;
		    	cancel();
		    }
	    });
		
    	this.setOnCancelListener(new DialogInterface.OnCancelListener() {
    		@Override
    		public void onCancel (DialogInterface dialogInterface) {
		    	if(leave)
		    	{
	    			CryptActivity ca = (CryptActivity)callingActivity;
			    	ca.setMessage(new ActivityMessage(CryptActivity.EXIT_CASCADE, null));
		    	}
    		}
    	});	
	}
	
	public void leaveButtonEnabled(boolean enabled)
	{
		leaveButton.setEnabled(enabled);
	}
	
	public boolean getActiveFlag()
	{
		return active;
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {           
            return true;
        } else return super.onKeyDown(keyCode, event);
    }
	
	// Only ASCII 32...126 allowed
    InputFilter filter = new InputFilter()
	{
	    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) 
	    { 
	    	if (source.length() < 1) return null;
	    	char last = source.charAt(source.length() - 1);
        	
	    	if(last > 126 || last < 32) 
        	{
	    		Dialog showMessageDialog = ComponentProvider.getShowMessageDialog(
						(Activity)appContext, 
						appContext.getResources().getString(R.string.passwordDialog_title_incorrectCharacter), 
						appContext.getResources().getString(R.string.passwordDialog_incorrectCharacterReport), 
    					ComponentProvider.DRAWABLE_ICON_INFO_BLUE
    			);
				showMessageDialog.show();
        		
        		return source.subSequence(0, source.length() - 1);
        	}
        	return null;
	    }  
	};
}
