package com.moodoff.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.util.Log;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by snaskar on 12/15/2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    SQLiteDatabase mydatabaseReadable, myDatabaseWritable;

    public DBHelper(Context context) {
        super(context, "moodoff" , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }
    public void createTable(String tableName,HashMap<String,String> columnNameAndDataType){
        SQLiteDatabase mydatabase = getWritableDatabase();
        String query = "CREATE TABLE IF NOT EXISTS "+tableName+"(";
        for(String columns : columnNameAndDataType.keySet()) {
            query=query+(columns+" "+columnNameAndDataType.get(columns)+",");
        }
        // To avoid the last COMMA
        Log.e("RegistrationActi",query+" "+columnNameAndDataType.size());
        query=query.substring(0,query.length()-1)+");";
        Log.e("DBInternal_CREATE_QUERY",query);
        mydatabase.execSQL(query);
        mydatabase.close();
    }

    public boolean todoWorkEntry(String API){
        try {
            mydatabaseReadable = this.getWritableDatabase();
            mydatabaseReadable.execSQL("insert into worktodo(api) values('" + API + "');");
            mydatabaseReadable.close();
            return true;
        }
        catch(Exception ee){
            Log.e("DBInternal_Err",ee.getMessage());
            return false;
        }
    }
    public void toDoWorkExit(){
        Log.e("DBHelper","Here i am");
        mydatabaseReadable = getReadableDatabase();
        Cursor resultSet = mydatabaseReadable.rawQuery("Select * from worktodo", null);
        resultSet.moveToFirst();
        while (!resultSet.isAfterLast()) {
            final int id = resultSet.getInt(0);
            final String apiToFire = resultSet.getString(1);
            Log.e("DBHelper_data",id+apiToFire);
            new Handler().postDelayed(new Runnable() {
                HttpURLConnection urlConnection = null;
                @Override
                public void run() {
                    try {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    URL url = new URL(apiToFire);
                                    Log.e("DBHelper_API_TO_FIRE", url.toString());
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                    urlConnection.setDoOutput(true);
                                    if(urlConnection.getResponseCode()==200){
                                        Log.e("DBHelper_API_SUCCESS","Successful");
                                        SQLiteDatabase dbWrite = getWritableDatabase();
                                        String queryToDelete = "DELETE FROM worktodo WHERE ID="+id;
                                        Log.e("DBHelper_DELETE",queryToDelete);
                                        dbWrite.execSQL(queryToDelete);
                                    }
                                }
                                catch(Exception ee){
                                    Log.e("DBHelper_Err","Some issues.."+ee.getMessage());
                                }
                                finally {
                                    if(urlConnection!=null)
                                        urlConnection.disconnect();
                                }
                            }
                        }).start();
                        toDoWorkExit();
                    } catch (Exception ee) {
                        Log.e("DBhelper_Err","Some issues.."+ee.getMessage());
                    }
                }
            },5000);
            resultSet.moveToNext();
        }
        mydatabaseReadable.close();
    }
}
