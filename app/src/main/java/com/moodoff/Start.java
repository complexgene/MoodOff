package com.moodoff;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.AllNotifications;
import com.moodoff.helper.DBInternal;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.PlaylistSongs;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Start extends AppCompatActivity {
    private String serverURL = HttpGetPostInterface.serverURL;
    private boolean doorClosed1 = true, doorClosed2 = true, letMeReadAllTheMoods = true, contactReadFinished = true;
    private static int readAllPlaylistComplete = 0;
    private HashMap<String,ArrayList<String>> allMoodPlayList = new HashMap<>();
    ProgressBar spinner;
    TextView temp,weather,specialDate,greet;
    SQLiteDatabase mydatabase;
    ArrayList<String> allC = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ActivityCompat.requestPermissions(Start.this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS},
                1);

        StoreRetrieveDataInterface rd = null;

        spinner = (ProgressBar) findViewById(R.id.spinner);
        //specialDate = (TextView) findViewById(R.id.specialDate);
        greet = (TextView) findViewById(R.id.greet);

        spinner.setVisibility(ProgressBar.VISIBLE);

        try {
            rd = new StoreRetrieveDataImpl("UserData.txt");
            if (rd.fileExists()) {

                rd.beginReadTransaction();
                UserDetails.setUserName(rd.getValueFor("user"));
                UserDetails.setPhoneNumber(rd.getValueFor("phoneNo"));
                UserDetails.setEmailId(rd.getValueFor("email"));
                rd.endReadTransaction();

                Calendar c = Calendar.getInstance();
                //specialDate.setText("Today is: " + c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR));
                int hour = c.get(Calendar.HOUR_OF_DAY);
                String greetStr = "- Good Morning -";
                if (hour >= 12 && hour <= 18) greetStr = "- Good Afternoon -";
                if (hour >= 18 && hour <= 23) greetStr = "- Good Evening -";
                greet.setText(greetStr);

                try {
                    final String userMobileNumber = UserDetails.getPhoneNumber();
                    final String serverURL = HttpGetPostInterface.serverURL;

                    new Thread(new Runnable() {
                        HttpURLConnection urlConnection = null;
                        InputStreamReader isr = null;
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(serverURL + "/notifications/" + userMobileNumber);
                                Log.e("Start_Notf_Read","Notitification");
                                urlConnection = (HttpURLConnection) url.openConnection();
                                InputStream is = urlConnection.getInputStream();
                                isr = new InputStreamReader(is);
                                int data = isr.read();
                                final StringBuilder response = new StringBuilder("");
                                while (data != -1) {
                                    response.append((char) data);
                                    data = isr.read();
                                }
                                AllNotifications.allNotifications = ParseNotificationData.getNotification(response.toString());
                                //  AllNotifications.totalNoOfNot = AllNotifications.allNotifications.size();
                                doorClosed1 = false;
                                Log.e("Start_Notif_Read","Notification read complete..");
                            } catch (Exception ee) {
                                Log.e("Start_notification_Read",ee.getMessage());
                            }
                            finally {
                                try {
                                    isr.close();
                                }catch (Exception ee){Log.e("Start_Err","InputStreamReader couldn't be closed");}
                                urlConnection.disconnect();

                            }
                        }
                    }).start();

                    //Contacts Read and store in local DB
                    if(!checkIfATableExists("allcontacts")){
                        Log.e("Start_cntcts","Not present");
                        Toast.makeText(this,"You look just awesome today!!",Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                allC = ContactList.getContactNames(getContentResolver());
                                getOrStoreContactsTableData(1,allC);
                                contactReadFinished=false;
                            }
                        }).start();
                    }
                    else{
                        Toast.makeText(this,"You look just awesome today!!",Toast.LENGTH_SHORT).show();
                        contactReadFinished=false;
                    }

                    final ArrayList<String> allMoods = new ArrayList<String>();
                    //allMoods.add("romantic");
                    //allMoods.add("on_tour");
                    new Thread(new Runnable() {
                        HttpURLConnection urlConnection = null;
                        BufferedReader bufferedReader = null;
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(HttpGetPostInterface.serverSongURL + "allmoods.txt");
                                Log.e("Start_allmoods_url",url.toString());
                                urlConnection = (HttpURLConnection) url.openConnection();
                                InputStream is = urlConnection.getInputStream();
                                InputStreamReader isr = new InputStreamReader(is);
                                bufferedReader = new BufferedReader(isr);
                                String line="";
                                while ((line=bufferedReader.readLine())!=null) {
                                    allMoods.add(line);
                                }
                                letMeReadAllTheMoods = false;
                                Log.e("Start_allmoods_Read","AllMoods read complete.."+allMoods.toString());
                            } catch (Exception ee) {
                                Log.e("Start_notification_Read",ee.getMessage());
                            }
                            finally {
                                try {
                                    bufferedReader.close();
                                }catch (Exception ee){Log.e("Start_allmoodsReadErr","BufferedReader couldn't be closed");}
                                urlConnection.disconnect();

                            }
                        }
                    }).start();
                    while(letMeReadAllTheMoods);
                    int noOfMoods = allMoods.size();
                    //Keeps track if all the playlist file has been read or not.
                    readAllPlaylistComplete = noOfMoods;

                    // Read each playlist file from server to be used later.
                    for (int i = 0; i < noOfMoods; i++) {
                        final String mood = allMoods.get(i);
                        final ArrayList<String> songList = new ArrayList<>();
                        new Thread(new Runnable() {
                            HttpURLConnection urlConnection = null;
                            BufferedReader bufferedReader = null;
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(HttpGetPostInterface.serverSongURL + "playlist_"+ mood +".txt");
                                    Log.e("Start_Playlist_url",url.toString());
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                    InputStream is = urlConnection.getInputStream();
                                    InputStreamReader isr = new InputStreamReader(is);
                                    bufferedReader = new BufferedReader(isr);
                                    String line="";
                                    while ((line=bufferedReader.readLine())!=null) {
                                        songList.add(line);
                                    }
                                    // Populate the variable in the PlayListSongs.java file which is to be accessed by MediaPlayer
                                    PlaylistSongs.allMoodPlayList.put(mood,songList);
                                    // Tracks if all the files have been read or not, once complete its reaches ZERO(0)
                                    readAllPlaylistComplete--;
                                    Log.e("Start_PL_ReadStatus",mood+" read complete");
                                } catch (Exception ee) {
                                    Log.e("Start_Playlist_Read",songList.toString());
                                }
                                finally {
                                    try {
                                        bufferedReader.close();
                                    }catch (Exception ee){Log.e("Start_Err","BufferedReader couldn't be closed");}
                                    urlConnection.disconnect();

                                }
                            }
                        }).start();
                    }

                } catch (Exception ee) {
                    Log.e("Start_Notifications", ee.getMessage());
                }


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Intent mainIntent = new Intent(Start.this, AllTabs.class);
                        while (doorClosed1 || contactReadFinished || readAllPlaylistComplete>0) ;
                        Log.e("Start_AllTabsLaunch","AllTabs will be launched");
                        Start.this.startActivity(mainIntent);
                        Start.this.finish();
                    }
                }, 4000);
            } else {
                Intent ii = new Intent(this, RegistrationActivity.class);
                startActivity(ii);
            }
        } catch (Exception e) {
            Log.e("Start_Error", e.getMessage());
        }
    }
    public boolean checkIfATableExists(String tableName){
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            Cursor allTables = mydatabase.rawQuery("SELECT name from sqlite_master WHERE type='table' and name='"+tableName+"'", null);
            if(allTables.getCount()==1) {
                Log.e("Start_chkTbl",tableName+" exists");
                mydatabase.close();
                return true;
            }
            else{
                Log.e("Start_chkTbl",tableName+" doesn't exist");
                mydatabase.close();
                return false;
            }
        }
        catch(Exception ee){
            Log.e("Start_chkEr",ee.getMessage());
        }
        mydatabase.close();
        return false;
    }
    public ArrayList<String> getOrStoreContactsTableData(int status, ArrayList<String> allContacts){
        ArrayList<String> allContactsPresent = new ArrayList<String>();
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            // status = 0 is for READ and RETURN as it means TABLE ALREADY EXISTS
            if(status == 0){
                //READ and RETURN data
                Cursor resultSet = mydatabase.rawQuery("Select * from allcontacts", null);
                resultSet.moveToFirst();
                while (!resultSet.isAfterLast()) {
                    String eachRow = resultSet.getString(0)+" "+resultSet.getString(1);
                    allContactsPresent.add(eachRow);
                    resultSet.moveToNext();
                }
            }
            // First time conatct table create or REFRESH done.
            else{
                String createQuery = "CREATE TABLE IF NOT EXISTS allcontacts(user_id VARCHAR,phone_no VARCHAR);";
                mydatabase.execSQL(createQuery);
                String deleteQuery = "DELETE FROM allcontacts;";
                mydatabase.execSQL(deleteQuery);
                String insertQuery = "";
                for(String eachContact:allContacts){
                    //Log.e("Start_CntErr",eachContact);
                    insertQuery = "INSERT INTO allcontacts(user_id,phone_no) values('"+eachContact.split("#")[0]+"','"+eachContact.split("#")[1]+"');";
                    //Log.e("Start_CntErr",insertQuery);
                    mydatabase.execSQL(insertQuery);
                }
                return null;
            }
            mydatabase.close();

        }catch (Exception ee){
            Log.e("Start_StrErr",ee.getMessage());
            ee.fillInStackTrace();
        }
        mydatabase.close();
        //allContactsPresent.add("santanu");
        return allContactsPresent;
    }
}
