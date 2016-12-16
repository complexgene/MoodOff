package com.moodoff.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.moodoff.R;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

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

        }
        catch(Exception ee){
            Log.e("DBInternal",ee.getMessage());
        }

    }

    public void createTable(String tableName,HashMap<String,String> columnNameAndDataType){
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        String query = "CREATE TABLE IF NOT EXISTS"+tableName+"(";
        for(String columns : columnNameAndDataType.keySet()) {
            query+=columns+" "+columnNameAndDataType.get(columns)+",";
        }
        // To avoid the last COMMA
        query=query.substring(0,query.length()-1)+");";
        Log.e("DBInternal_CREATE_QUERY",query);
        mydatabase.execSQL(query);
        mydatabase.close();
    }

    public boolean todoWorkEntry(String API){
        try {
            mydatabase = getApplicationContext().openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
            mydatabase.execSQL("insert into worktodo values('" + API + "');");
            mydatabase.close();
            return true;
        }
        catch(Exception ee){
            Log.e("DBInternal_Err",ee.getMessage());
            return false;
        }
    }

    public void toDoWorkExit(){
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("Select * from worktodo", null);
        resultSet.moveToFirst();
        while (!resultSet.isAfterLast()) {
            final int id = resultSet.getInt(0);
            final String apiToFire = resultSet.getString(1);
            new Handler().postDelayed(new Runnable() {
                HttpURLConnection urlConnection = null;
                InputStreamReader isr = null;
                @Override
                public void run() {
                    try {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    URL url = new URL(apiToFire);
                                    Log.e("DBInternal_API_TO_FIRE", url.toString());
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                    if(urlConnection.getResponseCode()==200){
                                        Log.e("DBInternal_API_SUCCESS","Successful");
                                        mydatabase.execSQL("DELETE FROM worktodo WHERE ID="+id);
                                    }
                                }
                                catch(Exception ee){
                                    Log.e("DBInternal_Err","Some issues.."+ee.getMessage());
                                }
                                finally {
                                    try {
                                        isr.close();
                                    } catch (Exception ee) {
                                        Log.e("DBInternal_Err", "InputStreamReader couldn't be closed");
                                    }
                                    urlConnection.disconnect();
                                }
                            }
                        }).start();
                        toDoWorkExit();
                    } catch (Exception ee) {
                        Log.e("DBInternal_Err","Some issues.."+ee.getMessage());
                    }
                }
            },5000);
        }
        mydatabase.close();
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
    // List all the tables in DB
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
            Log.e("DBInternal_Err",ee.getMessage());
        }
    }
    // Show the data in the corresponding table that has been written in the textbox
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
            Log.e("DBInternal_Err",ee.getMessage());
        }
    }
    // Delete all the data in the corresponding table that has been written in the textbox
    public void clearData(View v){
        try {
            mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
            mydatabase.execSQL("DELETE FROM "+tableName.getText());
            mydatabase.close();
            tv.setText("Deleted all data from table "+tableName.getText());
        }catch (Exception ee){
            Log.e("DBInternal_Err",ee.getMessage());
        }
    }
    // Check if a tble is existing in the database
    public boolean checkIfATableExists(String tableName){
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            Cursor allTables = mydatabase.rawQuery("SELECT name from sqlite_master WHERE type='table' and name='"+tableName+"'", null);
            if(allTables.getCount()==1) {
                Log.e("DBInternal_chkTbl",tableName+" exists");
                mydatabase.close();
                return true;
            }
            else{
                Log.e("DBInternal_chkTbl",tableName+" doesn't exist");
                mydatabase.close();
                return false;
            }
        }
        catch(Exception ee){
            Log.e("DBInternal_chkIfTbl_Er",ee.getMessage());
        }
        mydatabase.close();
        return false;
    }
}
