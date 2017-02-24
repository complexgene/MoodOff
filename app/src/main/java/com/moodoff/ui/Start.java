package com.moodoff.ui;

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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.R;
import com.moodoff.helper.AppData;
import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Start extends AppCompatActivity {
    // All Variables declaration
    public static int switchToTab = 0;
    private String serverURL = HttpGetPostInterface.serverURL;
    public static boolean fetchContactsNotComplete = true, notificationFetchNotComplete = true,
                          moodsAndSongsFetchNotComplete = true, fetchContactsFromServerNotComplete = true,
                          allProfilesDataFetchNotComplete = true;
    private ProgressBar spinner;
    private TextView greet;
    private SQLiteDatabase mydatabase;
    private DBHelper dbOpr = new DBHelper(this);
    private LinkedHashMap<String,String> allReadContacts = new LinkedHashMap<>();
    private UserDetails singleTonUserObject = UserDetails.getInstance();
    // Declaration of all varaibles complete

    // Check if user is having network connection
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
                singleTonUserObject.setUserName(rd.getValueFor("userName"));
                singleTonUserObject.setUserMobileNumber(rd.getValueFor("userPhoneNumber"));
                singleTonUserObject.setUserDateOfBirth(rd.getValueFor("userDob"));
                singleTonUserObject.setUserTextStatus(rd.getValueFor("userTextStatus"));
                singleTonUserObject.setUserAudioStatusSong(rd.getValueFor("userAudioStatus"));
                singleTonUserObject.setUserScore(Integer.parseInt(rd.getValueFor("userScore")));
                rd.endReadTransaction();
                return true;
            }
            else {
                Log.e("Start_USER_STATUS","User opening app first time..");
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
        Log.e("Start_contactsFetch","Came to fetch contacts..");
            getContactsTableData(allReadContacts);
            Log.e("Start_conatctsFetch",allReadContacts.size()+" total contacts fetched..");
            fetchContactsNotComplete = false;
        //NotificationFragment.allReadContacts = allReadContacts;
        Log.e("Start_contactsFetch","Contacts fetching done..");
    }
    ServerManager serverManager = new ServerManager(this);
    private void fetchNotifications() {
                try {
                    Log.e("Start_Notifications","Start loading notifications from DB");
                    ArrayList<String> allNotificationsFromDB = readNotificationsFromInternalDB();
                    AppData.allNotifications = allNotificationsFromDB;
                    AppData.totalNoOfNot = allNotificationsFromDB.size();
                    NotificationFragment.totalNumberOfNotifications = allNotificationsFromDB.size();
                    Log.e("Start_Notifictions","Fetched "+AppData.totalNoOfNot+" notifications..");
                    serverManager.readNotificationsFromServerAndWriteToInternalDB();
                    Log.e("Start_NotB4",allNotificationsFromDB.toString());
                    notificationFetchNotComplete = false;
                    Log.e("Start_Notif_Read", "Notification read complete and started auto script..");
                } catch (Exception ee) {
                    Log.e("Start_Notif_ReadErr", ee.getMessage());
                    ee.printStackTrace();
                }
    }

    private void fetchAllProfilesData(){
        try{
            Log.e("Start_ProfileFetch","Start reading profile data for all friends..");
            AppData.allProfileData = readAllProfilesDataFromInternalDB();
            allProfilesDataFetchNotComplete = false;
            serverManager.readAllProfileDataFromServerAndWriteToInternalDB();
            Log.e("Start_ProfileFetch","End reading profile data for all friends..");
        }
        catch (Exception ee){
            Log.e("Start_ProfileFetch_Err",ee.getMessage());
            ee.printStackTrace();
        }
    }
    private HashMap<String,HashMap<String,String>> readAllProfilesDataFromInternalDB(){
        DBHelper dbOperations = new DBHelper(this);
        return dbOperations.readAllProfilesDataFromInternalDB();
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
        Log.e("Start_PlaylistFileCNT",resultSet.getCount()+" no of rows..");
        while (!resultSet.isAfterLast()) {
            String moodType = resultSet.getString(1);
            String songName = resultSet.getString(2);
            String artistName = resultSet.getString(3);
            String movieOrAlbumname = resultSet.getString(3);
            //Log.e("Start_MOOD",moodType+" "+songName);
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
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 20;
    private boolean askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("Start_Permission","Asking Permission...");
            int contactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            int extStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            if(cameraPermission != PackageManager.PERMISSION_GRANTED && contactsPermission != PackageManager.PERMISSION_GRANTED && extStoragePermission != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            else{
                Log.e("Start_Permission","Already Granted...");
                startWork();
            }
            return true;
        }
        else {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                return true;
            }
        }
        return false;
    }

    private void resetAllDataContainers(){
        Log.e("Start_DataSetRESET","Resetting of all data conatiners started..");
        if(AppData.allMoodPlayList!=null)
            AppData.allMoodPlayList.clear();
        if(AppData.allNotifications!=null)
            AppData.allNotifications.clear();
        if(AppData.allProfileData!=null)
            AppData.allProfileData.clear();
            ContactsManager.countFriendsUsingApp=0;
            ContactsManager.countFriendsNotUsingApp = 0;
        if(ContactsManager.allReadContacts!=null)
            ContactsManager.allReadContacts.clear();
        if(ContactsManager.friendsWhoUsesApp!=null)
            ContactsManager.friendsWhoUsesApp.clear();
        if(ContactsManager.friendsWhoDoesntUseApp!=null)
            ContactsManager.friendsWhoDoesntUseApp.clear();
        Log.e("Start_DataSetRESET","Resetting of all data conatiners done..");
    }

    private void startWork(){
        if (!checkNetworkAvailability()) {
            Toast.makeText(getApplicationContext(), "Sorry! You need Internet Connection", Toast.LENGTH_LONG).show();
            spinner = (ProgressBar) findViewById(R.id.spinner);
            spinner.setVisibility(View.INVISIBLE);
            Messenger.print(getApplicationContext(),"Start Internet Connection and restart the app!!");

        } else {
            if (populateUserData()) {
                resetAllDataContainers();
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
                fetchContactsFromServer();

                fetchAllProfilesData();

                Log.e("Start_FILEREADS","DONEEEEEEEEEEEEEEEEE");
                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent mainIntent = new Intent(Start.this, AllTabs.class);
                            Log.e("Start_FILEREADS",notificationFetchNotComplete+" "+moodsAndSongsFetchNotComplete);
                            while (notificationFetchNotComplete || moodsAndSongsFetchNotComplete || allProfilesDataFetchNotComplete) ;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    /*Intent ii = new Intent(this,Start.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                    finish(); // destroy current activity..
                    startActivity(ii);*/
                    startWork();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Sorry!! The app needs all permissions to go ahead!!",Toast.LENGTH_LONG).show();
                }
            /*case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    Intent ii = new Intent(this,Start.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                    finish(); // destroy current activity..
                    startActivity(ii);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Sorry!! The app needs all permissions to go ahead!!",Toast.LENGTH_LONG).show();
                }*/
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            askForPermissions();
            // Implicit startWork() after granting permission from onPermissionResult
        }
        else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
            //For such versions you need to call it explicitly
            if(askForPermissions()){
                startWork();
            }
        }
    }

    private void fetchContactsFromServer(){
        ServerManager serverManager = new ServerManager(this);
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

    public LinkedHashMap<String,String> getContactsTableData(LinkedHashMap<String,String> allContacts){
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            Log.e("Start_CONTACTS_DATAB4",ContactsManager.friendsWhoUsesApp+" \n"+ContactsManager.friendsWhoDoesntUseApp);
                int countOfNonAppUsers = 0, countOfAppUsers = 0;
                Cursor resultSet = mydatabase.rawQuery("Select * from allcontacts order by name", null);
                resultSet.moveToFirst();
                while (!resultSet.isAfterLast()) {
                    String phone_no = resultSet.getString(0);
                    String name = resultSet.getString(1).replaceAll("'","\'");
                    int appUsingStatus = resultSet.getInt(2);
                    if (appUsingStatus == 1){
                        countOfAppUsers++;
                        if(!phone_no.equals(singleTonUserObject.getUserMobileNumber()))
                            ContactsManager.friendsWhoUsesApp.add(phone_no);
                    }
                    else{
                            countOfNonAppUsers++;
                            ContactsManager.friendsWhoDoesntUseApp.add(phone_no);
                    }
                    ContactsManager.allReadContacts.put(phone_no,name);
                    //allContacts.put(phone_no,name);
                    resultSet.moveToNext();
                    //ContactsManager.allReadContacts = allReadContacts;
                }
                allReadContacts = ContactsManager.allReadContacts;
                ContactsManager.countFriendsUsingApp = countOfAppUsers;
                ContactsManager.countFriendsNotUsingApp = countOfNonAppUsers;
                Log.e("Start_CONTACTS_DATAA4",ContactsManager.friendsWhoUsesApp+" \n"+ContactsManager.friendsWhoDoesntUseApp);
                Log.e("Start_COUNTS_USERS","[Using:"+ContactsManager.countFriendsUsingApp+"] [NotUsing:"+ContactsManager.countFriendsNotUsingApp+"]");
                mydatabase.close();
        }catch (Exception ee){
            Log.e("StartFragment_TBLErr",ee.getMessage());
            ee.fillInStackTrace();
        }
        mydatabase.close();
        return allContacts;
    }
}