package com.thisway.xunfeicloud;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class offLineDataActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = offLineDataActivity.class.getSimpleName();
    private Button btn_createDB,btn_addData,btn_queryData, btn_deleteData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_off_line_data);
        initView();
    }

    private void initView() {
        btn_createDB = (Button) findViewById(R.id.btn_createDB);
        btn_addData = (Button) findViewById(R.id.btn_addData);
        btn_queryData = (Button) findViewById(R.id.btn_queryData);
        btn_deleteData = (Button) findViewById(R.id.btn_deleteData);

        btn_createDB.setOnClickListener(this);
        btn_addData.setOnClickListener(this);
        btn_queryData.setOnClickListener(this);
        btn_deleteData.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_createDB://创建数据库
                LogUtil.i(TAG, "btn_createDB");
                LitePal.getDatabase();
                break;

            case R.id.btn_addData://添加数据库数据
                LogUtil.i(TAG, "btn_addData");
                readXunfeiAsrExcel();
                readInstructionsExcel();

                break;

            case R.id.btn_queryData://查询数据库数据
                LogUtil.i(TAG, "btn_queryData");
               List<RecognitionInstruction> instructions = DataSupport.findAll(RecognitionInstruction.class);
                for (RecognitionInstruction instruction : instructions) {

                    Log.d("Data", " id is " + instruction.getId());
                   Log.d("Data", " instuctionID is " + instruction.getInstuctionID());
                    Log.d("Data", " instruction is " + instruction.getInstruction());
                    Log.d("Data", "answer is " + instruction.getAnswer());
                }


                List<xunfeiAsr> xunfeiAsrs = DataSupport.findAll(xunfeiAsr.class);
                for (xunfeiAsr xunfeiasr : xunfeiAsrs) {

                    Log.d("Data", " id is " + xunfeiasr.getId());
                    Log.d("Data", " keyID is " + xunfeiasr.getKeyID());
                    Log.d("Data", " key is " + xunfeiasr.getKey());
                    Log.d("Data", "answer is " + xunfeiasr.getAnswer());
                }

                break;


            case R.id.btn_deleteData://删除数据库中的数据
                LogUtil.i(TAG, "btn_deleteData");
                DataSupport.deleteAll(RecognitionInstruction.class);
                DataSupport.deleteAll(xunfeiAsr.class);
                break;

        }

    }

    /******************************读取excel表*************************************/

//读取InstructionsExcel表
    private void readInstructionsExcel() {

        AssetManager assetManager = getResources().getAssets();

        try {
            //InputStream input = new FileInputStream(new File(excelPath));
            InputStream input = assetManager.open("instructions.xls");
            POIFSFileSystem fs = new POIFSFileSystem(input);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            // Iterate over each row in the sheet
            Iterator<Row> rows = sheet.rowIterator();
            while (rows.hasNext()) {

                HSSFRow row = (HSSFRow) rows.next();
                LogUtil.i(TAG,"Row #" + row.getRowNum());
                //每一行 = 新建一个类
                RecognitionInstruction instruction11 = new RecognitionInstruction();
                // Iterate over each cell in the row and print out the cell"s
                // content
                Iterator<Cell> cells = row.cellIterator();

                HSSFCell cell_1 = (HSSFCell) cells.next();

                LogUtil.i("EXCEL","number= " + (int) (cell_1.getNumericCellValue()));
                if((int) (cell_1.getNumericCellValue()) == 0)
                break;
                instruction11.setInstuctionID((int) (cell_1.getNumericCellValue())); ;

                HSSFCell cell_2 = (HSSFCell) cells.next();
                LogUtil.i("EXCEL","string= " + cell_2.getStringCellValue());
                instruction11.setInstruction(cell_2.getStringCellValue()) ;


                HSSFCell cell_3 = (HSSFCell) cells.next();
                LogUtil.i("EXCEL","string= " + cell_3.getStringCellValue());
                instruction11.setAnswer(cell_3.getStringCellValue()); ;

                instruction11.save();
 ;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //刷新列表
        //getAllStudent();
    }


    private void readXunfeiAsrExcel() {

        AssetManager assetManager = getResources().getAssets();

        try {
            //InputStream input = new FileInputStream(new File(excelPath));
            InputStream input = assetManager.open("xunfeiAsr.xls");
            POIFSFileSystem fs = new POIFSFileSystem(input);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            // Iterate over each row in the sheet
            Iterator<Row> rows = sheet.rowIterator();
            while (rows.hasNext()) {

                HSSFRow row = (HSSFRow) rows.next();
                LogUtil.i(TAG,"Row #" + row.getRowNum());
                System.out.println("Row #" + row.getRowNum());
                //每一行 = 新建一个类
                xunfeiAsr xunfeiAsr1 = new xunfeiAsr();
               // RecognitionInstruction instruction11 = new RecognitionInstruction();

                // Iterate over each cell in the row and print out the cell"s
                // content
                Iterator<Cell> cells = row.cellIterator();

                HSSFCell cell_1 = (HSSFCell) cells.next();

                LogUtil.i("xunfeiAsrEXCEL","number= " + (long) (cell_1.getNumericCellValue()));
                if((long) (cell_1.getNumericCellValue()) == 0)
                    break;
                xunfeiAsr1.setKeyID((long) (cell_1.getNumericCellValue())); ;

                HSSFCell cell_2 = (HSSFCell) cells.next();
                LogUtil.i("xunfeiAsrEXCEL","string= " + cell_2.getStringCellValue());
                xunfeiAsr1.setKey(cell_2.getStringCellValue());
               // xunfeiAsr1.setInstruction(cell_2.getStringCellValue()) ;


                HSSFCell cell_3 = (HSSFCell) cells.next();
                LogUtil.i("xunfeiAsrEXCEL","string= " + cell_3.getStringCellValue());
                xunfeiAsr1.setAnswer(cell_3.getStringCellValue()); ;

                xunfeiAsr1.save();

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //刷新列表
        //getAllStudent();
    }





}
