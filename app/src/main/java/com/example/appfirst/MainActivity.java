package com.example.appfirst;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import java.sql.SQLOutput;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private VideoView videoView;
    private Button btnAddPhoto;
    private Button btnNewPhoto;
    private Button btnAddVideo;
    private Button btnNewVideo;
    private static final int PICK_IMAGES_CODE = 0;
    private static final int PICK_VIDEO_REQUEST = 0;
    private static final int REQUEST_CODE_VIDEO_CAPTURE = 2607;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageIntent();
            }
        });

        btnNewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNewImage();
            }
        });

        btnAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickVideoIntent();
            }
        });

        btnNewVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordVideo();
//                Intent intent = new Intent(MainActivity.this, UserInstruction.class);
//                startActivity(intent);
            }
        });
    }

    private void getNewImage() {
        Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCamera, PICK_IMAGES_CODE);
    }
    private void pickImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), PICK_IMAGES_CODE);
    }

    private void pickVideoIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_CODE_VIDEO_CAPTURE );
    }

    private void recordVideo() {
        Intent recordVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(recordVideo, REQUEST_CODE_VIDEO_CAPTURE);

//        if(recordVideo.resolveActivity(getPackageManager()) == null) {
//            startActivityForResult(recordVideo, REQUEST_CODE_VIDEO_CAPTURE);
//        }
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            videoView.setVideoURI(videoUri);
            videoView.start();
        } else {
            //if(requestCode == PICK_IMAGES_CODE && resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(photo);
           // }
        }
    }

    private void initViews(){
        imageView = findViewById(R.id.captureImage);
        videoView = findViewById(R.id.videoView);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnNewPhoto = findViewById(R.id.btnNewPhoto);
        btnAddVideo = findViewById(R.id.btnAddVideo);
        btnNewVideo = findViewById(R.id.btnNewVideo);
    }
}