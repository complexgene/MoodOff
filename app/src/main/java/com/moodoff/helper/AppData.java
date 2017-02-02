package com.moodoff.helper;

import android.content.Context;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by snaskar on 12/21/2016.
 */

public class AppData {
    public static HashMap<String,ArrayList<String>> allMoodPlayList = new HashMap<>();
    public static HashMap<String,HashMap<String,String>> allProfileData = new HashMap<>();
    public static int totalNoOfNot = 0;
    public static int lovedDedicateOldCount = 0;
    public static int lovedDedicateNewCount = 0;
    public static ArrayList<String> allNotifications;
    public static int noOfFriendUsesTheApp = 0;

    public static Typeface getAppFont(Context ctx){
        return Typeface.createFromAsset(ctx.getAssets(), "fonts/BLKCHCRY.TTF");
    }

    /*public static HashMap<String, ArrayList<String>> getAllMoodPlayList() {
        return allMoodPlayList;
    }
    public static void setAllMoodPlayList(HashMap<String, ArrayList<String>> allMoodPlayList) {
        PlaylistSongs.allMoodPlayList = allMoodPlayList;
    }*/
}
