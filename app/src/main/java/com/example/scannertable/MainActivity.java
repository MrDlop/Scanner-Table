package com.example.scannertable;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //-----------------------------Initialize values------------------------------------------------
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    public static boolean WPhoto = false,
            WTmp = false,
            WPath = false;
    public static String path = null,
            pathToTpl = null;
    public static JSONObject template = null;
    public ImageView imageView = null;
    public static TemplateJSON templateJSON;
    //----------------------------------------------------------------------------------------------

    public TemplateJSON getUser(String response) throws JSONException {
        JSONObject userJson = new JSONObject(response);
        int n = userJson.getInt("n");
        JSONArray sizeJSON = userJson.getJSONArray("size");
        int[] size = new int[2];
        for (int i = 0; i < sizeJSON.length(); ++i) {
            size[i] = sizeJSON.getInt(i);
        }
        int[][] fields = new int[n][4];
        for (int i = 0; i < n; ++i) {
            JSONArray field = userJson.getJSONArray("field_" + Integer.toString(i));
            for (int j = 0; j < field.length() - 1; ++j) {
                fields[i][j] = field.getInt(j);
            }
        }
        String[] fields_name = new String[n];
        for (int i = 0; i < n; ++i) {
            JSONArray field = userJson.getJSONArray("field_" + Integer.toString(i));
            fields_name[i] = field.getString(4);
        }
        return new TemplateJSON(n, size, fields, fields_name);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 9998:
                if (data != null) {
                    Log.i("Test", "Result URI " + data.getData().getPath());
                    Log.i("Test", "Result URI " + data.getData().toString());
                    path = data.getData().getPath().replace("/document/primary:",
                            "/storage/emulated/0/");
                    File file = new File(path);
                    Log.i("OK", "------------OK-----------------");
                    // File reader
                }
                break;
            case 9999:
                if (data != null) {
                    path = data.getData().getPath().replace("/document/primary:",
                            "/storage/emulated/0/");
                    Log.i("Test", "Result URI " + path);
                    WPath = true;
                }
                break;
        }
    }

    private void openFile(String fileName) {
        try {
            InputStream inputStream = openFileInput(fileName);

            if (inputStream != null) {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isr);
                String line;
                StringBuilder builder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                inputStream.close();
                template = new JSONObject(builder.toString());
            }
        } catch (Throwable t) {
            Toast.makeText(getApplicationContext(),
                    "Exception: " + t.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btOpenCamera = (Button) findViewById(R.id.btOpenCamera);
        Button btChoosePath = (Button) findViewById(R.id.btChoosePath);
        Button btCreateTemplate = (Button) findViewById(R.id.btCreateTemplate);
        Button btChooseTemplate = (Button) findViewById(R.id.btChooseTemplate);
        Button btBt = (Button) findViewById(R.id.btBt);
        Button btConfirm = (Button) findViewById(R.id.btMainConfirm);
        imageView = (ImageView) findViewById(R.id.imageView);

        btOpenCamera.setOnClickListener(view -> {
            if (hasCameraPermission()) {
                if (WTmp) {
                    enableCamera();
                } else
                    Toast.makeText(MainActivity.this, "Template is None", Toast.LENGTH_SHORT).show();
            } else {
                requestPermission();
            }
        }); // Ok
        btChoosePath.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
        }); // Ok
        btCreateTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        btChooseTemplate.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("application/*");
            try {
                startActivityForResult(
                        Intent.createChooser(i, "Select a File to Upload"),
                        9998);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(MainActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            }
        });
        btBt.setOnClickListener(view -> {
            if (CameraService.image != null) {
                imageView.setImageBitmap(CameraService.image);
            }
        }); // Ok
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WPath) {
                    if (WTmp) {
                        if (WPhoto) {

                        } else {
                            Toast.makeText(MainActivity.this, "Photo is None", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Template is None", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Path is None", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    private void enableCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
