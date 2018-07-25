package com.paranoiaworks.unicus.android.sse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.DataFormatException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.paranoiaworks.android.sse.interfaces.Lockable;
import com.paranoiaworks.unicus.android.sse.adapters.ColorListAdapter;
import com.paranoiaworks.unicus.android.sse.adapters.PWVFolderAdapter;
import com.paranoiaworks.unicus.android.sse.adapters.PWVItemArrayAdapter;
import com.paranoiaworks.unicus.android.sse.components.ImageToast;
import com.paranoiaworks.unicus.android.sse.components.PWVNewEditFolderDialog;
import com.paranoiaworks.unicus.android.sse.components.PasswordDialog;
import com.paranoiaworks.unicus.android.sse.components.PasswordGeneratorDialog;
import com.paranoiaworks.unicus.android.sse.components.ScreenLockDialog;
import com.paranoiaworks.unicus.android.sse.components.SelectionDialog;
import com.paranoiaworks.unicus.android.sse.components.SimpleHTMLDialog;
import com.paranoiaworks.unicus.android.sse.components.SimpleWaitDialog;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;
import com.paranoiaworks.unicus.android.sse.dao.PasswordAttributes;
import com.paranoiaworks.unicus.android.sse.dao.Vault;
import com.paranoiaworks.unicus.android.sse.dao.VaultFolder;
import com.paranoiaworks.unicus.android.sse.dao.VaultItem;
import com.paranoiaworks.unicus.android.sse.misc.CryptFile;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;
import com.paranoiaworks.unicus.android.sse.misc.KeyboardAppearanceDetector;
import com.paranoiaworks.unicus.android.sse.misc.KeyboardAppearanceDetector.KeyboardVisibilityChangedListener;
import com.paranoiaworks.unicus.android.sse.utils.ColorHelper;
import com.paranoiaworks.unicus.android.sse.utils.ComponentProvider;
import com.paranoiaworks.unicus.android.sse.utils.DBHelper;
import com.paranoiaworks.unicus.android.sse.utils.Encryptor;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

import ext.com.andraskindler.quickscroll.QuickScroll;

/**
 * Password Vault activity class
 * 
 * @author Paranoia Works
 * @version 1.1.6
 */ 
public class PasswordVaultActivity extends CryptActivity implements Lockable {
	
	private int encryptAlgorithmCode;
	private boolean askOnLeave;
	private boolean lockOnPause;
	private boolean showBottomMenu;
	private boolean sldVeto = false;
	private int screenLockedPosition = -1;
	private Vault vault = null;
	private PWVFolderAdapter iconAdapter = null;
	private ViewAnimator layoutSwitcher;
	private SpinnerAdapter itemColorSpinnerAdapter = new ColorListAdapter(this, ColorHelper.getColorList(), ColorListAdapter.ICONSET_ITEMS);
	private Dialog waitDialog;
	private ScreenLockDialog sld;
	private PasswordDialog changePasswordDialog;
	private SelectionDialog moreDialog;
	private Toast commonToast;
	private KeyboardAppearanceDetector ked;
	
	// Start Layout
	private LinearLayout layoutStartButtons;
	private Button toMainPageButton;
	private Button helpMeButton;
		
	// Folders Layout
	private List<VaultItem> currentItems = new ArrayList<VaultItem>();
	private LinearLayout foldersBottomMenu;
	private Button foldersMoreButton;
	private Button foldersNewFolderButton;
	private Button foldersHelpButton;
	private Button showMenuButton;
	
	// Items Layout
	private VaultFolder currentFolder;	
	private PWVItemArrayAdapter itemsArrayAdapter;
	private ListView itemsListView;	
	private QuickScroll itemsQuickscroll;
		
	// Item Detail Layout
	private VaultItem currentItem;	
	private EditText itemNameEditText;
	private EditText itemPasswordEditText;
	private EditText itemCommentEditText;
	private Spinner itemColorSpinner;
	private TextView itemDeleteButton;
	private TextView itemEditSaveButton;
	private TextView itemMoveToButton;
	private Button passwordGeneratorButton;
	private Button nameToClipboardButton;
	private Button passwordToClipboardButton;
	private Button noteToClipboardButton;
	private Button switchTopBottomButton;
	private Button passwordToTextEncryptorButton;
	private Button passwordToFileEncryptorButton;
	private TextView noteCharCounter;
	private View topLeftContainer;
	private View bottomRightContainer;
	private View bottomButtonLine;
	private View bottomDelimiter;
	private View passwordContainer;

	
	public static final String PWV_DBPREFIX = "PASSWORD_VAULT";
	public static final String PWV_EXTRA_PASSWORD = "EXTRA_PASSWORD";
	public static final String PWV_EXTRA_LOCKSCREEN_KEY = "LOCKSCREEN_KEY";
	public static final String PWV_EXTRA_LOCKSCREEN_ON = "LOCKSCREEN_ON";
	public static final String PWV_EXPORT_EXT = "pwv";
	private static final int PWV_FORMAT_VERSION = 2;
	private static final int REQUEST_CODE_SEND_PASSWORD = 101;
		
	public static final int PWV_MESSAGE_FOLDER_NEW = -1101;
	public static final int PWV_MESSAGE_FOLDER_SAVE = -1102;
	public static final int PWV_MESSAGE_FOLDER_DELETE = -1103;
	public static final int PWV_MESSAGE_FOLDER_DELETE_CONFIRM = -1104;
	public static final int PWV_MESSAGE_ITEM_DELETE_CONFIRM = -1105;
	public static final int PWV_MESSAGE_ITEM_MOVETOFOLDER = -1106;
	public static final int PWV_MESSAGE_ITEM_SAVE_CONFIRM = -1107;
	public static final int PWV_MESSAGE_ITEM_NOTE_COPY = -1108;
	public static final int PWV_MESSAGE_MOREDIALOG = -1201;
	public static final int PWV_MESSAGE_MOREDIALOG_IMPORT = -1202;
	public static final int PWV_MESSAGE_MOREDIALOG_IMPORT_CONFIRM = -1203;
	public static final int PWV_MESSAGE_MOREDIALOG_RESET_CONFIRM = -1204;
	public static final int PWV_MESSAGE_MOREDIALOG_IMPORT_CONFIRM_XML = -1205;
	public static final int PWV_MESSAGE_MOREDIALOG_IMPORT_CONFIRM_XML_PASSWORD = -1206;
	public static final int PWV_MESSAGE_MOREDIALOG_MERGE = -1207;
	public static final int PWV_MESSAGE_MOREDIALOG_MERGE_CONFIRM = -1208;
	public static final int PWV_MESSAGE_PWGDIALOG_SET = -1301;
	public static final int PWV_MESSAGE_PWGDIALOG_SET_CONFIRM = -1302;
	public static final int PWV_MESSAGE_SCREENLOCK_UNLOCK = -1401;
	public static final int PWV_UNIVERSALHANDLER_SHOW_WAITDIALOG = -1501;
	public static final int PWV_UNIVERSALHANDLER_HIDE_WAITDIALOG = -1502;
	public static final int PWV_UNIVERSALHANDLER_SHOW_ERRORDIALOG = -1503;
	public static final int PWV_UNIVERSALHANDLER_MERGE_FINALIZE = -1504;
	
	private static final int PWV_LAYOUT_START = 0;
	private static final int PWV_LAYOUT_FOLDERS = 1;
	private static final int PWV_LAYOUT_ITEMS = 2;
	private static final int PWV_LAYOUT_ITEMDETAIL = 3;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	this.setTheme(R.style.ThemeAltB);
    	super.onCreate(savedInstanceState);
    	this.setContentView(R.layout.la_passwordvault);
    	this.setTitle(getResources().getString(R.string.common_app_passwordVault_name));
    	encryptAlgorithmCode = settingDataHolder.getItemAsInt("SC_PasswordVault", "SI_Algorithm");
    	askOnLeave = settingDataHolder.getItemAsBoolean("SC_Common", "SI_AskIfReturnToMainPage");
    	lockOnPause = settingDataHolder.getItemAsBoolean("SC_PasswordVault", "SI_LockScreen");
    	showBottomMenu = settingDataHolder.getItemAsBoolean("SC_PasswordVault", "SI_ShowMenu");
    	
    	layoutSwitcher = (ViewAnimator) findViewById(R.id.vaultLayoutSwitcher);
    	initLayoutStart();
    	
