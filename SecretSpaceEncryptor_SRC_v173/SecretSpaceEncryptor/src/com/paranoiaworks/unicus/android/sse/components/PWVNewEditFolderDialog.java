package com.paranoiaworks.unicus.android.sse.components;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.PasswordVaultActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.adapters.BasicListAdapter;
import com.paranoiaworks.unicus.android.sse.adapters.ColorListAdapter;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.dao.Vault;
import com.paranoiaworks.unicus.android.sse.dao.VaultFolder;
import com.paranoiaworks.unicus.android.sse.utils.ColorHelper;

/**
 * Edit/New Folder Dialog for Password Vault
 * similar to "Item Detail Layer" in PasswordVaultActivity.java (more comments there) 
 * 
 * @author Paranoia Works
 * @version 1.0.4
 */
public class PWVNewEditFolderDialog extends SecureDialog {
	
	private SpinnerAdapter folderColorSpinnerAdapter;
	private SpinnerAdapter orderSpinnerAdapter;
	private Activity context;
    private Spinner sFolderColor;
    private Spinner sFolderOrder;
    private EditText etFolderName;
    private EditText etFolderComment;
    private Button bFolderDelete;
    private Button bFolderEditSave;
    
    private int dialogMode;
    private Vault vault;
    private VaultFolder folder;
    private String originalHash;
    private int position = -1;
    private boolean buttonsLock = false;
    
	public final static int PWVFD_MODE_NEW_FOLDER = 1;
	//private final static int PWVFD_MODE_EDIT_FOLDER = 2;
	private final static int PWVFD_MODE_SAVE_FOLDER = 3;
	public final static int PWVFD_MODE_SHOW_FOLDER = 4;

	
	public PWVNewEditFolderDialog(View v, Vault vault, Integer position, int dialogMode) 
	{
		this((Activity)v.getContext(), vault, position, dialogMode);
	}	
	
	public PWVNewEditFolderDialog(Activity context, Vault vault, Integer position, int dialogMode) 
	{
		super(context);
		this.context = context;
		this.vault = vault;
		this.dialogMode = dialogMode;		
		if(dialogMode == PWVFD_MODE_SHOW_FOLDER)
		{
			this.position = position;
			this.folder = vault.getFolderByIndex(position);
		}
		this.init();
		this.setTitle(context.getResources().getString(R.string.pwv_folderDialog_title));
	}
	
	public void setOriginalHash(String originalHash)
	{
		this.originalHash = originalHash;
	}
	
