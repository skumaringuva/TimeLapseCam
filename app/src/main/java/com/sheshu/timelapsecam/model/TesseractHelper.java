package com.sheshu.timelapsecam.model;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.sheshu.timelapsecam.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by Sheshu on 7/9/17.
 */
public class TesseractHelper {
    private final Activity mActivity;
    private TessBaseAPI mTess; //Tess API reference
    private static final String TAG = "TessHelper";
    private String datapath;
    public TesseractHelper(Activity activity){
        mActivity = activity;
    }


    public void initTesseract(){

        //init image
       Bitmap image = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.test_image);

        //initialize Tesseract API
        String lang = "eng";
         datapath = mActivity.getFilesDir()+ "/tesseract/";
        mTess = new TessBaseAPI();

        checkFile(new File(datapath + "tessdata/"));
        mTess.init(datapath, lang);
    }


    public String processImageView(Image image)
    {

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        image.close();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        String OCRresult = null;
        mTess.setImage(bitmapImage);
        OCRresult = mTess.getUTF8Text();

        Log.e(TAG,"OCR RESULT "+OCRresult);
        return OCRresult;

    }


    public String processImageView(Bitmap bitmap)
    {

        String OCRresult = null;
        mTess.setImage(bitmap);

        OCRresult = mTess.getUTF8Text();
        bitmap.recycle();
        Log.e(TAG,"OCR RESULT "+OCRresult);
        return OCRresult;

    }


    String languages[] = {"eng","spa","hin","tel"};

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){

            copyFiles(languages);

        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles(languages);
            }
        }
    }

    private void copyFiles(String[] languages) {
        for (String language : languages) {
            try {
                String filepath = datapath + "/tessdata/"+language+".traineddata";
                AssetManager assetManager = mActivity.getAssets();
                InputStream instream = assetManager.open("tessdata/"+language+".traineddata");
                OutputStream outstream = new FileOutputStream(filepath);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = instream.read(buffer)) != -1) {
                    outstream.write(buffer, 0, read);
                }
                outstream.flush();
                outstream.close();
                instream.close();
                File file = new File(filepath);
                if (!file.exists()) {
                    throw new FileNotFoundException();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
