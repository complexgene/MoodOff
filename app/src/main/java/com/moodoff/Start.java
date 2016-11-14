package com.moodoff;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private boolean doorClosed1 = true, doorClosed2 = true, letMeReadAllTheMoods = true;
    private static int readAllPlaylistComplete = 0;
    private HashMap<String,ArrayList<String>> allMoodPlayList = new HashMap<>();
    ProgressBar spinner;
    TextView temp,weather,specialDate,greet;
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
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(serverURL + "/notifications/" + userMobileNumber);
                                urlConnection = (HttpURLConnection) url.openConnection();
                                InputStream is = urlConnection.getInputStream();
                                InputStreamReader isr = new InputStreamReader(is);
                                int data = isr.read();
                                final StringBuilder response = new StringBuilder("");
                                while (data != -1) {
                                    response.append((char) data);
                                    data = isr.read();
                                }
                                AllNotifications.allNotifications = ParseNotificationData.getNotification(response.toString());
                                //  AllNotifications.totalNoOfNot = AllNotifications.allNotifications.size();
                                doorClosed1 = false;

                            } catch (Exception ee) {
                                Log.e("Start_notification_Read",ee.getMessage());
                            }
                        }
                    }).start();

                    final ArrayList<String> allMoods = new ArrayList<String>();

                    new Thread(new Runnable() {
                        HttpURLConnection urlConnection = null;
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(HttpGetPostInterface.serverSongURL + "allmoods.txt");
                                Log.e("Start_allmoodstxt_url",url.toString());
                                urlConnection = (HttpURLConnection) url.openConnection();
                                InputStream is = urlConnection.getInputStream();
                                InputStreamReader isr = new InputStreamReader(is);
                                BufferedReader bufferedReader = new BufferedReader(isr);
                                String line="";
                                while ((line=bufferedReader.readLine())!=null) {
                                    allMoods.add(line);
                                }
                                Log.e("Start_allmoods",allMoods.toString());
                                letMeReadAllTheMoods = false;
                            } catch (Exception ee) {
                                Log.e("Start_allmoodtxt_Read",ee.getMessage());
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
                            @Override
                            public void run() {
                                try {
                                    URL url = new URL(HttpGetPostInterface.serverSongURL + "playlist_"+ mood +".txt");
                                    Log.e("Playlist_url",url.toString());
                                    urlConnection = (HttpURLConnection) url.openConnection();
                                    InputStream is = urlConnection.getInputStream();
                                    InputStreamReader isr = new InputStreamReader(is);
                                    BufferedReader bufferedReader = new BufferedReader(isr);
                                    String line="";
                                    while ((line=bufferedReader.readLine())!=null) {
                                        songList.add(line);
                                    }

                                    // Populate the variable in the PlayListSongs.java file which is to be accessed by MediaPlayer
                                    PlaylistSongs.allMoodPlayList.put(mood,songList);
                                    // Tracks if all the files have been read or not
                                    readAllPlaylistComplete--;
                                } catch (Exception ee) {
                                    Log.e("Start_Playlist_Read",songList.toString());
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
                        while (doorClosed1 || readAllPlaylistComplete>0) ;
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
}
