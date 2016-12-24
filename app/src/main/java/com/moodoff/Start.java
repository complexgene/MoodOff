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

import com.moodoff.helper.AppData;
import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.ServerManager;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

public class Start extends AppCompatActivity {
    private String serverURL = HttpGetPostInterface.serverURL;
    public static boolean fetchContactsNotComplete = true, notificationFetchNotComplete = true, moodsAndSongsFetchNotComplete = true;
    ProgressBar spinner;
    TextView greet;
    SQLiteDatabase mydatabase;
    LinkedHashMap<String,String> allReadContacts = new LinkedHashMap<>();

    private void askForPermissions(){
        ActivityCompat.requestPermissions(Start.this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS},
                1);
    }

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
                    ArrayList<String> allYourNotification = new ArrayList<String>();
                    NotificationFragment.totalNumberOfNotifications = allNotificationsFromDB.size();
                    Log.e("Start_NotB4",allNotificationsFromDB.toString());
                    Log.e("Start_SIZE",allReadContacts.size()+"");
                    for(String eachNotification : allNotificationsFromDB){
                        String[] allData = eachNotification.split(" ");
                        String fromUser = allData[0];
                        String toUser = allData[1];
                        String date = allData[2];
                        String time = allData[3];
                        time = time.substring(0,time.lastIndexOf(":"));
                        String type = allData[4];
                        String songName = allData[5];

                        if(fromUser.equals(UserDetails.getPhoneNumber())){
                            String nameOfTo = allReadContacts.get(toUser);
                            if(nameOfTo!=null && nameOfTo.length()>19)
                                nameOfTo = nameOfTo.substring(0,16)+"...";

                            if (nameOfTo != null) {
                                allYourNotification.add(toUser+"[ "+date+" at "+time+" ]: \nYou > " + nameOfTo + " " + songName);
                            } else {
                                allYourNotification.add(toUser+"[ "+date+" at "+time+" ]: \nYou > " + toUser + " " + songName);
                            }
                        }
                        else {
                            String nameOfFrom = allReadContacts.get(fromUser);
                            if(nameOfFrom!=null && nameOfFrom.length()>19)
                                nameOfFrom = nameOfFrom.substring(0,16)+"...";
                            if (nameOfFrom != null) {
                                allYourNotification.add(fromUser+"[ "+date+" at "+time+" ]: \n" + nameOfFrom + " > You " + songName);
                            } else {
                                allYourNotification.add(fromUser+"[ "+date+" at "+time+"]: \n" + fromUser + " > You " + songName);
                            }
                        }
                    }
                    Log.e("Start_allNot",allYourNotification.toString());
                    //NotificationFragment.
                    AppData.allNotifications = allYourNotification;
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

    private void fetchMoodsAndPlayListFiles() {
        ServerManager reads = new ServerManager();
        reads.readPlayListFromServer();
        //moodsAndSongsFetchNotComplete = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        askForPermissions();

        if (!checkNetworkAvailability()) {
            Toast.makeText(getApplicationContext(), "Sorry! You need Internet Connection", Toast.LENGTH_LONG).show();
            spinner.setVisibility(View.INVISIBLE);

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
                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent mainIntent = new Intent(Start.this, AllTabs.class);
                            while (notificationFetchNotComplete || moodsAndSongsFetchNotComplete) ;
                            Log.e("Start_AllTabsLaunch", "AllTabs will be launched");
                            Start.this.startActivity(mainIntent);
                            Start.this.finish();
                        }
                    }, 2500);
                } catch (Exception ee) {
                    Log.e("Start_AllTabsLaunchErr", "Error in Alltabs Launch");
                }
            }
        }
    }

    DBHelper dbOpr = new DBHelper(this);
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

