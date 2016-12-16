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
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.PlaylistSongs;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Start extends AppCompatActivity {
    private String serverURL = HttpGetPostInterface.serverURL;
    private boolean fetchContactsNotComplete = true, notificationFetchNotComplete = true, moodsAndSongsFetchNotComplete = true;
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

    private void populateUserData(){
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
                rd.endReadTransaction();
            }
            else {
                Intent ii = new Intent(this, RegistrationActivity.class);
                startActivity(ii);
            }
        }
        catch(Exception ee){Log.e("Start_popUsrData","Contacts not populated coz:"+ee.getMessage());}
    }

    private void greetUser() {
        Toast.makeText(this, "You look just awesome today!!", Toast.LENGTH_LONG).show();
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
    }

    private void fetchNotifications() {
        final String userMobileNumber = UserDetails.getPhoneNumber();
        final String serverURL = HttpGetPostInterface.serverURL;
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    URL url = new URL(serverURL+ "/notifications/" + userMobileNumber);
                    Log.e("Start_Notf_ReadURL", url.toString());
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
                    Log.e("Start_SIZE",allReadContacts.size()+"");
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
                            String nameOfTo = allReadContacts.get(toUser);
                            if(nameOfTo!=null && nameOfTo.length()>19)
                                nameOfTo = nameOfTo.substring(0,16)+"...";

                            if (nameOfTo != null) {
                                allYourNotification.add("[ "+date+" at "+time+" ]: \nYou -> " + nameOfTo + " " + songName);
                            } else {
                                allYourNotification.add("[ "+date+" at "+time+" ]: \nYou -> " + toUser + " " + songName);
                            }
                        }
                        else {
                            String nameOfFrom = allReadContacts.get(fromUser);
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
                    notificationFetchNotComplete = false;
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
    }

    private void fetchMoodsAndPlayListFiles() {
        final ArrayList<String> allMoods = new ArrayList<String>();
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(HttpGetPostInterface.serverSongURL + "allsongdata.txt");
                    Log.e("Start_allsongsURL", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream is = urlConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    bufferedReader = new BufferedReader(isr);

                    int noOfMoods = Integer.parseInt(bufferedReader.readLine());
                    ArrayList<String> typeOfMoods = new ArrayList<String>(noOfMoods);
                    for(int i=0;i<noOfMoods;i++) {
                        typeOfMoods.add(bufferedReader.readLine());
                    }

                    Log.e("Start_typeOfMoods",typeOfMoods.toString());

                    bufferedReader.readLine(); //SEPARATOR

                    String song = "";
                    ArrayList<String> allSongInAMood = new ArrayList<String>();
                    for(int i=0;i<noOfMoods;i++) {
                        while ((song = bufferedReader.readLine()) != null) {
                            if(!song.equals("")) {
                                allSongInAMood.add(song);
                            }
                            else {
                                break;
                            }
                        }
                        PlaylistSongs.getAllMoodPlayList().put(typeOfMoods.get(i),allSongInAMood);
                        Log.e("Start_disMood",typeOfMoods.get(i)+" "+allSongInAMood.toString());
                        allSongInAMood = new ArrayList<String>();
                    }
                    moodsAndSongsFetchNotComplete = false;
                    Log.e("Start_allmoods_Read", "AllMoods read complete..");
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        askForPermissions();
        if (!checkNetworkAvailability()) {
            Toast.makeText(getApplicationContext(),"Sorry! You need Internet Connection",Toast.LENGTH_LONG).show();
            spinner.setVisibility(View.INVISIBLE);

        } else {
            spinner = (ProgressBar) findViewById(R.id.spinner);
            spinner.setVisibility(ProgressBar.VISIBLE);


            Log.e("Start_Bots","Bots started");
            //startAutoBots();

            fetchMoodsAndPlayListFiles();
            populateUserData();
            Log.e("Start_populateUSrData", "User data populated");
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
                }, 5000);
            } catch (Exception ee) {
                Log.e("Start_AllTabsLaunchErr","Error in Alltabs Launch");
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

