package com.paranoiaworks.unicus.android.sse.components;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.paranoiaworks.unicus.android.sse.CryptActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.adapters.IconListAdapter;
import com.paranoiaworks.unicus.android.sse.dao.ActivityMessage;

/**
 * Simple Item Selection Dialog
 * 
 * @author Paranoia Works
 * @version 1.0.4
 */
public class SelectionDialog extends SecureDialog {

	public static final int ITEMTYPE_NORMAL = 0;
	public static final int ITEMTYPE_INACTIVE = 1;
	public static final int ITEMTYPE_HIGHLIGHTED = 2;
	
	private Context context;
	
	private final List<String> title;
	private List<String> comment;
	private List<Integer> iconResource;
	private List<Integer> itemTypes;
	private List<Object> tag;
	private Object attachment;
	private boolean blockShow = false;
	
	private TruncateAt commentTruncateAt = null;	
	
	private String dialogTitle = null;
	
	private int messageCode = -1;
	
	public SelectionDialog(View v, List<String> title,  List<String> comment, List<Integer> iconResource, List<Object> tag) 
	{
		this(v, title, comment, iconResource, tag, null);
	}
	
	public SelectionDialog(View v, List<String> title,  List<String> comment, List<Integer> iconResource, List<Object> tag, String dialogTitle) 
	{
		this((Activity)v.getContext(), title, comment, iconResource, tag, dialogTitle);
	}	
	
	public SelectionDialog(Activity context, List<String> title,  List<String> comment, List<Integer> iconResource, List<Object> tag) 
	{
		this(context, title, comment, iconResource, tag, null);
	}
	
	public SelectionDialog(Activity context, List<String> title,  List<String> comment, List<Integer> iconResource, List<Object> tag, String dialogTitle) 
	{
		this(context, title, comment, iconResource, tag, dialogTitle, false);
	}
	
	public SelectionDialog(Activity context, List<String> title,  List<String> comment, List<Integer> iconResource, List<Object> tag, String dialogTitle, boolean dontInit) 
	{
		super(context);
		this.context = context;
		this.title = title;
		if (title == null || title.size() < 1) {
			blockShow = true;
			return;
		}		
		this.comment = comment;
		this.iconResource = iconResource;
		this.tag = tag;
		this.dialogTitle = dialogTitle;
		if(!dontInit) init();
	}
	
    @Override
    public void show() 
    {
    	if(!blockShow) {
    		super.show();
    	}
    	else {
    		new ImageToast(context.getResources().getString(R.string.common_noItems_text), 
    				ImageToast.TOAST_IMAGE_CANCEL, (Activity)context).show();
    	}
    }
	
	public void setMessageCode(int messageCode)
	{
		this.messageCode = messageCode;
	}
	
	public void deferredInit() {
		init();
	}
	
	public void setCommentEllipsize(TruncateAt where) {
		this.commentTruncateAt = where;
	}
	
	public void setItemTypes(List<Integer> itemType) {
		this.itemTypes = itemType;
	}
	
	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	private void init()
	{		
		if(dialogTitle == null) this.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lc_selection_dialog);
		this.setCancelable(true);
		this.setCanceledOnTouchOutside(false);
		if(dialogTitle != null) setTitle(dialogTitle);

		ListView items = (ListView)findViewById(R.id.SD_listView);
		if(itemTypes != null) items.setDivider(null);
		
		if(comment == null) comment = new ArrayList<String>();
		if(iconResource == null) iconResource = new ArrayList<Integer>();		
		if(tag == null) { 
			tag = new ArrayList<Object>();
			for (String element : title) {
			    tag.add(element);
			}
		}
		
    	IconListAdapter ila = new IconListAdapter(context, title, comment, iconResource, itemTypes, commentTruncateAt);
    	items.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				CryptActivity ca = (CryptActivity)context;
        		String mainMessage = null;
        		if(tag.get(position) instanceof String) mainMessage = (String)tag.get(position);
        		ActivityMessage am = new ActivityMessage(messageCode, mainMessage, tag.get(position), attachment);
        		ca.setMessage(am);
				cancel();
				return;
			}
		});
    	
    	items.setAdapter(ila);	
    } 
}
