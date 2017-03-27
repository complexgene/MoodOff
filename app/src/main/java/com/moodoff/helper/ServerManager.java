package com.moodoff.helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.moodoff.dao.NotificationManagerDaoImpl;
import com.moodoff.dao.NotificationManagerDaoInterface;
import com.moodoff.dao.UserManagementdaoImpl;
import com.moodoff.dao.UserManagementdaoInterface;
import com.moodoff.model.User;
import com.moodoff.model.UserLiveMoodDetailsPojo;
import com.moodoff.ui.AllTabs;
import com.moodoff.ui.ContactsFragment;
import com.moodoff.ui.NotificationFragment;
import com.moodoff.ui.ParseNotificationData;
import com.moodoff.ui.Profile;
import com.moodoff.R;
import com.moodoff.ui.Start;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.moodoff.helper.AllAppData.serverURL;
import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by snaskar on 12/21/2016.
 */

public class ServerManager{
    DBHelper dbOperations;
    Context context;
    NotificationManagerDaoInterface notificationManagerDao = new NotificationManagerDaoImpl();
    UserManagementdaoInterface userManagementDao = new UserManagementdaoImpl();
    int currentNumberOfNotifications, oldNumberOfNotifications;
    User singleTonUser = User.getInstance();

    public ServerManager(){}

    public ServerManager(Context context){
        this.context = context;
        dbOperations = new DBHelper(context);
    }
    //--------------------------------CONTACTS RELATED FUNCTIONS----------------------------------------------

