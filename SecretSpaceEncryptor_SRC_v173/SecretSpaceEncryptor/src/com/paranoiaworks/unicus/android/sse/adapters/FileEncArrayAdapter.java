package com.paranoiaworks.unicus.android.sse.adapters;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.paranoiaworks.unicus.android.sse.FileEncActivity;
import com.paranoiaworks.unicus.android.sse.R;
import com.paranoiaworks.unicus.android.sse.StaticApp;
import com.paranoiaworks.unicus.android.sse.misc.CryptFileWrapper;
import com.paranoiaworks.unicus.android.sse.misc.ThumbnailMakerTask;
import com.paranoiaworks.unicus.android.sse.utils.BitmapCacherCompressed;
import com.paranoiaworks.unicus.android.sse.utils.Helpers;

import ext.com.andraskindler.quickscroll.Scrollable;

/**
 * Provides "File Row" appearance (for the File Encryptor)
 * 
 * @author Paranoia Works
 * @version 1.0.6
 */
public class FileEncArrayAdapter extends BaseAdapter implements Scrollable {
	private final FileEncActivity context;
	private final List<CryptFileWrapper> files;
	private final Map<String, long[]> dirSizeMap = new HashMap<String, long[]>(); // dirs size cache
	private boolean dataHasChanged = false;
	private int thumbnailSize = -1;
	private BitmapCacherCompressed bitmapCacher;
	
	private Handler selectHadler;

	public FileEncArrayAdapter(FileEncActivity context, List<CryptFileWrapper> files) 
	{
		this.context = context;
		this.files = files;	
	}
	
    public int getCount() {
        return files.size();
    }
    
    public Object getItem(int position) {
    	return files.get(position);
    }
    
    public long getItemId(int position) {
        return position;
    }
	
	static class ViewHolder {
		FrameLayout fileIconWrapper;
		ImageView fileIcon;
		ImageView selectIcon;
		TextView fileName;
		TextView fileSize;
		TextView fileDate;
		double originalTextSize = -1;
		int originalColor;
		int goldColor;
		AsyncTask<Object, Void, Bitmap> task; 
		
		boolean lastRenderSpecial = false;
	}
	
    public void setSelectHandler(Handler selectHadler)
    {
    	this.selectHadler = selectHadler;
    }

	@Override
	public synchronized View getView(int position, View convertView, ViewGroup parent) 
	{
		if(thumbnailSize < 0) 
		{
	    	double multiplier = 1.0;
	    	int sizeCode = (context.getThumbnailSizeCode());
	    	if(sizeCode == 2)
	    		multiplier = 1.25;
	    	else if(sizeCode == 3)
	    		multiplier = 1.50;
	    	
	    	thumbnailSize = StaticApp.dpToPx((float)Math.floor(52 * multiplier)); // (57 - 2 - 2 - 1)
		}
		
		if(bitmapCacher == null)
		{
			long thumbnailSizeInBytes = (thumbnailSize * thumbnailSize * 4);
			long freeMemory = Helpers.getMaxFreeMemory();
			int cacheSize = (int)((freeMemory - 10485760) / thumbnailSizeInBytes); // min 10MB free
			if(cacheSize < 10) cacheSize = 10;	
			if(cacheSize > 500) cacheSize = 500;	
			bitmapCacher = new BitmapCacherCompressed(cacheSize);
		}
		
		ViewHolder holder;
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.lc_fileenc_listrow, null, true);
			holder = new ViewHolder();
			holder.fileName = (TextView) rowView.findViewById(R.id.FE_fileName);
			holder.fileSize = (TextView) rowView.findViewById(R.id.FE_fileSize);
			holder.fileDate = (TextView) rowView.findViewById(R.id.FE_fileDate);
			holder.fileIcon = (ImageView) rowView.findViewById(R.id.FE_fileIcon);
			holder.selectIcon = (ImageView) rowView.findViewById(R.id.FE_selectedIcon);
			holder.fileIconWrapper = (FrameLayout) rowView.findViewById(R.id.FE_fileIconWrapper);
			
