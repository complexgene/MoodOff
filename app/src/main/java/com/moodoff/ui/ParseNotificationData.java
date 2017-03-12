package com.moodoff.ui;

/**
 * Created by Arindam on 02-10-2016.
 */

import android.util.Log;

import com.moodoff.helper.AppData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ParseNotificationData {

    public static ArrayList<String> parseAllContacts(String contactsInJson){
        ArrayList<String> allContactNumbersInServer = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(contactsInJson);
            JSONArray jsonArray = jsonRootObject.optJSONArray("allUserPhoneNumbers");
            for(int i=0;i<jsonArray.length();i++){
                allContactNumbersInServer.add(jsonArray.get(i).toString());
            }
            Log.e("ParseNot_CNTCServer",allContactNumbersInServer.toString());
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
            AppData.lovedDedicateNewCount = lovedDedicateServerCount;
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
            String name = jsonRootObject.optString("userName").toString();
            String phoneNumber = jsonRootObject.optString("userPhoneNo").toString();
            String dob = jsonRootObject.optString("userDob");
            String textStatus = jsonRootObject.optString("userTextStatus").toString();
            String audioStatusURL = jsonRootObject.optString("userAudioStatus").toString();

            allProfileData.put("userName",name);
            allProfileData.put("userPhoneNo",phoneNumber);
            allProfileData.put("userDob",dob);
            allProfileData.put("userTextStatus",textStatus);
            allProfileData.put("userAudioStatus",audioStatusURL);
            allProfileData.put("userTextStatusLoveCount","0");
            allProfileData.put("userAudioStatusLoveCount","0");

            return allProfileData;

        } catch (JSONException e) {Log.e("ParseNotification_Err",e.getMessage());e.fillInStackTrace();}
        return allProfileData;
    }
}
