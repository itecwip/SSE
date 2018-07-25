package com.paranoiaworks.unicus.android.sse.components;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.StaticApp;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.misc.CryptFile;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

/** Steganogram Parameters Settings Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.2
 */ 
public class SteganogramSettingsDialog extends SecureDialog {
	
	private Activity context;
	private int carrierImageOriginalWidth = -1;
	private int carrierImageOriginalHeight = -1;
	private double carrierImageAspectRatio = -1;
	private int outputImageWidth = -1;
	private int outputImageHeight = -1;
	private boolean blockOnChangedListener = false;
	private int parentMessage = -1;
	
	private SeekBar qualitySB;
	private TextView qualityTV;
	private TextView memoryWarningTV;
	private Button cancelButton;
	private Button continueButton;
	private ImageView thumbnailIV;
	private EditText outputImageWidthET;
	private EditText outputImageHeightET;
	private EditText outputImagePercET;
	private LinearLayout memoryWarningontainer;
	private CryptFileWrapper carrierImage;
	
	
	public SteganogramSettingsDialog(View v, CryptFileWrapper carrierImage, int parentMessage) throws Exception 
	{
		this((Activity)v.getContext(), carrierImage, parentMessage);
	}	
	
	public SteganogramSettingsDialog(Activity context, CryptFileWrapper carrierImage, int parentMessage) throws Exception 
	{
		super(context);
		System.gc();
		this.context = context;
		this.carrierImage = carrierImage;
		this.parentMessage = parentMessage;
		this.init();
	}
	
