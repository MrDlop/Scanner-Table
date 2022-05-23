package com.example.scannertable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.List;

public class RecognizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        List<View> attributes = new ArrayList<>();
        final LinearLayout linear = findViewById(R.id.layout);
        int x1, x2, y1, y2;

        for (int i = 0; i < MainActivity.templateJSON.getN(); ++i) {
            int[] arr = MainActivity.templateJSON.getFieldN(i);
            x1 = arr[0];
            y1 = arr[1];
            x2 = arr[2];
            y2 = arr[3];
            Bitmap bitmap = Bitmap.createBitmap(CameraService.image, x1, y1, x2 - x1, y2 - y1);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    // Task completed successfully
                                    // ...
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });
            String resultText = result.getResult().getText();
            final View view = getLayoutInflater().inflate(R.layout.layout, null);
            TextView textView = view.findViewById(R.id.eq);
            textView.setText(MainActivity.templateJSON.getFieldName(i));
            EditText text = view.findViewById(R.id.ed);
            text.setText(resultText);
            //добавляем все что создаем в массив
            attributes.add(view);
            //добавляем елементы в linearlayout
            linear.addView(view);
        }
        Button btOKTwo = findViewById(R.id.RecognizeOK);
        btOKTwo.setOnClickListener(view -> {
            finish();

        });
    }
}