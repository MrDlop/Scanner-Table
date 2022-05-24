package com.example.scannertable;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.Objects;

public class RecognizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        String resultText;
        Button btOKTwo = findViewById(R.id.RecognizeOK);
        LinearLayout linear = findViewById(R.id.layout);
        int x1, x2, y1, y2;
        Log.i("Test", "ON");
        for (int i = 0; i < MainActivity.templateJSON.getN(); ++i) {
            int[] arr = MainActivity.templateJSON.getFieldN(i);
            x1 = arr[0];
            y1 = arr[1];
            x2 = arr[2];
            y2 = arr[3];
            Bitmap bitmap = Bitmap.createBitmap(CameraService.image, x1, y1, x2 - x1, y2 - y1);
            Log.i("Test", "ON 2");
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();
            Log.i("Test", "ON 3");
            Task<FirebaseVisionText> result = detector.processImage(image);
            Log.i("Test", "ON 4");
            try {
                resultText = Objects.requireNonNull(result.getResult()).getText();
                Log.i("Test", "ON 5");
            }catch (Exception e){
                resultText = "No found";
                Log.e("Test", e.toString());
            }
            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.template_for_recognize, null, false);
            TextView textView = view.findViewById(R.id.nameView);
            textView.setText(MainActivity.templateJSON.getFieldName(i));
            EditText text = view.findViewById(R.id.contentView);
            Log.i("Test", "Recognize text: " + resultText);
            text.setText(resultText);
            linear.addView(view);
        }
        btOKTwo.setOnClickListener(view -> finish());
    }
}