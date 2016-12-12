package com.moodoff;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.LinkedHashMap;

public class Start extends AppCompatActivity {
    private String serverURL = HttpGetPostInterface.serverURL;
    private boolean doorClosed1 = true, doorClosed2 = true, letMeReadAllTheMoods = true, contactReadFinished = true;
    private static int readAllPlaylistComplete = 0;
    private HashMap<String, ArrayList<String>> allMoodPlayList = new HashMap<>();
    ProgressBar spinner;
    TextView temp, weather, specialDate, greet;
    SQLiteDatabase mydatabase;
    HashMap<String,String> allC = new HashMap<>();

   /* private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ActivityCompat.requestPermissions(Start.this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS},
                1);

       /* if (!isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(),"Sorry! You need Internet Connection",Toast.LENGTH_LONG).show();
            spinner.setVisibility(View.INVISIBLE);

        } else {*/

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

                    //Contacts Read and store in local DB
                    if (!checkIfATableExists("allcontacts")) {
                        Log.e("Start_cntcts", "Not present");
                        Toast.makeText(this, "You look just awesome today!!", Toast.LENGTH_SHORT).show();
                        allC = ContactList.getContactNames(getContentResolver());
                        contactReadFinished = false;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getOrStoreContactsTableData(1, allC);
                            }
                        }).start();
                    } else {
                        Log.e("Start_cntcts", "Present");
                        Toast.makeText(this, "You look awesome today!!", Toast.LENGTH_SHORT).show();
                        allC = getOrStoreContactsTableData(0,allC);
                        contactReadFinished = false;
                    }
                    try {
                        final String userMobileNumber = UserDetails.getPhoneNumber();
                        final String serverURL = HttpGetPostInterface.serverURL;

                        new Thread(new Runnable() {
                            HttpURLConnection urlConnection = null;
                            InputStreamReader isr = null;
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(serverURL+ "/notifications/" + userMobileNumber);
                                    Log.e("Start_Notf_Read", url.toString());
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                    InputStream is = urlConnection.getInputStream();
                                    isr = new InputStreamReader(is);
                                    int data = isr.read();
                                    final StringBuilder response = new StringBuilder("");
                                    while (data != -1) {
                                        response.append((char) data);
                                        data = isr.read();
                                    }
                                    ArrayList<String> allYourNotificationFromServer = ParseNotificationData.getNotification(response.toString());
                                    ArrayList<String> allYourNotification = new ArrayList<String>();
                                    NotificationFragment.totalNumberOfNotifications = allYourNotificationFromServer.size();
                                    while(contactReadFinished);
                                    Log.e("Start_SIZE",allC.size()+"");
                                    for(String eachNotification : allYourNotificationFromServer){
                                        String[] allData = eachNotification.split(" ");
                                        String fromUser = allData[0];
                                        String toUser = allData[1];
                                        String ts = allData[2];
                                        String timeSplit[] = ts.split("_");
                                        String date = timeSplit[0];
                                        String time = timeSplit[1];
                                        time = time.substring(0,time.lastIndexOf(":"));
                                        String type = allData[3];
                                        String songName = allData[4];


                                        if(fromUser.equals(UserDetails.getPhoneNumber())){
                                            String nameOfTo = allC.get(toUser);
                                            if(nameOfTo!=null && nameOfTo.length()>19)
                                                nameOfTo = nameOfTo.substring(0,16)+"...";

                                            if (nameOfTo != null) {
                                                allYourNotification.add("[ "+date+" at "+time+" ]: \nYou -> " + nameOfTo + " " + songName);
                                            } else {
                                                allYourNotification.add("[ "+date+" at "+time+" ]: \nYou -> " + toUser + " " + songName);
                                            }
                                        }
                                        else {
                                            String nameOfFrom = allC.get(fromUser);
                                            if(nameOfFrom!=null && nameOfFrom.length()>19)
                                                nameOfFrom = nameOfFrom.substring(0,16)+"...";
                                            if (nameOfFrom != null) {
                                                allYourNotification.add("[ "+date+" at "+time+" ]: \n" + nameOfFrom + " -> You " + songName);
                                            } else {
                                                allYourNotification.add("[ "+date+" at "+time+"]: \n" + fromUser + " -> You " + songName);
                                            }
                                        }
                                    }
                                    Log.e("Start_allNot",allYourNotification.toString());
                                    AllNotifications.allNotifications = allYourNotification;
                                    //  AllNotifications.totalNoOfNot = AllNotifications.allNotifications.size();
                                    doorClosed1 = false;
                                    Log.e("Start_Notif_Read", "Notification read complete..");
                                } catch (Exception ee) {
                                    Log.e("Start_Notifi_ReadErr", ee.getMessage());
                                } finally {
                                    try {
                                        isr.close();
                                    } catch (Exception ee) {
                                        Log.e("Start_Err", "InputStreamReader couldn't be closed");
                                    }
                                    urlConnection.disconnect();

                                }
                            }
                        }).start();


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
                                    Log.e("Start_allmoods_url", url.toString());
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                    InputStream is = urlConnection.getInputStream();
                                    InputStreamReader isr = new InputStreamReader(is);
                                    bufferedReader = new BufferedReader(isr);
                                    String line = "";
                                    while ((line = bufferedReader.readLine()) != null) {
                                        allMoods.add(line);
                                    }
                                    letMeReadAllTheMoods = false;
                                    Log.e("Start_allmoods_Read", "AllMoods read complete.." + allMoods.toString());
                                } catch (Exception ee) {
                                    Log.e("Start_notification_Read", ee.getMessage());
                                } finally {
                                    try {
                                        bufferedReader.close();
                                    } catch (Exception ee) {
                                        Log.e("Start_allmoodsReadErr", "BufferedReader couldn't be closed");
                                    }
                                    urlConnection.disconnect();

                                }
                            }
                        }).start();
                        while (letMeReadAllTheMoods) ;
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
                                        URL url = new URL(HttpGetPostInterface.serverSongURL + "playlist_" + mood + ".txt");
                                        Log.e("Start_Playlist_url", url.toString());
                                        urlConnection = (HttpURLConnection) url.openConnection();
                                        InputStream is = urlConnection.getInputStream();
                                        InputStreamReader isr = new InputStreamReader(is);
                                        bufferedReader = new BufferedReader(isr);
                                        String line = "";
                                        while ((line = bufferedReader.readLine()) != null) {
                                            if(!line.equals(""))
                                                songList.add(line);
                                        }
                                        // Populate the variable in the PlayListSongs.java file which is to be accessed by MediaPlayer
                                        PlaylistSongs.allMoodPlayList.put(mood, songList);
                                        // Tracks if all the files have been read or not, once complete its reaches ZERO(0)
                                        synchronized (Start.class) {
                                            readAllPlaylistComplete--;
                                            Log.e("Start_PL_ReadStatus", mood + " read complete :"+readAllPlaylistComplete);
                                        }
                                    } catch (Exception ee) {
                                        Log.e("Start_Playlist_Read", songList.toString());
                                    } finally {
                                        try {
                                            bufferedReader.close();
                                        } catch (Exception ee) {
                                            Log.e("Start_Err", "BufferedReader couldn't be closed");
                                        }
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
                            while (doorClosed1 || contactReadFinished || readAllPlaylistComplete > 0)
                                ;
                            Log.e("Start_AllTabsLaunch", "AllTabs will be launched");
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
    //}

    public boolean checkIfATableExists(String tableName) {
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            Cursor allTables = mydatabase.rawQuery("SELECT name from sqlite_master WHERE type='table' and name='" + tableName + "'", null);
            if (allTables.getCount() == 1) {
                Log.e("Start_chkTbl", tableName + " exists");
                mydatabase.close();
                return true;
            } else {
                Log.e("Start_chkTbl", tableName + " doesn't exist");
                mydatabase.close();
                return false;
            }
        } catch (Exception ee) {
            Log.e("Start_chkEr", ee.getMessage());
        }
        mydatabase.close();
        return false;
    }

    public LinkedHashMap<String,String> getOrStoreContactsTableData(int status, HashMap<String,String> allContacts){
        Log.e("Start_allC_INITSZ",allContacts.size()+"");
        LinkedHashMap<String,String> allContactsPresent = new LinkedHashMap<>();
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            // status = 0 is for READ and RETURN as it means TABLE ALREADY EXISTS
            if(status == 0){
                //READ and RETURN data
                Cursor resultSet = mydatabase.rawQuery("Select * from allcontacts", null);
                resultSet.moveToFirst();
                Log.e("Start_TBLDetect",resultSet.getCount()+" no of rows..");
                while (!resultSet.isAfterLast()) {
                    String phone_no = resultSet.getString(0);
                    String name = resultSet.getString(1);
                    allContacts.put(phone_no,name);
                    resultSet.moveToNext();
                }
            }
            // First time conatct table create or REFRESH done.
            else{
                String createQuery = "CREATE TABLE IF NOT EXISTS allcontacts(phone_no VARCHAR,name VARCHAR);";
                mydatabase.execSQL(createQuery);
                Log.e("Start_TBLCRT","allcontacts table created..");
                String insertQuery = "";
                for(String eachContact:allContacts.keySet()){
                    insertQuery = "INSERT INTO allcontacts values('"+eachContact+"','"+allContacts.get(eachContact)+"');";
                    Log.e("Start_InsertQuery",insertQuery);
                    mydatabase.execSQL(insertQuery);
                }
                mydatabase.close();
                return null;
            }
        }catch (Exception ee){
            Log.e("StartFragment_TBLErr",ee.getMessage());
            ee.fillInStackTrace();
        }
        mydatabase.close();
        return allContactsPresent;
    }
}

