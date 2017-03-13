package com.moodoff.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.util.Log;

import com.moodoff.ui.ContactList;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.moodoff.helper.ContactsManager.allReadContacts;

/**
 * Created by snaskar on 12/15/2016.
 */

public class DBHelper extends SQLiteOpenHelper {

    SQLiteDatabase mydatabaseReadable, myDatabaseWritable;
    Context context;

    public DBHelper(Context context) {
        super(context, "moodoff" , null, 1);
        this.context = context;
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


    public boolean todoWorkEntry(String API){
        try {
            SQLiteDatabase mydatabaseW = getWritableDatabase();
            mydatabaseW.execSQL("insert into worktodo(api) values('" + API + "');");
            return true;
        }
        catch(Exception ee){
            Log.e("DBInternal_Err",ee.getMessage());
            return false;
        }
    }
    public void toDoWorkExit(){
        Log.e("DBHelper","Here i am");
        SQLiteDatabase mydatabaseReadable = getReadableDatabase();
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
    }
    // Store and process APIs
    public boolean setAPIIntoInternalDBToProcessLater(String API){
        try{
            myDatabaseWritable = getWritableDatabase();
            String queryToFire = "insert into worktodo(api) values('"+API+"');";
            myDatabaseWritable.execSQL(queryToFire);
            Log.e("DBHelper_worktodo","Successfully inserted the API: "+API);
            return true;
        }
        catch (Exception ee){
            return false;
        }

    }
    public boolean getAPIFromInternalDBAndProcess(){
        try {
            mydatabaseReadable = getReadableDatabase();
            Cursor resultSet = mydatabaseReadable.rawQuery("Select * from worktodo", null);
            resultSet.moveToFirst();
            int idOfAPIToProcess = resultSet.getInt(0);
            String APIToProcess = resultSet.getString(1);
            myDatabaseWritable = getWritableDatabase();
            //if(ServerManager returns true after firing the API)
            //    myDatabaseWritable.execSQL("delete from rnotifications where id = " + idOfAPIToProcess);
            Log.e("DBHelper_worktodo","API Done:"+APIToProcess);
            return true;
        }
        catch (Exception ee){
            return false;
        }
    }
    // Store and Process APIs part ENDS
    public HashMap<String,HashMap<String,String>> readAllProfilesDataFromInternalDB() {
        HashMap<String,HashMap<String,String>> allProfilesData = new HashMap<>();
        mydatabaseReadable = getReadableDatabase();
        Cursor resultSet = mydatabaseReadable.rawQuery("Select * from all_profiles", null);
        resultSet.moveToFirst();
        while (!resultSet.isAfterLast()) {
            HashMap<String,String> profileDataForEach = new HashMap<>();
            final String phno = resultSet.getString(0);
            final String name = resultSet.getString(1);
            final String text_status = resultSet.getString(2);
            final String audio_status = resultSet.getString(3);
            final String text_status_likes = resultSet.getString(4);
            final String audio_status_likes = resultSet.getString(5);
            profileDataForEach.put("name",name);
            profileDataForEach.put("text_status",text_status);
            profileDataForEach.put("audio_Status",audio_status);
            profileDataForEach.put("text_status_likes",text_status_likes);
            profileDataForEach.put("audio_status_likes",audio_status_likes);
            Log.e("DBHelper_ProfileDB",profileDataForEach.get(name));
            allProfilesData.put(phno,profileDataForEach);
        }
        return allProfilesData;
    }
    public ArrayList<String> readNotificationsFromInternalDB() {
        Log.e("DBHelper", "Came to read the notifications from the Internal DB");
        ArrayList<String> allNotifications = new ArrayList<>();
        mydatabaseReadable = getReadableDatabase();
        Cursor resultSet = mydatabaseReadable.rawQuery("Select * from rnotifications", null);
        resultSet.moveToFirst();
        int countLoveType = 0;
        while (!resultSet.isAfterLast()) {
            final String from_user = resultSet.getString(0);
            final String to_user = resultSet.getString(1);
            final String fileName = resultSet.getString(2);
            final String type = resultSet.getString(3);
            if(type.equals("5"))countLoveType++;
            final int send_done = resultSet.getInt(4);
            final String timestamp = resultSet.getString(5);
            String data = from_user+" "+to_user+" "+timestamp+" "+type+" "+fileName;
            allNotifications.add(data);
            //Log.e("DBHelper_RDNot",data);
            resultSet.moveToNext();
        }
        AppData.lovedDedicateOldCount = countLoveType;
        Log.e("DBHelper", "Reading of the notifications from the Internal DB -> DONE");
        return allNotifications;
    }
    public void writeNewNotificationsToInternalDB(ArrayList<String> newNotifications){
        Log.e("DBHelper", "Came to write the newly arrived notifications to internal DB..");
        myDatabaseWritable = getWritableDatabase();
        ArrayList<String> loveUpdateMarkerQueries = new ArrayList<>();
        for(String eachNotification : newNotifications){
            Log.e("DBHelper",eachNotification);
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

            String queryToFire = "insert into rnotifications values('"+fromUser+"','"+toUser+"','"+fileName+"','"+type+"',0,'"+(date+" "+time)+"');";
                myDatabaseWritable.execSQL(queryToFire);

            Log.e("DBHelper_STATUSTYPE",queryToFire);
        }
        for(String eachUpdateQuery : loveUpdateMarkerQueries){
            myDatabaseWritable.execSQL(eachUpdateQuery);
        }
        Log.e("DBHelper", "Writing of newly arrived notifications to internal DB is DONE..");
    }
    public void deleteAllDataFromNotificationTableFromInternalDB(){
        Log.e("DBHelper", "Came to delete all the notifications from the notifications table..");
        myDatabaseWritable = getWritableDatabase();
        myDatabaseWritable.execSQL("delete from rnotifications");
        Log.e("DBHelper", "Deletion of all the notifications from the notifications table DONE..");
    }

    public void changeStatusOfUsersInContactsTable(ArrayList<String> allUsers){
        Log.e("DBHelper_StatusChngQury","Initiating status change of few users..");
        SQLiteDatabase writer = getWritableDatabase();
        for(String phNo : allUsers){
            String updateQuery = "update allcontacts set status=1 where phone_no='"+phNo+"'";
            Log.e("DBHelper_StatusChngQury",updateQuery);
            writer.execSQL(updateQuery);
        }
        Log.e("DBHelper_StatusChngQury","Status change of few users done..");
    }


}
