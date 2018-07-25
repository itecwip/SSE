package com.paranoiaworks.unicus.android.sse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.paranoiaworks.android.sse.interfaces.SettingsCheckBoxCustom;
import com.paranoiaworks.unicus.android.sse.components.SelectionDialog;
import com.paranoiaworks.unicus.android.sse.components.SimpleHTMLDialog;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.misc.CryptFile;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

import ext.com.nononsenseapps.filepicker.FilePickerActivity;

/**
 * Settings activity class
 * 
 * @author Paranoia Works
 * @version 1.0.10
 * @related SettingDataHolder.java, data.xml
 */ 
public class SettingsActivity extends CryptActivity  {	

	private List<SettingCategory> settings;
	private List<View> viewsContainer = new ArrayList<View>();
	
	private LinearLayout settingsLL;
	private Button saveButton;
	private Button helpButton;
	private String currentValueText = "";
	private Context context;
	private View lastClickedView;
	private boolean lastDeliveryFailed = false;
	
	private static final int S_HANDLE_SELECTOR = -4001;
	private static final int S_HANDLE_CHECKBOX = -4002;
	private static final int S_HANDLE_TEXT = -4003;
	private static final int S_HANDLE_TEXT_PATH_OR_NULL = -4004;
	private static final int S_HANDLE_TEXT_PATH = 4001; // request code has to be > 0
	
	private static final String SELECTOR_ITEM_PREFIX = "SIV_";
	public static final String PATH_DISABLED = "///";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	this.setContentView(R.layout.la_settings);
    	setTitle(getResources().getString(R.string.common_app_settings_name));
    	context = this;
    	
    	loadSetting(); //load Settings
    	
    	currentValueText = getResources().getString(R.string.common_current_text) + ": " ;
    	  	
        // Create settings list (graphical representation)
    	settingsLL = (LinearLayout)findViewById(R.id.S_settingsList);
    	for(SettingCategory settingCategory : settings)
        {     		
        	TextView catView = (TextView)getLayoutInflater().inflate(R.layout.lct_settingcategory_name, null);
        	catView.setText(getStringResource(settingCategory.categoryName));
        	settingsLL.addView((View)catView);
        	
        	for(int i = 0; i < settingCategory.itemsList.size(); ++i)
            {
        		SettingItem settingItem = settingCategory.itemsList.get(i);
        		View delimiter = getLayoutInflater().inflate(R.layout.lct_delimiter_thin_grey, null);
        		settingsLL.addView((View)getViewForItem(settingItem));
            	if(i + 1 < settingCategory.itemsList.size()) settingsLL.addView(delimiter);
            }
        }
        