    	commonToast = new ImageToast("***", ImageToast.TOAST_IMAGE_CANCEL, this);
    }
    
    
    /** Handle Message */
    void processMessage()
    {
        ActivityMessage am = getMessage();
        if (am == null) return;
        
        int messageCode = am.getMessageCode();   
        String mainMessage = am.getMainMessage();
        switch (messageCode) 
        {        
        	case CryptActivity.COMMON_MESSAGE_SET_ENCRYPTOR:   		
        		if(mainMessage.equals("merge"))
            	{
        			Encryptor tempEncryptor = (Encryptor)((List)am.getAttachement()).get(1);
        			List<Object> vaultParam = (List<Object>)am.getAttachement2();
        			
        			Vault tempVault = null;
        					
        			try {
						tempVault = deserializeVault((byte[])vaultParam.get(0), (Integer)vaultParam.get(1), tempEncryptor);
					} catch (Exception e) {
						// swallow
					}
        			
        			if(tempVault != null) {
        				mergeVaults(tempVault);        				
        			}
        			else {
        				ComponentProvider.getShowMessageDialog(this, 
        						getResources().getString(R.string.pwv_mergeVaults), 
        						getResources().getString(R.string.me_decryptError), 
        						ComponentProvider.DRAWABLE_ICON_INFO_RED).show();
        			}
        			
        			this.resetMessage();
        			return;
            	}
        		
        		this.passwordAttributes = (PasswordAttributes)((List)am.getAttachement()).get(0);
            	this.encryptor = (Encryptor)((List)am.getAttachement()).get(1);
            	
            	if(mainMessage.equals("enter"))
            	{
	            	try {
	        			vault = loadVaultfromDB(); 
	            	} catch (DataFormatException e) { // corrupted data probably
	            		ComponentProvider.getShowMessageDialog(this, null, e.getLocalizedMessage(), ComponentProvider.DRAWABLE_ICON_CANCEL).show();
		    			encryptor = null;
		    			this.resetMessage();
		    			return;
	            	} catch (Exception e) {
	        			getStartScreenPasswordDialog().show();
	        			Toast tt = new ImageToast(
	        					this.getResources().getString(R.string.pwv_failedOnEnter), 
	        					ImageToast.TOAST_IMAGE_CANCEL, this);
		    			tt.show();
		    			encryptor = null;
		    			this.resetMessage();
		    			return;
	        		}
	        		
	        		if (vault == null)
	        		{
	        			vault = getVaultOnFirstRun(null);
	        			try {
							saveVaultToDB();
						} catch (Exception e) {
							e.printStackTrace();
							showErrorDialog(e);
						}
	        		}
	        		
	        		if(vault.isIntegrityCheckFailed())
	        			ComponentProvider.getShowMessageDialog(this, null, "CRC Check Warning", ComponentProvider.DRAWABLE_ICON_INFO_RED).show();
            	     	
	        		vault.notifyFolderDataSetChanged();
	        		initLayoutFolders();
	            	initLayoutItems();
	            	initLayoutItemDetail();
	
	    	        this.resetMessage();
	    	        layoutSwitcher.showNext();
            	}
            	
            	if(mainMessage.equals("change") && vault != null && encryptor != null)
            	{        		
            		try {
						saveVaultToDB();
					} catch (Exception e) {
		        		Toast tt = new ImageToast(e.getMessage(), ImageToast.TOAST_IMAGE_CANCEL, this);
		        		tt.show();
						e.printStackTrace();
					}
					
					changePasswordDialog = null;
	        		Toast tt = new ImageToast(
	        				this.getResources().getString(R.string.passwordDialog_passwordChanged), 
	        				ImageToast.TOAST_IMAGE_OK, 
	        				this);
	        		tt.show();
            	}
            	
            	if(mainMessage.startsWith("xmlimport") && vault != null && encryptor != null)
            	{        		
            		try {
						DBHelper.deleteBlobData(PWV_DBPREFIX);
						saveVaultToDB();					
						
						vault.notifyFolderDataSetChanged();
		        		initLayoutFolders();
		            	initLayoutItems();
		            	initLayoutItemDetail();
		            	
		            	layoutSwitcher.setDisplayedChild(PWV_LAYOUT_FOLDERS);
		            	
						ComponentProvider.getImageToast(this.getResources().getString(R.string.pwv_moreDialog_importVault_Loaded)
								 .replaceAll("<1>", am.getMainMessage().substring(am.getMainMessage().indexOf(":") + 1)), ImageToast.TOAST_IMAGE_OK, this).show();
						
					} catch (Exception e) {
		        		Toast tt = new ImageToast(e.getMessage(), ImageToast.TOAST_IMAGE_CANCEL, this);
		        		tt.setDuration(Toast.LENGTH_LONG);
		        		tt.show();
						e.printStackTrace();
					}
					this.resetMessage();
            	}
            	
            	if(waitDialog != null) waitDialog.cancel();
            	waitDialog = new SimpleWaitDialog(this);
            	break;
            
        	case PWV_MESSAGE_FOLDER_NEW:
        		vault.addFolder((VaultFolder)am.getAttachement());
    			try {
    				saveVaultToDB();
    			} catch (Exception e) {
    				e.printStackTrace();
    				showErrorDialog(e);
    			}     		
        		vault.notifyFolderDataSetChanged();
        		iconAdapter.notifyDataSetChanged();
        		
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_FOLDER_SAVE:
    			try {
    				saveVaultToDB();
    			} catch (Exception e) {
    				e.printStackTrace();
    				showErrorDialog(e);
    			}     		
        		vault.notifyFolderDataSetChanged();
        		iconAdapter.notifyDataSetChanged();
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_FOLDER_DELETE:
        		ComponentProvider.getBaseQuestionDialog(
        				this, 
        				getResources().getString(R.string.common_delete_text) + " " + getResources().getString(R.string.common_folder_text), 
        				getResources().getString(R.string.common_question_delete)
						.replaceAll("<1>", vault.getFolderByIndex((Integer)am.getAttachement()).getFolderName()), 
						(Integer)am.getAttachement() + ":" + (String)am.getMainMessage(), PWV_MESSAGE_FOLDER_DELETE_CONFIRM).show();
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_FOLDER_DELETE_CONFIRM:
				if(am.getAttachement().equals(new Integer(1)))
				{
	        		String[] mm = am.getMainMessage().split(":");
					vault.removeFolderWithIndex(Integer.parseInt(mm[0]), mm[1]);
	        		try {
	    				saveVaultToDB();
	    			} catch (Exception e) {
	    				e.printStackTrace();
	    				showErrorDialog(e);
	    			}     		
	        		vault.notifyFolderDataSetChanged();
	        		iconAdapter.notifyDataSetChanged();	
	        		ComponentProvider.getImageToast(
	        				this.getResources().getString(R.string.common_question_delete_confirm), 
	        				ImageToast.TOAST_IMAGE_OK, this).show();
				}       		
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_ITEM_DELETE_CONFIRM:
				if(am.getAttachement().equals(new Integer(1)))
				{
	        		String[] mm = am.getMainMessage().split(":"); // item index, item security hash
			    	currentFolder.removeItemWithIndex(Integer.parseInt(mm[0]), mm[1]);	    	
			    	try {
						saveVaultToDB();
					} catch (Exception e) {
						e.printStackTrace();
						showErrorDialog(e);
					}
					resetItemsList();
					makeLayoutItemDetailReadOnly();
					currentItem = null;
					layoutSwitcher.showPrevious();
			    	itemDeleteButton.setEnabled(true);
			    	itemMoveToButton.setEnabled(true);
			    	itemEditSaveButton.setEnabled(true);
					}       		
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_ITEM_SAVE_CONFIRM:
				if(am.getAttachement().equals(new Integer(1))) {
					String mode = am.getMainMessage();
					handleItemSave(mode);
				}
				else {
					leaveItemDetailLayout();
				}
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_ITEM_MOVETOFOLDER:				
				String[] mm = am.getMainMessage().split(":"); // destination folder index : destination folder hash : item index : item security hash
			    VaultItem itemToMove = currentFolder.getItemByIndex(Integer.parseInt(mm[2]));
			    VaultFolder destinationFolder = vault.getFolderByIndex(Integer.parseInt(mm[0]));
			    destinationFolder.addItem(itemToMove);
				currentFolder.removeItemWithIndex(Integer.parseInt(mm[2]), mm[3]);		    	
			    try {
					saveVaultToDB();
				} catch (Exception e) {
					e.printStackTrace();
					showErrorDialog(e);
				}
				resetItemsList();
				makeLayoutItemDetailReadOnly();
				currentItem = null;
				layoutSwitcher.showPrevious();
			    itemDeleteButton.setEnabled(true);
			    itemMoveToButton.setEnabled(true);
			    itemEditSaveButton.setEnabled(true);
			    
				ComponentProvider.getShowMessageDialog(this, 
						null, 
						getResources().getString(R.string.pwv_itemMoveToFolderReport).replaceAll("<1>", itemToMove.getItemName()).replaceAll("<2>", destinationFolder.getFolderName()), 
						ComponentProvider.DRAWABLE_ICON_OK)
						.show();	
      		
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_MOREDIALOG:
            	if (am.getMainMessage().equals("pwv_moreDialog_changePassword")) 
            	{
    		    	changePasswordDialog = new PasswordDialog(this, PasswordDialog.PD_MODE_CHANGE_PASSWORD);
    		    	changePasswordDialog.setEncryptAlgorithmCode(encryptAlgorithmCode);
    		    	changePasswordDialog.setParentMessage("change");
    		    	changePasswordDialog.setCurrentDecryptSpec(encryptor.getKeyHash(), encryptor.getDecryptAlgorithmCode());
    		    	changePasswordDialog.setWaitDialog(waitDialog, false);
    		    	changePasswordDialog.show();
            	}
            	else if (am.getMainMessage().equals("pwv_moreDialog_importVault") || am.getMainMessage().equals("pwv_moreDialog_mergeVaults")) 
            	{
            		File importExportDir = Helpers.getImportExportDir();            		
            		if(importExportDir == null)
            		{
            			Dialog showMessageDialog = ComponentProvider.getShowMessageDialog(this, 
            					getResources().getString(R.string.pwv_moreDialog_importVault), 
            					getResources().getString(R.string.pwv_moreDialog_importExportVault_Invalid)
            					.replaceAll("<1>", Helpers.getImportExportPath()), 
            					ComponentProvider.DRAWABLE_ICON_CANCEL);
            			showMessageDialog.show();
            			return;
            		}
            		
            		List<String> fileListPWV = Arrays.asList(importExportDir.list(
            				Helpers.getOnlyExtFilenameFilter(PasswordVaultActivity.PWV_EXPORT_EXT)));
            		List<String> fileListXML = Arrays.asList(importExportDir.list(
            				Helpers.getOnlyExtFilenameFilter("xml")));
            		
            		List<String> fileList = new ArrayList<String>();
            		Collections.sort(fileListPWV);
            		Collections.sort(fileListXML);
            		fileList.addAll(fileListPWV);
            		fileList.addAll(fileListXML);
            		
            		List<String> fileComments = Helpers.getFileCommentsList(
            				fileList, 
            				importExportDir.getAbsolutePath(), 
            				getResources().getConfiguration().locale, -1);
            		
            		if(!(fileList.size() > 0))
            		{
            			Dialog showMessageDialog = ComponentProvider.getShowMessageDialog(this, 
            					getResources().getString(R.string.pwv_moreDialog_importVault), 
            					getResources().getString(R.string.pwv_moreDialog_importVault_NoFilesToImport)
            					.replaceAll("<1>", Helpers.getImportExportPath()), 
            					ComponentProvider.DRAWABLE_ICON_CANCEL);
            			showMessageDialog.show();
            			return;
            		}
            		
            		
            		SelectionDialog fileListDialog = null;
            		if(am.getMainMessage().equals("pwv_moreDialog_mergeVaults")) 
            		{	
	            		fileListDialog = new SelectionDialog(this, 
	            				fileList, 
	            				fileComments, null, null, 
	            				getResources().getString(R.string.pwv_mergeVaults));
	            		fileListDialog.setMessageCode(PWV_MESSAGE_MOREDIALOG_MERGE);
            		}
            		else // import
            		{
	            		fileListDialog= new SelectionDialog(this, 
	            				fileList, 
	            				fileComments, null, null, 
	            				getResources().getString(R.string.pwv_moreDialog_importVault_dialogTitle));
	            		fileListDialog.setMessageCode(PWV_MESSAGE_MOREDIALOG_IMPORT);
            		}
            		      		
    		    	if (fileListDialog != null) fileListDialog.show();
            	}
            	else if (am.getMainMessage().equals("pwv_moreDialog_exportVault") || am.getMainMessage().equals("pwv_moreDialog_exportVaultXML"))
            	{
            		File importExportDir = Helpers.getImportExportDir();
            		if(importExportDir == null)
            		{
            			Dialog showMessageDialog = ComponentProvider.getShowMessageDialog(this, 
            					getResources().getString(R.string.pwv_moreDialog_exportVault), 
            					getResources().getString(R.string.pwv_moreDialog_importExportVault_Invalid)
            					.replaceAll("<1>", Helpers.getImportExportPath()), 
            					ComponentProvider.DRAWABLE_ICON_CANCEL);
            			showMessageDialog.show();
            			return;
            		}
            		if(!importExportDir.canWrite())
            		{
            			Toast t = new ImageToast(
            					"Export failed. Import dir <b>" + Helpers.getImportExportPath() + "</b> is read only.",
            					ImageToast.TOAST_IMAGE_CANCEL, this);
            			t.show();
            			return;
            		}
			    	
            		try {
						if(vault != null) saveVaultToDB();
					} catch (Exception e) {
						e.printStackTrace();
						showErrorDialog(e);
					}
					
            		Dialog setVaultNameDialog = ComponentProvider.getVaultSetNameDialog(this, am.getMainMessage().equals("pwv_moreDialog_exportVaultXML") ? vault : null);
            		setVaultNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        			setVaultNameDialog.show();
            	}
            	else if (am.getMainMessage().equals("pwv_moreDialog_resetVault")) 
            	{
            		AlertDialog cad = ComponentProvider.getCriticalQuestionDialog(this,
            				getResources().getString(R.string.pwv_moreDialog_resetVault), 
            				getResources().getString(
            						R.string.pwv_moreDialog_resetVault_ResetCriticalQuestion), 
            						null, 
            						PWV_MESSAGE_MOREDIALOG_RESET_CONFIRM);
            		cad.show();
            		cad.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
            	}
            	else if (am.getMainMessage().equals("pwv_moreDialog_enterPassword")) 
            	{
            		getStartScreenPasswordDialog().show();
            	}
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_MOREDIALOG_IMPORT:
        		String fileName = (String)am.getMainMessage();
        		String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        		int importAction = ext.equalsIgnoreCase(PWV_EXPORT_EXT) ? PWV_MESSAGE_MOREDIALOG_IMPORT_CONFIRM : PWV_MESSAGE_MOREDIALOG_IMPORT_CONFIRM_XML;
        		
        		if(DBHelper.getBlobData(PWV_DBPREFIX) != null)
        		{
	        		AlertDialog cad = ComponentProvider.getCriticalQuestionDialog(this, 
	        				getResources().getString(R.string.common_importVault_text), 
	        				getResources().getString(R.string.pwv_moreDialog_importVault_ImportCriticalQuestion)
							.replaceAll("<1>", fileName), fileName, importAction);
	        		cad.show();
	        		cad.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        		}
        		else
        		{
	        		AlertDialog cad = ComponentProvider.getBaseQuestionDialog(this, 
	        				getResources().getString(R.string.common_importVault_text), 
	        				getResources().getString(R.string.pwv_moreDialog_importVault_ImportQuestion)
							.replaceAll("<1>", fileName), fileName, importAction);
	        		cad.show();
        		}
        		this.resetMessage();
        		break;
        	
        	case PWV_MESSAGE_MOREDIALOG_MERGE:       		
        		ComponentProvider.getBaseQuestionDialog(this, 
        				getResources().getString(R.string.pwv_mergeVaults), 
        				getResources().getString(R.string.pwv_mergeVaultsInfo),
        				(String)am.getMainMessage(),
        				PWV_MESSAGE_MOREDIALOG_MERGE_CONFIRM,
        				null,
        				true).show();;
        		
        		this.resetMessage();
        		break;
        		
        		
        	case PWV_MESSAGE_MOREDIALOG_IMPORT_CONFIRM:
        		if(am.getAttachement().equals(new Integer(1)))
        		{
        			importOrMergePWV(am.getMainMessage(), false);
        		}       		
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_MOREDIALOG_IMPORT_CONFIRM_XML:
        		if(am.getAttachement().equals(new Integer(1)))
        		{
        			importOrMergeXML(am.getMainMessage(), false);
        		}
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_MOREDIALOG_MERGE_CONFIRM:
        		if(am.getAttachement().equals(new Integer(1)))
        		{
        			String mergeFileName = am.getMainMessage();
        			
        			if(mergeFileName.toLowerCase().endsWith(PWV_EXPORT_EXT)) {
        				importOrMergePWV(am.getMainMessage(), true);
        			}
        			else {
        				importOrMergeXML(am.getMainMessage(), true);
        			}  			
        		}
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_MOREDIALOG_RESET_CONFIRM:
        		if(am.getAttachement().equals(new Integer(1)))
        		{
	        		DBHelper.deleteBlobData(PWV_DBPREFIX);
	        		
	        		setResult(RESTART_PASSWORDVAULTACTIVITY);
	        		setRunningCode(0);
	        		this.finish();
        		}
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_PWGDIALOG_SET:
        		if(itemPasswordEditText.getText().toString().trim().equals(""))
        		{
        			itemPasswordEditText.setText("");
        			itemPasswordEditText.append(am.getMainMessage());
        		}
        		else {
        			ComponentProvider.getBaseQuestionDialog(
        					this, 
        					this.getResources().getString(R.string.passwordGeneratorDialog_passwordGenerator_text), 
        					this.getResources().getString(R.string.passwordGeneratorDialog_replacePasswordQuestion), 
        					am.getMainMessage(),
        					PWV_MESSAGE_PWGDIALOG_SET_CONFIRM).show();
        		}
        			
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_PWGDIALOG_SET_CONFIRM:
        		if(am.getAttachement().equals(new Integer(1)))
        		{
        			itemPasswordEditText.setText("");
        			itemPasswordEditText.append(am.getMainMessage());
        		}
        		this.resetMessage();
        		break;
        		
        	case PWV_MESSAGE_ITEM_NOTE_COPY: 		
	        	{
	        		setToSystemClipboard((String)am.getAttachement());
	        		new ImageToast(getResources().getString(R.string.common_textCopiedToSystemClipboard), ImageToast.TOAST_IMAGE_OK, this).show();
	        	}	        		
        		this.resetMessage();
        		break;
        		
        	case COMMON_MESSAGE_CONFIRM_EXIT:
				if(am.getAttachement() == null || am.getAttachement().equals(new Integer(1)))
				{
		    		setRunningCode(0);
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
    
    
    /** Create "Login to Password Vault Layout" */
    private void initLayoutStart()
    {
    	PasswordDialog startDialog = getStartScreenPasswordDialog();
    	startDialog.show();

    	toMainPageButton = (Button) findViewById(R.id.PWVS_toMainPage);
    	helpMeButton = (Button) findViewById(R.id.PWVS_helpMe);
    	layoutStartButtons = (LinearLayout) findViewById(R.id.PWVS_buttons);
		
	    this.toMainPageButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	setRunningCode(0);
		    	finish();
		    }
	    });
	    
	    // Help me get in! button
	    this.helpMeButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	String dirComment = "(dir: " + Helpers.getImportExportPath() + ")";
		    	boolean existsVault = DBHelper.getBlobData(PWV_DBPREFIX) != null;
		    	List<String> itemList = new ArrayList<String>();
		    	List<String> commentsList = new ArrayList<String>();
		    	List<Object> keyList = new ArrayList<Object>();
		    	itemList.add(getResources().getString(R.string.pwv_moreDialog_enterPassword));
		    	commentsList.add(null);
		    	keyList.add("pwv_moreDialog_enterPassword");
		    	if(existsVault){
			    	itemList.add(getResources().getString(R.string.pwv_moreDialog_resetVault));
			    	commentsList.add(null);
			    	keyList.add("pwv_moreDialog_resetVault");
		    	}
		    	itemList.add(getResources().getString(R.string.pwv_moreDialog_importVault));
		    	commentsList.add(dirComment);
		    	keyList.add("pwv_moreDialog_importVault");
		    	if(existsVault){
			    	itemList.add(getResources().getString(R.string.pwv_moreDialog_exportVault));
			    	commentsList.add(dirComment);
			    	keyList.add("pwv_moreDialog_exportVault");
		    	}

		    	moreDialog = new SelectionDialog(v, 
		    			itemList, 
		    			commentsList, 
		    			null, 
        				keyList, 
        				getResources().getString(R.string.pwv_start_helpMe));
		    	moreDialog.setMessageCode(PWV_MESSAGE_MOREDIALOG);
		    	
		    	if (moreDialog != null) moreDialog.show();
		    }
	    });
    }
    
    
    /** Create Password Folders Layout */
    private void initLayoutFolders()
    {	
    	iconAdapter = new PWVFolderAdapter(this, vault);
		foldersBottomMenu = (LinearLayout) findViewById(R.id.PWVL_Folders_buttons);
		showMenuButton = (Button) findViewById(R.id.PWVL_Folders_showMenuButton);
		if(showBottomMenu) showBottomMenu();
		foldersMoreButton = (Button) findViewById(R.id.PWVL_Folders_moreButton);
		foldersNewFolderButton = (Button) findViewById(R.id.PWVL_Folders_newFolderButton);
		foldersHelpButton = (Button) findViewById(R.id.PWVL_helpButton);
		GridView gridview = (GridView) findViewById(R.id.PWVL_Folders_gridview);
        gridview.setAdapter(iconAdapter);
            

        gridview.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                currentFolder = vault.getFolderByIndex(position);
                resetItemsList();
                layoutSwitcher.showNext();
                }
            });
        
        gridview.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
            	TextView tw = (TextView) v.findViewById(R.id.iconTextPW);
		    	PWVNewEditFolderDialog nefd = new PWVNewEditFolderDialog(
		    			v, 
		    			vault,
		    			position,
		    			PWVNewEditFolderDialog.PWVFD_MODE_SHOW_FOLDER);
		    	nefd.setOriginalHash((String)tw.getTag());
		    	nefd.show();

            return true;
            }
        });
        
	    this.foldersMoreButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public void onClick(View v)
		    {
		    	String dirComment = "(dir: " + Helpers.getImportExportPath() + ")";
		    	List<String> itemList = new ArrayList<String>();
		    	List<Object> keyList = new ArrayList<Object>();
		    	List<String> commentsList = new ArrayList<String>();
		    	itemList.add(getResources().getString(R.string.pwv_moreDialog_changePassword));
		    	commentsList.add(null);
		    	keyList.add("pwv_moreDialog_changePassword");
		    	itemList.add(getResources().getString(R.string.pwv_moreDialog_importVault));
		    	commentsList.add(dirComment);
		    	keyList.add("pwv_moreDialog_importVault");
		    	itemList.add(getResources().getString(R.string.pwv_mergeVaults));
		    	commentsList.add(dirComment);
		    	keyList.add("pwv_moreDialog_mergeVaults");
		    	itemList.add(getResources().getString(R.string.pwv_moreDialog_exportVault));
		    	commentsList.add(dirComment);
		    	keyList.add("pwv_moreDialog_exportVault");
		    	itemList.add(getResources().getString(R.string.pwv_moreDialog_exportVaultXML));
		    	commentsList.add(dirComment);
		    	keyList.add("pwv_moreDialog_exportVaultXML");
		    	
		    	moreDialog = new SelectionDialog(v, 
		    			itemList, 
		    			commentsList, null, 
        				keyList, 
        				getResources().getString(R.string.me_moreDialog_Title));
		    	moreDialog.setMessageCode(PWV_MESSAGE_MOREDIALOG);
		    	
		    	if (moreDialog != null) moreDialog.show();
		    }
	    });
	    
	    this.foldersNewFolderButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	if(isButtonsLockActivated()) return;
		    	activateButtonsLock();
		    	
		    	PWVNewEditFolderDialog nefd = new PWVNewEditFolderDialog(v, vault, null, PWVNewEditFolderDialog.PWVFD_MODE_NEW_FOLDER);
		    	nefd.setTitle(getResources().getString(R.string.pwv_newFolder_text));
		    	
		    	nefd.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    		@Override
		    		public void onCancel (DialogInterface dialogInterface) {
		    			deactivateButtonsLock();
		    		}
		    	});
		    	
		    	nefd.show();
		    }
        });
	    
	    this.foldersHelpButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	SimpleHTMLDialog simpleHTMLDialog = new SimpleHTMLDialog(v);
		    	simpleHTMLDialog.loadURL(getResources().getString(R.string.helpLink_PasswordVault));
		    	simpleHTMLDialog.show();			
		    }
	    });
	    
	    this.showMenuButton.setOnClickListener(new OnClickListener()
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	showBottomMenu();
		    }
	    });
    }
    
    
    /** Create Password Items Layout */
    private void initLayoutItems()
    {
    	itemsListView = (ListView) findViewById(R.id.PWVL_Items_listView);
        
        itemsArrayAdapter = (new PWVItemArrayAdapter(this, currentItems));
        float fontSizeMultiplier = 1.0F;
        try {
			fontSizeMultiplier = Integer.parseInt(settingDataHolder.getItemValueName("SC_PasswordVault", "SI_PasswordListFontSize").split("::")[0]) / 100.0F;
		} catch (Exception e) {
			e.printStackTrace();
		}
        ((PWVItemArrayAdapter)itemsArrayAdapter).setFontSizeMultiplier(fontSizeMultiplier);
        itemsListView.setAdapter(itemsArrayAdapter);
        
        if(android.os.Build.VERSION.SDK_INT >= 12)
        {
	        itemsQuickscroll = (QuickScroll) findViewById(R.id.PWVL_quickscroll);
	        itemsQuickscroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, itemsListView, itemsArrayAdapter, QuickScroll.STYLE_HOLO);
	        itemsQuickscroll.setFixedSize(1);
	        itemsQuickscroll.setMinAllVsVisibleRatio(5);
	        itemsQuickscroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 42);
        }
        
        itemsListView.setOnItemClickListener(new OnItemClickListener() 
        {
            // click on item
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
            {
            	VaultItem tvi = null;
            	
            	if (position == 0) // first item - return to folder layout
            	{
            		layoutSwitcher.showPrevious();
            		return;
            	}           	
            	--position;
            	
            	try {
					tvi = currentFolder.getItemByIndex(position);
					tvi.setSelected(!tvi.isSelected());
					itemsArrayAdapter.notifyDataSetChanged();
				} catch (IndexOutOfBoundsException e) { // create new item (last position in the items list)		
					currentItem = new VaultItem();
					makeLayoutItemDetailEditable();
					itemDeleteButton.setEnabled(false);
					itemMoveToButton.setEnabled(false);
					itemEditSaveButton.setTag("new");
			    	List tagMessage = new ArrayList();
			    	tagMessage.add(currentItem.getItemSecurityHash());
			    	tagMessage.add(position);
			    	itemNameEditText.setTag(tagMessage); // hash + position
			    	prepareLayoutItemDetailForShow();
			    	layoutSwitcher.showNext();
				} 
            }
          });
		
        itemsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
            {
            	VaultItem tvi = null;
            	String itemHash = (String)view.findViewById(R.id.PWVI_itemName).getTag(); // hash
            	
            	if (position == 0) // first item - return to folder layout
            	{
            		layoutSwitcher.showPrevious();
            		return true;
            	}           	
            	--position;
            	
            	try {
					tvi = currentFolder.getItemByIndex(position);
					currentItem = tvi;
					
					if(!itemHash.equals(currentItem.getItemSecurityHash())) return false;
	
				} catch (IndexOutOfBoundsException e) { // create new item (last position in the items list)
					currentItem = new VaultItem();
					makeLayoutItemDetailEditable();
					itemDeleteButton.setEnabled(false);
					itemMoveToButton.setEnabled(false);
					itemEditSaveButton.setTag("new");
					
				} finally {
			    	List tagMessage = new ArrayList();
			    	tagMessage.add(currentItem.getItemSecurityHash());
			    	tagMessage.add(position);
			    	itemNameEditText.setTag(tagMessage); // hash + position
			    	prepareLayoutItemDetailForShow();
			    	layoutSwitcher.showNext();
			    	
			    	// Ugly hack solving Android ViewAnimator invalidation bug
			    	if(passwordContainer.getVisibility() == View.GONE)
			    	{
			    		final Handler handler = new Handler();
			    		handler.postDelayed(new Runnable() {
			    			@Override
			    			public void run() {
			    				passwordContainer.setVisibility(View.VISIBLE);
			    				passwordContainer.setVisibility(View.GONE);
			    			}
			    		}, 10);
			    	}
				}
       	
            	return true;
            }
          });
    }
    
    
    /** Create Item Detail Layout */
    private void initLayoutItemDetail()
    {
    	setLayoutItemDetailOrientation();
    	
    	ked = new KeyboardAppearanceDetector(this);  	
    	ked.setKeyboardVisibilityChangedListener(new KeyboardVisibilityChangedListener() {
			
			@Override
			public void onKeyboardVisibilityChanged(boolean isKeyboardVisible) {
				handleKeyboardAppear(isKeyboardVisible);
			}
		});
    	if(!this.isTablet()) ked.startDetection();
    	//-
    	
    	itemNameEditText = (EditText)findViewById(R.id.PWVD_name);
    	itemPasswordEditText = (EditText)findViewById(R.id.PWVD_password);
    	itemCommentEditText = (EditText)findViewById(R.id.PWVD_comment);
    	itemColorSpinner = (Spinner)findViewById(R.id.PWVD_colorCombo);
    	itemDeleteButton = (TextView)findViewById(R.id.PWVD_buttonDelete);
    	itemEditSaveButton = (TextView)findViewById(R.id.PWVD_buttonEditSave);
    	itemMoveToButton= (TextView)findViewById(R.id.PWVD_buttonMoveTo);
    	passwordGeneratorButton = (Button)findViewById(R.id.PWVD_passwordGeneratorButton);
    	nameToClipboardButton = (Button)findViewById(R.id.PWVD_nameToClipboardButton);
    	passwordToClipboardButton = (Button)findViewById(R.id.PWVD_passwordToClipboardButton);
    	passwordToTextEncryptorButton = (Button)findViewById(R.id.PWVD_passwordToTextEncryptorButton);
    	passwordToFileEncryptorButton = (Button)findViewById(R.id.PWVD_passwordToFileEncryptorButton);
    	noteToClipboardButton = (Button)findViewById(R.id.PWVD_noteToClipboardButton);
    	noteCharCounter = (TextView)findViewById(R.id.PWVD_noteCharCounter);
    	switchTopBottomButton = (Button)findViewById(R.id.PWVD_switchTopBottomButton);
    	topLeftContainer = findViewById(R.id.PWVD_mainTopLeft);
    	bottomRightContainer = findViewById(R.id.PWVD_mainBottomRight);
    	bottomButtonLine = findViewById(R.id.PWVD_buttonLine);
    	bottomDelimiter = findViewById(R.id.PWVD_bottomDelimiter);
    	passwordContainer = (View)findViewById(R.id.PWVD_passwordContainer);
    	
    	itemNameEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	itemPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    	itemEditSaveButton.setTag("edit");
    	itemColorSpinner.setAdapter(itemColorSpinnerAdapter);

	    this.itemEditSaveButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	String mode = (String)v.getTag();
		    	handleItemSave(mode);
		    }
	    });
	    
	    this.itemDeleteButton.setOnClickListener(new OnClickListener()
	    {
		    @Override
		    public synchronized void onClick(View v)
		    {
		    	if(isButtonsLockActivated()) return;
		    	activateButtonsLock();
		    	
		    	List tagMessage = (List)itemNameEditText.getTag();
		    	
        		Dialog deleteDialog = ComponentProvider.getBaseQuestionDialog(v, getResources().getString(R.string.common_delete_text) + " " + getResources().getString(R.string.common_item_text), 
        				getResources().getString(R.string.common_question_delete)
						.replaceAll("<1>", currentFolder.getItemByIndex((Integer)tagMessage.get(1)).getItemName()), (Integer)tagMessage.get(1) + ":" + (String)tagMessage.get(0), PWV_MESSAGE_ITEM_DELETE_CONFIRM);			
		    
        		deleteDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    		@Override
		    		public void onCancel (DialogInterface dialogInterface) {
		    			deactivateButtonsLock();
		    		}
		    	});
        		
        		deleteDialog.show();
		    }
	    });
	    
	    this.itemMoveToButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	if(isButtonsLockActivated()) return;
		    	activateButtonsLock();
		    	
		    	List tagMessage = (List)itemNameEditText.getTag();
		    	List<String> itemList = new ArrayList<String>();
		    	List<Integer> iconList = new ArrayList<Integer>();
		    	List<Object> keyList = new ArrayList<Object>();
		    	
		    	for(int i = 0; i < vault.getFolderCount(); ++i)
		        {   
		    		VaultFolder tempFolder = vault.getFolderByIndex(i);
		    		if(tempFolder == currentFolder) continue;
		    		itemList.add(tempFolder.getFolderName());
		    		keyList.add(Integer.toString(i) + ":" + 
		    					tempFolder.getFolderSecurityHash() + ":" + 
		    					(Integer)tagMessage.get(1) + ":" + 
		    					(String)tagMessage.get(0));
		    		iconList.add(ColorHelper.getColorBean(tempFolder.getColorCode()).folderIconRId);
		        }

		    	SelectionDialog moveToFolderDialog = new SelectionDialog(
		    			v, 
		    			itemList, 
		    			null, 
		    			iconList, 
		    			keyList, 
		    			getResources().getString(R.string.common_moveToFolder_text));	    	
		    	moveToFolderDialog.setMessageCode(PWV_MESSAGE_ITEM_MOVETOFOLDER);
		    	moveToFolderDialog.show();
		    	
		    	moveToFolderDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    		@Override
		    		public void onCancel (DialogInterface dialogInterface) {
		    			deactivateButtonsLock();
		    		}
		    	});
		    	
		    	moveToFolderDialog.show();		
		    }
	    });
	    
	    this.passwordGeneratorButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	new PasswordGeneratorDialog(v, PWV_MESSAGE_PWGDIALOG_SET).show();
		    }
	    });
	    
	    this.nameToClipboardButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	String text = itemNameEditText.getText().toString().trim();
		    	if(text.length() == 0)
		    	{
		    		ComponentProvider.getImageToastKO(getResources().getString(R.string.common_noTextToCopy), v).show();
		    		return;
		    	}
		    	
		    	setToSystemClipboard(text);
		    	ComponentProvider.getImageToastOK(getResources().getString(R.string.common_textCopiedToSystemClipboard), v).show();
		    }
	    });
	    
	    this.passwordToClipboardButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	String text = itemPasswordEditText.getText().toString().trim();
		    	if(text.length() == 0)
		    	{
		    		ComponentProvider.getImageToastKO(getResources().getString(R.string.common_noTextToCopy), v).show();
		    		return;
		    	}
		    	
		    	setToSystemClipboard(text);
		    	
		    	ComponentProvider.getShowMessageDialog(v, 
		    			getResources().getString(R.string.common_copyToClipboard_text),
		    			getResources().getString(R.string.common_passwordCopiedToClipboard_text) + "<br/><br/>" + getResources().getString(R.string.common_copyToClipboardWarning),
		    			ComponentProvider.DRAWABLE_ICON_INFO_BLUE).show();
		    }
	    });
	    
	    this.passwordToTextEncryptorButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	String text = itemPasswordEditText.getText().toString().trim();
		    	if(text.length() == 0)
		    	{
		    		ComponentProvider.getImageToastKO(getResources().getString(R.string.common_noTextToCopy), v).show();
		    		return;
		    	}
		    	
		    	Intent intent = new Intent(PasswordVaultActivity.this, MessageEncActivity.class);
		        intent.putExtra(PasswordVaultActivity.PWV_EXTRA_PASSWORD, text);
		        intent.putExtra(PasswordVaultActivity.PWV_EXTRA_LOCKSCREEN_KEY, encryptor.getKeyHash());
		        intent.putExtra(PasswordVaultActivity.PWV_EXTRA_LOCKSCREEN_ON, lockOnPause);
		        startActivityForResult(intent, REQUEST_CODE_SEND_PASSWORD);
		    }
	    });
	    
	    this.passwordToFileEncryptorButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	String text = itemPasswordEditText.getText().toString().trim();
		    	if(text.length() == 0)
		    	{
		    		ComponentProvider.getImageToastKO(getResources().getString(R.string.common_noTextToCopy), v).show();
		    		return;
		    	}
		    	
		    	Intent intent = new Intent(PasswordVaultActivity.this, FileEncActivity.class);
		        intent.putExtra(PasswordVaultActivity.PWV_EXTRA_PASSWORD, text);
		        intent.putExtra(PasswordVaultActivity.PWV_EXTRA_LOCKSCREEN_KEY, encryptor.getKeyHash());
		        intent.putExtra(PasswordVaultActivity.PWV_EXTRA_LOCKSCREEN_ON, lockOnPause);
		        startActivityForResult(intent, REQUEST_CODE_SEND_PASSWORD);
		    }
	    });
	    
	    this.noteToClipboardButton.setOnClickListener(new OnClickListener() 
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	String text = itemCommentEditText.getText().toString().trim();
		    	if(text.length() == 0)
		    	{
		    		ComponentProvider.getImageToastKO(getResources().getString(R.string.common_noTextToCopy), v).show();
		    		return;
		    	}
		    	
		    	String[] notes = text.split("\n");
		    	ArrayList<String> noteList = new ArrayList<String>();
		    	
		    	for(int i = 0; i < notes.length; ++i)
		    	{
		    		String tempNote = notes[i].trim();
		    		if(tempNote.length() > 0) noteList.add(tempNote);
		    	}
		    	
		    	if(noteList.size() == 1)
		    	{
	        		setToSystemClipboard(noteList.get(0));
	        		ComponentProvider.getImageToastOK(getResources().getString(R.string.common_textCopiedToSystemClipboard), v).show();
	        		return;
		    	}
		    	
		    	List<String> title = new ArrayList<String>();
		    	List<String> comment = new ArrayList<String>();
		    	List<Integer> icon = new ArrayList<Integer>();
		    	List<Object> tag = new ArrayList<Object>();	  
		    	
	    		title.add(getResources().getString(R.string.common_copyall_text));
		    	comment.add("...");
		    	icon.add(R.drawable.clipboard);
		    	tag.add(text);
		    	
		    	for(int i = 0; i < noteList.size(); ++i)
		    	{
		    		title.add(getResources().getString(R.string.common_copyParagraph_text) + " " + (i + 1));
			    	comment.add(noteList.get(i));
			    	tag.add(noteList.get(i));
			    	icon.add(R.drawable.clipboard_num);
		    	}
		    	
		    	SelectionDialog selectDirDialog = new SelectionDialog(v, title, comment, icon, tag);	    	
		    	selectDirDialog.setMessageCode(PWV_MESSAGE_ITEM_NOTE_COPY);
		    	selectDirDialog.show();
		    }
	    });
	    
	    this.switchTopBottomButton.setOnClickListener(new OnClickListener()
	    {
		    @Override
		    public synchronized void onClick(View v) 
		    {
		    	if(topLeftContainer.getVisibility() == View.VISIBLE)
		    	{
		    		bottomRightContainer.setVisibility(View.VISIBLE);
		    		topLeftContainer.setVisibility(View.GONE);
		    	}
		    	else
		    	{
		    		bottomRightContainer.setVisibility(View.GONE);
		    		topLeftContainer.setVisibility(View.VISIBLE);
		    	}
		    	moveCursorToEnd();
		    }
	    });
	    
	    // Comment Character Counter
	    itemCommentEditText.addTextChangedListener(new TextWatcher() 
	    {
	    	@Override
	    	public void onTextChanged(CharSequence s, int start, int before, int count) {}

	    	@Override
	    	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	    	@Override
	    	public void afterTextChanged(Editable s){
	    		noteCharCounter.setText(" (" + Integer.toString(itemCommentEditText.getText().length()) + "/" + Integer.toString(Vault.COMMENT_MAXCHARS) + ")");
	    	} 
        });
	    
	    makeLayoutItemDetailReadOnly();
    }
    
    
    /** Serialize, Compress and Encrypt given Vault Object */
    private byte[] serializeVault(Vault passwordVault, int version) throws Exception
    {
		byte[] serializedVault;	
    	String crcZipCompress = "";

    	List<String> crc = new ArrayList<String>();
    	byte[] output = null;
    	if(version < 2)
    	{
	    	serializedVault = Encryptor.zipObject(passwordVault, crc);
	    	crcZipCompress = crc.get(0);
			output = encryptor.encryptWithCRC(serializedVault, crcZipCompress);
    	}
    	else
    	{
    		serializedVault = Encryptor.compressObjectLZMA(passwordVault);
    		output = encryptor.encryptEAXWithAlgCode(serializedVault); 
    	}
    	
		return output;
    }
    
    
    /** Decrypt, Decompress and Deserialize given serialized Vault Object */
    private Vault deserializeVault(byte[] serializedVault, int version, Encryptor customEncryptor) throws Exception
    {
    	Vault unzipedVault = null;;
    	
    	if(version < 2)
    	{
	    	String crcZipDecompress = "";
			String crcZipFromFile = "";
			
			List<String> crcf = new ArrayList<String>();
			byte[] decrypted = (customEncryptor == null) ? encryptor.decryptWithCRC(serializedVault, crcf) : customEncryptor.decryptWithCRC(serializedVault, crcf);
			crcZipFromFile = crcf.get(0);		
	
	    	List<String> crcd = new ArrayList<String>();
	    	unzipedVault = (Vault)Encryptor.unzipObject(decrypted, crcd);
	    	crcZipDecompress = crcd.get(0);
	
			if(!crcZipFromFile.equals(crcZipDecompress))
				unzipedVault.setIntegrityCheckFailed(true);
    	}
    	else
    	{    		
    		byte[] decrypted = (customEncryptor == null) ? encryptor.decryptEAXWithAlgCode(serializedVault) : customEncryptor.decryptEAXWithAlgCode(serializedVault);
    		
    		try {
    			unzipedVault = (Vault)Encryptor.decompressObjectLZMA(decrypted);
    		} catch (Exception e) {
    			throw new DataFormatException(e.getLocalizedMessage());
    		}
    	}
		
		return unzipedVault;
    }
    
    
    /** Load Vault Object from Application Database */
    private synchronized Vault loadVaultfromDB() throws Exception
    {
		byte[] dbVault;	
		StringBuffer dbhs = new StringBuffer();
		
		ArrayList otherData = new ArrayList();
		dbVault = DBHelper.getBlobData(PWV_DBPREFIX, dbhs, otherData); 
    	if(dbVault == null) return null;
		
		int pwvVersion = ((Integer)otherData.get(0)).intValue();		
		
    	Vault tempVault = deserializeVault(dbVault, pwvVersion, null);
    	tempVault.setStampHashFromDB(dbhs.toString());
    	return tempVault;
    }
    
    
    /** Save Vault Object to Application Database */
    private synchronized void saveVaultToDB() throws Exception
    {
    	String oldStampHash = vault.getCurrentStampHash();
		String newStampHash = vault.generateNewStampHash();
		String dbStampHash = null;
		byte[] serializedVault = serializeVault(vault, PWV_FORMAT_VERSION);

		StringBuffer dbhs = new StringBuffer();
		byte[] blobData = DBHelper.getBlobData(PWV_DBPREFIX, dbhs, null);

		if (oldStampHash == null && blobData != null) // important - don't save FirstRun vault if exist db version
			throw new IllegalStateException(
					"DB inconsistent: current object cannot be saved.");

		if (!dbhs.toString().equals("")) dbStampHash = dbhs.toString();
		
		if (!(oldStampHash == null && dbStampHash == null) && !oldStampHash.equals(dbStampHash))
			throw new IllegalStateException(
					"DB invalid HashStamp: current object cannot be saved.");

		DBHelper.insertUpdateBlobData(PWV_DBPREFIX, serializedVault, newStampHash, PWV_FORMAT_VERSION); 
		
		// Auto Backup to File
		String autoBck = settingDataHolder.getItem("SC_PasswordVault", "SI_AutoBackup");
		if(autoBck.equals(SettingsActivity.PATH_DISABLED)) return;
		CryptFile bckDir = new CryptFile(autoBck);
		
		if(!bckDir.exists() || (new CryptFileWrapper(bckDir)).getWritePermissionLevelForDir() < 2)
		{
			ComponentProvider.getShowMessageDialog(this, null, getResources().getString(R.string.pwv_autoBackupDir_Invalid), ComponentProvider.DRAWABLE_ICON_CANCEL).show();
			return;
		}
		
		try {
			exportVaultToFile(getResources().getString(R.string.pwvAutoBackupFileName), bckDir, false, true, true);
		} catch (Exception e) {
			e.printStackTrace();
			ComponentProvider.getShowMessageDialog(this, null, e.getLocalizedMessage(), ComponentProvider.DRAWABLE_ICON_CANCEL).show();
		}		
    }
    
    /** Export Vault to a File */
    public void exportVaultToFile(String fileName, File exportDir, boolean xmlExport, boolean replaceExisting, boolean silentExport) throws Exception
    {   	
    	if(exportDir == null) exportDir = Helpers.getImportExportDir();    	
		
    	fileName += ".";	
    	if(!xmlExport) fileName += PasswordVaultActivity.PWV_EXPORT_EXT; //pwv file
			else fileName += "xml"; //xml file		
		File exportFile = new File(exportDir.getAbsolutePath() + File.separator + fileName);
		
		if (exportFile.exists() && !replaceExisting)
		{
			new ImageToast(getResources().getString(R.string.common_fileNameAlreadyExists_text).replaceAll("<1>", fileName), ImageToast.TOAST_IMAGE_CANCEL, this).show();
			return;
		}
		
		if(!xmlExport) //pwv file
		{
			byte[] dbVault;
			StringBuffer dbhs = new StringBuffer();
			ArrayList otherData = new ArrayList();
			dbVault = DBHelper.getBlobData(PasswordVaultActivity.PWV_DBPREFIX, dbhs, otherData); 
			int pwvVersion = ((Integer)otherData.get(0)).intValue();
			
			if(pwvVersion < 2)
			{
				byte[] hash = Encryptor.getShortHash(dbVault);
				
				FileOutputStream out = new FileOutputStream(exportFile);
				out.write(hash);
				out.write(dbVault);
				out.flush();
				out.close();
			}
			else
			{							
				FileOutputStream out = new FileOutputStream(exportFile);
				out.write("PWV".getBytes());
				out.write((byte)pwvVersion);
				out.write(dbVault);
				out.write(Encryptor.getSHA256Hash(dbVault));
				out.flush();
				out.close();
			}
		}
		else Helpers.saveStringToFile(exportFile, vault.asXML()); //xml file
		
		if(!silentExport)
		{
			ComponentProvider.getShowMessageDialog(this, null, getResources().getString(R.string.pwv_moreDialog_exportVault_Saved)
					.replaceAll("<1>", fileName).replaceAll("<2>", exportDir.getAbsolutePath()), null).show();
		}
    }
    
    
    /** Set and Reset Item Detail Variables before show */
    private void prepareLayoutItemDetailForShow()
    {
		itemNameEditText.setText(currentItem.getItemName());
    	itemPasswordEditText.setText(currentItem.getItemPassword());
    	itemCommentEditText.setText(currentItem.getItemComment());
    	itemCommentEditText.scrollTo(0, 0);
    	itemColorSpinner.setSelection(ColorHelper.getColorPosition(currentItem.getColorCode()));
    	if(android.os.Build.VERSION.SDK_INT >= 19) Linkify.addLinks(itemCommentEditText, Linkify.ALL);
    	if(itemPasswordEditText.getText().toString().trim().equals("") && !itemNameEditText.getText().toString().trim().equals("")) passwordContainer.setVisibility(View.GONE);
    	else passwordContainer.setVisibility(View.VISIBLE);
    }
    
    
    /** Prepare Item Detail Layout for View */
    private void makeLayoutItemDetailReadOnly()
    {
	    makeReadOnlyEditText(itemNameEditText);
	    makeReadOnlyEditText(itemPasswordEditText);
	    makeReadOnlyEditText(itemCommentEditText);
	    itemColorSpinner.setEnabled(false);
	    itemColorSpinner.setBackgroundResource(R.drawable.d_edittext_readonly);
	    itemEditSaveButton.setText(getResources().getString(R.string.common_edit_text));
	    itemEditSaveButton.setTag("edit");
	    passwordGeneratorButton.setVisibility(Button.GONE);
		topLeftContainer.setVisibility(View.VISIBLE);
		bottomRightContainer.setVisibility(View.VISIBLE);
		bottomDelimiter.setVisibility(View.VISIBLE);
		switchTopBottomButton.setVisibility(Button.GONE);
    }
    
    
    /** Prepare Item Detail Layout for Edit */
    private void makeLayoutItemDetailEditable()
    {
	    makeEditableEditText(itemNameEditText);
	    makeEditableEditText(itemPasswordEditText);
	    makeEditableEditText(itemCommentEditText);
	    itemColorSpinner.setEnabled(true);
	    itemColorSpinner.setBackgroundResource(R.drawable.d_edittext);
	    itemEditSaveButton.setText(getResources().getString(R.string.common_save_text));
	    itemEditSaveButton.setTag("save");
	    passwordGeneratorButton.setVisibility(Button.VISIBLE);
	    moveCursorToEnd();
	    if(android.os.Build.VERSION.SDK_INT >= 19) itemCommentEditText.setText(itemCommentEditText.getText().toString());
	    passwordContainer.setVisibility(View.VISIBLE);
    }
  
    
    /** Update Item List and other "current items related" variables */
    private void resetItemsList()
    {
        currentItems.clear();
        VaultItem tvi = VaultItem.getSpecial(VaultItem.SPEC_GOBACKITEM, currentFolder.getFolderName());
        tvi.setColorCode(currentFolder.getColorCode());
        currentFolder.notifyItemDataSetChanged();
        currentItems.add(tvi);
        currentItems.addAll(currentFolder.getItemList());
        currentItems.add(VaultItem.getSpecial(VaultItem.SPEC_NEWITEM));
        
        itemsArrayAdapter.notifyDataSetChanged();
    }
    
    
    /** Helper method for "makeLayoutItemDetailReadOnly" method */
    private void makeReadOnlyEditText(EditText et)
    {
    	//et.setEnabled(false);
    	et.setFocusableInTouchMode(false);
    	et.setFocusable(false);
    	et.setClickable(false);
    	et.setCursorVisible(false);
    	et.setBackgroundResource(R.drawable.d_edittext_readonly);
    	et.setTextColor(Color.BLACK);
    }
    
    
    /** Helper method for "makeLayoutItemDetailEditable" method */
    private void makeEditableEditText(EditText et)
    {
    	//et.setEnabled(true);
    	et.setFocusableInTouchMode(true);
    	et.setFocusable(true);
    	et.setClickable(true);
    	et.setCursorVisible(true);
    	et.setBackgroundResource(R.drawable.d_edittext);
    }
    
    /** Move Cursor to End of Text - if Current Focus is EditText **/
    private void moveCursorToEnd()
    {
		View view = getCurrentFocus();
	    if (view != null && view instanceof EditText) {
	    	EditText et = (EditText)view;
	    	int textLength = et.getText().length();
	    	et.setSelection(textLength, textLength);
	    }
    }   
    
    /** Solve differences between Portrait and Landscape orientation (Item Detail Layer) */ 
    private void setLayoutItemDetailOrientation()
    {
    	ViewGroup lMTL = (ViewGroup) this.findViewById(R.id.PWVD_mainTopLeft);
    	ViewGroup lMBR = (ViewGroup) this.findViewById(R.id.PWVD_mainBottomRight);
    	FrameLayout lC = (FrameLayout) this.findViewById(R.id.PWVD_centerer);
    	
    	if(ked != null) handleKeyboardAppear(ked.isOpen());
    	
    	int orientation = this.getResources().getConfiguration().orientation;
    	if(orientation == Configuration.ORIENTATION_PORTRAIT)
    	{   	
    		{
	    		RelativeLayout.LayoutParams relativeParams = 
	    			new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);    		
	    		relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
	    		
	    		lMTL.setLayoutParams(relativeParams);
    		}
    		{
	    		RelativeLayout.LayoutParams relativeParams = 
	    			new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);   		
	    		relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    		relativeParams.addRule(RelativeLayout.BELOW, lC.getId());
	    		
	    		lMBR.setLayoutParams(relativeParams);
    		}
    		{
	    		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(0, 0);
	    		relativeParams.addRule(RelativeLayout.BELOW, lMTL.getId());

	    		lC.setLayoutParams(relativeParams);
    		}
    	}
    	else if(orientation == Configuration.ORIENTATION_LANDSCAPE)
    	{
    		{
	    		RelativeLayout.LayoutParams relativeParams = 
	    			new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
	    		relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	    		relativeParams.addRule(RelativeLayout.LEFT_OF, lC.getId());
	    		
	    		lMTL.setLayoutParams(relativeParams);
    		}
    		{
	    		RelativeLayout.LayoutParams relativeParams = 
	    			new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);	    		
	    		relativeParams.addRule(RelativeLayout.RIGHT_OF, lC.getId());
	    		relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	    		
	    		lMBR.setLayoutParams(relativeParams);
    		}
    		{
	    		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(0, 0); 		
	    		relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
	    		
	    		lC.setLayoutParams(relativeParams);
    		}
    	}
    }
    
    private PasswordDialog getStartScreenPasswordDialog()
    {
    	PasswordDialog passwordDialog;
    	byte[] testVault = DBHelper.getBlobData(PWV_DBPREFIX);
    	if(testVault == null)
    		passwordDialog = new PasswordDialog(this, PasswordDialog.PD_MODE_SET_PASSWORD);
    	else {
    		passwordDialog = new PasswordDialog(this, PasswordDialog.PD_MODE_ENTER_PASSWORD);
    		passwordDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    	}
    	passwordDialog.setEncryptAlgorithmCode(encryptAlgorithmCode);
    	passwordDialog.setParentMessage("enter");
    	return passwordDialog;
    }
    
    /** Alter Item Detail Layout regarding Keyboard is/isn't visible */
    private void handleKeyboardAppear(boolean isKeyboardVisible)
    {
    	if(topLeftContainer == null || bottomRightContainer == null || switchTopBottomButton == null || bottomDelimiter == null) return;
    	
    	boolean orientationPortrait = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    	
    	if(layoutSwitcher.getDisplayedChild() == PWV_LAYOUT_ITEMDETAIL)
		{
			View focView = getWindow().getCurrentFocus();
			if(focView == null) return;
			Integer id = focView.getId();
							
			if(id == R.id.PWVD_comment && isKeyboardVisible && orientationPortrait) {
				topLeftContainer.setVisibility(View.GONE);
				switchTopBottomButton.setVisibility(View.VISIBLE);
				bottomDelimiter.setVisibility(View.GONE);
			}
			else if ((id == R.id.PWVD_name || id == R.id.PWVD_password) && isKeyboardVisible && orientationPortrait) {
				bottomRightContainer.setVisibility(View.GONE);
				switchTopBottomButton.setVisibility(View.VISIBLE);
				bottomDelimiter.setVisibility(View.GONE);
			}
			else {
				topLeftContainer.setVisibility(View.VISIBLE);
				bottomRightContainer.setVisibility(View.VISIBLE);
				switchTopBottomButton.setVisibility(View.GONE);
				bottomDelimiter.setVisibility(View.VISIBLE);
			}
		}	
    }
    
    /** Before PasswordVaultActivity Exit */
    private void handleExit()
    {
		if(askOnLeave)
		{
    		ComponentProvider.getBaseQuestionDialog(this, 
					getResources().getString(R.string.common_returnToMainMenuTitle),  
    				getResources().getString(R.string.common_question_leave).replaceAll("<1>", getResources().getString(R.string.common_app_passwordVault_name)), 
    				null, 
    				COMMON_MESSAGE_CONFIRM_EXIT
    				).show();
		}
		else setMessage(new ActivityMessage(COMMON_MESSAGE_CONFIRM_EXIT, null));
    }
    
    /**  Check, Add and Save Item */
    private void handleItemSave(String mode)
    {
    	if (itemNameEditText.getText().toString().trim().equals(""))
    	{
    		new ImageToast(getResources().getString(R.string.common_enterTheName_text), ImageToast.TOAST_IMAGE_CANCEL, this).show();
    		return;
    	}
    	
    	if(mode.equals("new"))
    	{
    		currentFolder.addItem(currentItem);
    		itemDeleteButton.setEnabled(true);
    		itemMoveToButton.setEnabled(true);
    	}
    	
    	if(mode.equals("edit"))
    	{
    		makeLayoutItemDetailEditable();
    		itemDeleteButton.setEnabled(false);
    		itemMoveToButton.setEnabled(false);
    	    return;
    	}
    	
    	itemDeleteButton.setEnabled(false);
    	itemEditSaveButton.setEnabled(false);
    	itemMoveToButton.setEnabled(false);
    	
    	List tagMessage = (List)itemNameEditText.getTag();
    	String itemHash = (String)tagMessage.get(0);
    	int position = (Integer)tagMessage.get(1);
    	
    	if(!((String)tagMessage.get(0)).equals(currentItem.getItemSecurityHash())) 
    		throw new IllegalStateException("hash doesn't match");
    	
    	currentItem.setItemName(itemNameEditText.getText().toString().trim());
    	currentItem.setItemPassword(itemPasswordEditText.getText().toString().trim());
    	currentItem.setItemComment(itemCommentEditText.getText().toString().trim());
    	currentItem.setColorCode(ColorHelper.getColorList().get(itemColorSpinner.getSelectedItemPosition()).colorCode);
    	currentItem.setDateModified();

    	try {
			saveVaultToDB();
		} catch (Exception e) {
			e.printStackTrace();
			showErrorDialog(e);
		}
    	
		tagMessage.clear();
		tagMessage.add(currentItem.getItemSecurityHash());
		tagMessage.add(position);	
		
		leaveItemDetailLayout();
    }
    
    /**  Import or Merge from XML file */
    private void importOrMergeXML(String fileName, boolean merge)
    {
		File importFile = new File(Helpers.getImportExportPath() + File.separator + fileName);
		
		Vault tempvault = null;

		try {
			tempvault = Vault.getInstance(Helpers.loadStringFromFile(importFile));
		} catch (Exception e) {
			ComponentProvider.getShowMessageDialog(this, this.getResources().getString(R.string.pwv_moreDialog_importVault), 
					this.getResources().getString(R.string.pwv_moreDialog_importVault_NotValid)
						.replaceAll("<1>", fileName) + "<br/><br/>"+ e.getLocalizedMessage(),ComponentProvider.DRAWABLE_ICON_CANCEL).show();
		} 
	
		if(!merge) 
		{
			if(tempvault != null)
			{
				vault = tempvault;
				PasswordDialog xmlPD = new PasswordDialog(this, PasswordDialog.PD_MODE_SET_PASSWORD);
				xmlPD.setEncryptAlgorithmCode(encryptAlgorithmCode);
				xmlPD.setParentMessage("xmlimport:" + fileName);
				xmlPD.setBlockCancellation(true);
				xmlPD.show();
			}	
		}
		else
		{
			if(tempvault != null) mergeVaults(tempvault);
		}
    }
    
    /**  Import or Merge from PWV file */
    private void importOrMergePWV(String fileName, boolean merge)
    {
		File importFile = new File(Helpers.getImportExportPath() + File.separator + fileName);
		
		if(importFile.length() > 10485760)
		{
			ComponentProvider.getImageToast("Sorry - file <1> is too large to import.<br/> 10MB max."
					 .replaceAll("<1>", fileName), ImageToast.TOAST_IMAGE_CANCEL, this).show();
			return;
		}
		

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		try {
			FileInputStream fis = new FileInputStream(importFile);
			byte[] buffer = new byte[10240];
			int noOfBytes = 0;
			
			while ((noOfBytes = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, noOfBytes);
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte[] input = bos.toByteArray();
		bos = null;
		
		// Version detection
		int version = -1;        			
		// version 2+ ?
		if(input[0] == 'P' && input[1] == 'W' && input[2] == 'V')
		{
			byte[] currentHash = Encryptor.getSHA256Hash(Helpers.getSubarray(input, 4, input.length - 36));
			byte[] storedHash = Helpers.getSubarray(input, input.length - 32, 32);
			
			if(Arrays.equals(currentHash, storedHash))
				version = input[3];
		}
		
		byte[] vaultData = null;
		
		// version 1
		if(version < 0)
		{
			version = 1;
			int offset = 4;
			byte[] hash = Helpers.getSubarray(input, 0, offset);
			byte[] data = Helpers.getSubarray(input, offset, input.length - offset);
			 
			if(!Arrays.equals(hash, Encryptor.getShortHash(data)))
			{
				ComponentProvider.getImageToast(this.getResources().getString(R.string.pwv_moreDialog_importVault_Corrupted)
						 .replaceAll("<1>", fileName), ImageToast.TOAST_IMAGE_CANCEL, this).show();
				return;
			}
			
			if(!merge)
				DBHelper.insertUpdateBlobData(PWV_DBPREFIX, data, "IMPORTED", 1);
			else 
				vaultData = data;
		}
		else // version 2+
		{
			byte[] data = Helpers.getSubarray(input, 4, input.length - 36);
			
			if(!merge)
				DBHelper.insertUpdateBlobData(PWV_DBPREFIX, data, "IMPORTED", version);
			else 
				vaultData = data;
		}
				
		if(!merge) 
		{
			ComponentProvider.getImageToast(this.getResources().getString(R.string.pwv_moreDialog_importVault_Loaded)
					 .replaceAll("<1>", fileName), ImageToast.TOAST_IMAGE_OK, this).show();
			 
			setResult(RESTART_PASSWORDVAULTACTIVITY);
			setRunningCode(0);
			this.finish();	
		}
		else
		{
			List<Object> attachment = new ArrayList<Object>();
			attachment.add(vaultData);
			attachment.add(version);
			
			PasswordDialog mergePD = new PasswordDialog(this, PasswordDialog.PD_MODE_ENTER_PASSWORD);
			mergePD.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			mergePD.setParentMessage("merge");
			mergePD.setAttachment(attachment);
			mergePD.show();
		}
    }
    
    /**  Merge given vault with current vault */
    private synchronized void mergeVaults(final Vault mergeVault)
    {
		waitDialog = new SimpleWaitDialog(this);
		new Thread (new Runnable() 
		{
			public void run() 
			{
				PowerManager.WakeLock wakeLock;
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PWV_MERGE_VAULTS");
				
				wakeLock.acquire();
				universalHandler.sendMessage(Message.obtain(universalHandler, PWV_UNIVERSALHANDLER_SHOW_WAITDIALOG));
				
				//+ Process
    			try {
    		    	List<Integer> mergeReport = Helpers.mergeVaults(vault, mergeVault);
    				saveVaultToDB();
    				
    				universalHandler.sendMessage(Message.obtain(universalHandler, PWV_UNIVERSALHANDLER_MERGE_FINALIZE, mergeReport));
    				
    			} catch (Exception e) {
    				e.printStackTrace();
    				universalHandler.sendMessage(Message.obtain(universalHandler, PWV_UNIVERSALHANDLER_SHOW_ERRORDIALOG, 
							Helpers.getShortenedStackTrace(e, 1)));
    			} 
        		//- Process
				
    			universalHandler.sendMessage(Message.obtain(universalHandler, PWV_UNIVERSALHANDLER_HIDE_WAITDIALOG));
    			wakeLock.release();
			}
		}).start();
    }
    
    /**  Leave Item Detail Layout */
    private void leaveItemDetailLayout()
    {
		currentFolder.notifyItemDataSetChanged();
		makeLayoutItemDetailReadOnly();
		resetItemsList();
		layoutSwitcher.showPrevious();
    	itemDeleteButton.setEnabled(true);
    	itemMoveToButton.setEnabled(true);
    	itemEditSaveButton.setEnabled(true);
    	
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    @Override
    protected void onStart()
    {
        setRunningCode(RUNNING_PASSWORDVAULTACTIVITY);
    	super.onStart();
    }
    
    /** Lock Screen */
    private void showScreenLockDialog()
    {
    	sld = new ScreenLockDialog(this, encryptor.getKeyHash());
        sld.show();
    }
    
    
    @Override
    protected void onPause() 
    {
        super.onPause();
        
        if(lockOnPause && layoutSwitcher.getDisplayedChild() > 0)
        {
	        screenLockedPosition = layoutSwitcher.getDisplayedChild();
	        layoutStartButtons.setVisibility(LinearLayout.GONE);
	        layoutSwitcher.setDisplayedChild(0);
	        
	        if(!Helpers.isScreenOn(this))
	        {
	        	showScreenLockDialog();
	        }
        }
    }
    
    @Override
    protected void onResume() 
    {
        super.onResume();
        
        if(sldVeto) {
        	doOnUnlock();
        	sldVeto = false;
        	return;
        }

        if(sld != null && sld.getActiveFlag()) return;
        
        if(screenLockedPosition > 0)
        {
        	showScreenLockDialog();
        }
    }
    
    //+ Lockable    
	public void doOnLock() {
		// N/A
	}
	
	public void doOnUnlock() {
		if(screenLockedPosition > 0) layoutSwitcher.setDisplayedChild(screenLockedPosition);
		screenLockedPosition = -1;
	}   
    //- Lockable
    
    /** Back Button - navigate back in the Password Vault Layers  
     *  if Folders or Start Layer, return to Main Menu
     */
    @Override
    public void onBackPressed()
    {
        switch (layoutSwitcher.getDisplayedChild()) 
        {        
        	case PWV_LAYOUT_FOLDERS:
        	{
        		handleExit();
        		break;
        	}    		
        	case PWV_LAYOUT_ITEMS:
        	{
        		layoutSwitcher.showPrevious();
        		break;
        	}
        	case PWV_LAYOUT_ITEMDETAIL:
        	{
            	boolean itemChanged = 
            	!(
            		currentItem.getItemName().equals(itemNameEditText.getText().toString().trim()) &&
            		currentItem.getItemPassword().equals(itemPasswordEditText.getText().toString().trim()) &&
            		currentItem.getItemComment().equals(itemCommentEditText.getText().toString().trim()) &&
            		(currentItem.getColorCode() == ColorHelper.getColorList().get(itemColorSpinner.getSelectedItemPosition()).colorCode ||
            		currentItem.getColorCode() == -1)       		
            	);
            	
            	if(itemChanged)
            	{
            		ComponentProvider.getBaseQuestionDialog(this, 
            				getResources().getString(R.string.common_save_text),  
            				getResources().getString(R.string.common_question_saveChanges), 
            				(String)itemEditSaveButton.getTag(), 
            				PWV_MESSAGE_ITEM_SAVE_CONFIRM
            				).show();
            	}
            	else leaveItemDetailLayout();
        		break;
        	}
        	case PWV_LAYOUT_START:
        	{
        		handleExit();
        		break;
        	}
        	default: 
            	break;
        }
    }
    
    /** Menu + Search Buttons */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {            
            return true;
        } 
        else if (keyCode == KeyEvent.KEYCODE_MENU) {
        	if(foldersBottomMenu != null)
        	{
	        	if(foldersBottomMenu.getVisibility() == LinearLayout.GONE) showBottomMenu();
	        	else hideBottomMenu();
        	}
        	return true;
        }
        else return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onConfigurationChanged(Configuration c)
    {
    	setLayoutItemDetailOrientation();
    		
    	super.onConfigurationChanged(c);
    }
    
    @Override
    public void onWindowFocusChanged(boolean b)
     {
    	if(this.encryptor == null)
    	{
    		layoutStartButtons.setVisibility(LinearLayout.VISIBLE);
    	}
    	super.onWindowFocusChanged(b);
    }  
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(ked != null) ked.stopDetection();
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) 
	{
		if (requestCode == REQUEST_CODE_SEND_PASSWORD) 
		{		
			if(resultCode != EXIT_CASCADE) 
				sldVeto = true;
			else
				setMessage(new ActivityMessage(EXIT_CASCADE, null));
		}  
	}
    Handler waitForSaveHandler = new Handler() 
    {
        public void handleMessage(Message msg)  
        {
        	if (msg.what == -100)
        	{            	
        		if(waitDialog != null) waitDialog.cancel();
        		return;
        	}
        	if (msg.what == -400)
        	{  
        		if(waitDialog != null) waitDialog.cancel();
        		Exception e = (Exception)msg.obj;
        		commonToast.setText(e.getMessage());
        		((ImageToast)commonToast).setImage(ImageToast.TOAST_IMAGE_CANCEL);
        		commonToast.show();
        		e.printStackTrace();
        	}
        }
    };
    
    /** Create default Vault Object on the first run */
    private Vault getVaultOnFirstRun(Vault v)
    {
    	Vault vault;
    	if(v == null) vault = Vault.getInstance();
    		else vault = v;
    	
    	//Items
    	VaultItem v00 = new VaultItem();
    	v00.setItemName(getResources().getString(R.string.pwv_data_item_00));
    	v00.setItemPassword(getResources().getString(R.string.pwv_data_item_00_password));
    	v00.setItemComment(getResources().getString(R.string.pwv_data_item_00_comment));
    	v00.setDateModified();
    	v00.setColorCode(Color.rgb(255, 0, 0));
    	waitPlease(3);
    	
    	VaultItem v01 = new VaultItem();
    	v01.setItemName(getResources().getString(R.string.pwv_data_item_01));
    	v01.setItemPassword(getResources().getString(R.string.pwv_data_item_01_password));
    	v01.setItemComment(getResources().getString(R.string.pwv_data_item_00_comment));
    	v01.setDateModified();
    	v01.setColorCode(Color.rgb(0, 0, 255));
    	
    	VaultItem v02 = new VaultItem();
    	v02.setItemName(getResources().getString(R.string.pwv_data_item_02));
    	v02.setItemPassword(getResources().getString(R.string.pwv_data_item_02_password));
    	v02.setItemComment(getResources().getString(R.string.pwv_data_item_02_comment));
    	v02.setDateModified();
    	v02.setColorCode(Color.rgb(255, 255, 0));
    	
    	//Folders
    	VaultFolder v0 = new VaultFolder();
    	v0.setFolderName(getResources().getString(R.string.pwv_data_folder_00));
    	v0.setColorCode(Color.rgb(255, 255, 0));
    	waitPlease(3);
    	vault.addFolder(v0);
    	
    	VaultFolder v1 = new VaultFolder();
    	v1.setFolderName(getResources().getString(R.string.pwv_data_folder_01));
    	v1.setColorCode(Color.rgb(0, 121, 240));
    	waitPlease(3);
    	vault.addFolder(v1);  	
    	
    	VaultFolder v2 = new VaultFolder();
    	v2.setFolderName(getResources().getString(R.string.pwv_data_folder_02));
    	v2.setColorCode(Color.rgb(0, 0, 255));
    	vault.addFolder(v2);
    	waitPlease(3);
    	
    	VaultFolder v3 = new VaultFolder();
    	v3.setFolderName(getResources().getString(R.string.pwv_data_folder_03));
    	v3.setColorCode(Color.rgb(255, 0, 0));
    	v3.addItem(v00);
    	v3.addItem(v01);
    	v3.addItem(v02);
    	vault.addFolder(v3);
    	
    	return vault;
    }
    
    /** Show Bottom Menu (Folders Layout) */
    private void showBottomMenu()
    {
		showMenuButton.setVisibility(LinearLayout.GONE);
		foldersBottomMenu.setVisibility(LinearLayout.VISIBLE);
    }
    
    /** Hide Bottom Menu (Folders Layout) */
    private void hideBottomMenu()
    {
		foldersBottomMenu.setVisibility(LinearLayout.GONE);
		showMenuButton.setVisibility(LinearLayout.VISIBLE);
    }
    
    private void waitPlease(int ms)
    {
    	try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    @SuppressWarnings("deprecation")
	private void setToSystemClipboard(String text)
    {
    	ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    	ClipMan.setText(text);
    }
    
    // Handler for miscellaneous background activities
    Handler universalHandler = new Handler() 
    {
        public void handleMessage(Message msg)  
        {    	
        	if (msg.what == PWV_UNIVERSALHANDLER_SHOW_WAITDIALOG)
        	{ 
        		if(waitDialog != null) waitDialog.show();
        		return;
        	}
        	if (msg.what == PWV_UNIVERSALHANDLER_HIDE_WAITDIALOG)
        	{ 
        		if(waitDialog != null) waitDialog.cancel();     		
        		return;
        	}
        	if (msg.what == PWV_UNIVERSALHANDLER_SHOW_ERRORDIALOG)
        	{ 
        		showErrorDialog((String)msg.obj);    		
        		return;
        	}
        	if (msg.what == PWV_UNIVERSALHANDLER_MERGE_FINALIZE)
        	{ 
        		iconAdapter.notifyDataSetChanged();        		
        		List<Integer> outputReportList = (List)msg.obj; 
        		int newFolders = outputReportList.get(0);
        		int newItems = outputReportList.get(1);
        		int replacedItems = outputReportList.get(2);
        		
        		int iconCode = (newFolders > 0 || newItems > 0 || replacedItems > 0) ? ComponentProvider.DRAWABLE_ICON_OK : ComponentProvider.DRAWABLE_ICON_INFO_BLUE;
        		
        		StringBuffer mergeReport = new StringBuffer();
        		mergeReport.append(getResources().getString(R.string.common_newIFolders) + ": " + newFolders + "<br/>");
        		mergeReport.append(getResources().getString(R.string.common_newItems) + ": " + newItems + "<br/>");
        		mergeReport.append(getResources().getString(R.string.common_replacedItems) + ": " + replacedItems + "<br/>");       		
        	   	
        		ComponentProvider.getShowMessageDialog(PasswordVaultActivity.this, 
        				getResources().getString(R.string.pwv_mergeVaults), 
        				mergeReport.toString(), 
        				iconCode).show();
        		
        		return;
        	}
        }
    };
}
