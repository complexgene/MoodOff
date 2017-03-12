package com.moodoff.dao;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.moodoff.helper.Messenger;
import com.moodoff.service.StartedTodoService;
import com.moodoff.ui.ContactList;

import java.util.LinkedHashMap;

import static com.moodoff.helper.ContactsManager.allReadContacts;

/**
 * Created by Santanu on 3/11/2017.
 */

public class StartedTodoDaoImpl extends SQLiteOpenHelper implements StartedTodoDaoInterface {
    private Context context;
    public StartedTodoDaoImpl(Context context) {
        super(context, "moodoff" , null, 1);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {}
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    public void dropAllInternalTables(){
        SQLiteDatabase myDatabaseWritable = getWritableDatabase();
        myDatabaseWritable.execSQL("drop table if exists allcontacts");
        myDatabaseWritable.execSQL("drop table if exists worktodo");
        myDatabaseWritable.execSQL("drop table if exists profiles");
        myDatabaseWritable.execSQL("drop table if exists allcontacts");
        myDatabaseWritable.execSQL("drop table if exists rnotifications");
        Log.e("DBHelper_DELTab","Deleted all tables");
    }
    public void createTable(String tableName, final LinkedHashMap<String,String> columnNameAndDataType){
        SQLiteDatabase mydatabase = getWritableDatabase();
        if(tableName.equals("allcontacts")){
            //SQLiteDatabase dbW = getWritableDatabase();
            mydatabase.execSQL("drop table if exists allcontacts;");
            Log.e("DBHelper_allcntct_TBL","For allcontacts table we will be populating data here itself..");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        allReadContacts = ContactList.getContactNames(context.getContentResolver());
                        getOrStoreContactsTableData(1, allReadContacts);
                    }catch (Exception ee){
                        StartedTodoService startedTodoService = new StartedTodoService(getContext());
                        startedTodoService.writeProblemTrace("Got the error as :"+ee.getMessage());
                        Activity act = (Activity)context;
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Messenger.print(context," Error Occured!! Moodoff team will contact you..");
                            }
                        });
                    }
                }
            }).start();
        }
        else{
            String query = "CREATE TABLE IF NOT EXISTS "+tableName+"(";
            for(String columns : columnNameAndDataType.keySet()) {
                query=query+(columns+" "+columnNameAndDataType.get(columns)+",");
            }
            // To avoid the last COMMA
            query=query.substring(0,query.length()-1)+");";
            Log.e("DBHelper_createQuery",query+" "+columnNameAndDataType.size());
            mydatabase.execSQL(query);
        }
    }
    public LinkedHashMap<String,String> getOrStoreContactsTableData(int status, LinkedHashMap<String,String> allContacts){
        SQLiteDatabase mydatabaseR = getReadableDatabase(),mydatabaseW = getWritableDatabase();
        try {
            // status = 0 is for READ and RETURN as it means TABLE ALREADY EXISTS
            if(status == 0){
                //READ and RETURN data
                Cursor resultSet = mydatabaseR.rawQuery("Select * from allcontacts order by name", null);
                resultSet.moveToFirst();
                Log.e("Start_TBLDetect",resultSet.getCount()+" no of rows..");
                while (!resultSet.isAfterLast()) {
                    String phone_no = resultSet.getString(0);
                    String name = resultSet.getString(1);
                    allContacts.put(phone_no,name);
                    resultSet.moveToNext();
                }
            }
            // First time contact table create or REFRESH done.
            else{
                String createQuery = "CREATE TABLE IF NOT EXISTS allcontacts(phone_no VARCHAR,name VARCHAR, status INTEGER);";
                mydatabaseW.execSQL(createQuery);
                Log.e("DBHelper_TBLCRT",createQuery+"\nallcontacts table created..");
                String insertQuery = "";
                for(String eachContact:allContacts.keySet()){
                    mydatabaseW = getWritableDatabase();
                    insertQuery = "INSERT INTO allcontacts values('"+eachContact+"','"+allContacts.get(eachContact).replaceAll("'","''")+"',0);";
                    Log.e("DBHelper_INSRT",insertQuery);
                    mydatabaseW.execSQL(insertQuery);
                }
            }
        }catch (Exception ee){
            Log.e("DBHelper_Err",ee.getMessage());
            ee.fillInStackTrace();
        }
        return allContacts;
    }
}
