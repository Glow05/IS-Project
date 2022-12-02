package com.gloria.glowapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gloria.glowapp.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    private Button select, predict;
    private TextView tv;
    private Bitmap img;
    int imageSize = 32;



//Initialize Variable
    ImageView imageView;
    Button btOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assign Value
        imageView = findViewById(R.id.image_view);
        btOpen = findViewById(R.id.button_camera);

        //Initialization
        tv = (TextView) findViewById(R.id.textView2);
        predict = (Button) findViewById(R.id.button_predict);
        select = (Button) findViewById(R.id.button_gallery);

        //The gallery Button
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Opening a new screen to get an image in the devise
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 101);
            }
        });
        //The Predict Button
        predict.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

               //We are in the 13th minute
               //First convert the model to a tflite model
                img = Bitmap.createScaledBitmap(img, 32, 32, true);
                try {
                    Model model = Model.newInstance(getApplicationContext());

                    // Creates inputs for reference.
                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);

                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(img);
                    ByteBuffer byteBuffer =tensorImage.getBuffer();


                    inputFeature0.loadBuffer(byteBuffer);

                    // Runs model inference and gets result.
                    Model.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                    // Releases model resources if no longer used.
                    model.close();

                     //..................
                    // Runs model inference and gets result.

                    float[] confidences = outputFeature0.getFloatArray();
                    //Find the index of the class with the biggest confidence.
                    int maxPos = 0;
                    float maxConfidence = 0;
                    for (int i = 0; i < confidences.length; i++){
                        if (confidences[i] > maxConfidence){
                            maxConfidence = confidences[i];
                            maxPos = i;
                        }
                    }
                    //Displaying!!!!!!!!!!!!!
                    // this is the second attempt
                    // Look at it Glowiee
                    String[] classes = {"Bottle", "Spoon", "Cup" };
                    predict.setText(classes[maxPos]);


                   //tv.setText(outputFeature0.getFloatArray()[0] + "\n"+outputFeature0.getFloatArray()[1]);

                } catch (IOException e) {
                    // TODO Handle the exception
                }

            }
        });
        //Request for Camera
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    100);
        }
        btOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open Camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);

            }
        });
    }
   public void classifyImage(Bitmap capturedImage){
    try {
        Model model = Model.newInstance(getApplicationContext());

        // Creates inputs for reference.
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
        ByteBuffer byteBuffer =ByteBuffer.allocateDirect(4 * imageSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[imageSize * imageSize];
        capturedImage.getPixels(intValues, 0, capturedImage.getWidth(), 0, 0, capturedImage.getWidth(), capturedImage.getHeight());

        int pixel = 0;
        //Iterate over each pixel and extract R, G and B values. Add those values individually to the byte buffer
        for (int i = 0; i < imageSize; i++){
            for (int j = 0; j < imageSize; j++){
                int val = intValues[pixel++];//RGB
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                byteBuffer.putFloat((val & 0xFF) * (1.f / 1));

            }
        }


        inputFeature0.loadBuffer(byteBuffer);

        // Runs model inference and gets result.
        Model.Outputs outputs = model.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

        float[] confidences = outputFeature0.getFloatArray();
        //Find the index of the class with the biggest confidence.
        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < confidences.length; i++){
            if (confidences[i] > maxConfidence){
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }
        //Displaying!!!!!!!!!!!!!
        String[] classes = {"Cup", "Spoon", "Plate" };
        predict.setText(classes[maxPos]);




        // Releases model resources if no longer used.
        model.close();
    } catch (IOException e) {
        // TODO Handle the exception
    }

}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            //Get Capture Image
            Bitmap captureImage = (Bitmap) data.getExtras().get("data");
            //Rescaling the image to a square since the model identifies squared images
            int dimension = Math.min(captureImage.getWidth(), captureImage.getHeight());
            captureImage = ThumbnailUtils.extractThumbnail(captureImage, dimension,dimension);
            //Set Capture Image to ImageView
            imageView.setImageBitmap(captureImage);

            //This converts the image captured from the camera to 32 by 32
            captureImage = Bitmap.createScaledBitmap(captureImage, imageSize, imageSize, false);
            classifyImage(captureImage);

            //Get the selected Image and set it to the ImageView
        }else if (requestCode == 101){
                imageView.setImageURI(data.getData());
                Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Glowie Convert the image from gallery to a size 32 by 32 this is in minute 9 of the #2nd video
            // imageView.setImageBitmap(image);
            //image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            //classifyImage(image);
        }

            // Intent myIntent = new Intent(this,SecondActivity.class);
            //        startActivity(myIntent);

        }
    }



