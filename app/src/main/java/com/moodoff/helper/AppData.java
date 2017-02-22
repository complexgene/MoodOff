package com.moodoff.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.widget.CalendarView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    public static String backPressedFragement="";
    public static int noOfTimesBackPressed=0;

    public static Typeface getAppFont(Context ctx){
        return Typeface.createFromAsset(ctx.getAssets(), "fonts/BLKCHCRY.TTF");
    }

    public static String getTodaysDate(){
        Calendar c = Calendar.getInstance();
        String todaysDate = c.get(Calendar.DATE)+""+(c.get(Calendar.MONTH)+1)+c.get(Calendar.YEAR);
        return todaysDate;
    }
    public static String getAppDirectoryPath(){
        return Environment.getExternalStorageDirectory().getAbsoluteFile().toString()+"/moodoff";
    }


}
