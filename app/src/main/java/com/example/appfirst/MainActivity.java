package com.example.appfirst;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String MODEL_PATH = "model.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private ImageView imageView;
    private VideoView videoView;
    private Button btnAddPhoto;
    private Button btnNewPhoto;
    private Button btnAddVideo;
    private Button btnNewVideo;
    private Button btnTest;
    private Button btnTest2;
    private TextView textViewResult;
    private TextView textViewTest;
    private ImageView imgFrame1;
    private ImageView imgFrame2;
    private EditText editText;
    private Switch switch1;
    int imageSize = 224;
    private static final int PICK_IMAGES_CODE = 0;
    private static final int IMAGES_CAPTURE_CODE = 1;
    private static final int PICK_VIDEO_CODE = 2;
    private static final int VIDEO_CAPTURE_CODE = 2607;

    public static final String SHARE_PREFS = "sharedPrefs";
    public static final String CheckInstruction = "1";
    private FrameLayout popUp;


    private String text;
    private boolean switchOff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initTensorFlowAndLoadModel();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        if (pref.contains(CheckInstruction)) {
            showStartActivity();
            textViewTest.setText(pref.getString(CheckInstruction, ""));
            editor.putString(CheckInstruction, "1");
            editor.commit();
        }

        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    pickImageIntent();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        btnNewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    getNewImage();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        btnAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    pickVideoIntent();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        btnNewVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    recordVideo();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

        loadData();


//        editor.putInt(CheckInstruction, "string value"); // Storing string
//
//        editor.commit(); // commit changes
//
//        if (pref.contains(CheckInstruction)) {
//            textViewTest.setText(pref.getString(CheckInstruction, ""));
//        }

    }

    private void showStartActivity() {
        //start popup activity

        Intent intent = new Intent(MainActivity.this, StartFrame1.class);
        startActivity(intent);
    }

    public void loadData(){

    }

    private void getNewImage() {
        Intent openCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(openCamera, IMAGES_CAPTURE_CODE);
    }
    private void pickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), PICK_IMAGES_CODE);

    }

    private void pickVideoIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_CAPTURE_CODE);
    }

    private void recordVideo() {
        Intent recordVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(recordVideo, VIDEO_CAPTURE_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case (IMAGES_CAPTURE_CODE):
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    image = ThumbnailUtils.extractThumbnail(image, image.getWidth(), image.getHeight());
                    imageView.setImageBitmap(image);
                    image = Bitmap.createScaledBitmap(image, INPUT_SIZE, INPUT_SIZE, false);
                    final List<Classifier.Recognition> results = classifier.recognizeImage(image);
                    textViewResult.setText(results.toString());
                    break;
                case (PICK_IMAGES_CODE):
                    Uri imageUri = data.getData();
//                    imageView.setImageURI(imageUri);
                    try {
                        Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        bitmap1 = Bitmap.createScaledBitmap(bitmap1, INPUT_SIZE, INPUT_SIZE, false);
                        imageView.setImageBitmap(bitmap1);

                        final List<Classifier.Recognition> results1 = classifier.recognizeImage(bitmap1);
                        textViewResult.setText(results1.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    break;
                case (VIDEO_CAPTURE_CODE):
                    Uri videoUri = data.getData();

                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(this, videoUri);

                    imgFrame1.setImageBitmap(retriever.getFrameAtTime(200,MediaMetadataRetriever.OPTION_CLOSEST));
                    imgFrame2.setImageBitmap(retriever.getFrameAtTime(10000000,MediaMetadataRetriever.OPTION_CLOSEST));

                    videoView.setVideoURI(videoUri);
                    videoView.start();

                    Bitmap img = (Bitmap) retriever.getFrameAtTime(200,MediaMetadataRetriever.OPTION_CLOSEST);
                    img = Bitmap.createScaledBitmap(img, INPUT_SIZE, INPUT_SIZE, false);
                    final List<Classifier.Recognition> results2 = classifier.recognizeImage(img);
                    textViewResult.setText(results2.toString());
                    imageView.setImageBitmap(img);
                    System.out.println("******************************");
                    System.out.println(results2.toString());

                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnAddPhoto.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViews(){

        imageView = findViewById(R.id.captureImage);
        imgFrame1 = findViewById(R.id.imgFrame1);
        imgFrame2 = findViewById(R.id.imgFrame2);
        videoView = findViewById(R.id.videoView);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnNewPhoto = findViewById(R.id.btnNewPhoto);
        btnAddVideo = findViewById(R.id.btnAddVideo);
        btnNewVideo = findViewById(R.id.btnNewVideo);
        //btnTest = findViewById(R.id.btnTest);
        //btnTest2 = findViewById(R.id.btnTest2);
        textViewResult = findViewById(R.id.textViewResult);
        textViewTest = findViewById(R.id.textViewTest);
        //editText = findViewById(R.id.editText);
        //switch1 = findViewById(R.id.switch1);
//        popUp = findViewById(R.id.popUp);
    }
}