package org.ertebat.ui;
import java.io.InputStream;
import java.security.InvalidParameterException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class Utilities {
	private static final String TAG = "Utilities";
	
	public static Bitmap getPictureThumbnail(Context context, Uri uri, int thumbnailWidth) {
        try {
			InputStream input = context.getContentResolver().openInputStream(uri);

			BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
			onlyBoundsOptions.inJustDecodeBounds = true;
			onlyBoundsOptions.inDither=true;
			onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
			BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
			
			input.close();
			
			if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
			    return null;

			int originalWidth = onlyBoundsOptions.outWidth;

			double ratio = (originalWidth > thumbnailWidth) ? (originalWidth / thumbnailWidth) : 1.0;

			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
			bitmapOptions.inDither=true;
			bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
			
			input = context.getContentResolver().openInputStream(uri);
			Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
			input.close();
			
			return bitmap;
		} catch (Exception ex) {
			Log.d(TAG, ex.getMessage());
			return null;
		}
    }
	
	public static Bitmap getPictureThumbnail(Context context, String path, int thumbnailWidth) {
        try {
			BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
			onlyBoundsOptions.inJustDecodeBounds = true;
			onlyBoundsOptions.inDither=true;
			onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
			BitmapFactory.decodeFile(path, onlyBoundsOptions);
			
			if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
			    return null;

			int originalWidth = onlyBoundsOptions.outWidth;

			double ratio = (originalWidth > thumbnailWidth) ? (originalWidth / thumbnailWidth) : 1.0;

			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
			bitmapOptions.inDither=true;
			bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
			
			Bitmap bitmap = BitmapFactory.decodeFile(path, bitmapOptions);
			
			return bitmap;
		} catch (Exception ex) {
			Log.d(TAG, ex.getMessage());
			return null;
		}
    }

	public static String getMonthIndex(String monthAbbreviation) {
		if (monthAbbreviation.equals("Jan"))
			return "01";
		if (monthAbbreviation.equals("Feb"))
			return "02";
		if (monthAbbreviation.equals("Mar"))
			return "03";
		if (monthAbbreviation.equals("Apr"))
			return "04";
		if (monthAbbreviation.equals("May"))
			return "05";
		if (monthAbbreviation.equals("Jun"))
			return "06";
		if (monthAbbreviation.equals("Jul"))
			return "07";
		if (monthAbbreviation.equals("Aug"))
			return "08";
		if (monthAbbreviation.equals("Sep"))
			return "09";
		if (monthAbbreviation.equals("Oct"))
			return "10";
		if (monthAbbreviation.equals("Nov"))
			return "11";
		if (monthAbbreviation.equals("Dec"))
			return "12";
		else
			throw new InvalidParameterException("Invalid month abbreviation.");
	}


	private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        Log.d(TAG, String.valueOf(ratio) + " " + String.valueOf(k));
        if(k==0) return 1;
        else return k;
    }
}
