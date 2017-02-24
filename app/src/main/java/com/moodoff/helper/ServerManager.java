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

import com.moodoff.ui.AllTabs;
import com.moodoff.ui.ContactsFragment;
import com.moodoff.ui.NotificationFragment;
import com.moodoff.ui.ParseNotificationData;
import com.moodoff.ui.Profile;
import com.moodoff.R;
import com.moodoff.ui.Start;
import com.moodoff.model.UserDetails;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.moodoff.helper.HttpGetPostInterface.serverURL;

/**
 *
 * Created by snaskar on 12/21/2016.
 *
 */

public class ServerManager{
    DBHelper dbOperations;
    Context context;
    int currentNumberOfNotifications, oldNumberOfNotifications;
    UserDetails userData = UserDetails.getInstance();

    public ServerManager(){}

    public ServerManager(Context context){
        this.context = context;
        dbOperations = new DBHelper(context);
    }
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
                            Log.e("ServerMan_AppUsers","Start reading Users from Server every 13 secs");
                            URL url = new URL(serverURL + "/users/all");
                            Log.e("ServerMan_AppUsers_URL", url.toString());
                            urlConnection = (HttpURLConnection) url.openConnection();
                            InputStream is = urlConnection.getInputStream();
                            isr = new InputStreamReader(is);
                            int data = isr.read();
                            final StringBuilder response = new StringBuilder("");
                            while (data != -1) {
                                response.append((char) data);
                                data = isr.read();
                            }
                            // Reading all the registered Users
                            ContactsManager.allReadContactsFromDBServer = ParseNotificationData.parseAllContacts(response.toString());
                            ArrayList<String> contactNumbers = new ArrayList<>(),contactsToBeRemoved = new ArrayList<>();
                            //Iterate through each of them
                            for(String eachContactNo : ContactsManager.allReadContactsFromDBServer){
                                // If the person is in my contact list and if its not my own number
                                if(ContactsManager.allReadContacts.containsKey(eachContactNo) && !eachContactNo.equals(userData.getUserMobileNumber())){
                                    contactNumbers.add(eachContactNo);
                                }
                            }
                            int currentCountOfAppUsersInMyContacts = contactNumbers.size();
                            if(currentCountOfAppUsersInMyContacts>ContactsManager.countFriendsUsingApp){
                                Log.e("ServerMan_AppUsers","Got some new app users\nUpdate the data containers\n Refresh the contacts display view..");
                                for(String eachno : contactNumbers){
                                    if(!ContactsManager.friendsWhoUsesApp.contains(eachno)){
                                        ContactsManager.friendsWhoUsesApp.add(eachno);
                                        ContactsManager.friendsWhoDoesntUseApp.remove(eachno);
                                        Log.e("ServerMan_AppUsers_New","Added to users and deleted from non-users");
                                        Log.e("ServerMan_AppUsers_New",ContactsManager.friendsWhoUsesApp.toString());
                                    }
                                }
                                // Update the container counts now as based on counts only the above is triggered
                                ContactsManager.countFriendsUsingApp = ContactsManager.friendsWhoUsesApp.size();
                                ContactsManager.countFriendsNotUsingApp = ContactsManager.friendsWhoDoesntUseApp.size();
                                // change the status to 1 for these new users
                                DBHelper dbHelper = new DBHelper(context);
                                dbHelper.changeStatusOfUsersInContactsTable(contactNumbers);
                                // update the contactsFragment list
                                ContactsFragment.updateViewCalled = true;
                               // Notify users that some new friends joined

                            }
                            Start.fetchContactsFromServerNotComplete = false;
                        } catch (Exception ee) {
                            Log.e("ServerManager_Not_Err0", ee.getMessage());
                            ee.printStackTrace();
                        }
                    }
                }).start();
                fetchContactsFromServer();
            }
        },13000);
    }

    // LIVE FEED FUNCTIONS
    String liveMood = "";
    public String getLiveMood(final String userNumber){
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    Log.e("ServerManager_LiveMood","Getting the live mood for the user:"+userNumber);
                    URL url = new URL(serverURL+ "/livefeed/" + userNumber );
                    Log.e("ServerManager_LiveURL", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream is = urlConnection.getInputStream();
                    isr = new InputStreamReader(is);
                    int data = isr.read();
                    final StringBuilder response = new StringBuilder("");
                    while (data != -1) {
                        response.append((char) data);
                        data = isr.read();
                    }
                    Log.e("ServerManager_LiveMood", "Getting done for live mood for the user:"+userNumber+" res:"+response.toString());
                    liveMood =  response.toString();
                    Profile.currentMood = liveMood;
                    Profile.profileDetailsNotRetrievedYet = false;
                } catch (Exception ee) {
                    Log.e("ServerManager_LIVE_Err1", ee.getMessage());
                    ee.printStackTrace();
                }
            }
        }).start();
        return liveMood;
    }
    public void setLiveMood(final String userNumber, final String moodName){
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    Log.e("ServerManager_LiveMood","Setting the live mood for the user:"+userNumber+" as:"+moodName);
                    URL url = new URL(serverURL+ "/livefeed/" + userNumber +"/"+ moodName);
                    Log.e("ServerManager_LiveURL", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    int responseCode = urlConnection.getResponseCode();
                    if(responseCode!=200){
                        throw new Exception("Live Mood couldn't be saved..");
                    }
                    Log.e("ServerManager_LiveMood", "Setting done for live mood for the user:"+userNumber);
                } catch (Exception ee) {
                    Log.e("ServerManager_LIVE_Err2", ee.getMessage());
                    ee.printStackTrace();
                }
            }
        }).start();
    }
    public String exitLiveMood(final String userNumber){
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    Log.e("ServerManager_LiveMood","Exiting the live mood for the user:"+userNumber);
                    URL url = new URL(serverURL+ "/livefeed/exit/" + userNumber );
                    Log.e("ServerManager_LiveURL", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    int responseCode = urlConnection.getResponseCode();
                    if(responseCode!=200){
                        throw new Exception("Live Mood exit couldn't be done..");
                    }
                    Log.e("ServerManager_LiveMood", "Exiting done for live mood for the user:"+userNumber);
                } catch (Exception ee) {
                    Log.e("ServerManager_LIVE_Err1", ee.getMessage());
                    ee.printStackTrace();
                }
            }
        }).start();
        return liveMood;
    }
    public void voteForLiveMood(final String userNumber, final int type){
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    Log.e("ServerManager_LiveMood","Voting for live mood for the user:"+userNumber+" with type:"+type);
                    URL url = new URL(serverURL+ "/livefeed/vote/" + userNumber +"/"+ type);
                    Log.e("ServerManager_LiveURL", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    int responseCode = urlConnection.getResponseCode();
                    if(responseCode!=200){
                        throw new Exception("Vote for Live Mood couldn't be done..");
                    }
                    Log.e("ServerManager_LiveMood", "Voting done for live mood for the user:"+userNumber);
                } catch (Exception ee) {
                    Log.e("ServerManager_LIVE_Err3", ee.getMessage());
                    ee.printStackTrace();
                }
            }
        }).start();
    }
    // LIVE FEED FUNCTIONS COMPLETE

    public void readPlayListFromServer(final Context curContext, final String todaysDate){
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
                    DBHelper dbOperations = new DBHelper(curContext);
                    SQLiteDatabase writeDB = dbOperations.getWritableDatabase();
                    writeDB.execSQL("delete from playlist");
                    Log.e("Start_Playlist","Deleted all songs from playlist");

                    HashMap<String,ArrayList<String>> allSongs = AppData.allMoodPlayList;
                    for(String moodType : allSongs.keySet())
                    {
                        ArrayList<String> songs = allSongs.get(moodType);
                        for(String eachSong : songs) {
                            String queryToFireToInsertSong = "insert into playlist values('" + todaysDate + "','"+ moodType +"','"+ eachSong +"','xxx','xxx')";
                            Log.e("Start_QUERYINSRT",queryToFireToInsertSong);
                            writeDB.execSQL(queryToFireToInsertSong);
                        }
                    }
                    Log.e("Start_Playlist","Songs written to DB");
                    Log.e("Start_allmoods_Read", "AllMoods read complete..");
                } catch (Exception ee) {
                    Log.e("Start_notRd_Err", "Server not reachable i think:"+ee.getMessage());
                } finally {
                    try {
                        bufferedReader.close();
                    } catch (Exception ee) {
                        Log.e("ServerManager_ReadErr1", "BufferedReader couldn't be closed");
                    }
                    urlConnection.disconnect();
                }
            }
        }).start();
    }
    public void readAllProfileDataFromServerAndWriteToInternalDB(){
        ArrayList<String> allContactsOfUser = ContactsManager.friendsWhoUsesApp;
        Log.e("ServerManager_FWUA",allContactsOfUser.toString());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /*for(String eachUser : allContactsOfUser){

                        }*/
                    }
                }).start();
                //readAllProfileDataFromServerAndWriteToInternalDB();
            }
        },8000);
    }
    public void readNotificationsFromServerAndWriteToInternalDB(){
        final String userMobileNumber = userData.getUserMobileNumber();
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
                            //Log.e("ServerManager_ReadURL", url.toString());
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
                            if((currentNumberOfNotifications>oldNumberOfNotifications)||(AppData.lovedDedicateNewCount>AppData.lovedDedicateOldCount)){
                                dbOperations.deleteAllDataFromNotificationTableFromInternalDB();
                                //stopWriteToReadTableCopyScript();
                                dbOperations.writeNewNotificationsToInternalDB(allYourNotificationFromServer);
                                //deleteAllNotificationsFromServerReadtable(UserMobileNumber);
                                //startWriteToReadTableCopyScript();
                                //AppData.allNotifications = allYourNotificationFromServer;
                                AppData.allNotifications = dbOperations.readNotificationsFromInternalDB();
                                AppData.totalNoOfNot = currentNumberOfNotifications;
                                //Log.e("ServerManager_allNot","Some new notifications written to DB..");
                                //Log.e("ServerManager_allNot",allYourNotificationFromServer.toString());

                                // Display the notification alert
                                if(currentNumberOfNotifications>oldNumberOfNotifications)
                                    displayAlertNotificationOnTopBarOfPhone(context,(currentNumberOfNotifications-oldNumberOfNotifications));
                                else
                                    NotificationFragment.changeDetected = true;

                                /*else
                                    displayAlertNotificationOnTopBarOfPhone(context,(AppData.lovedDedicateNewCount-AppData.lovedDedicateOldCount));*/
                            }
                            else{
                                //Log.e("ServerManager_allNot","No new Notifications..");
                            }
                            //Log.e("ServerManager_Not_Read", "Notification read complete from server");
                        } catch (Exception ee) {
                            Log.e("ServerManager_Not_Err1", ee.getMessage());
                            ee.printStackTrace();
                        }
                    }
                }).start();
                readNotificationsFromServerAndWriteToInternalDB();
            }
        },10000);
    }
    private void displayAlertNotificationOnTopBarOfPhone(final Context context, final int diff){
        // Getting the number of last unseen notifications from Userdata file
        StoreRetrieveDataInterface fileOpr = new StoreRetrieveDataImpl("UserData.txt");
        fileOpr.beginReadTransaction();
        int lastNumberOfUnseenNotifications = Integer.parseInt(fileOpr.getValueFor("numberOfOldNotifications"));
        fileOpr.endReadTransaction();
        final int currentNumberOfUnseenNotifications = lastNumberOfUnseenNotifications+diff;
        fileOpr.beginWriteTransaction();
        fileOpr.updateValueFor("numberOfOldNotifications",String.valueOf(currentNumberOfUnseenNotifications));
        fileOpr.endWriteTransaction();
        // Read Complete

        final Activity currActivity = (Activity)context;
        String notificationTextSingularPlural="notification";
        if(currentNumberOfUnseenNotifications>1)notificationTextSingularPlural="notifications";
        final NotificationCompat.Builder builder =
            new NotificationCompat.Builder(currActivity)
                    .setSmallIcon(R.drawable.btn_dedicate)
                    .setColor(001500)
                    .setContentTitle("MoodOff")
                    .setWhen(System.currentTimeMillis())
                    .setTicker(userData.getUserName().split("_")[0]+ "!! "+currentNumberOfUnseenNotifications+" new "+notificationTextSingularPlural+"!!")
                    .setContentText(userData.getUserName().split("_")[0]+ "!! "+currentNumberOfUnseenNotifications+" unseen "+notificationTextSingularPlural+"!!");

        final Intent notificationIntent = new Intent(currActivity, AllTabs.class);

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
                            AllTabs.tabNames.add("ACTIVITY["+currentNumberOfUnseenNotifications+"]");
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
    public void writeStatusChange(final int type, final String newValue, final Activity curActivity, final TextView userTextStatus){
        // type:0 for TEXT ,,,, type:1 for AUDIO
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    HttpURLConnection urlConnection = null;
                    InputStreamReader isr = null;
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(serverURL+"/users/update/" + type + "/" + userData.getUserMobileNumber() + "/" + newValue.replaceAll(" ","_"));
                            Log.e("ServerM_ASModf_url", url.toString());
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setDoOutput(true);
                            InputStream is = urlConnection.getInputStream();
                            isr = new InputStreamReader(is);
                            int data = isr.read();
                            final StringBuilder response = new StringBuilder("");
                            while (data != -1) {
                                response.append((char) data);
                                data = isr.read();
                            }
                            Log.e("ServerM_ASModf_RES",response.toString());
                            curActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(response.toString().equals("true")){
                                        if(type==0)
                                            userTextStatus.setText(newValue);
                                    }
                                }
                            });

                        } catch (Exception ee) {
                            Log.e("ServerM_ASModf_Err", ee.getMessage());
                        } finally {
                            try {
                                isr.close();
                            } catch (Exception ee) {
                                Log.e("ServerM_ASModf_Err", "BufferedReader couldn't be closed");
                            }
                            urlConnection.disconnect();

                        }
                    }
                }).start();
            }
        },0);
    }
    private String getStoryName(String moodType){return "story"+new Random().nextInt(16)+".txt";}
    private void writeTheStoryIntoFile(String storyTitle, String storyBody){
        try{
            File f = new File(AppData.getAppDirectoryPath()+"/story"+AppData.getTodaysDate()+".txt");
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
                            URL url = new URL(HttpGetPostInterface.serverStoriesURL + "/allstories/" + getStoryName(currentMood));
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
    public void loadQuote(final String currentMood, final Activity curActivity, final TextView storyTitleTV, final TextView storyBodyTV, final ProgressBar storyLoadSpinner){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    HttpURLConnection urlConnection = null;
                    BufferedReader bufferedReader = null;
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(HttpGetPostInterface.serverStoriesURL + "/allquotes/" + getStoryName(currentMood));
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
    public void loveTextStatus(final String user, final TextView txtViewToChange, final Activity curActivity){
    /*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    HttpURLConnection urlConnection = null;
                    InputStreamReader isr = null;
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://192.168.2.4:5789/controller/moodoff/users/update/" + type + "/" + UserDetails.getPhoneNumber() + "/" + newValue.replaceAll(" ","_"));
                            Log.e("ServerM_ASModf_url", url.toString());
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setDoOutput(true);
                            InputStream is = urlConnection.getInputStream();
                            isr = new InputStreamReader(is);
                            int data = isr.read();
                            final StringBuilder response = new StringBuilder("");
                            while (data != -1) {
                                response.append((char) data);
                                data = isr.read();
                            }
                            Log.e("ServerM_ASModf_RES",response.toString());
                            curActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(response.toString().equals("true")){
                                        int curValue = Integer.parseInt(txtViewToChange.getText().toString());
                                        txtViewToChange.setText((curValue+1)+" people loved the status");
                                    }
                                }
                            });

                        } catch (Exception ee) {
                            Log.e("ServerM_ASModf_Err", ee.getMessage());
                        } finally {
                            try {
                                isr.close();
                            } catch (Exception ee) {
                                Log.e("ServerM_ASModf_Err", "BufferedReader couldn't be closed");
                            }
                            urlConnection.disconnect();
                        }
                    }
                }).start();
            }
        },0);
    */
    }
}
