package com.example.scannertable;

import android.content.Context;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelService {
    private static Workbook workbook = null;

    public static void createExcelWorkbook() {
        workbook = new HSSFWorkbook();
        String EXCEL_SHEET_NAME = "Sheet1";
        Sheet sheet = workbook.createSheet(EXCEL_SHEET_NAME);
        for(int i = 0; i < MainActivity.data.size(); ++i){
            Row row = sheet.createRow(i);
            for(int j = 0; j < MainActivity.data.get(i).size();++j){
                Cell cell = row.createCell(j);
                cell.setCellValue(MainActivity.data.get(i).get(j));
            }
        }
    }

    public static  boolean storeExcelInStorage(Context context, String path){
        boolean isSuccess = false;
        File file = new File(path);
        FileOutputStream fileOutputStream = null;

        try{
            fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            Log.i(MainActivity.LOG_TAG, "Writing file" + file);
            isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(MainActivity.LOG_TAG, "Error writing Exception: " + e);
        } catch (Exception e){
            e.printStackTrace();
            Log.e(MainActivity.LOG_TAG, "Failed to save due Exception: " + e);
        } finally {
            try{
                if(null != fileOutputStream)
                    fileOutputStream.close();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return isSuccess;
    }
}
