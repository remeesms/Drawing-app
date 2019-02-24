package com.example.linearc;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.linearc.Utils.DrawingView;
import com.example.linearc.Utils.ZoomableLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    DrawingView mDrawingView;
    public boolean isEraserOn;
    ImageView imgview;
    Button btn_save;
    LinearLayout mDrawingPad;
    private ProgressDialog pDialog;
    private static final String TAG = MainActivity.class.getSimpleName();
    public ZoomableLayout zoomlayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawingPad=(LinearLayout)findViewById(R.id.view_drawing_pad);
        imgview = findViewById(R.id.image);
        btn_save = findViewById(R.id.btn_save);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        zoomlayout = (ZoomableLayout)findViewById(R.id.zoom_layout);

        getImagesFromServer();

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bmp = mDrawingView.mBitmap;


                if(isStoragePermissionGranted()){
                    Bitmap bitmap = Bitmap.createBitmap(zoomlayout.getWidth(), zoomlayout.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    zoomlayout.draw(canvas);
                    saveToPhone(bitmap);
                }


            }
        });
    }

    public void saveToPhone(Bitmap bitmap){
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/LinarcImages");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        Log.i(TAG, "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(MainActivity.this,"Image has been saved to folder LinarcImages",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    public void getImagesFromServer(){
        pDialog.show();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {

                    URL newurl = new URL("https://homepages.cae.wisc.edu/~ece533/images/cat.png");
                    //https://homepages.cae.wisc.edu/~ece533/images/cat.png  https://www.gstatic.com/webp/gallery/1.jpg
                    Log.d("image_url",newurl.toString());
                    final Bitmap downloadedImage = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                    setImage(downloadedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        pDialog.dismiss();
    }
    public void setImage(final Bitmap image){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            imgview.setImageBitmap(image);
            mDrawingView = new DrawingView(MainActivity.this);
            mDrawingPad.addView(mDrawingView);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_clear) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do you want to clear whole marking in this page? Cannot undo this process.").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }

        if (id == R.id.menu_mark) {
            zoomlayout.setEnabled(false);
            mDrawingView.isTouchable = true;
            isEraserOn = false;
            mDrawingView.onClickEraser(isEraserOn);
            return true;
        }

        if (id == R.id.menu_eraser) {
            mDrawingView.isTouchable = true;
            isEraserOn = true;
            mDrawingView.onClickEraser(isEraserOn);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    mDrawingPad.removeAllViews();
                    mDrawingView = new DrawingView(MainActivity.this);
                    mDrawingPad.addView(mDrawingView);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };


}