			if (holder.originalTextSize < 0)
			{
				holder.originalTextSize = holder.fileName.getTextSize();
				holder.fileName.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float)(holder.originalTextSize * 1.2));
				holder.originalColor = rowView.getResources().getColor(R.color.white_file);
				holder.goldColor = rowView.getResources().getColor(R.color.gold);
				
				resetView(holder, rowView);
			}
			
			rowView.setTag(holder);
			
		} else {
			holder = (ViewHolder) rowView.getTag();
			
			if(holder.task != null) {
				holder.task.cancel(true);
				holder.task = null;
			}
		}
			
		final CryptFileWrapper tempFile = files.get(position);		
		boolean writable = (tempFile.getMode() == CryptFileWrapper.MODE_FILE) ? tempFile.canWrite() : true;
		boolean encrypted = tempFile.isEncrypted();
		
		holder.fileIconWrapper.setOnClickListener(new View.OnClickListener() 
	    {
		    @Override
		    public void onClick(View v) 
		    {
		    	selectHadler.sendMessage(Message.obtain(selectHadler, FileEncActivity.FEA_UNIVERSALHANDLER_SELECT_DIR, tempFile));
		    }
	    });
		holder.fileIconWrapper.setClickable(false);		
		
		if (holder.lastRenderSpecial)resetView(holder, rowView);

		holder.fileName.setText(tempFile.getName());		
		
		if (tempFile.isFile()) holder.fileSize.setText(Helpers.getFormatedFileSize(tempFile.length()));
			else holder.fileSize.setText("");
		
		if (!tempFile.isBackDir()) holder.fileDate.setText(Helpers.getFormatedDate(tempFile.lastModified(), rowView.getResources().getConfiguration().locale));
		else 
		{
			holder.fileName.setText(".../" + tempFile.getName());
			holder.fileSize.setText("");
			holder.fileDate.setText("");
			holder.fileIcon.setImageResource(R.drawable.back_file);
			return rowView;
		}
		
		if(tempFile.isSelected())
		{
			holder.selectIcon.setImageResource(R.drawable.selected);
			rowView.setBackgroundResource(R.drawable.d_filerow_selected);
			holder.lastRenderSpecial = true;
		}

		if (tempFile.isDirectory()) 
		{
			holder.fileIconWrapper.setClickable(true);	
			if(writable)holder.fileIcon.setImageResource(R.drawable.directory);
				else holder.fileIcon.setImageResource(R.drawable.directory_readonly);
			long[] dirParam = dirSizeMap.get(tempFile.getUniqueIdentifier());
			if(tempFile.isSelected())
			{
				if(dirParam == null) holder.fileSize.setText("...");
				else holder.fileSize.setText("");
			}
			
			if(dirParam != null && dirParam.length > 2 && dirParam[0] > -1)
			{
				holder.fileSize.setText(Helpers.getFormatedFileSize(dirParam[0]));			
				holder.fileDate.setText("(" + rowView.getResources().getString(R.string.common_files_text).toLowerCase() +": " + dirParam[1] + ")");
			}
		} 
		else if(tempFile.isFile()) 
		{
			if(encrypted)
			{
				if(writable) holder.fileIcon.setImageResource(R.drawable.file_enc);
					else holder.fileIcon.setImageResource(R.drawable.file_enc_readonly);
				if(!tempFile.isSelected())rowView.setBackgroundResource(R.drawable.d_filerow_encrypted);
				holder.fileName.setTextColor(holder.goldColor);
				holder.fileSize.setTextColor(holder.goldColor);
				holder.fileDate.setTextColor(holder.goldColor);
				holder.lastRenderSpecial = true;
			}
			else
			{				
				String type = null;
				String fileExt = Helpers.getFileExtWrapped(tempFile).toLowerCase();

				if(fileExt != null && fileExt.length() >= 3)
				{
					if(context.getResources().getString(R.string.fileExtensionVideo).indexOf(fileExt) > -1) type = "video";
					else if(context.getResources().getString(R.string.fileExtensionAudio).indexOf(fileExt) > -1) type = "audio";
					else if(context.getResources().getString(R.string.fileExtensionImage).indexOf(fileExt) > -1) type = "image";
					else if(context.getResources().getString(R.string.fileExtensionText).indexOf(fileExt) > -1) type = "text";	
					else if(context.getResources().getString(R.string.fileExtensionArchive).indexOf(fileExt) > -1) type = "archive";
					else if(context.getResources().getString(R.string.fileExtensionMSWord).indexOf(fileExt) > -1) type = "msword";	
					else if(context.getResources().getString(R.string.fileExtensionMSExcel).indexOf(fileExt) > -1) type = "msexcel";
					else if(context.getResources().getString(R.string.fileExtensionMSPowerPoint).indexOf(fileExt) > -1) type = "mspowerpoint";
					else if(context.getResources().getString(R.string.fileExtensionPDF).indexOf(fileExt) > -1) type = "pdf";
					else if(context.getResources().getString(R.string.fileExtensionPWV).indexOf(fileExt) > -1) type = "pwv";
				}
				
				try {
					if(type == null) type = URLConnection.guessContentTypeFromName(tempFile.getName());
				} catch (Exception e) {}
				
				boolean standardFile = type == null ? true : false;
								
				if(!standardFile)
				{				
					if(type.startsWith("image"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_image);
							else holder.fileIcon.setImageResource(R.drawable.file_image_readonly);

						Bitmap thumbnailFromCache = bitmapCacher.getBitmap(tempFile.getUniqueIdentifier());
						if(thumbnailFromCache != null)
						{
							holder.fileIcon.setImageBitmap(thumbnailFromCache);
						}
						else
						{					
							if(context.getThumbnailSizeCode() != 0) // thumbnail
								holder.task = new ThumbnailMakerTask(holder.fileIcon).execute(tempFile, ThumbnailMakerTask.TYPE_IMAGE, thumbnailSize, bitmapCacher);
						}
					}
					else if(type.startsWith("video"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_video);
							else holder.fileIcon.setImageResource(R.drawable.file_video_readonly);
						
						//holder.task = new ThumbnailMakerTask(holder.fileIcon).execute(tempFile, ThumbnailMakerTask.TYPE_VIDEO);
					}
					else if(type.startsWith("audio"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_audio);
							else holder.fileIcon.setImageResource(R.drawable.file_audio_readonly);
					}
					else if(type.startsWith("text"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_text);
							else holder.fileIcon.setImageResource(R.drawable.file_text_readonly);
					}
					else if(type.startsWith("archive"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_archive);
							else holder.fileIcon.setImageResource(R.drawable.file_archive_readonly);
					}
					else if(type.startsWith("msword"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_word);
							else holder.fileIcon.setImageResource(R.drawable.file_word_readonly);
					}
					else if(type.startsWith("msexcel"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_excel);
							else holder.fileIcon.setImageResource(R.drawable.file_enc_readonly);
					}
					else if(type.startsWith("mspowerpoint"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_powerpoint);
							else holder.fileIcon.setImageResource(R.drawable.file_powerpoint_readonly);
					}
					else if(type.startsWith("pdf"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_pdf);
							else holder.fileIcon.setImageResource(R.drawable.file_pdf_readonly);
					}
					else if(type.startsWith("pwv"))
					{
						if(writable) holder.fileIcon.setImageResource(R.drawable.file_pwv);
							else holder.fileIcon.setImageResource(R.drawable.file_pwv_readonly);
					}
					else standardFile = true;
				}

				if(standardFile)
				{
					if(writable) holder.fileIcon.setImageResource(R.drawable.file);
						else holder.fileIcon.setImageResource(R.drawable.file_readonly);
				}
			}
		} 
		else 
		{
			if(tempFile.isSpecialType()) holder.fileDate.setText("");
			holder.fileIcon.setImageResource(R.drawable.null_image); // others - no image
		}
		
		return rowView;
	}
	
	private void resetView(ViewHolder holder, View rowView)
	{
		holder.selectIcon.setImageResource(R.drawable.null_image); 
		rowView.setBackgroundResource(R.drawable.d_filerow);
		holder.fileName.setTextColor(holder.originalColor);
		holder.fileSize.setTextColor(holder.originalColor);
		holder.fileDate.setTextColor(holder.originalColor);
		holder.lastRenderSpecial = false;		
	}
	
	public void setDirSize(String dirPath, long[] dirParam)
	{
		dirSizeMap.put(dirPath, dirParam);
	}
	
	public void removeDirSize(String dirPath)
	{
		dirSizeMap.remove(dirPath);
	}
	
	public void clearDirSizeMap()
	{
		dirSizeMap.clear();
	}
	
	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
		dataHasChanged = true;
	}
	
	public String getIndicatorForPosition(int childposition, int groupposition) {
		CryptFileWrapper tempFile = files.get(childposition);
		if(tempFile.isBackDir()) return "..:Y";
		return Character.toString(tempFile.getName().toUpperCase().charAt(0)) + ":" + (tempFile.isDirectory() ? "Y" : "W");
	}

	public int getScrollPosition(int childposition, int groupposition) {
		return childposition;
	}
	
	public synchronized boolean hasDataChanged()
	{
		boolean temp = dataHasChanged;
		dataHasChanged = false;
		return temp;
	}
}