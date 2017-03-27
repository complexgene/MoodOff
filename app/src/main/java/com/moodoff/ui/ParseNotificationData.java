package com.moodoff.ui;

/**
 * Created by Arindam on 02-10-2016.
 */

import android.util.Log;

import com.moodoff.helper.AllAppData;
import com.moodoff.helper.LoggerBaba;
import com.moodoff.helper.Messenger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import static com.moodoff.helper.LoggerBaba.printMsg;

public class ParseNotificationData {

    public static ArrayList<String> getAllAppUsers(String contactsInJson) {
        ArrayList<String> allContactNumbersInServer = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(contactsInJson);
            Iterator<String> keys = jsonRootObject.keys();
            while (keys.hasNext()) {
                allContactNumbersInServer.add(keys.next());
            }
            printMsg("ParseNotificationData", allContactNumbersInServer.toString());
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return allContactNumbersInServer;
    }

    public static ArrayList<String> getNotification(String raw_json) {
        int lovedDedicateServerCount = 0;
        String strJson = raw_json;
        ArrayList<String> allNotifications = new ArrayList<>();
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            Iterator<String> allTS = jsonRootObject.keys();
            while (allTS.hasNext()) {
                String key = allTS.next();
                String[] allData = jsonRootObject.get(key).toString().split("#");
                String fromUser = allData[0];
                String toUser = allData[1];
                String songName = allData[2];
                String type = allData[3];
                if (type.equals("5")) lovedDedicateServerCount++;
                String ts = allData[4];

                allNotifications.add(fromUser + " " + toUser + " " + ts + " " + type + " " + songName);
            }
            Log.e("ParseNotification", allNotifications.toString());
            return allNotifications;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static HashMap<String, String> parseAndGetBasicProfileData(String raw_json) {
        HashMap<String, String> allProfileBasicData = new HashMap<>();
        Log.e("Profile_jsonObj", raw_json);
        String strJson = raw_json;

        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            String userName = jsonRootObject.optString(AllAppData.userName).toString();
            String userMobileNumber = jsonRootObject.optString(AllAppData.userMobileNumber).toString();
            String userDateOfBirth = jsonRootObject.optString(AllAppData.userDateOfBirth);
            String userTextStatus = jsonRootObject.optString(AllAppData.userTextStatus).toString();
            String userAudioStatus = jsonRootObject.optString(AllAppData.userAudioStatus).toString();

            allProfileBasicData.put(AllAppData.userName, userName);
            allProfileBasicData.put(AllAppData.userMobileNumber, userMobileNumber);
            allProfileBasicData.put(AllAppData.userDateOfBirth, userDateOfBirth);
            allProfileBasicData.put(AllAppData.userTextStatus, userTextStatus);
            allProfileBasicData.put(AllAppData.userAudioStatus, userAudioStatus);
            allProfileBasicData.put(AllAppData.userTextStatusLoveCount, "0");
            allProfileBasicData.put(AllAppData.userAudioStatusLoveCount, "0");

            printMsg("ParseNotificationData", "Opened User:" + allProfileBasicData.toString());

            return allProfileBasicData;

        } catch (JSONException e) {
            Log.e("ParseNotification_Err1", e.getMessage());
            e.fillInStackTrace();
        }
        return allProfileBasicData;
    }

