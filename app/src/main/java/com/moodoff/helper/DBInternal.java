package com.moodoff.helper;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.moodoff.ContactList;
import com.moodoff.R;

import android.database.sqlite.*;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by snaskar on 10/15/2016.
 */

public class DBInternal extends AppCompatActivity{
    SQLiteDatabase mydatabase;
    EditText tableName;
    TextView tv = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbinternal);
        try {

            tv = (TextView) findViewById(R.id.alldata);
            tableName = (EditText) findViewById(R.id.datatostore);

            mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
            mydatabase.execSQL("CREATE TABLE IF NOT EXISTS notifications1(from_user_id VARCHAR,filename VARCHAR, type VARCHAR);");
            mydatabase.close();
        }
        catch(Exception ee){
            Log.e("DBInternal",ee.getMessage());
        }

    }
    public void runRawQuery(View v){
        String query = ((TextView)findViewById(R.id.rawquery)).getText().toString();
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        String DDLorDML = query.split(" ")[0];
        if(DDLorDML.equals("CREATE") || DDLorDML.equals("DROP") || DDLorDML.equals("RENAME")){
            mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
            mydatabase.execSQL(query);
            mydatabase.close();
        }
        else {
            if (DDLorDML.equals("DELETE") || DDLorDML.equals("INSERT")) {
                mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
                mydatabase.execSQL(query);
                mydatabase.close();
            } else {
                try {
                    mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
                    Cursor resultSet = mydatabase.rawQuery(query, null);
                    resultSet.moveToFirst();
                    int coulumnCount = resultSet.getColumnCount();
                    StringBuilder parsingData = new StringBuilder("");
                    parsingData.append("Result : " + tableName.getText() + "\n-----------------------------------------------------------\n");
                    int i = 0;
                    while (!resultSet.isAfterLast()) {
                        String eachRow = new String("");
                        for (int j = 0; j < coulumnCount; j++) {
                            eachRow += resultSet.getString(j) + "     ";
                        }
                        parsingData.append(eachRow + "\n");
                        resultSet.moveToNext();
                    }
                    tv.setText(parsingData);
                    mydatabase.close();
                } catch (Exception ee) {
                    Log.e("DBInternal", ee.getMessage());
                }
            }
        }

    }

    public void storeData(View v){
        try {
            mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
            Cursor allTables = mydatabase.rawQuery("SELECT name from sqlite_master WHERE type='table' and name!='android_metadata'", null);
            allTables.moveToFirst();
            StringBuilder allTablesHere = new StringBuilder();
            while (!allTables.isAfterLast()) {
                Log.e("tab",allTables.getString(0));
                allTablesHere.append(allTables.getString(0) + "\n");
                allTables.moveToNext();
            }
            tv.setText("All Tables in DB:\n" + allTablesHere);
            mydatabase.close();
        }catch (Exception ee){
            Log.e("DBInternal",ee.getMessage());
        }
    }



    public void showData(View v){
        try {
            mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
            Cursor resultSet = mydatabase.rawQuery("Select * from " + tableName.getText(), null);
            resultSet.moveToFirst();
            int coulumnCount = resultSet.getColumnCount();
            String[][] allData = new String[resultSet.getCount()][coulumnCount];
            StringBuilder parsingData = new StringBuilder("");
            parsingData.append("All Data in table : "+tableName.getText()+"\n-----------------------------------------------------------\n");
            int i = 0;
            while (!resultSet.isAfterLast()) {
                String eachRow = new String("");
                for (int j = 0; j < coulumnCount; j++) {
                    eachRow += resultSet.getString(j)+"     ";
                }
                parsingData.append(eachRow + "\n");
                resultSet.moveToNext();
            }
            tv.setText(parsingData);
            mydatabase.close();
        }catch (Exception ee){
            Log.e("DBInternal",ee.getMessage());
        }
    }

    public void clearData(View v){
        try {
            mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
            mydatabase.execSQL("DELETE FROM "+tableName.getText());
            mydatabase.close();
            tv.setText("Deleted all data from table "+tableName.getText());
        }catch (Exception ee){
            Log.e("DBInternal",ee.getMessage());
        }
    }

    public void checkAndPopulateContactsTable(ArrayList<String> allContacts){
        try {
            mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);

                String createQuery = "CREATE TABLE IF NOT EXISTS allcontacts(user_id VARCHAR,phone_no VARCHAR);";
                String insertQuery = "";
                mydatabase.execSQL(createQuery);
                ContactList contactList = new ContactList();
                tv.setText(allContacts.get(0));
                for(String eachContact:allContacts){
                    Log.e("Connnn",eachContact);
                    insertQuery = "INSERT INTO allcontacts values('"+eachContact.split(" ")[0]+"','"+eachContact.split(" ")[1]+"');";
                    mydatabase.execSQL(createQuery);
                }
                mydatabase.close();
                tv.setText("Created contacts table");


        }catch (Exception ee){
            Log.e("DBInternal1",ee.getMessage());
            ee.fillInStackTrace();
        }
    }

    public void checkAndPopulateNotificationsTable(ArrayList<String> allContacts){
        try {
            mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);

            String createQuery = "CREATE TABLE IF NOT EXISTS allnotifications(from_user_id VARCHAR,to_user_id VARCHAR,file_name VARCHAR,type CHAR,ts VARCHAR);";
            String insertQuery = "";
            mydatabase.execSQL(createQuery);
            ContactList contactList = new ContactList();
            tv.setText(allContacts.get(0));
            for(String eachContact:allContacts){
                Log.e("Connnn",eachContact);
                insertQuery = "INSERT INTO allcontacts values('"+eachContact.split(" ")[0]+"','"+eachContact.split(" ")[1]+"');";
                mydatabase.execSQL(createQuery);
            }
            mydatabase.close();
            tv.setText("Created contacts table");


        }catch (Exception ee){
            Log.e("DBInternal1",ee.getMessage());
            ee.fillInStackTrace();
        }
    }
}
