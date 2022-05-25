package com.example.scannertable;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class RecognizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        String resultText;
        Button btOKTwo = findViewById(R.id.RecognizeOK);
        Button btBack = findViewById(R.id.RecognizeBack);
        LinearLayout linear = findViewById(R.id.layout);
        List<View> attributes = new ArrayList<>();
        int x1, x2, y1, y2;
        Log.i("Test", "ON");
        MainActivity.data.add(new Vector<>(MainActivity.templateJSON.getN()));
        for (int i = 0; i < MainActivity.templateJSON.getN(); ++i) {
            int[] arr = MainActivity.templateJSON.getFieldN(i);
            x1 = arr[0];
            y1 = arr[1];
            x2 = arr[2];
            y2 = arr[3];
            Log.i(MainActivity.LOG_TAG, "x*y = " + CameraService.image.getHeight() + "X" + CameraService.image.getWidth());
            Bitmap bitmap = Bitmap.createBitmap(CameraService.image, x1, y1, x2 - x1, y2 - y1);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();
            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    Log.i("Test", "Recognize ok");
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Test", "Error recognize");
                                        }
                                    });

            try {
                resultText = Objects.requireNonNull(result.getResult()).getText();
            } catch (Exception e) {
                resultText = "No found";
                Log.e("Test", e.toString());
            }
            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.template_for_recognize, null, false);
            TextView textView = view.findViewById(R.id.nameView);
            ImageView imageView = view.findViewById(R.id.imageViewTemplate);
            imageView.setImageBitmap(bitmap);
            textView.setText(MainActivity.templateJSON.getFieldName(i));
            EditText text = view.findViewById(R.id.contentView);
            Log.i("Test", "Recognize text: " + resultText);
            text.setText(resultText);
            attributes.add(view);
            linear.addView(view);
        }
        btOKTwo.setOnClickListener(view -> {
            finish();
            for(View i: attributes){
                EditText editText = i.findViewById(R.id.contentView);
                MainActivity.data.get(MainActivity.data.size() - 1).add(editText.getText().toString());
            }
            ActivityAdd.getResultActivityRecognize = true;
        });
        btBack.setOnClickListener(view -> {
            finish();
            ActivityAdd.getResultActivityRecognize = false;
            MainActivity.data.remove(MainActivity.data.size() - 1);
        });
    }
}