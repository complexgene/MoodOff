package com.moodoff.helper;

import android.os.Environment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by snaskar on 12/21/2016...
 */

public class AllAppData {
    public static LinkedHashMap<String,String> allReadContacts = new LinkedHashMap<>();
    public static ArrayList<String> allReadContactsFromDBServer = new ArrayList<>();
    public static ArrayList<String> friendsWhoUsesApp = new ArrayList<>();
    public static ArrayList<String> friendsWhoDoesntUseApp = new ArrayList<>();
    public static int countFriendsUsingApp = 0;
    public static int countFriendsNotUsingApp = 0;

    public static HashMap<String,ArrayList<String>> allMoodPlayList = new HashMap<>();
    public static HashMap<String,HashMap<String,String>> allProfileData = new HashMap<>();
    public static int totalNoOfNot = 0;
    //public static int numberOfLikedDedicatesOldCount = 0;
    public static int numberOfLikedDedicatesCurrentServerCount = 0;
    public static ArrayList<String> allNotifications;
    public static int noOfFriendUsesTheApp = 0;
    public static String backPressedFragement="";
    public static int noOfTimesBackPressed=0;
    public static int fileSizeToRead = 2048*2048;

    public static String serverURL = "https://moodoff-ff2cf.firebaseio.com";
    public static String serverSongURL = "http://hipilab.com/data/songs/";
    public static String serverStoriesURL = "http://hipilab.com/data/stories/";

    public static ArrayList<String> getAllWordsOfHangmanGame = new ArrayList<>(Arrays.asList(
            "DOCUMENT","DATABASE","QUESTION","EDUCATION","LITERATE","PARLIAMENT","KEYBOARD","SINGER",
            "DEDICATE","HOLIDAY","COUNTRY","EXCELLENT","TALENTED","SUPERIOR"));
    public static String getTodaysDate(){
        Calendar c = Calendar.getInstance();
        String todaysDate = c.get(Calendar.DATE)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.YEAR);
        return todaysDate;
    }
    public static String getTodaysDateAndTime(){
        Calendar c = Calendar.getInstance();
        String todaysDate = getTodaysDate()+"_"+c.getTime().toString().split(" ")[3];
        return todaysDate;
    }

    public static String getAppDirectoryPath(){
        return Environment.getExternalStorageDirectory().getAbsoluteFile().toString()+"/moodoff";
    }

    //UserObjects Constants
    public static String userName = "userName";
    public static String userMobileNumber = "userMobileNumber";
    public static String userDateOfBirth = "userDateOfBirth";
    public static String userTextStatus = "userTextStatus";
    public static String userAudioStatus = "userAudioStatusSong";
    public static String userTextStatusLoveCount = "userTextStatusLoveCount";
    public static String userAudioStatusLoveCount = "userAudioStatusLoveCount";
    public static String userNumberOfOldNotifications = "userNumberOfOldNotifications";
    public static String userNumberOfOldLikedDedicates = "userNumberOfOldLikedDedicates";
    public static String userScore = "userScore";
    public static String likedTextStatusLine = " liked the text status";
    public static String likedAudioStatusLine = " liked the audio status";
    public static String defaultAudioStatusSong = "romantic/HERO.mp3";
    // >> Mood Related
    public static String moodLiveFeedNode = "liveMoodFeeds";
    public static String userLiveMood = "userLiveMood";
    public static String userMoodLikeCount = "moodLikeCount";
    public static String userMoodLoveCount = "moodLoveCount";
    public static String userMoodSadCount = "moodSadCount";
    public static String timeStamp = "timeStamp";
    // >> File Related
    public static String userDetailsFileName = "UserData.txt";
}
