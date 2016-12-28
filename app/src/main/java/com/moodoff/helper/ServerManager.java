package com.moodoff.helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

import com.moodoff.AllTabs;
import com.moodoff.NotificationFragment;
import com.moodoff.ParseNotificationData;
import com.moodoff.R;
import com.moodoff.Start;
import com.moodoff.model.UserDetails;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by snaskar on 12/21/2016.
 */

public class ServerManager{
    DBHelper dbOperations;
    Context context;
    int currentNumberOfNotifications, oldNumberOfNotifications;

    public ServerManager(){}

    public ServerManager(Context context){
        this.context = context;
        dbOperations = new DBHelper(context);
    }

    public void readPlayListFromServer(){
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
                    urlConnection.setConnectTimeout(3000);
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
                        AppData.allMoodPlayList.put(typeOfMoods.get(i),allSongInAMood);
                        Log.e("Start_MoodAndSongs",typeOfMoods.get(i)+" "+allSongInAMood.toString());
                        allSongInAMood = new ArrayList<String>();
                    }
                    Start.moodsAndSongsFetchNotComplete = false;
                    Log.e("Start_allmoods_Read", "AllMoods read complete..");
                } catch (Exception ee) {
                    Log.e("Start_notRd_Err", "Server not reachable i think:"+ee.getMessage());
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

    public void readNotificationsFromServerAndWriteToInternalDB(){
        final String userMobileNumber = UserDetails.getPhoneNumber();
        final String serverURL = HttpGetPostInterface.serverURL;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    HttpURLConnection urlConnection = null;
                    InputStreamReader isr = null;
                    @Override
                    public void run() {
                        try {
                            //Log.e("ServerManager_Not","Start reading notifications from Server");
                            URL url = new URL(serverURL+ "/notifications/" + userMobileNumber);
                            Log.e("ServerManager_ReadURL", url.toString());
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
                            currentNumberOfNotifications = allYourNotificationFromServer.size();
                            oldNumberOfNotifications = AppData.totalNoOfNot;
                            if(currentNumberOfNotifications>oldNumberOfNotifications){
                                dbOperations.deleteAllDataFromNotificationTableFromInternalDB();
                                dbOperations.writeNewNotificationsToInternalDB(allYourNotificationFromServer);
                                //AppData.allNotifications = allYourNotificationFromServer;
                                AppData.allNotifications = dbOperations.readNotificationsFromInternalDB();
                                AppData.totalNoOfNot = currentNumberOfNotifications;
                                Log.e("ServerManager_allNot","Some new notifications written to DB..");
                                //Log.e("ServerManager_allNot",allYourNotificationFromServer.toString());

                                // Display the notification alert
                                displayAlertNotificationOnTopBarOfPhone(context);

                            }
                            else{
                                Log.e("ServerManager_allNot","No new Notifications..");
                            }

                            //Log.e("ServerManager_Not_Read", "Notification read complete from server");
                        } catch (Exception ee) {
                            Log.e("ServerManager_Not_RdErr", ee.getMessage());
                            ee.printStackTrace();
                        }
                    }
                }).start();
                readNotificationsFromServerAndWriteToInternalDB();
            }
        },7000);
    }
    private void displayAlertNotificationOnTopBarOfPhone(final Context context){
        final Activity currActivity = (Activity)context;
        final NotificationCompat.Builder builder =
            new NotificationCompat.Builder(currActivity)
                    .setSmallIcon(R.drawable.btn_dedicate)
                    .setColor(001500)
                    .setContentTitle("MoodOff")
                    .setContentText(UserDetails.getUserName()+ "!! You got new notifications!!");

        final Intent notificationIntent = new Intent(currActivity, Start.class);

        if(currActivity==null){
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        else{
            currActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isAppForground(context)) {
                        Log.e("SM","Here yoooooo foreground");
                        if(AllTabs.mViewPager.getCurrentItem()!=1) {
                            AllTabs.tabNames.clear();
                            AllTabs.tabNames.add("MOODS");
                            AllTabs.tabNames.add("ACTIVITY*");
                            AllTabs.tabNames.add("PROFILES");
                        }
                        ViewPager mViewPager = AllTabs.mViewPager;
                        mViewPager.getAdapter().notifyDataSetChanged();
                        NotificationFragment.changeDetected = true;
                        Toast.makeText(context,"Hey! You got new notifications!!",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Log.e("SM","Here yoooooo");
                        Start.switchToTab = 1;
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent contentIntent = PendingIntent.getActivity(currActivity, 0, notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
                        builder.setContentIntent(contentIntent);
                        builder.setAutoCancel(true);

                        // Add as notification
                        NotificationManager manager = (NotificationManager) currActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                        manager.notify(0, builder.build());

                    }
                }
            });

        }
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(currActivity, notification);
        r.play();


    }
    public boolean isAppForground(Context mContext) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(mContext.getPackageName())) {
                return false;
            }
        }
        return true;
    }
    public boolean voteLove(String fromUser, String toUser, String ts)
    {
        /*new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    Log.e("ServerManager_Not","Start loving the notification");
                    URL url = new URL(serverURL+ "/notifications/" + userMobileNumber);
                    Log.e("ServerManager_ReadURL", url.toString());
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
                    int currentNumberOfNotifications = allYourNotificationFromServer.size();
                    int oldNumberOfNotifications = AppData.totalNoOfNot;
                    if(currentNumberOfNotifications>oldNumberOfNotifications){
                        dbOperations.deleteAllDataFromNotificationTableFromInternalDB();
                        dbOperations.writeNewNotificationsToInternalDB(allYourNotificationFromServer);
                        //AppData.allNotifications = allYourNotificationFromServer;
                        AppData.allNotifications = dbOperations.readNotificationsFromInternalDB();
                        AppData.totalNoOfNot = currentNumberOfNotifications;
                        Log.e("ServerManager_allNot","Some new notifications written to DB..");
                        //Log.e("ServerManager_allNot",allYourNotificationFromServer.toString());

                        // Display the notification alert
                        displayAlertNotificationOnTopBarOfPhone(context);

                    }
                    else{
                        Log.e("ServerManager_allNot","No new Notifications..");
                    }

                    Log.e("ServerManager_Not_Read", "Notification read complete from server");
                } catch (Exception ee) {
                    Log.e("ServerManager_Not_RdErr", ee.getMessage());
                    ee.printStackTrace();
                }
            }
        }).start();*/
        return false;
    }
}