        // Save Button
        saveButton = (Button)findViewById(R.id.S_saveButton);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	saveSettings();
	    		setRunningCode(0);
	    		finish();
		    }
	    });
        
	    // Help Button
        helpButton = (Button)findViewById(R.id.S_helpButton);
        helpButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	SimpleHTMLDialog simpleHTMLDialog = new SimpleHTMLDialog(v);
		    	simpleHTMLDialog.loadURL(getResources().getString(R.string.helpLink_Settings));
		    	simpleHTMLDialog.show();
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
	        case S_HANDLE_SELECTOR:
	        {
	        	View clickedView = (View)am.getAttachement();
		    	LinearLayout leftView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(0);
		    	LinearLayout rightView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(1);
		    	View dataHolderView = ((LinearLayout)(rightView.getChildAt(0))).getChildAt(0);
		    	TextView currentValueTextView = (TextView)((LinearLayout)(leftView.getChildAt(0))).getChildAt(2);    	
		    	SettingItem si = (SettingItem)dataHolderView.getTag();
	        	
		    	si.itemValue = (String)am.getMainMessage();
		    	currentValueTextView.setText(Html.fromHtml(currentValueText + highlightText(getSelectorItemName(si))));
		    	currentValueTextView.setMinWidth(10); //force refresh
		    	handleAlteration();
	        	
	        	this.resetMessage();
	            break;
	        }
	            
	        case S_HANDLE_CHECKBOX:
	        {
	        	View clickedView = (View)am.getAttachement();
		    	LinearLayout rightView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(1);
		    	View dataHolderView = ((LinearLayout)(rightView.getChildAt(0))).getChildAt(0);
		    	SettingItem si = (SettingItem)dataHolderView.getTag();
		    	
				CheckBox tempView = (CheckBox)dataHolderView;
				si.itemValue = Boolean.toString(!tempView.isChecked());
				tempView.setChecked(Boolean.parseBoolean(si.itemValue));
		    	
	        	this.resetMessage();
	            break;
	        }
	        
	        case S_HANDLE_TEXT_PATH_OR_NULL:
	        {
	        	String mm = am.getMainMessage();
	        	
	        	View clickedView = lastClickedView;
		    	LinearLayout leftView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(0);
		    	LinearLayout rightView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(1);
		    	View dataHolderView = ((LinearLayout)(rightView.getChildAt(0))).getChildAt(0);
		    	TextView currentValueTextView = (TextView)((LinearLayout)(leftView.getChildAt(0))).getChildAt(2);    	
		    	SettingItem si = (SettingItem)dataHolderView.getTag();
	        	
	        	if(mm.equals("set"))
	        	{
	        		if(lastClickedView != null)
	        			startActivityForResult(getDirPickerIntent(si.itemValue), S_HANDLE_TEXT_PATH);	
	        	}
	        	else if(mm.equals("disable"))
	        	{        	
			    	si.itemValue = PATH_DISABLED;
			    	currentValueTextView.setText(getResources().getString(R.string.common_disabled_text));
			    	currentValueTextView.setMinWidth(10); //force refresh
			    	handleAlteration();
	        	}
	        	
	        	this.resetMessage();
	            break;
	        }
	        
	        case S_HANDLE_TEXT:
	        {
	        	View clickedView = (View)am.getAttachement();
		    	LinearLayout leftView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(0);
		    	LinearLayout rightView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(1);
		    	View dataHolderView = ((LinearLayout)(rightView.getChildAt(0))).getChildAt(0);
		    	TextView currentValueTextView = (TextView)((LinearLayout)(leftView.getChildAt(0))).getChildAt(2);    	
		    	SettingItem si = (SettingItem)dataHolderView.getTag();
	        	
		    	si.itemValue = (String)am.getMainMessage();
		    	currentValueTextView.setText(Html.fromHtml(currentValueText + highlightText(si.itemValue)));
		    	currentValueTextView.setMinWidth(10); //force refresh
		    	handleAlteration();
	        	
	        	this.resetMessage();
	            break;
	        }
	        
        	case COMMON_MESSAGE_CONFIRM_EXIT:
				if(am.getAttachement() != null && am.getAttachement().equals(new Integer(1)))
				{
					saveSettings();
				}
				setRunningCode(0);
	    		finish();
        		break;
	
	        default: 
	        	break;
        }
	}
    
	
    /** 
     * Create main settings object ("List<SettingCategory> settings")  
	 * using SettingDataHolder.java and resource file Data.xml
	 */
	private void loadSetting()
	{
		Map<String, SettingCategory> categories = new HashMap<String, SettingCategory>();
		Map<String, String> comments = new HashMap<String, String>();
		Resources resources = this.getResources();
		String[] settingsCategoriesTemp = resources.getStringArray(R.array.settings_categories);
    	String[] settingsItemsTemp = StaticApp.licenseLevel < 2 ? resources.getStringArray(R.array.settings_items) : resources.getStringArray(R.array.settings_items);
    	String[] settingsCommentsTemp = resources.getStringArray(R.array.settings_comments);
    	
        for(String settingCategory : settingsCategoriesTemp) 
        {
        	String[] parameters = settingCategory.split("\\|\\|");
        	SettingCategory sc = new SettingCategory();
        	sc.categoryName = parameters[0];
        	sc.categorySequence = Integer.parseInt(parameters[1]);
        	
        	categories.put(sc.categoryName , sc);
        }
        
        for(String settingsComments : settingsCommentsTemp) 
        {
        	String[] parameters = settingsComments.split("\\|\\|");
        	String id = parameters[0] + ":" + parameters[1];
        	
        	comments.put(id , parameters[2]);
        }
        
        for(String settingItem : settingsItemsTemp)
        {
        	String[] parAndDir = settingItem.split("!!");
        	String[] parameters = parAndDir[0].split("\\|\\|");
        	SettingItem si = new SettingItem();
        	si.parentCategoryName = parameters[0];
        	si.itemName = parameters[1];
        	si.itemSequence = Integer.parseInt(parameters[2]);
        	si.itemType = parameters[3];
        	si.itemValueNames = parameters[4].split("\\|");
        	si.itemValue = settingDataHolder.getItem(si.parentCategoryName, si.itemName);
        	
        	if(parAndDir.length > 1) 
        	{
        		Map<String, String> directiveMap = new HashMap<String, String>();
        		String[] directives = parAndDir[1].split("\\|\\|");
        		for(String directiveItem : directives)
        		{
        			String[] directive = directiveItem.split("\\|");
        			directiveMap.put(directive[0], directive[1]);
        		}
        		si.directives = directiveMap;
        	}
        	
        	// Ignore Items based on "api_version" directive
        	if(si.directives != null) 
        	{
        		String directiveS = si.directives.get("apiversion");
        		if(directiveS != null) 
        		{
	        		int apiversionDirective = Integer.parseInt(directiveS);
	        		if(android.os.Build.VERSION.SDK_INT < apiversionDirective) continue;
        		}
        	}
        	
        	String commentTemp = comments.get(si.getFullyQualifiedItemName());
        	if(commentTemp != null) si.itemComment = commentTemp;
        	
        	SettingCategory sc = categories.get(si.parentCategoryName);
        	if(sc != null) sc.itemsList.add(si);
        }
        
        List<SettingCategory> categoriesList = new ArrayList<SettingCategory>(categories.values());
        
        Collections.sort(categoriesList);
        for(SettingCategory settingCategory : categoriesList)
        {
        	Collections.sort(settingCategory.itemsList);
        }
        
        settings = categoriesList;
	}
    
	
	/** Create Graphical Representation of SettingItem Object */
	private View getViewForItem(SettingItem si)
	{
		RelativeLayout returnViewWrapper = (RelativeLayout)getLayoutInflater().inflate(R.layout.lct_settingitem_root, null);
		LinearLayout returnViewLeft = (LinearLayout)returnViewWrapper.getChildAt(0);
		LinearLayout returnViewRight = (LinearLayout)returnViewWrapper.getChildAt(1);
		LinearLayout itemTexts = (LinearLayout)getLayoutInflater().inflate(R.layout.lct_settingitem_texts, null);
		
		TextView itemTitle = (TextView)itemTexts.getChildAt(0);
		TextView itemComment = (TextView)itemTexts.getChildAt(1);
		TextView currentValue = (TextView)itemTexts.getChildAt(2);
		
    	itemTitle.setText(getStringResource(si.itemName));

		if(si.itemComment != null)
		{
			itemComment.setVisibility(TextView.VISIBLE);
			itemComment.setText(getStringResource(si.itemComment));
		}
		returnViewLeft.addView(itemTexts);
		
		returnViewWrapper.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	//LinearLayout leftView = (LinearLayout)((RelativeLayout)v).getChildAt(0);
		    	LinearLayout rightView = (LinearLayout)((RelativeLayout)v).getChildAt(1);
		    	View dataHolderView = ((LinearLayout)(rightView.getChildAt(0))).getChildAt(0);	    						  
		    	SettingItem si = (SettingItem)dataHolderView.getTag();	    	
		    	
				if(dataHolderView.getClass().equals(CheckBox.class)) // for checkbox
				{
					setMessage(new ActivityMessage(S_HANDLE_CHECKBOX, "", v));
				}
				else if(dataHolderView.getClass().equals(ImageView.class) && si.itemType.equalsIgnoreCase("selector")) // for selector
				{
					String[] valuesCopy = new String[si.itemValueNames.length];
					System.arraycopy(si.itemValueNames, 0, valuesCopy, 0, si.itemValueNames.length);
					List<String> itemList = (Arrays.asList(valuesCopy));
			    	List<String> keyList = new ArrayList<String>();
			    	
			    	for(int i = 0; i < itemList.size(); ++i)
			    	{
			    		String itemValue = itemList.get(i);
			    		String[] values = itemValue.split("::");
			    		if(values[0].startsWith(SELECTOR_ITEM_PREFIX)) itemList.set(i, getStringResource(values[0]));
			    		else itemList.set(i, values[0]);
			    		keyList.add(values[1]);
			    	}

			    	AlertDialog selectionDialog = (AlertDialog)ComponentProvider.getItemSelectionDialog(
			    			v, 
			    			getStringResource(si.itemName),
			    			itemList,
			    			keyList,
			    			S_HANDLE_SELECTOR,
			    			v,
			    			getSelectorItemPosition(si)
			    			);
			    	selectionDialog.show();
				}
				else if(dataHolderView.getClass().equals(ImageView.class) && si.itemType.toLowerCase().startsWith("text"))  // for text (special types: path, pathornull)
				{
					String[] specials = si.itemType.toLowerCase().split(":");
					
					if(specials.length < 2 || android.os.Build.VERSION.SDK_INT < 12 || lastDeliveryFailed)
					{
						Dialog enterTextDialog = ComponentProvider.getTextSetDialog(v,
								getStringResource(si.itemName), 
								si.itemValue, 
								S_HANDLE_TEXT, 
								v
								);
						enterTextDialog.show();
					} 
					else
					{
						if(specials[1].equals("path"))
						{
		    		        lastClickedView = v;
		    		        if(lastClickedView != null)
		    		        	startActivityForResult(getDirPickerIntent(si.itemValue), S_HANDLE_TEXT_PATH);	    		        
						}
						else if (specials[1].equals("pathornull"))
						{
							lastClickedView = v;
							if(si.itemValue.equals(PATH_DISABLED)) // disabled
							{
								if(lastClickedView != null)
									startActivityForResult(getDirPickerIntent(Helpers.getImportExportPath()), S_HANDLE_TEXT_PATH);
							}
							else
							{
								List<String> title = new ArrayList<String>();
						    	List<Object> tag = new ArrayList<Object>();	 
						    	
						    	title.add(getResources().getString(R.string.common_folder_text));
						    	tag.add("set");
						    	title.add(getResources().getString(R.string.common_disable_text));
						    	tag.add("disable");
								
						    	SelectionDialog selectDirDialog = new SelectionDialog(v, title, null, null, tag);	    	
						    	selectDirDialog.setMessageCode(S_HANDLE_TEXT_PATH_OR_NULL);
						    	selectDirDialog.show();
							}
						}
					}
				}

		    	//SSElog.d(dataHolderView.getTag().toString() + " : " + dataHolderView.getClass().getName());	
		    }
	    });
		
		if(si.itemType.equalsIgnoreCase("checkbox"))  // for checkbox
		{
			LinearLayout rl = (LinearLayout)getLayoutInflater().inflate(R.layout.lct_settingitem_checkbox, null);
			CheckBox view = (CheckBox)rl.getChildAt(0);
			view.setChecked(Boolean.parseBoolean(si.itemValue));
			view.setTag(si.getTag());
			currentValue.setVisibility(TextView.GONE);
			
			view.setOnCheckedChangeListener(new OnCheckedChangeListener()
	    	{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	    	    {
					handleAlteration();
	    	    }
	    	});
			
			returnViewRight.addView(rl);
			viewsContainer.add(view);
		}
		else if(si.itemType.toLowerCase().startsWith("checkboxcustom")) 
		{
			LinearLayout rl = (LinearLayout)getLayoutInflater().inflate(R.layout.lct_settingitem_checkbox, null);
			final CheckBox view = (CheckBox)rl.getChildAt(0);
			view.setChecked(Boolean.parseBoolean(si.itemValue));
			view.setTag(si.getTag());
			currentValue.setVisibility(TextView.GONE);
			
			final String dialogName = si.itemType.split(":")[1];
			final List<Boolean> blockListener = new ArrayList<Boolean>();
			blockListener.add(false);
			
			view.setOnCheckedChangeListener(new OnCheckedChangeListener()
	    	{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked)
	    	    {
					if(blockListener.get(0)){
		        		blockListener.set(0, false);
						return;
					}
					
					final Handler cbHandler = new Handler() 
				    {
				        public void handleMessage(Message msg)  
				        {
				        	if (msg.what == SettingsCheckBoxCustom.OK)
				        	{ 
				        		handleAlteration();
				        	} 
				        	else
				        	{
				        		blockListener.set(0, true);
						    	SettingItem si = (SettingItem)view.getTag();
								si.itemValue = Boolean.toString(!isChecked);
				        		view.setChecked(!isChecked);
				        	}
				        }
				    };
					
					try {
						String dialogClassName = "com.paranoiaworks.unicus.android.sse.components." + dialogName;
						Class<?> dialogClass = Class.forName(dialogClassName);
						Object dialogObject = dialogClass.getConstructor(View.class, Handler.class).newInstance(buttonView, cbHandler);
						
						if(!isChecked)
							dialogClass.getMethod("doOnUncheck").invoke(dialogObject);
						else
							dialogClass.getMethod("doOnCheck").invoke(dialogObject);
						
					} catch (Exception e) {
						e.printStackTrace();
					} 
	    	    }
	    	});
			
			returnViewRight.addView(rl);
			viewsContainer.add(view);
		}
		else if(si.itemType.equalsIgnoreCase("selector"))  // for selector
		{
			LinearLayout rl = (LinearLayout)getLayoutInflater().inflate(R.layout.lct_settingitem_selector, null);
			ImageView view = (ImageView)rl.getChildAt(0);
			String currentValueString = getSelectorItemName(si);
			currentValue.setText(Html.fromHtml(currentValueText + highlightText(currentValueString)));
			view.setTag(si.getTag());			
		
			returnViewRight.addView(rl);
			viewsContainer.add(view);
		}
		else if(si.itemType.toLowerCase().startsWith("text"))  // for text
		{		
			String[] specials = si.itemType.toLowerCase().split(":");
			
			LinearLayout rl = (LinearLayout)getLayoutInflater().inflate(R.layout.lct_settingitem_entertext, null);
			ImageView view = (ImageView)rl.getChildAt(0);
			
			if(specials.length < 2 || specials[1].equals("path") || !si.itemValue.equals(PATH_DISABLED))
			{
				currentValue.setText(Html.fromHtml(currentValueText + highlightText(si.itemValue)));
			}
			else
			{
				currentValue.setText(getResources().getString(R.string.common_disabled_text));
			}
			view.setTag(si.getTag());
			
			returnViewRight.addView(rl);
			viewsContainer.add(view);
		}
		return returnViewWrapper;
	}
	
	
    /** Is current ("memory") version of Settings saved in the application DB? */
    private boolean checkForAlteration()
	{
    	boolean altered = false;
		
		for(View tempView : viewsContainer)
    	{
    		SettingItem itemFromView = (SettingItem)tempView.getTag();
    		String itemValueFromView = itemFromView.itemValue;
    		String itemValueFromHolder = settingDataHolder.getItem(itemFromView.parentCategoryName, itemFromView.itemName);
    		
    		if(!itemValueFromView.equals(itemValueFromHolder))
    		{
    			altered = true;
    			break;
    		}
    	}
		return altered;
	}
	
	
    /** Save current Settings to application DB */
    private void saveSettings()
	{
    	for(View tempView : viewsContainer)
    	{
    		SettingItem item = (SettingItem)tempView.getTag();
    		settingDataHolder.addOrReplaceItem(item.parentCategoryName, item.itemName, item.itemValue);
    	}
    	
		settingDataHolder.save();
	}
	
	
	/** Enable Save Button - if current Setting is not saved in the application DB */
	private void handleAlteration()
	{
    	if(checkForAlteration())
    		saveButton.setEnabled(true);
    	else 
    		saveButton.setEnabled(false); 
	}
	
    /** Keeps Setting Category Data */
	private static class SettingCategory implements Comparable<SettingCategory>
    {    
        protected String categoryName = null;
        protected Integer categorySequence = -1;
        protected List<SettingItem> itemsList = new ArrayList<SettingItem>();
        
    	public int compareTo(SettingCategory category)
    	{ 		
    		return this.categorySequence.compareTo(category.categorySequence);
    	}
    }
	
	/** Keeps Setting Item Data */
	private static class SettingItem implements Comparable<SettingItem>
    {    
    	protected String parentCategoryName = null;
    	protected String itemName = null;
        protected Integer itemSequence  = -1;
        protected String itemType = null;
        protected String[] itemValueNames = null;
        protected String itemValue = null;
        protected String itemComment = null;
        protected Map<String, String> directives = null;
        
    	public String getFullyQualifiedItemName()
    	{ 		
    		return parentCategoryName + ":" + itemName;
    	}
        
        public SettingItem getTag()
    	{ 		
    		return this;
    	}
        
    	public int compareTo(SettingItem item)
    	{ 		
    		return this.itemSequence.compareTo(item.itemSequence);
    	}
    }
	
    @Override
    protected void onStart ()
    {
        setRunningCode(RUNNING_SETTINGSACTIVITY);
    	super.onStart();
    }
    
    private static String highlightText(String text)
    {
    	return "<font color='#FFFFC0'>" + text + "</font>";
    }
    
    private Intent getDirPickerIntent(String startDir)
    {
		Intent intent = new Intent(context, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
        intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
        intent.putExtra(FilePickerActivity.EXTRA_START_PATH, startDir);
        
        return intent;
    }
    
    private int getSelectorItemPosition(SettingItem si)
    {
    	for(int i = 0; i < si.itemValueNames.length; ++i)
    	{
    		if(si.itemValueNames[i].endsWith("::" + si.itemValue))
    			return i;
    	}
    	return -1;
    }
    
    private String getSelectorItemName(SettingItem si)
    {
    	String name = null;
    	for(int i = 0; i < si.itemValueNames.length; ++i)
    	{  		
    		if(si.itemValueNames[i].endsWith("::" + si.itemValue))
    		{
    			name = si.itemValueNames[i].split("::")[0];
    			if(name.startsWith(SELECTOR_ITEM_PREFIX)) 
    				name = getStringResource(name);
    			break;
    		}
    	}
    	return name;
    }
    
    /** Back Button - exit to Main Menu, if Settings are altered ask for save and exit to Main Menu */
    @Override
    public void onBackPressed()
    {
		if(checkForAlteration())
		{
    		ComponentProvider.getBaseQuestionDialog(this, 
					getResources().getString(R.string.common_save_text),  
    				getResources().getString(R.string.common_question_saveChanges), 
    				null, 
    				COMMON_MESSAGE_CONFIRM_EXIT
    				).show();
		}
		else setMessage(new ActivityMessage(COMMON_MESSAGE_CONFIRM_EXIT, null));
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {		
		if (requestCode == S_HANDLE_TEXT_PATH) 
		{
			if(resultCode == RESULT_OK)
			{
				CryptFileWrapper selectedDir = new CryptFileWrapper(new CryptFile(resultData.getData().getPath()));
				if(selectedDir.getWritePermissionLevelForDir() < 2)
				{
					ComponentProvider.getShowMessageDialog(this, getResources().getString(R.string.fe_readOnlyWarningTitle), getResources().getString(R.string.fe_directoryReadOnly), ComponentProvider.DRAWABLE_ICON_CANCEL).show();
					return;
				}
			
				try {
					View clickedView = lastClickedView;
					LinearLayout leftView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(0);
					LinearLayout rightView = (LinearLayout)((RelativeLayout)clickedView).getChildAt(1);
					View dataHolderView = ((LinearLayout)(rightView.getChildAt(0))).getChildAt(0);
					TextView currentValueTextView = (TextView)((LinearLayout)(leftView.getChildAt(0))).getChildAt(2);    	
					SettingItem si = (SettingItem)dataHolderView.getTag();
					
					si.itemValue = selectedDir.getAbsolutePath();
					currentValueTextView.setText(Html.fromHtml(currentValueText + highlightText(si.itemValue)));
					currentValueTextView.setMinWidth(10); //force refresh
					handleAlteration();
				} catch (Exception e) {
					ComponentProvider.getImageToast("Something went wrong. Please try again.", ComponentProvider.DRAWABLE_ICON_CANCEL, this).show();
					lastDeliveryFailed = true;
					e.printStackTrace();
				}
			}		
		}
	}
}
