package com.paranoiaworks.unicus.android.sse.utils;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.MainActivity;
import com.paranoiaworks.unicus.android.sse.MessageEncActivity;
import com.paranoiaworks.unicus.android.sse.PasswordVaultActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.components.ImageToast;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.dao.Vault;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;

/**
 * Provides shared and even single-purpose Components (mainly dialogs) 
 * 
 * @author Paranoia Works
 * @version 1.0.10
 */ 
public class ComponentProvider {
	
	public static final int DRAWABLE_ICON_INFO_BLUE = 100;
	public static final int DRAWABLE_ICON_INFO_RED = 101;
	//public static final int DRAWABLE_ICON_STOP = 102;
	public static final int DRAWABLE_ICON_OK = 103;
	public static final int DRAWABLE_ICON_CANCEL = 104;
	public static final int DRAWABLE_ICON_SPEC_PRO = 200;
	
	public static Toast getImageToastOK(String sText, View v) 
    {
    	return getImageToast(sText, ImageToast.TOAST_IMAGE_OK, v);
    }
    
    public static Toast getImageToastKO(String sText, View v) 
    {
    	return getImageToast(sText, ImageToast.TOAST_IMAGE_CANCEL, v);
    }
    
    public static Toast getImageToastKO(String sText, Activity c) 
    {
    	return getImageToast(sText, ImageToast.TOAST_IMAGE_CANCEL, c);
    }
    
    public static Toast getImageToastInfo(String sText, View v) 
    {
    	return getImageToast(sText, ImageToast.TOAST_IMAGE_INFO, v);
    }
	
    public static Toast getImageToast(String sText, int imageCode, View v) 
    {
    	return getImageToast(sText, imageCode, (Activity)v.getContext());
    }
    
    public static Toast getImageToast(String sText, int imageCode, Activity ac) 
    {
    	return new ImageToast(sText, imageCode, ac);
    }

    public static Dialog getExitDialog(View v)
    {
    	return getExitDialog((Activity)v.getContext());
    }
    
    /** Get Application Exit Dialog (single-purpose) */
    public static Dialog getExitDialog(Activity ac)
    {
    	final Activity a = ac;
    	final AlertDialog exitDialog = new AlertDialog.Builder(a).create();
    	exitDialog.setTitle(a.getResources().getString(R.string.main_exitDialog_title));
    	exitDialog.setMessage(a.getResources().getString(R.string.main_exitDialog_message));
    	exitDialog.setIcon(R.drawable.exit_icon_large);
    	exitDialog.setCanceledOnTouchOutside(false);
    	exitDialog.setButton2(a.getResources().getString(R.string.common_ok_text), new DialogInterface.OnClickListener() {
    	   public void onClick(DialogInterface dialog, int which) {
    		   MainActivity.setReadyForDestroy();
    		   a.finish();
    	   }
    	});
    	exitDialog.setButton(a.getResources().getString(R.string.common_cancel_text), new DialogInterface.OnClickListener() {
     	   public void onClick(DialogInterface dialog, int which) {
     		  exitDialog.cancel();
     	   }
     	});
    	return exitDialog;
    }
    
    public static Dialog getBaseQuestionDialog(View v, String title, String question, String parentMessage, final int messageCode)
    {
    	return getBaseQuestionDialog((Activity)v.getContext(), title, question, parentMessage, messageCode, null);
    }
    
    public static AlertDialog getBaseQuestionDialog(final Activity a, String title, String question, final String parentMessage, final int messageCode)
    {  
    	return getBaseQuestionDialog(a, title, question, parentMessage, messageCode, null);
    }
    
    public static AlertDialog getBaseQuestionDialog(final Activity a, String title, String question, final String parentMessage, final int messageCode, final Object attachement)
    {  
    	return getBaseQuestionDialog(a, title, question, parentMessage, messageCode, attachement, false);
    }
    
