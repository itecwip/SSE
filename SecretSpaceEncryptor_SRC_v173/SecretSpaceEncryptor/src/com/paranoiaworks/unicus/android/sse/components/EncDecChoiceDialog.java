package com.paranoiaworks.unicus.android.sse.components;

import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.FileEncActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

/**
 * If Multiple Files are selected choose what to do
 * 
 * @author Paranoia Works
 * @version 1.0.3
 */ 
public class EncDecChoiceDialog extends SecureDialog {
	
	private Activity context;
	
	private Button continueButton;
	private Button cancelButton;
	private TextView mainTextTV;
	private TextView fileAlreadyExistsTV;
	private EditText fileNameET;
	private CheckBox allToOneFileCB;	
	private LinearLayout enterFileNameContainer;
	private Map<String, CryptFileWrapper> selectedFiles;
	private CryptFileWrapper parentFile = null;
	private CryptFileWrapper customEncFileDestination = null;
	private boolean suppressRewriteWarning = false;

	public EncDecChoiceDialog(View v, TreeMap<String, CryptFileWrapper> selectedFiles) 
	{
		this((Activity)v.getContext(), selectedFiles);
	}	
	
	public EncDecChoiceDialog(Activity context, TreeMap<String, CryptFileWrapper> selectedFiles) 
	{
		super(context);
		this.selectedFiles = selectedFiles;
		this.parentFile = selectedFiles.get(selectedFiles.firstKey()).getParentFile();

		this.context = context;
		this.init();
	}
	
	public void setCustumEncFileDestination(CryptFileWrapper destinationDir)
	{
		this.customEncFileDestination = destinationDir;
	}
	
	public void setSuppressRewriteWarning(boolean suppress)
	{
		this.suppressRewriteWarning = suppress;
	}
		
	private void init()
	{		
		this.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		this.setContentView(R.layout.lc_encdecchoice_dialog);
		this.setTitle(context.getResources().getString(R.string.common_multipleFilesSelected));
		this.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ask_icon_large);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(false);
		
		cancelButton = (Button)this.findViewById(R.id.EDCD_cancel);
		continueButton = (Button)this.findViewById(R.id.EDCD_continue);
		mainTextTV = (TextView)this.findViewById(R.id.EDCD_text);
		allToOneFileCB = (CheckBox)this.findViewById(R.id.EDCD_allToOneFile);
		fileNameET = (EditText)this.findViewById(R.id.EDCD_fileName);
		fileAlreadyExistsTV = (TextView)this.findViewById(R.id.EDCD_fileAlreadyExists);
		enterFileNameContainer = (LinearLayout)this.findViewById(R.id.EDCD_enterFileNameContainer);
		
		fileNameET.setFilters(new InputFilter[] { Helpers.getFileNameInputFilter() });
		allToOneFileCB.setText(Html.fromHtml(context.getResources().getString(R.string.encDecChoiceDialog_allToOneFile)));
		
		int[] encdec = Helpers.getNumberOfEncAndUnenc(selectedFiles);
		
		int willBeEncrypted = encdec[1];
		int willBeDecrypted = encdec[0];
		
		String report = context.getResources().getString(R.string.encDecChoiceDialog_report);
		report = report.replaceAll("<1>", "" + selectedFiles.size())
							.replaceAll("<2>", "" + willBeEncrypted)
								.replaceAll("<3>", "" + willBeDecrypted);
		mainTextTV.setText(Html.fromHtml(report));
		
		continueButton.setOnClickListener(new View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
				if(allToOneFileCB.isChecked() && fileNameET.getText().toString().trim().equals(""))
	    		{
	    			ComponentProvider.getImageToast(context.getResources().getString(R.string.common_enterFileName_text), ImageToast.TOAST_IMAGE_CANCEL, context).show();
	    			return;
	    		}
		    	
		    	CryptActivity ca = (CryptActivity)context;
		    	ca.setMessage(new ActivityMessage(FileEncActivity.FEA_MESSAGE_DIALOG_ENCDECCHOICE, null, allToOneFileCB.isChecked(), fileNameET.getText().toString().trim()));
		    	cancel();
		    }
	    });
		
		cancelButton.setOnClickListener(new View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	cancel();
		    }
	    });
		
		final InputMethodManager imm = (InputMethodManager)context.getSystemService(Service.INPUT_METHOD_SERVICE);
		
		allToOneFileCB.setOnCheckedChangeListener(new OnCheckedChangeListener()
    	{
    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    	    {
    	        if (isChecked)
    	        {
    	        	mainTextTV.setVisibility(View.GONE);
    	        	enterFileNameContainer.setVisibility(View.VISIBLE);
    	        	fileNameET.requestFocus();
    	        	imm.showSoftInput(fileNameET, 0);
    	        	

    	        } else {
    	        	mainTextTV.setVisibility(View.VISIBLE);
    	        	enterFileNameContainer.setVisibility(View.GONE);
    	        	imm.hideSoftInputFromWindow(fileNameET.getWindowToken(), 0); 
    	        	fileNameET.setText("");
    	        	continueButton.setEnabled(true);
    	        }
    	    }
    	});
		
		fileNameET.addTextChangedListener(new TextWatcher() 
	    {
            public void afterTextChanged (Editable s)
            {
        		String fileName = fileNameET.getText().toString().trim() + "." + Encryptor.ENC_FILE_EXTENSION;
        		if(selectedFiles.containsKey(fileName)) {
        			continueButton.setEnabled(false);
        			ComponentProvider.getImageToast(
        					context.getResources().getString(R.string.encDecChoiceDialog_oneOftheSourceFiles), 
        					ImageToast.TOAST_IMAGE_CANCEL, 
        					context).show();
        		} else continueButton.setEnabled(true);
        			 
            	if(!suppressRewriteWarning)
            	{
	        		if((customEncFileDestination != null && customEncFileDestination.existsChild(fileName)) || (customEncFileDestination == null && parentFile.existsChild(fileName)))
	        			fileAlreadyExistsTV.setVisibility(View.VISIBLE);
	        		else fileAlreadyExistsTV.setVisibility(View.GONE);
            	}
            }
            public void  beforeTextChanged (CharSequence s, int start, int count, int after)
            {
            }
            public void onTextChanged (CharSequence s, int start, int before, int count) 
            {
            }
    	});
	}
}