	private void init()
	{		
		this.setContentView(R.layout.lc_pwv_neweditfolder_dialog);
		this.setCanceledOnTouchOutside(false);
		folderColorSpinnerAdapter = new ColorListAdapter(context, ColorHelper.getColorList(), ColorListAdapter.ICONSET_FOLDERS);
		orderSpinnerAdapter = new BasicListAdapter(context, getOrderList());
		sFolderColor = (Spinner)findViewById(R.id.PWVFD_colorCombo);
		sFolderColor.setAdapter(folderColorSpinnerAdapter);
		sFolderOrder = (Spinner)findViewById(R.id.PWVFD_positionCombo);
		sFolderOrder.setAdapter(orderSpinnerAdapter);
		bFolderDelete = (Button)findViewById(R.id.PWVFD_deleteButton);
		bFolderEditSave = (Button)findViewById(R.id.PWVFD_editSaveButton);
	    etFolderName = (EditText)findViewById(R.id.PWVFD_name);
	    etFolderComment = (EditText)findViewById(R.id.PWVFD_comment);
	    
	    if(dialogMode == PWVFD_MODE_NEW_FOLDER)
	    {
	    	makeFolderDetailEditable();
	    	bFolderDelete.setEnabled(false);
	    }	    
	    else if(dialogMode == PWVFD_MODE_SHOW_FOLDER)
	    {
	    	if(folder == null) throw new InvalidParameterException("PWVNewEditFolderDialog: Folder is null");
	    	prepareFolderDetailForShow();
	    	makeFolderDetailReadOnly();
	    	bFolderDelete.setEnabled(true);
	    }
		
	    bFolderEditSave.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	if (etFolderName.getText().toString().trim().equals(""))
		    	{
	        		Toast tt = new ImageToast(context.getResources().getString(R.string.common_enterTheName_text), ImageToast.TOAST_IMAGE_CANCEL, context);
	        		tt.show();
	        		return;
		    	}
		    	
		    	if(dialogMode == PWVFD_MODE_SHOW_FOLDER)
		    	{
		    		makeFolderDetailEditable();
		    		bFolderDelete.setEnabled(false);
		    		dialogMode = PWVFD_MODE_SAVE_FOLDER;
		    		return;
		    	}
		    	
		    	if(dialogMode == PWVFD_MODE_NEW_FOLDER)
		    	{
		    		disableButtons();
		    		folder = new VaultFolder();
	        		folder.setFolderName(etFolderName.getText().toString().trim());
	        		folder.setFolderComment(etFolderComment.getText().toString().trim());
	        		folder.setColorCode(ColorHelper.getColorList().get(sFolderColor.getSelectedItemPosition()).colorCode);
	        		setFolderAttributePosition();
	        		CryptActivity ca = (CryptActivity)context;
	        		ActivityMessage am = new ActivityMessage(PasswordVaultActivity.PWV_MESSAGE_FOLDER_NEW, null, folder);
	        		ca.setMessage(am);
	        		cancel();
	        		return;
		    	}	    	
		    	
		    	if(dialogMode == PWVFD_MODE_SAVE_FOLDER)
		    	{
		    		disableButtons();
		    		if(!originalHash.equals(folder.getFolderSecurityHash())) throw new IllegalStateException("PWVNewEditFolderDialog: folder hash doesn't match");
		    		folder.setFolderName(etFolderName.getText().toString().trim());
	        		folder.setFolderComment(etFolderComment.getText().toString().trim());
	        		folder.setColorCode(ColorHelper.getColorList().get(sFolderColor.getSelectedItemPosition()).colorCode);
	        		setFolderAttributePosition();
	        		CryptActivity ca = (CryptActivity)context;
	        		ActivityMessage am = new ActivityMessage(PasswordVaultActivity.PWV_MESSAGE_FOLDER_SAVE, originalHash, null);
	        		ca.setMessage(am);
	        		cancel();
	        		return;
		    	}
		    }
	    });
	    
	    bFolderDelete.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	if(buttonsLock) return;
		    	buttonsLock = true;
		    	
		    	if(position < 0) throw new InvalidParameterException("Position is null");
		    	if(originalHash == null || !originalHash.equals(folder.getFolderSecurityHash())) throw new IllegalStateException("PWVNewEditFolderDialog: Folder hash doesn't match");
		    	CryptActivity ca = (CryptActivity)context;
        		ActivityMessage am = new ActivityMessage(PasswordVaultActivity.PWV_MESSAGE_FOLDER_DELETE, originalHash, position);
        		ca.setMessage(am);
        		buttonsLock = false;
        		cancel();
        		return;
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
	
	private List<String> getOrderList()
	{
		List<String> orderList = new ArrayList<String>();
		orderList.add("ABC...");
		int numberOfFolders = vault.getFolderCount();
		if(dialogMode == PWVFD_MODE_NEW_FOLDER) ++numberOfFolders;
		for(int i = 0; i < numberOfFolders; ++i)
			orderList.add(Integer.toString(i + 1));
		return orderList;
	}
	
	private void setFolderAttributePosition()
	{
		Integer orderPosition = sFolderOrder.getSelectedItemPosition();
		if(orderPosition == 0) orderPosition = null;
		folder.setAttribute(VaultFolder.VAULTFOLDER_ATTRIBUTE_POSITION, orderPosition);
	}
	
    private void prepareFolderDetailForShow()
    {
		etFolderName.setText(folder.getFolderName());
    	etFolderComment.setText(folder.getFolderComment());
    	sFolderColor.setSelection(ColorHelper.getColorPosition(folder.getColorCode()));
    	Integer orderPosition = (Integer)folder.getAttribute(VaultFolder.VAULTFOLDER_ATTRIBUTE_POSITION);
    	if(orderPosition == null) orderPosition = 0;
    	sFolderOrder.setSelection(orderPosition);
    }
	
    private void makeFolderDetailEditable()
    {
	    makeEditableEditText(etFolderName);
	    makeEditableEditText(etFolderComment);
	    sFolderColor.setEnabled(true);
	    sFolderColor.setBackgroundResource(R.drawable.d_edittext);
	    sFolderOrder.setEnabled(true);
	    sFolderOrder.setBackgroundResource(R.drawable.d_edittext);
	    bFolderEditSave.setText(context.getResources().getString(R.string.common_save_text));
	    if (dialogMode != PWVFD_MODE_NEW_FOLDER) dialogMode = PWVFD_MODE_SAVE_FOLDER;
    }
    
    private void makeFolderDetailReadOnly()
    {
	    makeReadOnlyEditText(etFolderName);
	    makeReadOnlyEditText(etFolderComment);
	    sFolderColor.setEnabled(false);
	    sFolderColor.setBackgroundResource(R.drawable.d_edittext_readonly);
	    sFolderOrder.setEnabled(false);
	    sFolderOrder.setBackgroundResource(R.drawable.d_edittext_readonly);
	    bFolderEditSave.setText(context.getResources().getString(R.string.common_edit_text));
    }
    
    private void makeEditableEditText(EditText et)
    {
    	et.setFocusable(true);
    	et.setFocusableInTouchMode(true);
    	et.setEnabled(true);
    	et.setBackgroundResource(R.drawable.d_edittext);
    }
    
    private void makeReadOnlyEditText(EditText et)
    {
    	et.setFocusable(false);
    	et.setFocusableInTouchMode(false);
    	et.setEnabled(false);
    	et.setBackgroundResource(R.drawable.d_edittext_readonly);
    	et.setTextColor(Color.BLACK);
    }
    
    private void disableButtons()
    {
    	bFolderDelete.setEnabled(false);
    	bFolderEditSave.setEnabled(false);
    }
}
