package com.example.monthlyexpenses;

import static com.example.monthlyexpenses.MainActivity.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create Table "+MASTER_TABLE+"(table_id INTEGER primary key autoincrement, table_unique_name TEXT, table_casual_name TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop Table if exists "+MASTER_TABLE);
        onCreate(sqLiteDatabase);
    }
    public Boolean createNewTable(String table_unique_name, String table_casual_name)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("table_unique_name", table_unique_name);
        contentValues.put("table_casual_name", table_casual_name);
        long result=DB.insert(MASTER_TABLE, null, contentValues);
        if(result!=-1){
            DB.execSQL("create Table "+table_unique_name+"(t_id INTEGER primary key autoincrement, t_title TEXT, t_date TEXT, t_type TEXT, t_amount Integer, balance Integer)");
            return true;
        }
        else
            return false;
    }

    public Boolean addTransaction(String t_title,String t_date, String t_type ,int t_amount)
    {
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("t_title", t_title);
        contentValues.put("t_date", t_date);
        contentValues.put("t_type", t_type);
        contentValues.put("t_amount", t_amount);
        String table_casual_name=dateToTableName(t_date);
        String table_unique_name=table_casual_name.replace(" ","");
        boolean flag=true;
        ArrayList<MasterTableNodeInfo> allTables=getAllTables();
        for(MasterTableNodeInfo node:allTables){
            if(node.getTable_unique_name().equals(table_unique_name))
                flag=false;
        }
        long result;
        if(flag){
            createNewTable(table_unique_name,table_casual_name);
            int preBalance=0;
            if(allTables.size()>0) {
                ArrayList<NodeInfo> allTransactions=getAllTransactions(allTables.get(allTables.size()-1).getTable_unique_name());
                preBalance = allTransactions.get(allTransactions.size()-1).getBalance();
            }
            ContentValues contentValueNew = new ContentValues();
            contentValueNew.put("t_title", "Opening Balance");
            contentValueNew.put("t_date", "01"+t_date.substring(2));
            if(preBalance<0)
                contentValueNew.put("t_type", GAVE_MONEY);
            else
                contentValueNew.put("t_type", GOT_MONEY);
            contentValueNew.put("t_amount", preBalance);
            contentValueNew.put("balance", preBalance);
            DB.insert(table_unique_name, null, contentValueNew);
            contentValues.put("balance", preBalance+t_amount);
        }
        else {
            ArrayList<NodeInfo> allTransactions=getAllTransactions(table_unique_name);
            int preBalance = allTransactions.get(allTransactions.size()-1).getBalance();
            contentValues.put("balance", preBalance+t_amount);
        }
        result=DB.insert(table_unique_name, null, contentValues);
        if(result!=-1)
            return true;
        else
            return false;
    }
    public Boolean deleteData (String table_unique_name, int key){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from "+table_unique_name+" where t_id = "+key, null);
        if (cursor.getCount() > 0) {
            long result = DB.delete(table_unique_name, "t_id="+key, null);
            if (result != -1)
                return true;
        }
        return false;
    }
    public Boolean updateTransaction(String table_unique_name, int key,String newTransactionTitle, int newAmount, int transactionDifference, int updateField){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("t_title", newTransactionTitle);

        if (updateField>0){
            Cursor cursor = DB.rawQuery("Select * from "+table_unique_name, null);
            ContentValues tempContentValues = new ContentValues();
            while(cursor.moveToNext()){
                int t_id=Integer.parseInt(cursor.getString(0));
                if (t_id>=key){
                    int balance=Integer.parseInt(cursor.getString(5));
                    int t_amount=Integer.parseInt(cursor.getString(4));

                    if(t_id==key) {
                        tempContentValues.put("balance",balance+transactionDifference);
                        tempContentValues.put("t_amount", newAmount);
                    }
                    else {
                        tempContentValues.put("balance",balance+transactionDifference);
                        tempContentValues.put("t_amount", t_amount);
                    }
                    Cursor innerCursor = DB.rawQuery("Select * from "+table_unique_name+" where t_id = "+t_id, null);
                    if (innerCursor.getCount() > 0) {
                        long result = DB.update(table_unique_name, tempContentValues, "t_id="+t_id, null);
                    }
                }
            }
        }

        Cursor cursor = DB.rawQuery("Select * from "+table_unique_name+" where t_id = "+key, null);
        if (cursor.getCount() > 0) {
            long result = DB.update(table_unique_name, contentValues, "t_id="+key, null);
            if (result != -1)
                return true;
        }
        return false;
    }
    public ArrayList<MasterTableNodeInfo> getAllTables ()
    {
        ArrayList<MasterTableNodeInfo> dataList=new ArrayList<MasterTableNodeInfo>();
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from "+MASTER_TABLE, null);
        while(cursor.moveToNext()){
            String table_unique_name=cursor.getString(1);
            String table_casual_name=cursor.getString(2);
            int table_id=Integer.parseInt(cursor.getString(0));
            dataList.add(new MasterTableNodeInfo(table_id,table_unique_name,table_casual_name));
        }
        return dataList;
    }
    public ArrayList<NodeInfo> getAllTransactions (String table_unique_name)
    {
        ArrayList<NodeInfo> dataList=new ArrayList<NodeInfo>();
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from "+table_unique_name, null);
        while(cursor.moveToNext()){
            int t_id=Integer.parseInt(cursor.getString(0));
            String t_title=cursor.getString(1);
            String t_date=cursor.getString(2);
            String t_type=cursor.getString(3);
            int t_amount=Integer.parseInt(cursor.getString(4));
            int balance=Integer.parseInt(cursor.getString(5));
            dataList.add(new NodeInfo(t_id,t_title,t_date,t_type,t_amount,balance));
        }
        return dataList;
    }
}