    public void fetchContactsFromServer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    HttpURLConnection urlConnection = null;
                    InputStreamReader isr = null;
                    @Override
                    public void run() {
                        try {
                            printMsg("ServerManager","Start reading Users from Server every 10 secs");
                            URL url = new URL(serverURL + "/userlist.json");
                            printMsg("ServerManager", url.toString());
                            urlConnection = (HttpURLConnection) url.openConnection();
                            InputStream is = urlConnection.getInputStream();
                            isr = new InputStreamReader(is);
                            int data = isr.read();
                            final StringBuilder response = new StringBuilder("");
                            while (data != -1) {
                                response.append((char) data);
                                data = isr.read();
                            }
                            printMsg("ServerManager", "Response: "+response);
                            // Reading all the registered Users
                            AllAppData.allReadContactsFromDBServer = ParseNotificationData.getAllAppUsers(response.toString());
                            ArrayList<String> allAppUsersInMyContact = new ArrayList<>();
                            //Iterate through each of the app users got from the cloud DB and check if they belong in my contacts.
                            for(String eachContactNo : AllAppData.allReadContactsFromDBServer){
                                // If the person is in my contact list and if its not my own number
                                if(AllAppData.allReadContacts.containsKey(eachContactNo) && !eachContactNo.equals(singleTonUser.getUserMobileNumber())){
                                    allAppUsersInMyContact.add(eachContactNo);
                                }
                            }
                            int currentCountOfAppUsersInMyContacts = allAppUsersInMyContact.size();
                            if(currentCountOfAppUsersInMyContacts > AllAppData.countFriendsUsingApp){
                                printMsg("ServerManager","Got some new app users.\nWill update the data containers.\n Refresh the contacts display view..");
                                for(String eachno : allAppUsersInMyContact){
                                    if(!AllAppData.friendsWhoUsesApp.contains(eachno)){
                                        AllAppData.friendsWhoUsesApp.add(eachno);
                                        AllAppData.friendsWhoDoesntUseApp.remove(eachno);
                                        printMsg("ServerManager","Added to App users and deleted from non-app users");
                                        printMsg("ServerManager", "Incase you want to see the list.\n" + AllAppData.friendsWhoUsesApp.toString());
                                    }
                                }
                                // Update the container counts now as based on counts only the above is triggered
                                AllAppData.countFriendsUsingApp = AllAppData.friendsWhoUsesApp.size();
                                AllAppData.countFriendsNotUsingApp = AllAppData.friendsWhoDoesntUseApp.size();
                                // change the status to 1 for these new users
                                DBHelper dbHelper = new DBHelper(context);
                                dbHelper.changeStatusOfUsersInContactsTable(allAppUsersInMyContact);
                                // update the contactsFragment list
                                ContactsFragment.updateViewCalled = true;
                               // Notify users that some new friends joined
                            }
                            Start.fetchContactsFromServerNotComplete = false;
                        } catch (Exception ee) {
                            printMsg("ServerManager_ERR", ee.getMessage());
                            ee.printStackTrace();
                        }
                    }
                }).start();
                fetchContactsFromServer();
            }
        },10000);
    }

    //--------------------------------CONTACTS RELATED FUNCTIONS COMPLETE----------------------------------------------



    //----------------------------- LIVE FEED FUNCTIONS-------------------------------------------------------

    public void setLiveMood(final String userMobileNumber, UserLiveMoodDetailsPojo userLiveMoodDetails){
        userManagementDao.setLiveMood(userMobileNumber, userLiveMoodDetails);
    }
    public void exitLiveMood(final String userMobileNumber){
        //userManagementDao.exitLiveMood(userMobileNumber);
    }

    //------------------------- LIVE FEED FUNCTIONS COMPLETE---------------------------------------------------




    //---------------------------SONG RELATED FUNCTIONS--------------------------------------------------------

    public void readPlayListFromServer(final Context curContext, final String todaysDate){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        // Create a child reference
        StorageReference imagesRef = storageRef.child("allsongdata.txt");
        Log.e("ServerManager",imagesRef.getPath());
        imagesRef.getBytes(AllAppData.fileSizeToRead).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < bytes.length; i++) {
                        sb.append((char) bytes[i]);
                    }
                    Log.e("ServerManager", sb.toString());
                    BufferedReader bufferedReader = new BufferedReader(new StringReader(sb.toString()));

                    int noOfMoods = Integer.parseInt(bufferedReader.readLine());
                    ArrayList<String> typeOfMoods = new ArrayList<String>(noOfMoods);
                    for (int i = 0; i < noOfMoods; i++) {
                        typeOfMoods.add(bufferedReader.readLine());
                    }

                    Log.e("Start_typeOfMoods", typeOfMoods.toString());
                    bufferedReader.readLine(); //SEPARATOR
                    String song = "";
                    ArrayList<String> allSongInAMood = new ArrayList<String>();
                    for (int i = 0; i < noOfMoods; i++) {
                        while ((song = bufferedReader.readLine()) != null) {
                            if (!song.equals("")) {
                                allSongInAMood.add(song);
                            } else {
                                break;
                            }
                        }
                        AllAppData.allMoodPlayList.put(typeOfMoods.get(i), allSongInAMood);
                        Log.e("Start_MoodAndSongs", typeOfMoods.get(i) + " " + allSongInAMood.toString());
                        allSongInAMood = new ArrayList<String>();
                    }
                    Start.moodsAndSongsFetchNotComplete = false;
                    DBHelper dbOperations = new DBHelper(curContext);
                    SQLiteDatabase writeDB = dbOperations.getWritableDatabase();
                    writeDB.execSQL("delete from playlist");
                    Log.e("Start_Playlist", "Deleted all songs from playlist");

                    HashMap<String, ArrayList<String>> allSongs = AllAppData.allMoodPlayList;
                    for (String moodType : allSongs.keySet()) {
                        ArrayList<String> songs = allSongs.get(moodType);
                        for (String eachSong : songs) {
                            String queryToFireToInsertSong = "insert into playlist values('" + todaysDate + "','" + moodType + "','" + eachSong + "','xxx','xxx')";
                            Log.e("Start_QUERYINSRT", queryToFireToInsertSong);
                            writeDB.execSQL(queryToFireToInsertSong);
                        }
                    }
                    Log.e("Start_Playlist", "Songs written to DB");
                    Log.e("Start_allmoods_Read", "AllMoods read complete..");
                } catch (Exception ee) {
                    Log.e("Start_notRd_Er", "Server not reachable i think:" + ee.getMessage());
                }
            }
        });
    }

    //---------------------------SONG RELATED FUNCTIONS COMPLETE-----------------------------------------------




    //---------------------------NOTIFICATION FUNCTIONS--------------------------------------------------------/

    public void readNotificationsFromServerAndWriteToInternalDB(){
        final String userMobileNumber = singleTonUser.getUserMobileNumber();
        final String serverURL = AllAppData.serverURL;
                new Thread(new Runnable() {
                    HttpURLConnection urlConnection = null;
                    InputStreamReader isr = null;
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(serverURL + "/allNotifications/" + userMobileNumber + ".json");
                            Log.e("ServerManager", "readNotificationsFromServerAndWriteToInternalDB(): " + url.toString());
                            urlConnection = (HttpURLConnection) url.openConnection();
                            InputStream is = urlConnection.getInputStream();
                            isr = new InputStreamReader(is);
                            int data = isr.read();
                            final StringBuilder response = new StringBuilder("");
                            while (data != -1) {
                                response.append((char) data);
                                data = isr.read();
                            }
                            Log.e("ServerManager", "readNotificationsFromServerAndWriteToInternalDB():" + response.toString());
                            ArrayList<String> allYourNotificationFromServer = ParseNotificationData.getNotification(response.toString());

                            currentNumberOfNotifications = allYourNotificationFromServer.size();
                            oldNumberOfNotifications = AllAppData.totalNoOfNot;
                            //------TRUE : We got some new notifications-----------------------------------------//
                            if ((currentNumberOfNotifications > oldNumberOfNotifications)) {
                                //-------Delete all notifications from Internal DB----------------------------//
                                printMsg("ServerManager","readNotificationsFromServerAndWriteToInternalDB: delete all notifications from Internal DB");
                                dbOperations.deleteAllDataFromNotificationTableFromInternalDB();
                                //-------Write all the read notifications from cloud to Internal DB---------------//
                                printMsg("ServerManager","readNotificationsFromServerAndWriteToInternalDB: write all notifications got from cloud to Internal DB");
                                dbOperations.writeNewNotificationsToInternalDB(allYourNotificationFromServer);
                                //-----------------Update the variables that holds info about notifications---------------//
                                printMsg("ServerManager","readNotificationsFromServerAndWriteToInternalDB: read all notifications from Internal DB");
                                AllAppData.allNotifications = dbOperations.readNotificationsFromInternalDB();
                                printMsg("ServerManager","readNotificationsFromServerAndWriteToInternalDB: read all notifications from Internal DB is DONE");
                                AllAppData.totalNoOfNot = currentNumberOfNotifications;
                                //---------------------Display notifications to User for new Notifications--------------------//
                                displayAlertNotificationOnTopBarOfPhone(context, (currentNumberOfNotifications - oldNumberOfNotifications));

                            }
                        } catch (Exception ee) {
                            Log.e("ServerManager_Not_Err1", "readNotificationsFromServerAndWriteToInternalDB():" + ee.getMessage() + ee.fillInStackTrace().toString());
                            ee.printStackTrace();
                            ee.fillInStackTrace();
                        }
                    }
                }).start();
    }
    public boolean writeSongDedicateToCloudDB(String ts, String fromUser, final String toUser, String currentMood, String currentSong, String type){
        boolean writeToCloudDBIsSuccessful = notificationManagerDao.writeSongDedicateToCloudDB(ts, fromUser, toUser, currentMood, currentSong, type);
        if(writeToCloudDBIsSuccessful){
            return true;
        }
        return false;
    }

    //---------------------------NOTIFICATION FUNCTIONS ENDS--------------------------------------------------------/

    public void displayAlertNotificationOnTopBarOfPhone(final Context context, final int diff){
        // Getting number of last unseen notifications from file and add the current unseen to get total unseen
        printMsg("ServerManager","displayAlertNotificationOnTopBarOfPhone: came for alert display on top bar of phone");
        StoreRetrieveDataInterface fileOpr = new StoreRetrieveDataImpl("UserData.txt");
        fileOpr.beginReadTransaction();
        int lastNumberOfUnseenNotifications = Integer.parseInt(fileOpr.getValueFor(AllAppData.userNumberOfOldNotifications));
        fileOpr.endReadTransaction();
        printMsg("ServerManager","displayAlertNotificationOnTopBarOfPhone: 1came for alert display on top bar of phone");
        final int currentNumberOfUnseenNotifications = lastNumberOfUnseenNotifications+diff;
        fileOpr.beginWriteTransaction();
        fileOpr.updateValueFor(AllAppData.userNumberOfOldNotifications,String.valueOf(currentNumberOfUnseenNotifications));
        fileOpr.endWriteTransaction();
        printMsg("ServerManager","displayAlertNotificationOnTopBarOfPhone: 2came for alert display on top bar of phone");
        // Read Complete and Operation Done---------------------------------------
        // Use Plural english in case no of notifications is more than 1(ONE)---------------------------
        String notificationTextSingularPlural="notification";
        if(currentNumberOfUnseenNotifications>1)notificationTextSingularPlural="notifications";

        final Activity currActivity = (Activity)context;
        final NotificationCompat.Builder builder =
            new NotificationCompat.Builder(currActivity)
                    .setSmallIcon(R.drawable.btn_dedicate)
                    .setColor(001500)
                    .setContentTitle("MoodOff")
                    .setWhen(System.currentTimeMillis())
                    .setTicker(singleTonUser.getUserName().split("_")[0]+ "!! "+currentNumberOfUnseenNotifications+" new "+notificationTextSingularPlural+"!!")
                    .setContentText(singleTonUser.getUserName().split("_")[0]+ "!! "+currentNumberOfUnseenNotifications+" unseen "+notificationTextSingularPlural+"!!");

        final Intent notificationIntent = new Intent(currActivity, AllTabs.class);

        if(currActivity==null){
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        else{
            currActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isAppForground(context)) {
                        Log.e("ServerManager","App is running in foreground..");
                        if(AllTabs.mViewPager.getCurrentItem()!=1) {
                            AllTabs.tabNames.clear();
                            AllTabs.tabNames.add("MOODS");
                            AllTabs.tabNames.add("ACTIVITY["+currentNumberOfUnseenNotifications+"]");
                            AllTabs.tabNames.add("PROFILES");
                        }
                        ViewPager mViewPager = AllTabs.mViewPager;
                        mViewPager.getAdapter().notifyDataSetChanged();
                        Toast.makeText(context, singleTonUser.getUserName() + "! You got new notifications!!",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Log.e("ServerManager","App is running in background..");
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
        //--------------This would trigger a child change in cloud for async listener-----------------------
        userManagementDao.setRebuildNotificationPanelNodeInCloud(singleTonUser.getUserMobileNumber());
        //-------------Play Notification sound-------------------------------------------------------------
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(currActivity, notification);
        r.play();
    }
    public boolean voteLove(final String urlAPI, final Activity curActivity, final ImageButton loveButton) {
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    Log.e("ServerManager_Not","Start loving the notification");
                    URL url = new URL(serverURL+ "/notifications/" + urlAPI);
                    Log.e("ServerManager_ReadURL", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    int responseCode = urlConnection.getResponseCode();
                    if(responseCode==200){
                        curActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(curActivity.getApplicationContext(),"You loved a dedicate",Toast.LENGTH_SHORT).show();
                                loveButton.setImageResource(R.drawable.like_s);
                            }
                        });
                    }
                    else{
                        curActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(curActivity.getApplicationContext(),"Sorry!! Please try after sometime!!",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    Log.e("ServerManager_Not_Read", "Notification read complete from server");
                } catch (Exception ee) {
                    Log.e("ServerManager_Not_Err2", ee.getMessage());
                    ee.printStackTrace();
                }
            }
        }).start();
        return false;
    }

    private String getStoryName(){return "story"+new Random().nextInt(16)+".txt";}
    private void writeTheStoryIntoFile(String storyTitle, String storyBody){
        try{
            File f = new File(AllAppData.getAppDirectoryPath()+"/story"+ AllAppData.getTodaysDate()+".txt");
            Log.e("ServerManager_READSTORY",f.getAbsolutePath().toString());

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(storyTitle+"\n"+storyBody);
            bw.close();
        }
        catch(Exception ee){
            Log.e("ServerMan_StoryWrtErr","Story File Write Error:"+ee.getMessage());
        }
    }
    public void loadStory(final String currentMood, final Activity curActivity, final TextView storyTitleTV, final TextView storyBodyTV, final ProgressBar storyLoadSpinner){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    HttpURLConnection urlConnection = null;
                    BufferedReader bufferedReader = null;
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(AllAppData.serverStoriesURL + "/allstories/" + getStoryName());
                            Log.e("ServerMan_STORY_Url", url.toString());
                            urlConnection = (HttpURLConnection) url.openConnection();
                            InputStream is = urlConnection.getInputStream();
                            InputStreamReader isr = new InputStreamReader(is);
                            bufferedReader = new BufferedReader(isr);
                            final StringBuilder storyBody = new StringBuilder("");
                            String body="";
                            final String title=bufferedReader.readLine();
                            while ((body = bufferedReader.readLine()) != null) {
                                storyBody.append(body);
                            }
                            writeTheStoryIntoFile(title,storyBody.toString());
                            Log.e("ServerMan_STORY","Story file written..");
                            curActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    storyTitleTV.setText(title);
                                    storyBodyTV.setText(storyBody.toString());
                                    storyLoadSpinner.setVisibility(View.GONE);
                                }
                            });

                        } catch (Exception ee) {
                            Log.e("GenericM_StoryReadErr", ee.getMessage());
                        } finally {
                            try {
                                bufferedReader.close();
                            } catch (Exception ee) {
                                Log.e("GenericM_Err", "BufferedReader couldn't be closed");
                            }
                            urlConnection.disconnect();

                        }
                    }
                }).start();
            }
        },0);
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

    public void loveTextStatus(final String userMobileNumber, String currentUserMobileNumber, Activity profileActivity){
        userManagementDao.voteForTextStatus(userMobileNumber, currentUserMobileNumber, profileActivity);
    }
    public void loveAudioStatus(final String userMobileNumber, String currentUserMobileNumber, Activity profileActivity){
        userManagementDao.voteForAudioStatus(userMobileNumber, currentUserMobileNumber, profileActivity);
    }
    public void likeCurrentMood(final String userMobileNumber, String currentUserMobileNumber, Activity profileActivity){
        userManagementDao.likeCurrentMood(userMobileNumber, currentUserMobileNumber, profileActivity);
    }
    public void loveCurrentMood(final String userMobileNumber, String currentUserMobileNumber, Activity profileActivity){
        userManagementDao.loveCurrentMood(userMobileNumber, currentUserMobileNumber, profileActivity);
    }
    public void sadCurrentMood(final String userMobileNumber, String currentUserMobileNumber, Activity profileActivity){
        userManagementDao.sadCurrentMood(userMobileNumber, currentUserMobileNumber, profileActivity);
    }

    public void writeTextStatusChange(String userMobileNumber, String newTextStatus, Activity curActivity, TextView userTextStatus){
        userManagementDao.writeTextStatusChange(userMobileNumber, newTextStatus, curActivity, userTextStatus);
    }
    public void writeAudioStatusChange(String userMobileNumber, String newTextStatus, Activity curActivity, TextView userTextStatus){
        userManagementDao.writeAudioStatusChange(userMobileNumber, newTextStatus, curActivity, userTextStatus);
    }
}
