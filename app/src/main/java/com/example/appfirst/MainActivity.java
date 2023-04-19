package com.example.appfirst;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appfirst.ml.ModelWithMetadata;
import com.example.appfirst.ml.ModelMush1;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.model.Model;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String MODEL_PATH = "model_with_metadata.tflite";
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "tflite_label_map.txt";
    private static final int INPUT_SIZE = 160;

    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor();

    private Button btnAddPhoto;
    private Button btnNewPhoto;
    private Button btnAddVideo;
    private Button btnNewVideo;

    private TextView textViewResult;

    private ImageView imageView;
    private ImageView imgFrame1;
    private ImageView imgFrame2;

    private VideoView videoView;

    int imageSize = 224;
    private static final int PICK_IMAGES_CODE = 0;
    private static final int IMAGES_CAPTURE_CODE = 1;
    private static final int PICK_VIDEO_CODE = 2;
    private static final int VIDEO_CAPTURE_CODE = 2607;
    public static final String CheckInstruction = "0";
    public String aaa = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initTensorFlowAndLoadModel();

        SharedPreferences sp = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = sp.edit();

        if (!sp.contains(CheckInstruction)) {
            editor.putString(CheckInstruction, "1");
            editor.commit();
            showStartActivity();
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
    }

    private void showStartActivity() {
        //start popup activity
        Intent intent = new Intent(MainActivity.this, StartFrame1.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    private void classifyImage(Bitmap bitmap){
        try {
            Model.Options options;
            CompatibilityList compatList = new CompatibilityList();

            if(compatList.isDelegateSupportedOnThisDevice()){
                // if the device has a supported GPU, add the GPU delegate
                options = new Model.Options.Builder().setDevice(Model.Device.GPU).build();
            } else {
                // if the GPU is not supported, run on 4 threads
                options = new Model.Options.Builder().setNumThreads(4).build();
            }

            ModelMush1 model = ModelMush1.newInstance(getApplicationContext(), options);


            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 160, 160, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(convertBitmapToByteBuffer(bitmap));


            // Runs model inference and gets result.
            ModelMush1.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] data1=outputFeature0.getFloatArray();
            textViewResult.setText(outputFeature0.getDataType().toString());
            String s = String.valueOf(data1[1]) + "\n" + String.valueOf(data1[2]) + "\n" + String.valueOf(data1[3]);
            textViewResult.setText(s);

//            float[] confidences = outputFeature0.getFloatArray();
//            int maxPos = 0;
//            float maxConfidence = 0;
//            for(int i = 0; i < confidences.length; i++){
//                if(confidences[i] > maxConfidence){
//                    maxConfidence = confidences[i];
//                    maxPos = i;
//                }
//            }
//            String[] classes = {"0", "Odnobochka", "Poganka", "3"};
//            textViewResult.setText(classes[maxPos]);
//            String s = "";
//            for(int i = 0; i < classes.length; i++){
//                s +=  classes[i] + " " + confidences[i];
//            }
//            textViewResult.setText(aaa);
//            aaa += "0";

            // Releases model resources if no longer used.
            model.close();

//            ModelWithMetadata model = ModelWithMetadata.newInstance(getApplicationContext(), options);
//            // Creates inputs for reference.
//            TensorImage image = TensorImage.fromBitmap(bitmap);
//
//            // Runs model inference and gets result.
//            ModelWithMetadata.Outputs outputs = model.process(image);
//            ModelWithMetadata.DetectionResult detectionResult;
//            String text = "";
//
//            for (int i = 0; i < 3; i++) {
//                detectionResult = outputs.getDetectionResultList().get(i);
//                float location = detectionResult.getScoreAsFloat();
//                //RectF category = detectionResult.getLocationAsRectF();
//                String score = detectionResult.getCategoryAsString();
//
//                text += score + " " + location + "\n";
//            }
//            textViewResult.setText(text);
//            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }


    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        final int BATCH_SIZE = 1;
        final int PIXEL_SIZE = 3;

        final int IMAGE_MEAN = 128;
        final float IMAGE_STD = 128.0f;

        byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE);

        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
        return byteBuffer;
    }


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case (IMAGES_CAPTURE_CODE):
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    image = ThumbnailUtils.extractThumbnail(image, image.getWidth(), image.getHeight());
                    imageView.setImageBitmap(image);
                    image = Bitmap.createScaledBitmap(image, INPUT_SIZE, INPUT_SIZE, false);
                    //final List<Classifier.Recognition> results = classifier.recognizeImage(image);
                    //textViewResult.setText(results.toString());
                    classifyImage(image);
                    break;

                case (PICK_IMAGES_CODE):
                    Uri imageUri = data.getData();
//                    imageView.setImageURI(imageUri);
                    try {
                        Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        bitmap1 = Bitmap.createScaledBitmap(bitmap1, INPUT_SIZE, INPUT_SIZE, false);
                        imageView.setImageBitmap(bitmap1);
                        imageView.setImageBitmap(bitmap1);
                        classifyImage(bitmap1);
                        //final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap1);
                        //textViewResult.setText(results.toString());
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
                    img = img.copy(Bitmap.Config.ARGB_8888, true);
                    imageView.setImageBitmap(img);
                    classifyImage(img);
                    //final List<Classifier.Recognition> results2 = classifier.recognizeImage(img);
                    //textViewResult.setText(results2.toString());
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
        textViewResult = findViewById(R.id.textViewResult);

    }
}