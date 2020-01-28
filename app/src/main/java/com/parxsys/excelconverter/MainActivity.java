package com.parxsys.excelconverter;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PICK_RESULT_CODE = 1;
    private List<User> userList = new ArrayList<>();

    XSSFWorkbook workbook;
    InputStream stream;
    String extension, src, value, key;
    Uri uri;
    Intent chooseFile;
    XSSFSheet sheet;
    FormulaEvaluator formulaEvaluator;
    Row row;
    StringBuilder rowValue;
    String[] column;
    Cell cell;
    CellValue cellValue;
    ProgressBar pb;
    JSONArray jsonArray=new JSONArray();

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        pb = findViewById(R.id.pb);

        checkFilePermissions();


        ////file picker intent
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        //////


        findViewById(R.id.btnRead).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(chooseFile, PICK_RESULT_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            uri = data != null ? data.getData() : null;
            src = uri != null ? uri.getPath() : "";
            if (src != null) {
                extension = src.substring(src.lastIndexOf(".") + 1);
                if (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls"))
                    new ReadDataTask().execute(uri);
            }
        }
    }

    private void checkFilePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
            permissionCheck += this.checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private void readData(Uri filePath) {
        try {
            stream = getContentResolver().openInputStream(filePath);
            try {
                if (stream != null) {
                    workbook = new XSSFWorkbook(stream);
                    sheet = workbook.getSheetAt(0);

                    int rowsCount = sheet.getPhysicalNumberOfRows();
                    formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    for (int r = 1; r < rowsCount; r++) {

                        row = sheet.getRow(r);

                        if (isRowEmpty(row)) {
                            break;
                        } else {
                            int cellsCount = row.getPhysicalNumberOfCells();

                            rowValue = new StringBuilder();
                            JSONObject object = new JSONObject();

                            for (int c = 1; c < cellsCount; c++) {
                                key = getCellAsString(sheet.getRow(0), c, formulaEvaluator);
                                value = getCellAsString(row, c, formulaEvaluator);
                                try {
                                    object.put(key, value);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            jsonArray.put(object);
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pb.setVisibility(View.GONE);
                            textView.setText(jsonArray.toString());

                        }
                    });


                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error:" + e.getLocalizedMessage());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "readData:" + e.getLocalizedMessage());
        }
    }

    private static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                return false;
        }
        return true;
    }

    protected String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        value = "";
        try {
            cell = row.getCell(c);
            cellValue = formulaEvaluator.evaluate(cell);

            if(cellValue!=null) {
                switch (cellValue.getCellType()) {
                    case Cell.CELL_TYPE_BOOLEAN:
                        value = "" + cellValue.getBooleanValue();
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        double numericValue = cellValue.getNumberValue();
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            double date = cellValue.getNumberValue();
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
                            value = formatter.format(HSSFDateUtil.getJavaDate(date));
                        } else {
                            value = "" + numericValue;
                        }
                        break;
                    case Cell.CELL_TYPE_STRING:
                        value = "" + cellValue.getStringValue();
                        break;
                    default:
                }
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "getCellAsString:Error" + e.getLocalizedMessage());
        }
        return value;
    }

    class ReadDataTask extends AsyncTask<Uri,Void,JSONArray>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONArray doInBackground(Uri... uris) {
            readData(uris[0]);
            return null;
        }
    }


}
