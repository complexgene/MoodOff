package com.moodoff.ui;

/**
 * Created by Arindam on 02-10-2016.
 */

import android.util.Log;

import com.moodoff.helper.AllAppData;
import com.moodoff.helper.LoggerBaba;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.moodoff.helper.LoggerBaba.printMsg;

public class ParseNotificationData {

    public static ArrayList<String> getAllAppUsers(String contactsInJson){
        ArrayList<String> allContactNumbersInServer = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(contactsInJson);
            Iterator<String> keys = jsonRootObject.keys();
            while(keys.hasNext()) {
                allContactNumbersInServer.add(keys.next());
            }
            printMsg("ParseNotificationData",allContactNumbersInServer.toString());
        }
        catch(Exception ee){
            ee.printStackTrace();
        }
        return allContactNumbersInServer;
    }

    public static ArrayList<String> getNotification(String raw_json)
    {
        int lovedDedicateServerCount = 0;
        String strJson = raw_json;
        ArrayList<String> allNotifications = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            Iterator<String> allTS = jsonRootObject.keys();
            int sizeOfNots = jsonRootObject.length();
            Log.e("ParseNotifications", "No of notifications from server:"+sizeOfNots);
            while(allTS.hasNext()){
                String[] allData = jsonRootObject.get(allTS.next()).toString().split("#");
                String fromUser = allData[0];
                String toUser = allData[1];
                String songName = allData[2];
                String type = allData[3];
                if(type.equals("5"))lovedDedicateServerCount++;
                String ts = allData[4];

                allNotifications.add(fromUser+" "+toUser+" "+ts+" "+type+" "+songName);
            }
            Log.e("ParseNotification",allNotifications.toString());
            return allNotifications;
        } catch (JSONException e) {e.printStackTrace();}
        return null;
    }
    static HashMap<String,String> parseAndGetProfileData(String raw_json){
        HashMap<String,String> allProfileData = new HashMap<>();
        Log.e("Profile_jsonObj",raw_json);
        String strJson = raw_json;

        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            String userName = jsonRootObject.optString(AllAppData.userName).toString();
            String userMobileNumber = jsonRootObject.optString(AllAppData.userMobileNumber).toString();
            String userDateOfBirth = jsonRootObject.optString(AllAppData.userDateOfBirth);
            String userTextStatus = jsonRootObject.optString(AllAppData.userTextStatus).toString();
            String userAudioStatus = jsonRootObject.optString(AllAppData.userAudioStatus).toString();

            allProfileData.put(AllAppData.userName,userName);
            allProfileData.put(AllAppData.userMobileNumber,userMobileNumber);
            allProfileData.put(AllAppData.userDateOfBirth,userDateOfBirth);
            allProfileData.put(AllAppData.userTextStatus,userTextStatus);
            allProfileData.put(AllAppData.userAudioStatus,userAudioStatus);
            allProfileData.put(AllAppData.userTextStatusLoveCount,"0");
            allProfileData.put(AllAppData.userAudioStatusLoveCount,"0");

            printMsg("ParseNotificationData", "Opened User:" + allProfileData.toString());

            return allProfileData;

        } catch (JSONException e) {Log.e("ParseNotification_Err",e.getMessage());e.fillInStackTrace();}
        return allProfileData;
    }
}
