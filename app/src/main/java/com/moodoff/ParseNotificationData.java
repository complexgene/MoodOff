package com.moodoff;

/**
 * Created by Arindam on 02-10-2016.
 */

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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
        String strJson = raw_json;
        ArrayList<String> allNotifications = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);

            //Get the instance of JSONArray that contains JSONObjects
            JSONObject jsonTypeObject = jsonRootObject.getJSONObject("allNotifications");
            JSONArray jsonArray = jsonTypeObject.optJSONArray("entry");
            //Iterate the jsonArray and print the info of JSONObjects
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String timeStamp = jsonObject.optString("key").toString();
                String fileWithType = jsonObject.optString("value").toString();

                //Log.e("jsonObj",jsonObject.toString());

                String[] allData = fileWithType.split("#");
                String fromUser = allData[0];
                String toUser = allData[1];
                String songName = allData[2];
                String type = allData[3];
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
            String name = jsonRootObject.optString("name").toString();
            String phoneNumber = jsonRootObject.optString("phoneNumber").toString();
            String email = jsonRootObject.optString("email").toString();
            String dob = jsonRootObject.optString("dob");
            String textStatus = jsonRootObject.optString("textStatus").toString();
            String audioStatusURL = jsonRootObject.optString("audioStatusURL").toString();
            String textStatusLoveCount = jsonRootObject.optString("textStatusLoveCount").toString();
            String audioStatusLoveCount =  jsonRootObject.optString("audioStatusLoveCount").toString();

            allProfileData.put("name",name);
            allProfileData.put("phoneNumber",phoneNumber);
            allProfileData.put("email",email);
            allProfileData.put("dob",dob);
            allProfileData.put("textStatus",textStatus);
            allProfileData.put("audioStatusURL",audioStatusURL);
            allProfileData.put("textStatusLoveCount",textStatusLoveCount);
            allProfileData.put("audioStatusLoveCount",audioStatusLoveCount);

            return allProfileData;

        } catch (JSONException e) {Log.e("ParseNotification_Err",e.getMessage());e.fillInStackTrace();}
        return allProfileData;
    }
}
