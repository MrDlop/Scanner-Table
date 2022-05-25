package com.example.scannertable;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //-----------------------------Initialize values------------------------------------------------
    public static final String LOG_TAG = "Test";

    private static final int FILE_REQUEST_CODE = 11;
    private final String[] FILE_PERMISSION = new String[]{READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static TemplateJSON templateJSON;
    public static String path = null;


    private boolean WTmp = false,
            WPath = false;
    //----------------------------------------------------------------------------------------------


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 9999:
                if (data != null) {
                    path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                            data.getData().getLastPathSegment().replace("primary:", "/");
                    Log.i(LOG_TAG, "Result path " + path);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Input file name");

                    final EditText input = new EditText(this);
                    builder.setView(input);
                    builder.setPositiveButton("OK", (dialog, which) -> {
                        path += input.getText().toString() + ".xlsx";
                        Log.i(LOG_TAG, "Result path and update " + path);
                        WPath = true;
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        WPath = false;
                        dialog.cancel();
                    });
                    builder.show();

                }
                break;

            case 9998:
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        String pathToTemplate = Environment.getExternalStorageDirectory().getAbsolutePath() +
                                data.getData().getLastPathSegment().replace("primary:", "/");
                        Log.i(LOG_TAG, "Result URI " + pathToTemplate);
                        FileReader reader = new FileReader(pathToTemplate);
                        String JSONContent;
                        int c;
                        StringBuilder JSONContentBuilder = new StringBuilder();
                        while ((c = reader.read()) != -1) {
                            JSONContentBuilder.append((char) c);
                        }
                        JSONContent = JSONContentBuilder.toString();
                        templateJSON = JSONConverter(JSONContent);
                        Log.i(LOG_TAG, "Content " + JSONContent);
                        WTmp = true;
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    protected TemplateJSON JSONConverter(String response) throws JSONException {
        JSONObject userJson = new JSONObject(response);
        int n = userJson.getInt("n");
        JSONArray sizeJSON = userJson.getJSONArray("size");
        int[] size = new int[2];
        for (int i = 0; i < sizeJSON.length(); ++i) {
            size[i] = sizeJSON.getInt(i);
        }
        int[][] fields = new int[n][4];
        String[] fields_name = new String[n];
        for (int i = 0; i < n; ++i) {
            JSONArray field = userJson.getJSONArray("field_" + i);
            for (int j = 0; j < field.length() - 1; ++j) {
                fields[i][j] = field.getInt(j);
            }
            fields_name[i] = field.getString(4);
        }
        return new TemplateJSON(n, size, fields, fields_name);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btChoosePath = findViewById(R.id.btChoosePath);
        Button btAddVariant = findViewById(R.id.btAddVariant);
        Button btOK = findViewById(R.id.btMainConfirm);
        Button btCreateTemplate = findViewById(R.id.btCreateTemplate);
        Button btChooseTemplate = findViewById(R.id.btChooseTemplate);

        btChoosePath.setOnClickListener(view -> {
            if (hasFilePermissions()) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
            } else {
                requestPermission();
            }
        });
        btCreateTemplate.setOnClickListener(view -> {

        });
        btChooseTemplate.setOnClickListener(view -> {
            if (hasFilePermissions()) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("application/*");

                i.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(
                            Intent.createChooser(i, "Select a File to Upload"),
                            9998);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btAddVariant.setOnClickListener(view -> {
            if (WTmp) {
                enableAdd();
            } else {
                Toast.makeText(MainActivity.this, "Template is None", Toast.LENGTH_SHORT).show();
            }
        });
        btOK.setOnClickListener(view -> {
            if(WPath){

            }else{
                Toast.makeText(MainActivity.this, "Path is None", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableAdd() {
        Intent intent = new Intent(this, ActivityAdd.class);
        startActivity(intent);
    }

    private boolean hasFilePermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                FILE_PERMISSION,
                FILE_REQUEST_CODE
        );
    }
}
