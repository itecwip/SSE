package com.paranoiaworks.unicus.android.sse.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery.LayoutParams;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.dao.Vault;
import com.paranoiaworks.unicus.android.sse.dao.VaultFolder;
import com.paranoiaworks.unicus.android.sse.utils.ColorHelper;

/**
 * Provides "Text under Folder icon" appearance for Vault object
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class PWVFolderAdapter extends BaseAdapter {
    private Context mContext;
    private Vault passwordVault;
    
    public PWVFolderAdapter(Context c, Vault passwordVault) {
        mContext = c;
        this.passwordVault = passwordVault;
    }

    public int getCount() {
        return passwordVault.getFolderCount();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        TextView textView;
        View layout;
        int colorCode;
        if (convertView == null)
        {       	
        	layout = ((Activity)mContext).getLayoutInflater().inflate(R.layout.lc_icon, null);
        	imageView = (ImageView) layout.findViewById(R.id.iconImagePW);
        	textView = (TextView) layout.findViewById(R.id.iconTextPW);
        	layout.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        } else {
            layout = (View)convertView;
            imageView = (ImageView) layout.findViewById(R.id.iconImagePW);
            textView = (TextView) layout.findViewById(R.id.iconTextPW);
        }
    	
        VaultFolder actualFolder = passwordVault.getFolderByIndex(position);     
        
    	textView.setText(actualFolder.getFolderName());  	
    	textView.setTag(actualFolder.getFolderSecurityHash());
           	
    	colorCode = actualFolder.getColorCode();
    	imageView.setImageResource(ColorHelper.getColorBean(colorCode).folderIconRId);

        return layout;
    }
}