    /** Simple Question Dialog, "Do you want to ... " (universal) */
    public static AlertDialog getBaseQuestionDialog(final Activity a, String title, String question, final String parentMessage, final int messageCode, final Object attachement, boolean infoMode)
    {  
	    AlertDialog.Builder builder = new AlertDialog.Builder(a);
	    int iconResource = infoMode ? R.drawable.info_icon_large : R.drawable.ask_icon_large;
	    String positiveText = infoMode ? a.getResources().getString(R.string.common_continue_text) : a.getResources().getString(R.string.common_yes_text);
	    String negativeText = infoMode ? a.getResources().getString(R.string.common_cancel_text) : a.getResources().getString(R.string.common_no_text);
	    builder.setMessage(Html.fromHtml(question))
	           .setIcon(iconResource)
	           .setTitle(title)
	           .setNegativeButton(positiveText, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   CryptActivity ca = (CryptActivity)a;
	            	   ca.setMessage(new ActivityMessage(messageCode, parentMessage, new Integer(1), attachement));
	            	   dialog.cancel();
	               }
	           })
	           .setPositiveButton(negativeText, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   CryptActivity ca = (CryptActivity)a;
	            	   ca.setMessage(new ActivityMessage(messageCode, parentMessage, new Integer(0), attachement));
	            	   dialog.cancel();
	               }
	           });
	    AlertDialog alert = builder.create();
	    alert.setCanceledOnTouchOutside(false);
	    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {		
		        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
		        	return true;
		        }
		        return false;
			}
		});   
	    return alert;
    }
    
    public static AlertDialog getBaseQuestionDialog(final Context a, String title, String question, final Handler handler, boolean infoMode)
    {  
	    AlertDialog.Builder builder = new AlertDialog.Builder(a);
	    int iconResource = infoMode ? R.drawable.info_icon_large : R.drawable.ask_icon_large;
	    String positiveText = infoMode ? a.getResources().getString(R.string.common_continue_text) : a.getResources().getString(R.string.common_yes_text);
	    String negativeText = infoMode ? a.getResources().getString(R.string.common_cancel_text) : a.getResources().getString(R.string.common_no_text);
	    builder.setMessage(Html.fromHtml(question))
	           .setIcon(iconResource)
	           .setTitle(title)
	           .setNegativeButton(positiveText, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   handler.sendMessage(Message.obtain(handler, 1));
	               }
	           })
	           .setPositiveButton(negativeText, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   handler.sendMessage(Message.obtain(handler, 0));
	               }
	           });
	    AlertDialog alert = builder.create();
	    alert.setCanceledOnTouchOutside(false);
	    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {		
		        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
		        	return true;
		        }
		        return false;
			}
		});   
	    return alert;
    }
    
    /** Simple Question Dialog, "Do you want to ... " (universal) - Using Handler */
    public static AlertDialog getBaseQuestionDialog(Context context, final Handler handler, String title, String question, final String parentMessage, final int messageCode, final Object attachement)
    {  
	    AlertDialog.Builder builder = new AlertDialog.Builder(context);
	    builder.setMessage(Html.fromHtml(question))
	           .setIcon(R.drawable.ask_icon_large)
	           .setTitle(title)
	           .setNegativeButton(context.getResources().getString(R.string.common_yes_text), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   ActivityMessage am = new ActivityMessage(messageCode, parentMessage, new Integer(1), attachement);
	            	   handler.sendMessage(Message.obtain(handler, messageCode, am));
	            	   dialog.cancel();
	               }
	           })
	           .setPositiveButton(context.getResources().getString(R.string.common_no_text), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   ActivityMessage am = new ActivityMessage(messageCode, parentMessage, new Integer(0), attachement);
	            	   handler.sendMessage(Message.obtain(handler, messageCode, am));
	            	   dialog.cancel();
	               }
	           });
	    AlertDialog alert = builder.create();
	    alert.setCanceledOnTouchOutside(false);
	    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {		
		        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
		        	return true;
		        }
		        return false;
			}
		});   
	    return alert;
    }
    
    public static AlertDialog getCriticalQuestionDialog(View v, String title, String question, String parentMessage, final int messageCode)
    {
    	return getCriticalQuestionDialog((Activity)v.getContext(), title, question, parentMessage, messageCode);
    }
    
    /** Question Dialog with "intent verification" (universal) */
    public static AlertDialog getCriticalQuestionDialog(final Activity a, String title, String question, final String parentMessage, final int messageCode)
    {  
    	LayoutInflater inflater = (LayoutInflater) a.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    	View layout = inflater.inflate(R.layout.lc_criticalask_dialog, (ViewGroup) a.findViewById(R.id.CAD_layout_root));
    	
    	TextView codeView = (TextView) layout.findViewById(R.id.CAD_code);
    	EditText etCodeView = (EditText) layout.findViewById(R.id.CAD_codeET);
    	TextView tvQuestion = (TextView) layout.findViewById(R.id.CAD_Question);
    	
    	final String codeTemp = Encryptor.getMD5Hash(Long.toString(System.currentTimeMillis())).substring(0, 5).toUpperCase();
    	codeView.setText(Html.fromHtml("<b>" + codeTemp + "</b>"));
    	tvQuestion.setText(Html.fromHtml(question));
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(a);
    	builder.setView(layout);
	    //builder.setMessage(Html.fromHtml(question))
	    builder.setIcon(R.drawable.ask_icon_red_large)
	           .setTitle(title)
	           .setNegativeButton(a.getResources().getString(R.string.common_yes_text), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   CryptActivity ca = (CryptActivity)a;
	            	   ca.setMessage(new ActivityMessage(messageCode, parentMessage, new Integer(1)));
	            	   dialog.cancel();
	               }
	           })
	           .setPositiveButton(a.getResources().getString(R.string.common_no_text), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   CryptActivity ca = (CryptActivity)a;
	            	   ca.setMessage(new ActivityMessage(messageCode, parentMessage, new Integer(0)));
	            	   dialog.cancel();
	               }
	           });
	    
	    
	    final AlertDialog alert = builder.create();
	    alert.setCanceledOnTouchOutside(false);
	    
	    Button tempB = alert.getButton(Dialog.BUTTON_NEGATIVE);
	    if(tempB != null) tempB.setEnabled(false);
	        
	    etCodeView.addTextChangedListener(new TextWatcher() 
	    {
            public void  afterTextChanged (Editable s)
            {
        		Button okButton = alert.getButton(Dialog.BUTTON_NEGATIVE);
        		okButton.setEnabled(s.toString().trim().equalsIgnoreCase(codeTemp));
            }
            public void  beforeTextChanged  (CharSequence s, int start, int count, int after)
            {
            }
            public void onTextChanged  (CharSequence s, int start, int before, int count) 
            {
            }
    	});
	    
	    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {		
		        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
		        	return true;
		        }
		        return false;
			}
		});
	    
	    return alert;
    }
    
    public static Dialog getShowMessageDialog(final Activity a, String title, String message, Integer iconCode)
    {
    	return getShowMessageDialog(a, title, message, iconCode, null, null);
    }
    
    public static Dialog getShowMessageDialog(View v, String title, String message, Integer iconCode)
    {
    	return getShowMessageDialog((Activity)v.getContext(), title, message, iconCode, null, null);
    }
    
    public static Dialog getShowMessageDialog(View v, String title, String message, Integer iconCode, final String parentMessage, final Integer messageCode)
    {  
    	return getShowMessageDialog((Activity)v.getContext(), title, message, iconCode, parentMessage, messageCode);
    }
    
    /** Simple Alert Dialog (universal) */
    public static Dialog getShowMessageDialog(final Activity a, String title, String message, Integer iconCode, final String parentMessage, final Integer messageCode)
    {  
    	if(iconCode == null) iconCode = DRAWABLE_ICON_INFO_BLUE;
    	if(title == null) title = a.getResources().getString(R.string.common_message_text);
    	AlertDialog.Builder builder = new AlertDialog.Builder(a);
	    builder.setMessage(Html.fromHtml(message))
	           .setIcon(getDrawableCode(iconCode))
	           .setTitle(title)
	           .setNeutralButton(a.getResources().getString(R.string.common_ok_text), new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   	if(parentMessage != null && messageCode != null)
	            	   	{
	            	   		CryptActivity ca = (CryptActivity)a;
	            	   		ca.setMessage(new ActivityMessage(messageCode, parentMessage, null));
	            	   	}
	            	   dialog.cancel();
	               }
	           });
	    AlertDialog alert = builder.create();
	    alert.setCanceledOnTouchOutside(false);
	    alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {		
		        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
		        	return true;
		        }
		        return false;
			}
		});   
	    return alert;
    }
    
    public static Dialog getItemSelectionDialog(View v, String title,  final List itemList, final List keyList, int messageCode)
    {
    	return getItemSelectionDialog((Activity)v.getContext(), title, itemList, keyList, messageCode, null, null);
    }
    
    public static Dialog getItemSelectionDialog(View v, String title,  final List itemList, final List keyList, int messageCode, final Object attachement)
    {
    	return getItemSelectionDialog((Activity)v.getContext(), title, itemList, keyList, messageCode, attachement, null);
    }
    
    public static Dialog getItemSelectionDialog(View v, String title,  final List itemList, final List keyList, int messageCode, final Object attachement, int radioButtonSelected)
    {
    	return getItemSelectionDialog((Activity)v.getContext(), title, itemList, keyList, messageCode, attachement, radioButtonSelected);
    }
    
    public static Dialog getItemSelectionDialog(final Activity a, String title, final List itemList, final List keyList, final int messageCode) 
    {
    	return getItemSelectionDialog(a, title, itemList, keyList, messageCode, null, null);
    }
    
    public static Dialog getItemSelectionDialog(final Activity a, String title, final List itemList, final List keyList, final int messageCode, final Object attachement) 
    {
    	return getItemSelectionDialog(a, title, itemList, keyList, messageCode, attachement, null);
    }
    
    /**  Selector Dialog (universal) */
    public static Dialog getItemSelectionDialog(final Activity a, String title, final List itemList, final List keyList, final int messageCode, final Object attachement, Integer radioButtonSelected) 
    {
    	if (itemList == null || itemList.size() < 1)
    	{
    		getImageToast(a.getResources().getString(R.string.common_noItems_text), ImageToast.TOAST_IMAGE_CANCEL, a).show();
    		return null;
    	}
    	final CharSequence[] items = new CharSequence[itemList.size()];
    	
    	for (int i = 0; i < itemList.size(); ++i) items[i] = (CharSequence)itemList.get(i);
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(a);
	    builder.setTitle(title);
	    
	    if(radioButtonSelected == null || radioButtonSelected < 0)
	    {
		    builder.setItems(items, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int item) {
		            CryptActivity ca = (CryptActivity)a;
		            if (keyList == null)
		            	ca.setMessage(new ActivityMessage(messageCode, (String)itemList.get(item), attachement));
		            else
		            	ca.setMessage(new ActivityMessage(messageCode, (String)keyList.get(item), attachement));
		            dialog.cancel();
		        }
		    });
	    }
	    else
	    {
		    builder.setSingleChoiceItems(items, radioButtonSelected, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int item) {
		            CryptActivity ca = (CryptActivity)a;
		            if (keyList == null)
		            	ca.setMessage(new ActivityMessage(messageCode, (String)itemList.get(item), attachement));
		            else
		            	ca.setMessage(new ActivityMessage(messageCode, (String)keyList.get(item), attachement));
		            dialog.cancel();
		        }
		    });
	    }
	    AlertDialog itemSelector = builder.create();
	    itemSelector.setCanceledOnTouchOutside(false);
	    return itemSelector;    
    }
    
    public static Dialog getTextSetDialog(View v, final String title, final String currentValue, final int messageCode, final Object attachement)
    {
    	return getTextSetDialog((Activity)v.getContext(), title, currentValue, messageCode, attachement);
    }
    
    /** Simple Input Text Dialog (universal) */
    public static Dialog getTextSetDialog(final Activity a, String title, String currentValue, final int messageCode, final Object attachement)
    {    	
    	final Dialog dialog = new Dialog(a);
    	 
    	dialog.setCancelable(true);
    	dialog.setCanceledOnTouchOutside(false);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.lc_settext_dialog);
    	
    	final TextView titleView = (TextView)dialog.findViewById(R.id.STD_Title);
    	titleView.setText(title);
    	
    	final EditText enteredText = (EditText)dialog.findViewById(R.id.enteredText);
    	if(currentValue == null) currentValue = "";
    	enteredText.setText(currentValue);
    	
    	// OK Button
    	Button okButton = (Button)dialog.findViewById(R.id.okButton);
    	okButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	String et = enteredText.getText().toString().trim();
		    	
	            CryptActivity ca = (CryptActivity)a;
	            ca.setMessage(new ActivityMessage(messageCode, et, attachement));
	            dialog.cancel();
		    }
	    });
    	
    	
    	// Cancel Button
    	Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
    	cancelButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	dialog.cancel();
		    }
	    });
    	return dialog;
    }
    
    public static Dialog getMessageSetNameDialog(View v, final String message, final String lastLoadedFileName)
    {
    	return getMessageSetNameDialog((Activity)v.getContext(), message, lastLoadedFileName);
    }
    
    /** Set Message Name Dialog for Message Encryptor (single-purpose) */
    public static Dialog getMessageSetNameDialog(final Activity a, final String message, final String lastLoadedFileName)
    {    	
    	final Dialog dialog = new Dialog(a);
    	 
    	dialog.setCancelable(true);
    	dialog.setCanceledOnTouchOutside(false);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.lc_settext_dialog);
    	
    	final TextView titleView = (TextView)dialog.findViewById(R.id.STD_Title);
    	titleView.setText(a.getResources().getString(R.string.me_moreDialog_saveMessage_SetNameTitle));
    	
    	final EditText enteredText = (EditText)dialog.findViewById(R.id.enteredText);
    	
    	if(lastLoadedFileName != null && lastLoadedFileName.trim().length() > 0) {
    		enteredText.setText(lastLoadedFileName);
    		enteredText.selectAll();
    	}
    	
    	// OK Button
    	Button okButton = (Button)dialog.findViewById(R.id.okButton);
    	okButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	try {
					String et = enteredText.getText().toString().trim();
		    		
					if (et.equals(""))
		    		{
		    			getImageToast(a.getResources().getString(R.string.me_moreDialog_noName), ImageToast.TOAST_IMAGE_CANCEL, a).show();
		    			return;
		    		}
					
					List<Object> messageData = DBHelper.getMessageNamesAndData();
            		List<String> messagesNames = messageData != null ? (List<String>)messageData.get(0) : null;
					
					if (messagesNames != null && messagesNames.contains(et))
					{
		    			getImageToast(a.getResources().getString(R.string.me_moreDialog_nameAlreadyExist).replaceAll("<1>", et), ImageToast.TOAST_IMAGE_CANCEL, a).show();
		    			return;
					}
					
					DBHelper.insertMessage(et, message);
					getImageToast(a.getResources().getString(R.string.me_moreDialog_saveMessage_Saved).replaceAll("<1>", et), ImageToast.TOAST_IMAGE_OK, a).show();
					MessageEncActivity ca = (MessageEncActivity)a;
		            ca.currentEncTextUsed = true;
		            ca.setMessage(new ActivityMessage(MessageEncActivity.MEA_MESSAGE_STATUSTEXT_SET, Helpers.capitalizeFirstLetter(a.getResources().getString(R.string.me_moreDialog_saveMessage).toLowerCase())  + " : " + et + " : OK", null));

				} catch (Exception e) {
					e.printStackTrace();
				}
		    	dialog.cancel();
		    }
	    });
    	
    	
    	// Cancel Button
    	Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
    	cancelButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	dialog.cancel();
		    }
	    });
    	return dialog;
    }
    
    /** Set File Name Dialog for Message Encryptor (single-purpose) */
    public static Dialog getMessageSetFileNameDialog(final Activity a, final String message, final String lastLoadedFileName)
    {    	
    	final Dialog dialog = new Dialog(a);
    	 
    	dialog.setCancelable(true);
    	dialog.setCanceledOnTouchOutside(false);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.lc_settext_dialog);
    	
    	final TextView titleView = (TextView)dialog.findViewById(R.id.STD_Title);
    	titleView.setText(a.getResources().getString(R.string.common_enterFileName_text));
    	
    	final EditText enteredText = (EditText)dialog.findViewById(R.id.enteredText);
    	enteredText.setFilters(new InputFilter[] { Helpers.getFileNameInputFilter() });
    	
    	if(lastLoadedFileName != null && lastLoadedFileName.trim().length() > 0) {
    		enteredText.setText(lastLoadedFileName);
    		enteredText.selectAll();
    	}
    	
       	final Handler miscHandler = new Handler() 
        {
            public void handleMessage(Message msg)  
            {
            	if (msg.what == 1000)
            	{ 
            		ActivityMessage am = (ActivityMessage)msg.obj;
            		if(am.getAttachement().equals(new Integer(1)))
            		{
            			File file = (File)am.getAttachement2();
            			try {
							Helpers.saveStringToFile(file, message);
							getImageToast(a.getResources().getString(R.string.me_moreDialog_saveMessage_Saved).replaceAll("<1>", file.getName()), ImageToast.TOAST_IMAGE_OK, a).show();
							MessageEncActivity ca = (MessageEncActivity)a;
							ca.currentEncTextUsed = true;
							ca.setMessage(new ActivityMessage(MessageEncActivity.MEA_MESSAGE_STATUSTEXT_SET, Helpers.capitalizeFirstLetter(a.getResources().getString(R.string.me_moreDialog_saveMessageFile).toLowerCase())  + " : " + file.getName() + " : OK", null));
							dialog.cancel();
						} catch (Exception e) {
							getShowMessageDialog(a, "Error", e.getLocalizedMessage(), DRAWABLE_ICON_CANCEL).show();
							e.printStackTrace();
						}
            		}          		
            	}   	
            }
        };
    	
    	// OK Button
    	Button okButton = (Button)dialog.findViewById(R.id.okButton);
    	okButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	try {
					String et = enteredText.getText().toString().trim();
		    		
					if (et.equals(""))
		    		{
		    			getImageToast(a.getResources().getString(R.string.me_moreDialog_noName), ImageToast.TOAST_IMAGE_CANCEL, a).show();
		    			return;
		    		}
					
					File dir = new File(Helpers.getImportExportDir().getAbsolutePath() + File.separator + a.getResources().getString(R.string.textsSubdir));   
					if(!dir.exists()) dir.mkdirs();
					
					File file = new File(dir.getAbsolutePath() + File.separator + et + "." + MessageEncActivity.MEA_FILE_EXT);
					
					MessageEncActivity ca = (MessageEncActivity)a;
					if (file.exists())
					{
		    			getBaseQuestionDialog(a, miscHandler, 
		    					a.getResources().getString(R.string.common_replacefile_text), 
		    					a.getResources().getString(R.string.common_replacefile_question).replaceAll("<1>", file.getName()),
		    					"", 1000, file).show();
		    			return;
					}
					else
					{
						Helpers.saveStringToFile(file, message);
					}
				
					ca.currentEncTextUsed = true;
					getImageToast(a.getResources().getString(R.string.me_moreDialog_saveMessage_Saved).replaceAll("<1>", file.getName()), ImageToast.TOAST_IMAGE_OK, a).show();
					ca.setMessage(new ActivityMessage(MessageEncActivity.MEA_MESSAGE_STATUSTEXT_SET, Helpers.capitalizeFirstLetter(a.getResources().getString(R.string.me_moreDialog_saveMessageFile).toLowerCase())  + " : " + file.getName() + " : OK", null));

				} catch (Exception e) {
					getShowMessageDialog(a, "Error", e.getLocalizedMessage(), DRAWABLE_ICON_CANCEL).show();
					e.printStackTrace();
				}
		    	dialog.cancel();
		    }
	    });
    	
    	
    	// Cancel Button
    	Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
    	cancelButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	dialog.cancel();
		    }
	    });

    	return dialog;
    }
    
    public static Dialog getVaultSetNameDialog(View v, Vault vault)
    {
    	return getVaultSetNameDialog((PasswordVaultActivity)v.getContext(), vault);
    }
    
    /** Set File Name for Vault Export - Password Vault (single-purpose) */
    public static Dialog getVaultSetNameDialog(final PasswordVaultActivity a, final Vault vault)
    {    	
    	final Dialog dialog = new Dialog(a);
    	 
    	dialog.setCancelable(true);
    	dialog.setCanceledOnTouchOutside(false);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.lc_settext_dialog);
    	
    	final TextView titleView = (TextView)dialog.findViewById(R.id.STD_Title);
    	titleView.setText(a.getResources().getString(R.string.pwv_moreDialog_exportVault_SetNameTitle));
    	
    	final EditText enteredText = (EditText)dialog.findViewById(R.id.enteredText);
    	enteredText.setFilters(new InputFilter[] { Helpers.getFileNameInputFilter() });
    	
    	// OK Button
    	Button okButton = (Button)dialog.findViewById(R.id.okButton);
    	okButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	try {
					String et = enteredText.getText().toString().trim();
					
			    	if (et.equals(""))
					{
			    		new ImageToast(a.getResources().getString(R.string.common_enterFileName_text), ImageToast.TOAST_IMAGE_CANCEL, a).show();
						return;
					}
					
					a.exportVaultToFile(et, null, vault == null ? false : true, false, false);

				} catch (Exception e) {
					e.printStackTrace();
					ComponentProvider.getShowMessageDialog(v, null, e.getLocalizedMessage(), ComponentProvider.DRAWABLE_ICON_CANCEL).show();
				}
		    	dialog.cancel();
		    }
	    });
    	
    	// Cancel Button
    	Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
    	cancelButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	dialog.cancel();
		    }
	    });
    	
    	return dialog;
    }
    
    public static Dialog getFileSetNameDialog(View v, CryptFileWrapper file, final int messageCode)
    {
    	return getFileSetNameDialog((Activity)v.getContext(), file, messageCode);
    }
    
    /** Set new File Name - rename file in File Encryptor (single-purpose) */
    public static Dialog getFileSetNameDialog(final Activity a, final CryptFileWrapper file, final int messageCode)
    {    	
    	final Dialog dialog = new Dialog(a);
    	 
    	dialog.setCancelable(true);
    	dialog.setCanceledOnTouchOutside(false);
    	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	dialog.setContentView(R.layout.lc_settext_dialog);
    	
    	final TextView titleView = (TextView)dialog.findViewById(R.id.STD_Title);
    	titleView.setText(a.getResources().getString(R.string.fe_fileRename));
    	
    	final EditText enteredText = (EditText)dialog.findViewById(R.id.enteredText);
    	enteredText.setFilters(new InputFilter[] { Helpers.getFileNameInputFilter() });
    	final String originalName = file.getName();
    	enteredText.setText(originalName);
    	
    	int textLength = enteredText.getText().length();
    	enteredText.setSelection(textLength, textLength);
    	
    	// OK Button
    	Button okButton = (Button)dialog.findViewById(R.id.okButton);
    	okButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	String et = enteredText.getText().toString().trim();
		    	try {
		    		
					if (et.equals(""))
		    		{
		    			getImageToast(a.getResources().getString(R.string.common_enterFileName_text), ImageToast.TOAST_IMAGE_CANCEL, a).show();
		    			return;
		    		}
					
					if (et.equals(file.getName())){
						dialog.cancel();
						return;
					}
					
					if(file.renameTo(et))
					{
		    			getImageToast(a.getResources().getString(R.string.common_fileRenamed_text)
		    					.replaceAll("<1>", originalName).replaceAll("<2>", et),
		    					ImageToast.TOAST_IMAGE_OK, 
		    					a)
		    					.show();
			            CryptActivity ca = (CryptActivity)a;
			            ca.setMessage(new ActivityMessage(messageCode, "", null));
					}
					else
					{
						throw new IllegalStateException("Rename: Failed");
					}
				} catch (Exception e) {
					if(e.getMessage().toLowerCase().equals("exists")) 
						getImageToast(a.getResources().getString(R.string.common_fileNameAlreadyExists_text).replaceAll("<1>", et), ImageToast.TOAST_IMAGE_CANCEL, a).show();
					else getImageToast(a.getResources().getString(R.string.common_fileFailedToRename).replaceAll("<1>", originalName), ImageToast.TOAST_IMAGE_CANCEL, a).show();
					e.printStackTrace();
				}
		    	dialog.cancel();
		    }
	    });
    	
    	// Cancel Button
    	Button cancelButton = (Button)dialog.findViewById(R.id.cancelButton);
    	cancelButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	dialog.cancel();
		    }
	    });
    	
    	return dialog;
    }
    
    private static int getDrawableCode(int code)
    {
        int dCode = 0;
    	switch (code) 
        {        
        	case DRAWABLE_ICON_INFO_BLUE:
        		dCode = R.drawable.info_icon_large;
            	break;
        	case DRAWABLE_ICON_INFO_RED:
        		dCode = R.drawable.info_icon_red_large;
            	break;
        	case DRAWABLE_ICON_OK:
        		dCode = R.drawable.ok_icon_large;
            	break;
        	case DRAWABLE_ICON_CANCEL:
        		dCode = R.drawable.cancel_icon_large;
            	break;
        	case DRAWABLE_ICON_SPEC_PRO:
        		dCode = R.drawable.ssepro_large;
            	break;
            default: 
            	break;
        }
    	return dCode;
    }
}
