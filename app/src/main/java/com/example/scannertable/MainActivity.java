package com.example.scannertable;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    //-----------------------------Initialize values------------------------------------------------
    public static boolean WTmp = false,
            WPath = false;
    public static String path = null,
            pathToTpl = null;
    public static JSONObject template = null;
    public static TemplateJSON templateJSON;
    //----------------------------------------------------------------------------------------------


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 9999:
                if (data != null) {
                    String ssss = Environment.getExternalStorageDirectory().getAbsolutePath();
                    path = ssss + data.getData().getLastPathSegment().replace("primary:", "/");
                    Log.i("Test", "Result URI " + path);
                    WPath = true;
                }
                break;

            case 9998:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    Log.i("Test", "Result URI: " + uri);
                    File source = new File(uri.getPath());
                    Log.i("Test","2) "+ source.toString());
                    String filename = uri.getLastPathSegment();
                    Log.i("Test","3) "+ filename);
                    File destination = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/" + filename.replace("primary:", ""));
                    Log.i("Test", "4) " + destination.toString());
                    try {
                        FileReader reader = new FileReader(destination.toString());
                        String ss = "";
                        int c;
                        while((c=reader.read())!=-1){
                            ss += (char)c;
                        }
                        templateJSON = JSONConverter(ss);
                        Log.i("Test", "5) " + ss);
                        WTmp = true;
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }

                    // File reader
                }
                break;
        }
    }

    public TemplateJSON JSONConverter(String response) throws JSONException {
        JSONObject userJson = new JSONObject(response);
        int n = userJson.getInt("n");
        JSONArray sizeJSON = userJson.getJSONArray("size");
        int[] size = new int[2];
        for (int i = 0; i < sizeJSON.length(); ++i) {
            size[i] = sizeJSON.getInt(i);
        }
        int[][] fields = new int[n][4];
        for (int i = 0; i < n; ++i) {
            JSONArray field = userJson.getJSONArray("field_" + i);
            for (int j = 0; j < field.length() - 1; ++j) {
                fields[i][j] = field.getInt(j);
            }
        }
        String[] fields_name = new String[n];
        for (int i = 0; i < n; ++i) {
            JSONArray field = userJson.getJSONArray("field_" + i);
            fields_name[i] = field.getString(4);
        }
        return new TemplateJSON(n, size, fields, fields_name);
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
                    "Exception: " + t,
                    Toast.LENGTH_LONG).show();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1
            );
        }
        Button btChoosePath = findViewById(R.id.btChoosePath);
        Button btAddVariant = findViewById(R.id.btAddVariant);
        Button btOK = findViewById(R.id.btMainConfirm);
        Button btCreateTemplate = findViewById(R.id.btCreateTemplate);
        Button btChooseTemplate = findViewById(R.id.btChooseTemplate);

        btChoosePath.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
        }); // Ok
        btCreateTemplate.setOnClickListener(view -> {

        });
        btChooseTemplate.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("application/*");

            i.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(
                        Intent.createChooser(i, "Select a File to Upload"),
                        9998);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(MainActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            }
        });
        btAddVariant.setOnClickListener(view -> {
            if (WTmp){
                if(WPath){
                    enableAdd();
                } else{
                    Toast.makeText(MainActivity.this, "Path is None", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(MainActivity.this, "Template is None", Toast.LENGTH_SHORT).show();
            }
        });
        btOK.setOnClickListener(view -> {});
    }
    private void enableAdd() {
        Intent intent = new Intent(this, ActivityAdd.class);
        startActivity(intent);
    }
}
