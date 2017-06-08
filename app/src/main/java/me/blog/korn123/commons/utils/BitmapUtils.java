package me.blog.korn123.commons.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;

/**
 * Created by hanjoong on 2017-06-08.
 */

public class BitmapUtils {

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize, int fixedWidth, int fixedHeight)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        o2.outWidth = fixedWidth;
        o2.outHeight = fixedHeight;
        Bitmap tempBitmap = BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, fixedWidth, fixedHeight, false);
        return scaledBitmap;
    }

}
