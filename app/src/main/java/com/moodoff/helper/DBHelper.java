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
import java.util.ArrayList;
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
    public ArrayList<String> readNotificationsFromInternalDB() {
        ArrayList<String> allNotifications = new ArrayList<>();
        mydatabaseReadable = getReadableDatabase();
        Cursor resultSet = mydatabaseReadable.rawQuery("Select * from notifications", null);
        resultSet.moveToFirst();
        while (!resultSet.isAfterLast()) {
            final String from_user = resultSet.getString(0);
            final String to_user = resultSet.getString(1);
            final String fileName = resultSet.getString(2);
            final String type = resultSet.getString(3);
            final int send_done = resultSet.getInt(4);
            final String timestamp = resultSet.getString(5);
            String data = from_user+" "+to_user+" "+timestamp+" "+type+" "+fileName;
            allNotifications.add(data);
            Log.e("DBHelper_RDNot",data);
            resultSet.moveToNext();
        }
        return allNotifications;
    }
    public void writeNewNotificationsToInternalDB(ArrayList<String> newNotifications){
        myDatabaseWritable = getWritableDatabase();
        for(String eachNotification : newNotifications){
            String[] allData = eachNotification.split(" ");
            String fromUser = allData[0];
            String toUser = allData[1];
            String ts = allData[2];
            String timeSplit[] = ts.split("_");
            String date = timeSplit[0];
            String time = timeSplit[1];
            //time = time.substring(0,time.lastIndexOf(":"));
            String type = allData[3];
            String fileName = allData[4];

            String queryToFire = "insert into notifications values('"+fromUser+"','"+toUser+"','"+fileName+"','"+type+"',0,'"+(date+" "+time)+"');";
            myDatabaseWritable.execSQL(queryToFire);

        }
    }
    public void deleteAllDataFromNotificationTableFromInternalDB(){
        myDatabaseWritable = getWritableDatabase();
        myDatabaseWritable.execSQL("delete from notifications");
    }
}
