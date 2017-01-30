package com.moodoff;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.AppData;
import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Start extends AppCompatActivity {
    public static int switchToTab = 0;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 100;
    private String serverURL = HttpGetPostInterface.serverURL;
    public static boolean fetchContactsNotComplete = true, notificationFetchNotComplete = true, moodsAndSongsFetchNotComplete = true, fetchContactsFromServerNotComplete = true;
    ProgressBar spinner;
    TextView greet;
    SQLiteDatabase mydatabase;
    DBHelper dbOpr = new DBHelper(this);
    LinkedHashMap<String,String> allReadContacts = new LinkedHashMap<>();

    private boolean checkNetworkAvailability(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean populateUserData(){
        StoreRetrieveDataInterface rd = null;
        try {
            rd = new StoreRetrieveDataImpl("UserData.txt");
            if (rd.fileExists()) {
                rd.beginReadTransaction();
                UserDetails.setUserName(rd.getValueFor("user"));
                UserDetails.setPhoneNumber(rd.getValueFor("phoneNo"));
                UserDetails.setEmailId(rd.getValueFor("email"));
                UserDetails.setDateOfBirth(rd.getValueFor("dob"));
                UserDetails.setUserTextStatus(rd.getValueFor("textStatus"));
                UserDetails.setUserAudioStatusSong(rd.getValueFor("audioStatus"));
                UserDetails.setScore(Integer.parseInt(rd.getValueFor("score")));
                rd.endReadTransaction();
                return true;
            }
            else {
                Intent ii = new Intent(this, RegistrationActivity.class);
                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                finish(); // destroy current activity..
                startActivity(ii);
                return false;
            }
        }
        catch(Exception ee){Log.e("Start_popUsrData","Contacts not populated coz:"+ee.getMessage());}
        return true;
    }

    private void greetUser() {
        //Toast.makeText(this, "You look just awesome today!!", Toast.LENGTH_SHORT).show();
        greet = (TextView) findViewById(R.id.greet);
        Calendar c = Calendar.getInstance();
        //specialDate.setText("Today is: " + c.get(Calendar.DATE) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.YEAR));
        int hour = c.get(Calendar.HOUR_OF_DAY);
        String greetStr = "- Good Morning -";
        if (hour >= 12 && hour <= 18) greetStr = "- Good Afternoon -";
        if (hour >= 18 && hour <= 23) greetStr = "- Good Evening -";
        greet.setText(greetStr);
    }

    private void fetchContacts() {
        if (!checkIfATableExists("allcontacts")) {
            Log.e("Start_cntctsTAB", "Not present");
            allReadContacts = ContactList.getContactNames(getContentResolver());
            fetchContactsNotComplete = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getOrStoreContactsTableData(1, allReadContacts);
                }
            }).start();
        } else {
            Log.e("Start_cntctsTAB", "Present");
            allReadContacts = getOrStoreContactsTableData(0,allReadContacts);
            fetchContactsNotComplete = false;
        }
        ContactsManager.allReadContacts = allReadContacts;
        //NotificationFragment.allReadContacts = allReadContacts;
    }

    private void fetchNotifications() {
                try {
                    Log.e("Start_Notifications","Start loading notifications from DB");
                    ArrayList<String> allNotificationsFromDB = readNotificationsFromInternalDB();
                    AppData.totalNoOfNot = allNotificationsFromDB.size();
                    Log.e("Start_Notifictions","Fetched "+AppData.totalNoOfNot+" notifications..");
                    ServerManager serverManager = new ServerManager(this);
                    serverManager.readNotificationsFromServerAndWriteToInternalDB();
                    //ArrayList<String> allYourNotification = new ArrayList<String>();
                    NotificationFragment.totalNumberOfNotifications = allNotificationsFromDB.size();
                    Log.e("Start_NotB4",allNotificationsFromDB.toString());
                    Log.e("Start_SIZE",allReadContacts.size()+"");
                    AppData.allNotifications = allNotificationsFromDB;
                    notificationFetchNotComplete = false;
                    Log.e("Start_Notif_Read", "Notification read complete..");
                } catch (Exception ee) {
                    Log.e("Start_Notif_ReadErr", ee.getMessage());
                    ee.printStackTrace();
                }
    }

    private ArrayList<String> readNotificationsFromInternalDB(){
        DBHelper dbOperations = new DBHelper(this);
        return dbOperations.readNotificationsFromInternalDB();
    }

    private boolean checkEntryOfPlaylistInInternalTableAndReadIfRequired(String todaysDate){
        SQLiteDatabase readData = dbOpr.getReadableDatabase();
        Cursor resultSet = readData.rawQuery("Select * from playlist where date='"+todaysDate+"'", null);
        // If todays playlist file is not downloaded yet
        if(resultSet.getCount()==0)return false;
        // File downloaded
        HashMap<String,ArrayList<String>> allSongs = new HashMap<>();
        resultSet.moveToFirst();
        Log.e("Start_TBLDetect",resultSet.getCount()+" no of rows..");
        while (!resultSet.isAfterLast()) {
            String moodType = resultSet.getString(1);
            String songName = resultSet.getString(2);
            String artistName = resultSet.getString(3);
            String movieOrAlbumname = resultSet.getString(3);
            Log.e("Start_MOOD",moodType+" "+songName);
            if(allSongs.containsKey(moodType)){
                allSongs.get(moodType).add(songName);
            }
            else{
                ArrayList<String> songs = new ArrayList<>();
                songs.add(songName);
                allSongs.put(moodType,songs);
            }
            resultSet.moveToNext();
        }
        AppData.allMoodPlayList = allSongs;
        moodsAndSongsFetchNotComplete = false;
        return true;
    }
    private void fetchMoodsAndPlayListFiles() {
        Calendar c = Calendar.getInstance();
        String todaysDate = c.get(Calendar.DATE)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR);
        Log.e("Start_Date",todaysDate);
        if(!checkEntryOfPlaylistInInternalTableAndReadIfRequired(todaysDate)) {
         // Not yet downloaded to internal table and will be downloaded only once.
            Log.e("Start_MOOD","Downloading the playlist for the first itme of day..");
            ServerManager reads = new ServerManager();
            reads.readPlayListFromServer(this,todaysDate);
        }
        else{
            Log.e("Start_MOOD","As playlist file already fetched to internal DB so will ste the lock to FALSE");
            Start.moodsAndSongsFetchNotComplete = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        /*ViewPager viewPager = (ViewPager) AllTabs.mViewPager.findViewById(R.id.container);
        viewPager.setCurrentItem(switchToTab);*/
        //if(switchToTab)

        //askForPermissions();
        //while(permissionNotDone);

        if (!checkNetworkAvailability()) {
            Toast.makeText(getApplicationContext(), "Sorry! You need Internet Connection", Toast.LENGTH_LONG).show();
            spinner = (ProgressBar) findViewById(R.id.spinner);
            spinner.setVisibility(View.INVISIBLE);
            Messenger.print(getApplicationContext(),"Start Internet Connection and restart the app!!");

        } else {

            if (populateUserData()) {
                spinner = (ProgressBar) findViewById(R.id.spinner);
                spinner.setVisibility(ProgressBar.VISIBLE);
                Log.e("Start_populateUSrData", "User data populated");

                //Log.e("Start_Bots", "Bots started");
                //startAutoBots();
                fetchMoodsAndPlayListFiles();
                Log.e("Start_moodPlaylist", "Playlist file fetched");
                greetUser();
                Log.e("Start_greetUSr", "Greet User done");
                fetchContacts();
                while (fetchContactsNotComplete) ;
                fetchNotifications();
                // Distinguishes who uses app and who don't
                //fetchContactsFromServer();

                Log.e("Start_FILEREADS","DONEEEEEEEEEEEEEEEEE");
                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent mainIntent = new Intent(Start.this, AllTabs.class);
                            Log.e("Start_FILEREADS",notificationFetchNotComplete+" "+moodsAndSongsFetchNotComplete);
                            while (notificationFetchNotComplete || moodsAndSongsFetchNotComplete) ;
                            Log.e("Start_AllTabsLaunch", "AllTabs will be launched");
                            Start.this.startActivity(mainIntent);
                            Start.this.finish();
                        }
                    }, 2000);
                } catch (Exception ee) {
                    Log.e("Start_AllTabsLaunchErr", "Error in Alltabs Launch");
                }
            }
        }
    }

    private void fetchContactsFromServer(){
        ServerManager serverManager = new ServerManager();
        serverManager.fetchContactsFromServer();
    }

    private void startAutoBots(){
        Log.e("Start_Bots","Bots in work");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dbOpr.toDoWorkExit();
            }
        },5000);
    }

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

    public LinkedHashMap<String,String> getOrStoreContactsTableData(int status, LinkedHashMap<String,String> allContacts){
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            // status = 0 is for READ and RETURN as it means TABLE ALREADY EXISTS
            if(status == 0){
                //READ and RETURN data
                Cursor resultSet = mydatabase.rawQuery("Select * from allcontacts order by name", null);
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
            }
        }catch (Exception ee){
            Log.e("StartFragment_TBLErr",ee.getMessage());
            ee.fillInStackTrace();
        }
        mydatabase.close();
        return allContacts;
    }

}

