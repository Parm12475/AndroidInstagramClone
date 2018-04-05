package com.example.harindermaan.instagramclone.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Harinder Maan on 08/03/2018.
 */

public class ImageManager
{
    private static final String TAG = "ImageManager";

    public static Bitmap getBitMap(String imgUrl)
    {
        File imageFile = new File(imgUrl);
        FileInputStream fis= null;
        Bitmap bitmap = null;

        try
        {
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        }//try
        catch(FileNotFoundException ex)
        {
            Log.d(TAG, "getBitMap: "+ ex.getMessage());
        }//catch
        finally
        {
            try
            {
                fis.close();
            }//try
            catch(IOException ex)
            {
                Log.d(TAG, "getBitMap: ERROR: "+ex.getMessage());
            }//catch
        }//finally

        return bitmap;
    }//getBitMap

    public static byte[] getBytesFromBitmap(Bitmap bm,int quality)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }//getBytesFromBitmap

}//ImageManager