    static HashMap<String, String> parseAndGetLiveProfileData(String raw_json) {
        HashMap<String, String> allProfileLiveData = new HashMap<>();
        String strJson = raw_json;

        try {
            int textStatusLoveCount, audioStatusLoveCount;
            int moodLikeVoteCount, moodLoveVoteCount, moodSadVoteCount;
            String userLiveMood;

            if (raw_json.equals("null")) {
                textStatusLoveCount = audioStatusLoveCount = 0;
                userLiveMood = "- No Mood Yet -";
                moodLikeVoteCount = moodLoveVoteCount = moodSadVoteCount = 0;
            } else {
                JSONObject jsonRootObject = new JSONObject(strJson);
                //---------------------------------------------------------------------------------------------------//
                // If nobody yet voted text stats the this node won't exist in DB
                if (jsonRootObject.isNull(AllAppData.userTextStatusLoveCount))
                    textStatusLoveCount = 0;
                else
                    textStatusLoveCount = jsonRootObject.getJSONObject(AllAppData.userTextStatusLoveCount).length();
                // If nobody yet voted audio status textStatsuLoveCountRootObject will be NULL
                if (jsonRootObject.isNull(AllAppData.userAudioStatusLoveCount))
                    audioStatusLoveCount = 0;
                else
                    audioStatusLoveCount = jsonRootObject.getJSONObject(AllAppData.userAudioStatusLoveCount).length();
                //----------------------------------------------------------------------------------------------------//
                // Check each expression count for the mood type of user
                if (!jsonRootObject.isNull(AllAppData.moodLiveFeedNode)) {
                    jsonRootObject = jsonRootObject.getJSONObject(AllAppData.moodLiveFeedNode);
                    //Enter into liveMood Node.
                    if (jsonRootObject.isNull(AllAppData.userLiveMood))
                        userLiveMood = "- No Mood Yet -";
                    else
                        userLiveMood = jsonRootObject.getJSONObject(AllAppData.userLiveMood).optString("moodType").toString();

                    printMsg("ParseNotification", userLiveMood);

                    if (jsonRootObject.isNull(AllAppData.userMoodLikeCount))
                        moodLikeVoteCount = 0;
                    else
                        moodLikeVoteCount = jsonRootObject.getJSONObject(AllAppData.userMoodLikeCount).length();

                    if (jsonRootObject.isNull(AllAppData.userMoodLoveCount))
                        moodLoveVoteCount = 0;
                    else
                        moodLoveVoteCount = jsonRootObject.getJSONObject(AllAppData.userMoodLoveCount).length();

                    if (jsonRootObject.isNull(AllAppData.userMoodSadCount))
                        moodSadVoteCount = 0;
                    else
                        moodSadVoteCount = jsonRootObject.getJSONObject(AllAppData.userMoodSadCount).length();
                } else {
                    userLiveMood = "- No Mood Yet -";
                    moodLikeVoteCount = moodLoveVoteCount = moodSadVoteCount = 0;
                }
                Log.e("Profile_LIVE_Counts", textStatusLoveCount + ", " + audioStatusLoveCount);
            }
            allProfileLiveData.put(AllAppData.userTextStatusLoveCount, String.valueOf(textStatusLoveCount));
            allProfileLiveData.put(AllAppData.userAudioStatusLoveCount, String.valueOf(audioStatusLoveCount));
            allProfileLiveData.put(AllAppData.userLiveMood, userLiveMood);
            allProfileLiveData.put(AllAppData.userMoodLikeCount, String.valueOf(moodLikeVoteCount));
            allProfileLiveData.put(AllAppData.userMoodLoveCount, String.valueOf(moodLoveVoteCount));
            allProfileLiveData.put(AllAppData.userMoodSadCount, String.valueOf(moodSadVoteCount));

            printMsg("ParseNotificationData", "Opened User:" + allProfileLiveData.toString());
            return allProfileLiveData;

        } catch (JSONException e) {
            Log.e("ParseNotification_Err2", e.getMessage() + "\n" + e.getStackTrace()[0].toString());
            e.fillInStackTrace();
        }
        return allProfileLiveData;
    }

    public static String getLiveMoodDetailsSeparatedByAT(String liveMoodDetailsNode_json) {
        String moodType, enteredTS;
        int isLive;
        if (liveMoodDetailsNode_json.equals("null")) {
            moodType = "-No Mood Yet-";
            enteredTS = "00-00-00_00:00:00";
            isLive = 0;
        } else {
            try {
                JSONObject jsonRootObject = new JSONObject(liveMoodDetailsNode_json);
                moodType = jsonRootObject.optString("moodType");
                isLive = jsonRootObject.optInt("liveNow");
                enteredTS = jsonRootObject.optString("enteredAt");
            } catch (Exception jexc) {
                moodType = "-No Mood Yet-";
                enteredTS = "00-00-00_00:00:00";
                isLive = 0;
                printMsg("ParseNotificationData", "ERROR!! getLiveMoodDetailsSeparatedByAT():" + jexc.getMessage());
            }
        }
        return moodType + "@" + isLive + "@" + enteredTS;
    }
}
