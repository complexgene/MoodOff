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
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class Start extends AppCompatActivity {
    private String serverURL = HttpGetPostInterface.serverURL;
    private boolean doorClosed = true;
    ProgressBar spinner;
    TextView temp,weather,specialDate,greet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ActivityCompat.requestPermissions(Start.this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET,Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS},
                1);

        StoreRetrieveDataInterface rd=null;

        spinner = (ProgressBar)findViewById(R.id.spinner);
        specialDate = (TextView)findViewById(R.id.specialDate);
        greet = (TextView)findViewById(R.id.greet);

        spinner.setVisibility(ProgressBar.VISIBLE);


        try {
            rd = new StoreRetrieveDataImpl("UserData.txt");
            if(rd.fileExists()){

                rd.beginReadTransaction();
                UserDetails.setUserName(rd.getValueFor("user"));
                UserDetails.setPhoneNumber(rd.getValueFor("phoneNo"));
                UserDetails.setEmailId(rd.getValueFor("email"));
                rd.endReadTransaction();

                Calendar c = Calendar.getInstance();
                specialDate.setText("Today is: "+c.get(Calendar.DATE)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.YEAR));
                int hour = c.get(Calendar.HOUR_OF_DAY);
                String greetStr = "- Good Morning -";
                if(hour>=12 && hour<=18)greetStr = "- Good Afternoon -";
                if(hour>=18 && hour<=23)greetStr = "- Good Evening -";
                greet.setText(greetStr);

                try{
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
                                doorClosed = false;

                            } catch (Exception ee) {
                            }
                        }
                    }).start();
                }catch(Exception ee){
                    Log.e("Start_Notifications",ee.getMessage());
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Intent mainIntent = new Intent(Start.this, AllTabs.class);
                        while (doorClosed);
                        Start.this.startActivity(mainIntent);
                        Start.this.finish();
                    }
                }, 4000);
            }
            else{
                Intent ii = new Intent(this,RegistrationActivity.class);
                startActivity(ii);
            }
            }catch(Exception e){
            Log.e("Start_Error",e.getMessage());
        }
    }
}
