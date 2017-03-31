package com.moodoff.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.moodoff.helper.AllAppData;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.model.User;
import com.moodoff.service.ContentManagementService;
import com.moodoff.service.StartedTodoService;
import com.moodoff.service.UserManagementService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import static com.moodoff.helper.LoggerBaba.printMsg;

public class Start extends AppCompatActivity {
    // All Variables declaration
    public static int switchToTab = 0;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 20;
    public static boolean fetchContactsNotComplete = true, notificationFetchNotComplete = true,
                          moodsAndSongsFetchNotComplete = true, fetchContactsFromServerNotComplete = true,
                          allProfilesDataFetchNotComplete = true;
    private ProgressBar spinner;
    private TextView greet;
    private LinkedHashMap<String,String> allReadContacts;
    private User singleTonUserObject;
    private ServerManager serverManager = new ServerManager(this);
    private DBHelper dbOperations = new DBHelper(this);
    Context currentContext;
    UserManagementService userManagementService;
    ContentManagementService contentManagementService;
    StartedTodoService startedTodoService;
    // Declaration of all varaibles complete

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        try {
            initComponents();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askForPermissions();
                // Implicit startWork() after granting permission from onPermissionResult
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //For such versions you need to call startWork() explicitly
                if (askForPermissions()) {
                    startWork();
                }
            }
        }catch (Exception ee){
            startedTodoService.writeProblemTrace("Got the error as:" + ee.getMessage());
            Messenger.print(getApplicationContext(), "Problem occured in launching!! MoodOff Team will contact you soon!!");
            finish();
        }
    }

    private void initComponents(){
        currentContext = getApplicationContext();
        singleTonUserObject = User.getInstance();
        allReadContacts = new LinkedHashMap<>();
        userManagementService = new UserManagementService(currentContext);
        contentManagementService = new ContentManagementService(currentContext);
        startedTodoService = new StartedTodoService(currentContext);
    }
    private boolean askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.e("Start_Permission","Asking Permission as not yet asked...");
            int contactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            int extStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            if(  // Check if any of the permissions not given yet..
                    cameraPermission != PackageManager.PERMISSION_GRANTED &&
                    contactsPermission != PackageManager.PERMISSION_GRANTED &&
                    extStoragePermission != PackageManager.PERMISSION_GRANTED
              ){ // Ask for the permissions if any of the permissions not given yet..
                    requestPermissions(new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },  REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            else {
                Log.e("Start_Permission","Already granted all the permissions...");
                startWork();
            }
            return true;
        }
        else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        },      MY_PERMISSIONS_REQUEST_READ_CONTACTS);
               return true;
            }
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if  (   grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED
                    )
                {
                    startWork();
                }
                else { Messenger.print(this, "[__Sorry!! The app needs all permissions to go ahead!!__]"); }
                break;
            }
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if  (    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED
                    )
                {
                    startWork();
                }
                else
                {
                    Messenger.print(this, "[__Sorry!! The app needs all permissions to go ahead!!__]");
                }
            }
        }
    }
    // Check if user is having network connection
    private void startWork() {
        if (!checkNetworkAvailability()) {
            Toast.makeText(getApplicationContext(), "Sorry! You need Internet Connection", Toast.LENGTH_LONG).show();
            spinner = (ProgressBar) findViewById(R.id.spinner);
            spinner.setVisibility(View.INVISIBLE);
            Messenger.print(getApplicationContext(), "Start Internet Connection and restart the app!!");
        } else {
            if (userManagementService.populateUserData()) {
                printMsg("Start","Reading Userdata.txt file from local and populating the POJO complete..");
                resetAllDataContainers();
                spinner = (ProgressBar) findViewById(R.id.spinner);
                spinner.setVisibility(ProgressBar.VISIBLE);
                Log.e("Start_populateUSrData", "User data populated");
                //Log.e("Start_Bots", "Bots started");
                //startAutoBots();
                fetchMoodsAndPlayListFiles();
                Log.e("Start_moodPlaylist", "Playlist file fetched");
                greetUser();
                Log.e("Start_greetUser", "Greet User done");
                notificationFetchNotComplete = false;
                allProfilesDataFetchNotComplete = false;
                // Get and separate all the contacts based on who uses app and who doesn't uses from the internal table.
                fetchContacts();
                while (fetchContactsNotComplete) ;
                fetchNotifications();
                // Distinguishes who uses app and who don't
                fetchContactsFromServer();
                //fetchAllProfilesData();
                Log.e("Start_FILEREADS", "DONE..");
                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent mainIntent = new Intent(Start.this, AllTabs.class);
                            Log.e("Start_FILEREADS", notificationFetchNotComplete + " " + moodsAndSongsFetchNotComplete);
                            while (notificationFetchNotComplete || moodsAndSongsFetchNotComplete || allProfilesDataFetchNotComplete)
                                ;
                            Log.e("Start_AllTabsLaunch", "AllTabs will be launched");
                            Start.this.startActivity(mainIntent);
                            Start.this.finish();
                        }
                    }, 2000);
                } catch (Exception ee) {
                    Log.e("Start_AllTabsLaunchErr", "Error in Alltabs Launch");
                }
            }
            else{
                Log.e("Start_USER_STATUS","User opening app first time..");
                Intent ii = new Intent(this, RegistrationActivity.class);
                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                finish(); // destroy current activity..
                startActivity(ii);
            }
        }
    }

    private void fetchNotifications() {
        try {
            Log.e("NotificationFragment","fetchNotifications(): Start loading notifications from Internal DB");
            ArrayList<String> allNotificationsFromDB = dbOperations.readNotificationsFromInternalDB();
            AllAppData.allNotifications = allNotificationsFromDB;
            AllAppData.totalNoOfNot = allNotificationsFromDB.size();
            //NotificationFragment.totalNumberOfNotifications = allNotificationsFromDB.size();
            Log.e("NotificationFragment","fetchNotifications(): Fetched " + AllAppData.totalNoOfNot + " notifications from internal DB..");
            notificationFetchNotComplete = false;
        } catch (Exception ee) {
            Log.e("NotificationFragmentErr", "fetchNotifications():" + ee.getMessage());
            ee.printStackTrace();
        }
    }

    private boolean checkNetworkAvailability(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void resetAllDataContainers(){
        Log.e("Start_DataSetRESET","Resetting of all data conatiners started..");
        if(AllAppData.allMoodPlayList!=null)
            AllAppData.allMoodPlayList.clear();
        if(AllAppData.allNotifications!=null)
            AllAppData.allNotifications.clear();
        if(AllAppData.allProfileData!=null)
            AllAppData.allProfileData.clear();
        AllAppData.countFriendsUsingApp=0;
        AllAppData.countFriendsNotUsingApp = 0;
        if(AllAppData.allReadContacts!=null)
            AllAppData.allReadContacts.clear();
        if(AllAppData.friendsWhoUsesApp!=null)
            AllAppData.friendsWhoUsesApp.clear();
        if(AllAppData.friendsWhoDoesntUseApp!=null)
            AllAppData.friendsWhoDoesntUseApp.clear();
        Log.e("Start_DataSetRESET","Resetting of all data conatiners done..");
    }
    private void fetchMoodsAndPlayListFiles(){
        Calendar c = Calendar.getInstance();
        String todaysDate = c.get(Calendar.DATE)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR);
        Log.e("Start_Date",todaysDate);
        if(!checkEntryOfPlaylistInInternalTableAndReadIfRequired(todaysDate)) {
            // Not yet downloaded to internal table and will be downloaded only once.
            Log.e("Start_MOOD","Downloading the playlist for the first time of day..");
            ServerManager reads = new ServerManager();
            reads.readPlayListFromServer(this,todaysDate);
        }
        else{
            Log.e("Start_MOOD","As playlist file already fetched to internal DB so will set the lock to FALSE");
            Start.moodsAndSongsFetchNotComplete = false;
        }
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
            getContactsTableData(allReadContacts, dbOperations, singleTonUserObject);
            Log.e("Start_conatctsFetch",allReadContacts.size()+" total contacts fetched..");
            fetchContactsNotComplete = false;
        //NotificationFragment.allReadContacts = allReadContacts;
        Log.e("Start_contactsFetch","Contacts fetching done..");
    }

    private void fetchContactsFromServer(){
        ServerManager serverManager = new ServerManager(this);
        serverManager.fetchContactsFromServer();
    }
    /*private void fetchAllProfilesData(){
        try{
            Log.e("Start_ProfileFetch","Start reading profile data for all friends..");
            AllAppData.allProfileData = dbOperations.readAllProfilesDataFromInternalDB();
            allProfilesDataFetchNotComplete = false;
            serverManager.readAllProfileDataFromServerAndWriteToInternalDB();
            Log.e("Start_ProfileFetch","End reading profile data for all friends..");
        }
        catch (Exception ee){
            Log.e("Start_ProfileFetch_Err",ee.getMessage());
            ee.printStackTrace();
        }
    }*/


    private void startAutoBots(){
        Log.e("Start_Bots","Bots in work");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dbOperations.toDoWorkExit();
            }
        },5000);
    }
    private boolean checkEntryOfPlaylistInInternalTableAndReadIfRequired(String todaysDate){
        return startedTodoService.checkEntryOfPlaylistInInternalTableAndReadIfRequired(dbOperations, todaysDate, moodsAndSongsFetchNotComplete);
    }
    public LinkedHashMap<String,String> getContactsTableData(LinkedHashMap<String,String> allContacts, DBHelper dbOpr, User singleTonUserObject){
        return startedTodoService.getContactsTableData(allContacts, dbOpr, singleTonUserObject);
    }
}