package com.Ninja.Camera_Dialer;

import android.Manifest;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect;
    private ImageView ivImage;
    private String userChoosenTask;
    private Uri file;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelect = (Button) findViewById(R.id.button);
        btnSelect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        ivImage = (ImageView) findViewById(R.id.Logo_pic);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
    }



    public class GenericFileProvider extends FileProvider {}

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        file = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider",getOutputMediaFile());
        //fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);

        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d("CameraDemo", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }


        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            switch (requestCode) {
                case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if(userChoosenTask.equals("Take Photo"))
                            cameraIntent();
                        else if(userChoosenTask.equals("Choose from Library"))
                            galleryIntent();
                    } else {
                        //code for deny
                    }
                    break;
            }
        }

        public void selectImage() {
            final CharSequence[] items = { "Take Photo", "Choose from Library",
                    "Cancel" };

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Add Photo!");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    boolean result=Utility.checkPermission(MainActivity.this);

                    if (items[item].equals("Take Photo")) {
                        userChoosenTask ="Take Photo";
                        if(result)
                            cameraIntent();

                    } else if (items[item].equals("Choose from Library")) {
                        userChoosenTask ="Choose from Library";
                        if(result)
                            galleryIntent();

                    } else if (items[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        }

        private void galleryIntent()
        {


            Intent intent=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);

        }

        private void cameraIntent()
        {
            takePicture();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECT_FILE)
                {
                    file = data.getData();
                    //ivImage.setImageURI(file);
                    ImageToString();
                }
                else if (requestCode == REQUEST_CAMERA) {
                    //ivImage.setImageURI(file);
                    ImageToString();
                }
            }
        }




        private void ImageToString (){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //Log.d("width",String.valueOf(bm.getWidth()));
            //Log.d("height",String.valueOf(bm.getHeight()));
            //bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object

//            byte[] byteArrayImage = baos.toByteArray();
//            String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
//            Log.d("Image String", encodedImage);
//            System.out.println(encodedImage.length());

            //Go to the next Activity
            Intent myIntent = new Intent(getBaseContext(),Call_Save.class);
            //Log.d("imagefile", file.toString());
            myIntent.putExtra("Image_file", file);
            startActivity(myIntent);
            //System.out.println(encodedImage);
            //System.out.println(Arrays.toString(byteArrayImage));

        }


}
