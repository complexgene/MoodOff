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

                String fromUser = jsonObject.optString("key").toString();
                String fileWithType = jsonObject.optString("value").toString();

                Log.e("jsonObj",jsonObject.toString());

                for(String eachDedicate : fileWithType.split(" ")){
                    String[] fileNameAndType = eachDedicate.split("#");
                    if (fileNameAndType[1].equals("S")) {
                        allNotifications.add(fromUser + "Type: Song " + fileNameAndType[0]);
                    } else {
                        allNotifications.add(fromUser + "Type: Karaoke " + fileNameAndType[0]);
                    }
                }
            }
            Log.e("seeit",allNotifications.toString());
            return allNotifications;
        } catch (JSONException e) {e.printStackTrace();}
        return null;
    }
}
