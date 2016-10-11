package com.moodoff;

/**
 * Created by Arindam on 02-10-2016.
 */

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseNotificationData {

    static ArrayList<String> getNotification(String raw_json)
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

                String id = jsonObject.optString("key").toString();
                String value = jsonObject.optString("value").toString();

                String[] eachDedication = value.split("#");
                if (eachDedication[1].equals("S")) {
                    allNotifications.add(id + " has dedicated you the song " + eachDedication[0]);
                } else {
                    allNotifications.add(id + " has dedicated you the karaoke " + eachDedication[0]);
                }
            }
            return allNotifications;
        } catch (JSONException e) {e.printStackTrace();}
        return null;
    }
}
