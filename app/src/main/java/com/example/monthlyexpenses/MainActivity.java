package com.example.monthlyexpenses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    public static final String T_ID = "tID";
    public static final String T_AMOUNT = "tAmount";
    public static final String BALANCE = "balance";
    public static final String T_TITLE = "tTitle";
    public static final String T_DATE = "tDate";
    public static final String T_TYPE = "tType";
    public static final String TABLE_NAME = "tableName";
    private static RecyclerView recyclerViewMain;
    public static DBHelper DB;
    private static ArrayList<NodeInfo> dataList=new ArrayList<NodeInfo>();
    public static String GAVE_MONEY="GaveMoney";
    public static String GOT_MONEY="GotMoney";
    public static String TRANSACTION_TYPE="TransactionType";
    public static String UPDATE_TRANSACTION="UpdateTransaction";
    public static String BUNDLE_KEY="BundleKey";
    public static String DATABASE_NAME="MonthlyExpenses.db";
    public static String MASTER_TABLE="MasterTable";
    public static int totalTable,showTableNo;
    private static TextView textViewMonthYear,textViewCurrentMonthStatus,textViewAvailableBalance;
    private static String currentMonthUID;
    public static DecimalFormat formatter;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DB = new DBHelper(this);
        showTableNo=DB.getAllTables().size();
        formatter= new DecimalFormat("#,##,###");
        findViewById(R.id.textViewYouGot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,InputActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString(TRANSACTION_TYPE,GOT_MONEY);
                intent.putExtra(BUNDLE_KEY,bundle);
                startActivity(intent);
            }
        });
        findViewById(R.id.imageViewGoLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showTableNo>1){
                    showTableNo--;
                    refreshRecyclerView(false);
                }
                else
                    Toast.makeText(MainActivity.this, "No data found", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.imageViewGoRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showTableNo<totalTable) {
                    showTableNo++;
                    refreshRecyclerView(false);
                }
                else
                    Toast.makeText(MainActivity.this, "No data found", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.textViewYouGave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,InputActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString(TRANSACTION_TYPE,GAVE_MONEY);
                intent.putExtra(BUNDLE_KEY,bundle);
                startActivity(intent);
            }
        });
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String current_date= formatter.format(date);
        currentMonthUID=dateToTableName(current_date).replace(" ","");
        textViewMonthYear=findViewById(R.id.textViewMonthYear);
        textViewCurrentMonthStatus=findViewById(R.id.textViewCurrentMonthStatus);
        textViewAvailableBalance=findViewById(R.id.textViewAvailableBalance);
        recyclerViewMain=findViewById(R.id.recyclerViewMain);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},101);
        textViewMonthYear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(totalTable!=0){
                    try {
                        sharePDF(DB.getAllTables().get(showTableNo-1).getTable_casual_name());
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });
        recyclerViewMain.setLayoutManager(new LinearLayoutManager(this));
        refreshRecyclerView(true);


    }

    private void sharePDF(String table_casual_name) throws Exception {
        String table_unique_name=table_casual_name.replace(" ","");

        String fileName=table_casual_name.replace(" ","_")+"_"+System.currentTimeMillis()+".pdf";
        File pdfFile=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),fileName);
        FileOutputStream fileOutputStream=new FileOutputStream(pdfFile);
        Document document=new Document();
        PdfWriter.getInstance(document,fileOutputStream);
        Uri uri= FileProvider.getUriForFile(MainActivity.this,BuildConfig.APPLICATION_ID+".provider",pdfFile);

        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri,"application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent,"Sare File:"));

        document.open();

        Font fontHeading=new Font(Font.FontFamily.HELVETICA,22.0f,Font.BOLD,new BaseColor(57,57,57));
        Font fontSubHeading=new Font(Font.FontFamily.HELVETICA,17.0f,Font.NORMAL,new BaseColor(57,57,57));
        Font tableText=new Font(Font.FontFamily.HELVETICA,13.0f,Font.NORMAL,new BaseColor(57,57,57));
        Font tableHeading=new Font(Font.FontFamily.HELVETICA,14.0f,Font.BOLD,new BaseColor(57,57,57));
        Font tableHeadingRed=new Font(Font.FontFamily.HELVETICA,14.0f,Font.BOLD,new BaseColor(223, 24, 55));
        Font tableHeadingGreen=new Font(Font.FontFamily.HELVETICA,14.0f,Font.BOLD,new BaseColor(15, 129, 77));
        Font dcFont=new Font(Font.FontFamily.HELVETICA,10.0f,Font.BOLD,new BaseColor(57,57,57));

        BaseColor lightGreen = new BaseColor(15, 129, 77,30);
        BaseColor lightRed = new BaseColor(223, 24, 55,30);
        BaseColor darkGreen = new BaseColor(15, 129, 77,45);
        BaseColor darkRed = new BaseColor(223, 24, 55,45);
        BaseColor gray = new BaseColor(0, 0, 0,15);

        Paragraph paragraphHeading=new Paragraph("Monthly Expenses",fontHeading);
        Paragraph paragraphSubHeading=new Paragraph("For the month of "+table_casual_name,fontSubHeading);
        Paragraph developerCredit=new Paragraph("Developed by Mr. Parijat Adhikary",dcFont);

        paragraphHeading.setAlignment(Element.ALIGN_CENTER);
        paragraphSubHeading.setAlignment(Element.ALIGN_CENTER);
        developerCredit.setAlignment(Element.ALIGN_RIGHT);

        float[] pointColumnWidths = {7F, 14F, 38F,13F, 13F, 15F};
        float cellPaddingTop=4,cellPaddingSide=8,cellPaddingBot=7;
        PdfPTable pdfPTable = new PdfPTable(6);
        pdfPTable.setWidths(pointColumnWidths);
        pdfPTable.setWidthPercentage(100);
        String[] data={"Sl No.","Date","Description","Credit(+)","Debit(-)","Balance"};
        for(int i=0;i<6;i++){
            PdfPCell pdfPCell;
            if(i==3)
                pdfPCell = new PdfPCell(new Paragraph(data[i],tableHeadingGreen));
            else if (i==4)
                pdfPCell = new PdfPCell(new Paragraph(data[i],tableHeadingRed));
            else
                pdfPCell = new PdfPCell(new Paragraph(data[i],tableHeading));
            pdfPCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPCell.setPaddingTop(cellPaddingTop+3);
            pdfPCell.setPaddingBottom(cellPaddingBot+3);
            pdfPCell.setBorderColor(gray);
            pdfPTable.addCell(pdfPCell);
        }

        ArrayList<NodeInfo> testDataList = DB.getAllTransactions(table_unique_name);
        int totalTransactions=testDataList.size();
        for(int j=0;j<=totalTransactions;j++){
            ArrayList<PdfPCell> pdfPCellList=new ArrayList<>();
            if(j<totalTransactions){
                NodeInfo nodeInfo=testDataList.get(j);
                if(j==0){
                    pdfPCellList.add(new PdfPCell(new Paragraph("",tableText)));
                    pdfPCellList.add(new PdfPCell(new Paragraph("",tableText)));
                }
                else {
                    pdfPCellList.add(new PdfPCell(new Paragraph(""+(nodeInfo.getT_id()-1),tableText)));
                    pdfPCellList.add(new PdfPCell(new Paragraph(""+nodeInfo.getT_date().replace("-20","-"),tableText)));
                }
                pdfPCellList.add(new PdfPCell(new Paragraph(""+nodeInfo.getT_title(),tableText)));
                if(nodeInfo.getT_type().equals(MainActivity.GAVE_MONEY)){
                    pdfPCellList.add(new PdfPCell(new Paragraph(" ",tableText)));
                    pdfPCellList.add(new PdfPCell(new Paragraph(formatter.format(nodeInfo.getT_amount()).replace("-",""),tableText)));
                }
                else{
                    pdfPCellList.add(new PdfPCell(new Paragraph(""+formatter.format(nodeInfo.getT_amount()),tableText)));
                    pdfPCellList.add(new PdfPCell(new Paragraph(" ",tableText)));
                }
                pdfPCellList.add(new PdfPCell(new Paragraph(""+formatter.format(nodeInfo.getBalance()),tableText)));
            }
            else {
                pdfPCellList.add(new PdfPCell(new Paragraph(" ",tableText)));
                pdfPCellList.add(new PdfPCell(new Paragraph(" ",tableText)));
                pdfPCellList.add(new PdfPCell(new Paragraph("Closing Balance",tableText)));
                int previousBalance=testDataList.get(j-1).getBalance();
                if(previousBalance<=0){
                    pdfPCellList.add(new PdfPCell(new Paragraph(""+formatter.format(previousBalance).replace("-",""),tableText)));
                    pdfPCellList.add(new PdfPCell(new Paragraph(" ",tableText)));
                }
                else {
                    pdfPCellList.add(new PdfPCell(new Paragraph(" ",tableText)));
                    pdfPCellList.add(new PdfPCell(new Paragraph(""+formatter.format(previousBalance),tableText)));
                }
                pdfPCellList.add(new PdfPCell(new Paragraph("0",tableText)));
            }
            for(int i=0;i<6;i++){
                pdfPCellList.get(i).setVerticalAlignment(Element.ALIGN_MIDDLE);
                if(i<2)
                    pdfPCellList.get(i).setHorizontalAlignment(Element.ALIGN_CENTER);
                else if (i==2 && j!=0 && j!=totalTransactions)
                    pdfPCellList.get(i).setHorizontalAlignment(Element.ALIGN_LEFT);
                else
                    pdfPCellList.get(i).setHorizontalAlignment(Element.ALIGN_RIGHT);
                pdfPCellList.get(i).setPaddingLeft(cellPaddingSide);
                pdfPCellList.get(i).setPaddingRight(cellPaddingSide);
                pdfPCellList.get(i).setPaddingTop(cellPaddingTop);
                pdfPCellList.get(i).setPaddingBottom(cellPaddingBot);
                pdfPCellList.get(i).setBorderColor(gray);
                if(j%2==1&&i<3)
                    pdfPCellList.get(i).setBackgroundColor(gray);
                else if (i==3){
                    if(j%2==1)
                        pdfPCellList.get(i).setBackgroundColor(darkGreen);
                    else
                        pdfPCellList.get(i).setBackgroundColor(lightGreen);
                }
                else if (i==4){
                    if(j%2==1)
                        pdfPCellList.get(i).setBackgroundColor(darkRed);
                    else
                        pdfPCellList.get(i).setBackgroundColor(lightRed);
                }
                else if(i==5) {
                    if(j<totalTransactions && testDataList.get(j).getBalance()<0){
                        if(j%2==1)
                            pdfPCellList.get(i).setBackgroundColor(darkRed);
                        else
                            pdfPCellList.get(i).setBackgroundColor(lightRed);
                    }
                    else{
                        if(j%2==1)
                            pdfPCellList.get(i).setBackgroundColor(darkGreen);
                        else
                            pdfPCellList.get(i).setBackgroundColor(lightGreen);
                    }
                }
                pdfPTable.addCell(pdfPCellList.get(i));
            }
        }
        document.add(paragraphHeading);
        document.add(paragraphSubHeading);
        document.add(new Paragraph(" "));
        document.add(pdfPTable);
        document.add(developerCredit);
        document.close();
    }

    public static void refreshRecyclerView(boolean goToLast){
        ArrayList<MasterTableNodeInfo> allTables = DB.getAllTables();
        totalTable=allTables.size();
        if(goToLast)
            showTableNo=totalTable;
        if(totalTable>0){
            dataList=DB.getAllTransactions(allTables.get(showTableNo-1).getTable_unique_name());
        }
        if(showTableNo<0||totalTable==0)
            textViewMonthYear.setText("No Data");
        else {
            textViewMonthYear.setText(allTables.get(showTableNo - 1).getTable_casual_name());
            if (!currentMonthUID.equals(allTables.get(showTableNo - 1).getTable_unique_name())) {
                textViewCurrentMonthStatus.setVisibility(View.VISIBLE);
                textViewAvailableBalance.setVisibility(View.GONE);
            }
            else {
                textViewCurrentMonthStatus.setVisibility(View.GONE);
                textViewAvailableBalance.setVisibility(View.VISIBLE);
            }
        }
        Collections.reverse(dataList);
        if (!dataList.isEmpty()) {
            int availableBalance = dataList.get(0).getBalance();
            textViewAvailableBalance.setText("Available Balance: " + formatter.format(availableBalance));
            if (availableBalance < 0) {
                textViewAvailableBalance.setTextColor(Color.parseColor("#df1837"));
                textViewAvailableBalance.setBackgroundColor(Color.parseColor("#1DDF1837"));
            } else {
                textViewAvailableBalance.setTextColor(Color.parseColor("#0f814d"));
                textViewAvailableBalance.setBackgroundColor(Color.parseColor("#1D0f814d"));
            }
        }
        else
            textViewAvailableBalance.setVisibility(View.GONE);
        recyclerViewMain.setAdapter(new RecyclerViewDataAdapter(recyclerViewMain.getContext(), dataList,totalTable>0?allTables.get(showTableNo-1).getTable_unique_name():null));
    }

    public static String dateToTableName(String date) {
        String tableName="";
        switch (Integer.parseInt(date.substring(3,5))){
            case 1:tableName=tableName+"January";
                break;
            case 2:tableName=tableName+"February";
                break;
            case 3:tableName=tableName+"March";
                break;
            case 4:tableName=tableName+"April";
                break;
            case 5:tableName=tableName+"May";
                break;
            case 6:tableName=tableName+"June";
                break;
            case 7:tableName=tableName+"July";
                break;
            case 8:tableName=tableName+"August";
                break;
            case 9:tableName=tableName+"September";
                break;
            case 10:tableName=tableName+"October";
                break;
            case 11:tableName=tableName+"November";
                break;
            case 12:tableName=tableName+"December";
                break;
        }
        tableName=tableName+" "+date.substring(6);
        return tableName;
    }
}