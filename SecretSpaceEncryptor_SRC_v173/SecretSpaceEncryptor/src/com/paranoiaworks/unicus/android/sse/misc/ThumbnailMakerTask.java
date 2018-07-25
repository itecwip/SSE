package com.paranoiaworks.unicus.android.sse.misc;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.paranoiaworks.unicus.android.sse.utils.BitmapCacherCompressed;

/**
 * AsyncTask - Make image thumbnail
 * 
 * @author Unicus (unicus<atmark>paranoiaworks.com) for Paranoia Works
 * @version 1.0.0
 */
public class ThumbnailMakerTask extends AsyncTask<Object, Void, Bitmap> {

    private final ImageView imageViewReference;
    
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    public ThumbnailMakerTask(ImageView imageView) {
        imageViewReference = imageView;
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        return makeThumbnail((CryptFileWrapper)params[0], (Integer)params[1], (Integer)params[2], (BitmapCacherCompressed)params[3]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }


        if (bitmap != null) {
        	imageViewReference.setImageBitmap(bitmap);
        }

    }
    
    private Bitmap makeThumbnail(CryptFileWrapper cryptFileWrapper, int type, int thumbnailSize, BitmapCacherCompressed bitmapCacher) 
    {  	    	
    	Bitmap thumbnail = null;
    	try {
    		
    		if(type == TYPE_IMAGE) 
    		{
	    		//thumbnail = getSampleBitmapFromCryptFileWrapper(cryptFileWrapper, 128, 128);
	    		thumbnail = ThumbnailUtils.extractThumbnail(getSampleBitmapFromCryptFileWrapper(cryptFileWrapper, thumbnailSize, thumbnailSize), thumbnailSize, thumbnailSize);
    		}
    		else if(type == TYPE_VIDEO) // TODO
    		{
    			thumbnail = ThumbnailUtils.extractThumbnail(ThumbnailUtils.createVideoThumbnail(cryptFileWrapper.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND), thumbnailSize, thumbnailSize); 
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	if(thumbnail != null)
    		bitmapCacher.putBitmap(cryptFileWrapper.getUniqueIdentifier(), thumbnail);
    	
    	return thumbnail;
    }
     
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) 
    {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float)height / (float)reqHeight);
            final int widthRatio = Math.round((float)width / (float)reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    } 
    
    private static Bitmap getSampleBitmapFromCryptFileWrapper(CryptFileWrapper cryptFileWrapper, int reqWidth, int reqHeight) throws IOException 
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(cryptFileWrapper.getInputStream(), null, options);

        int scale = calculateInSampleSize(options, reqWidth, reqHeight);

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        return BitmapFactory.decodeStream(cryptFileWrapper.getInputStream(), null, o2);
    }
}