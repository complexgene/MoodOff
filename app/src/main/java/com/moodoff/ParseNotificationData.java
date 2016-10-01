package com.moodoff;

/**
 * Created by Arindam on 02-10-2016.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseNotificationData {

    static ArrayList<String> getNotification(String raw_json)
    {
        String strJson = raw_json;
        ArrayList<String> data = new ArrayList<>();
        try {
            JSONObject  jsonRootObject = new JSONObject(strJson);

            //Get the instance of JSONArray that contains JSONObjects

            JSONObject jsonTypeObject = jsonRootObject.getJSONObject("allNotifications");
            JSONArray jsonArray = jsonTypeObject.optJSONArray("entry");
            //Iterate the jsonArray and print the info of JSONObjects
            for(int i=0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String id = jsonObject.optString("key").toString();
                String value = jsonObject.optString("value").toString();

                for(String eachDedicatedSong:value.split(" ")){
                    String[] songAndType=eachDedicatedSong.split("#");
                    if(songAndType[1].equals("S")){
                        data.add(id+" has dedicated you the song "+songAndType[0]);
                    }
                    else{
                        data.add(id+" has dedicated you the karaoke "+songAndType[0]);
                    }

                }
                return data;

            }

        } catch (JSONException e) {e.printStackTrace();}

        return data;
    }
}