	private void init() throws Exception
	{		
		this.setContentView(R.layout.lc_steganogram_settings_dialog);
		this.setTitle(context.getResources().getString(R.string.common_steganogramParameters));
		this.setCancelable(false);
		this.setCanceledOnTouchOutside(false);
		
		qualitySB = (SeekBar)this.findViewById(R.id.SSD_qualitySB);
		qualityTV = (TextView)this.findViewById(R.id.SSD_qualityTV);
		memoryWarningTV = (TextView)this.findViewById(R.id.SSD_memoryWarningTV);
		thumbnailIV = (ImageView)this.findViewById(R.id.SSD_thumbnailIV);
		cancelButton = (Button)this.findViewById(R.id.SSD_cancelButton);
		continueButton = (Button)this.findViewById(R.id.SSD_continueButton);
		outputImageWidthET = (EditText)this.findViewById(R.id.SSD_outputImageWidth);
		outputImageHeightET = (EditText)this.findViewById(R.id.SSD_outputImageHeight);
		outputImagePercET = (EditText)this.findViewById(R.id.SSD_outputImagePerc);
		memoryWarningontainer = (LinearLayout)this.findViewById(R.id.SSD_memoryWarningContainer);
		
		List<Integer> imageDimension = Helpers.getImageDimension(carrierImage);
		
		carrierImageOriginalWidth = imageDimension.get(0);
		carrierImageOriginalHeight = imageDimension.get(1);
		carrierImageAspectRatio = carrierImageOriginalWidth /(double)carrierImageOriginalHeight;
		
		int thumbnailSizeHeight = StaticApp.dpToPx(150);
		Bitmap thumbnail = Helpers.getDownscaledBitmap(carrierImage, (int)(thumbnailSizeHeight * carrierImageAspectRatio), thumbnailSizeHeight);
		thumbnailIV.setImageBitmap(thumbnail);
		
		setInitialSizes();
		renderOutputSizes(true);
		
		qualityTV.setText(context.getResources().getString(R.string.common_jpegQuality) + " (" + getJpegQuality() + ")");
		
		qualitySB.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				qualityTV.setText(context.getResources().getString(R.string.common_jpegQuality) + " (" + getJpegQuality() + ")"); 
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {		
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		
		outputImageWidthET.addTextChangedListener((new TextWatcher()
    	{
            public void  afterTextChanged (Editable s) {
            	if(blockOnChangedListener) return;
    			renderOutputSizes(false);
            }
            public void  beforeTextChanged (CharSequence s, int start, int count, int after) {

            }
            public void onTextChanged (CharSequence s, int start, int before, int count) {
            	if(blockOnChangedListener) return;
            	String temp = outputImageWidthET.getText().toString();
            	if(temp.trim().length() < 1) return;
            	outputImageWidth = Integer.parseInt(temp);
            	if(outputImageWidth > carrierImageOriginalWidth) setOutputSizesToMax();
    			outputImageHeight = (int)Math.round(outputImageWidth / carrierImageAspectRatio);
            }
    	}));
		
		outputImageHeightET.addTextChangedListener((new TextWatcher()
    	{
            public void  afterTextChanged (Editable s) {
            	if(blockOnChangedListener) return;
    			renderOutputSizes(false);
            }
            public void  beforeTextChanged (CharSequence s, int start, int count, int after) {

            }
            public void onTextChanged (CharSequence s, int start, int before, int count) {
            	if(blockOnChangedListener) return;
            	String temp = outputImageHeightET.getText().toString();
            	if(temp.trim().length() < 1) return;
            	outputImageHeight = Integer.parseInt(temp);
            	if(outputImageHeight > carrierImageOriginalHeight) setOutputSizesToMax();
    			outputImageWidth = (int)Math.round(outputImageHeight * carrierImageAspectRatio);
            }
    	}));
		
		outputImagePercET.addTextChangedListener((new TextWatcher()
    	{
            public void  afterTextChanged (Editable s) {
            	if(blockOnChangedListener) return;
    			renderOutputSizes(false);
            }
            public void  beforeTextChanged (CharSequence s, int start, int count, int after) {

            }
            public void onTextChanged (CharSequence s, int start, int before, int count) {
            	if(blockOnChangedListener) return;
            	String temp = outputImagePercET.getText().toString();
            	if(temp.trim().length() < 1) return;
            	int perc = Integer.parseInt(temp);
            	if(perc > 100) {
            		setOutputSizesToMax();
            		perc = 100;
            	}
            	outputImageWidth = (int)Math.round(carrierImageOriginalWidth * (perc / (double)100));
    			outputImageHeight = (int)Math.round(carrierImageOriginalHeight * (perc / (double)100));
            }
    	}));
		
		cancelButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	cancel();
		    }
	    });
		
		continueButton.setOnClickListener(new android.view.View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
				CryptActivity ca = (CryptActivity)context;
				
				if(outputImageWidth < 1 || outputImageHeight < 1) {
					setInitialSizes();
					renderOutputSizes(true);
					return;
				}
				
				double scale = 1;
				if(carrierImageOriginalWidth > carrierImageOriginalHeight) 
					scale = outputImageWidth / (double)carrierImageOriginalWidth;
				else
					scale = outputImageHeight / (double)carrierImageOriginalHeight;
				
				List<Object> parameters = new ArrayList<Object>();
				parameters.add(carrierImage);
				parameters.add(scale);
				parameters.add(getJpegQuality());
				
        		ActivityMessage am = new ActivityMessage(parentMessage, null, parameters, null);
        		ca.setMessage(am);
				cancel();
				return;
		    }
	    });
		
		outputImageWidthET.setSelection(outputImageWidthET.getText().length());
	}
	
	private void setInitialSizes() {
		int longerSide = 1000;
		
		while(true) 
		{
			if(carrierImageOriginalWidth >= carrierImageOriginalHeight) {
				outputImageWidth = longerSide;
				if(outputImageWidth > carrierImageOriginalWidth) outputImageWidth = carrierImageOriginalWidth;
				outputImageHeight = (int)Math.round(outputImageWidth / carrierImageAspectRatio);
			}
			else {
				outputImageHeight = longerSide;
				if(outputImageHeight > carrierImageOriginalHeight) outputImageHeight = carrierImageOriginalHeight;
				outputImageWidth = (int)Math.round(outputImageHeight * carrierImageAspectRatio);
			}
			
			int memoryTest = (int)Math.round(outputImageHeight * outputImageWidth * 4 * 7.5);
			if(memoryTest > Helpers.getMaxFreeMemory()) 
				longerSide /= 2;
			else break;
		}
	}
	
	private void renderOutputSizes(boolean ignorefocus) {
		blockOnChangedListener = true;
		
		if(!outputImageWidthET.isFocused() || ignorefocus) outputImageWidthET.setText(Integer.toString(outputImageWidth));
		if(!outputImageHeightET.isFocused() || ignorefocus) outputImageHeightET.setText(Integer.toString(outputImageHeight));
		if(!outputImagePercET.isFocused() || ignorefocus) outputImagePercET.setText(Integer.toString((int)Math.round((outputImageWidth / (double)carrierImageOriginalWidth * 100))));
		
		boolean memoryWarning = false;
		boolean longProcessiogWarning = false;
		
		int memoryTest = (int)Math.round(outputImageHeight * outputImageWidth * 4 * 7.5);
		if(memoryTest > Helpers.getMaxFreeMemory()) memoryWarning = true;
		if(outputImageHeight * outputImageWidth > 3000000) longProcessiogWarning = true;
		
		StringBuffer warnings = new StringBuffer();
		
		if(memoryWarning) warnings.append("<font color='#FF0000'>" + context.getResources().getString(R.string.common_insufficientMemoryMaybe) + "</font><br/><br/>");
		if(longProcessiogWarning) warnings.append(context.getResources().getString(R.string.me_steganogramLongTimeWarning) + "<br/><br/>");
		warnings.append(context.getResources().getString(R.string.me_steganogramInsufficientMemoryWhatToDo));
		
		memoryWarningTV.setText(Html.fromHtml(warnings.toString()));
		
		if(memoryWarning || longProcessiogWarning)
			memoryWarningontainer.setVisibility(LinearLayout.VISIBLE);
		else
			memoryWarningontainer.setVisibility(LinearLayout.GONE);
		
		blockOnChangedListener = false;
	}
	
	private void setOutputSizesToMax() {
		blockOnChangedListener = true; 
		outputImageWidth = carrierImageOriginalWidth;
		outputImageHeight = carrierImageOriginalHeight;
		outputImageWidthET.setText(Integer.toString(outputImageWidth));
		outputImageHeightET.setText(Integer.toString(outputImageHeight));
		outputImagePercET.setText(Integer.toString((int)Math.round((outputImageWidth / (double)carrierImageOriginalWidth * 100))));
		blockOnChangedListener = false;
	}
	
	private int getJpegQuality()
	{
		return qualitySB.getProgress() + 50;
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {           
            return true;
        } else return super.onKeyDown(keyCode, event);
    }
}