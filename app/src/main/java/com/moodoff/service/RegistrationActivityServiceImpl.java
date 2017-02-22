package com.moodoff.service;

import android.content.Context;
import android.util.Log;

import com.moodoff.helper.DBHelper;

import java.util.LinkedHashMap;

/**
 * Created by santanu on 22/2/17.
 */

public class RegistrationActivityServiceImpl implements RegistrationActivityServiceInterface{
    public void createAllNecessaryTablesForAppOperation(Context context){
        DBHelper dbOperations = new DBHelper(context);
        dbOperations.dropAllTables();

        LinkedHashMap<String,String> worktodoColumns = new LinkedHashMap<>();
        worktodoColumns.put("id","INTEGER PRIMARY KEY AUTOINCREMENT");
        worktodoColumns.put("api","VARCHAR");
        dbOperations.createTable("worktodo",worktodoColumns);
        Log.e("RegistrationAct_TBL","worktodo table created.");

        LinkedHashMap<String,String> profilesColumns = new LinkedHashMap<>();
        profilesColumns.put("id","VARCHAR PRIMARY KEY");
        profilesColumns.put("name","VARCHAR");
        profilesColumns.put("mailid","VARCHAR");
        profilesColumns.put("dob","VARCHAR");
        profilesColumns.put("textstatus","VARCHAR");
        profilesColumns.put("audiostatus","VARCHAR");
        dbOperations.createTable("profiles",profilesColumns);
        Log.e("RegistrationAct_TBL","profiles table created.");

        LinkedHashMap<String,String> rnotificationsColumns = new LinkedHashMap<>();
        rnotificationsColumns.put("from_user_id","VARCHAR");
        rnotificationsColumns.put("to_user_id","VARCHAR");
        rnotificationsColumns.put("file_name","VARCHAR");
        rnotificationsColumns.put("type","VARCHAR");
        rnotificationsColumns.put("send_done","INTEGER");
        rnotificationsColumns.put("create_ts","VARCHAR");
        dbOperations.createTable("rnotifications",rnotificationsColumns);
        Log.e("RegistrationAct_TBL","rnotifications table created");

        LinkedHashMap<String,String> playListColumns = new LinkedHashMap<>();
        playListColumns.put("date","VARCHAR");
        playListColumns.put("mood_type","VARCHAR");
        playListColumns.put("song_name","VARCHAR");
        playListColumns.put("artist_name","VARCHAR");
        playListColumns.put("movie_or_album_name","VARCHAR");
        dbOperations.createTable("playlist",playListColumns);
        Log.e("RegistrationAct_TBL","playlist table created");

        LinkedHashMap<String,String> allprofileColumns = new LinkedHashMap<>();
        allprofileColumns.put("phno","VARCHAR");
        allprofileColumns.put("name","VARCHAR");
        allprofileColumns.put("text_status","VARCHAR");
        allprofileColumns.put("audio_status","VARCHAR");
        allprofileColumns.put("text_status_likes","INTEGER");
        allprofileColumns.put("audio_status_likes","INTEGER");
        dbOperations.createTable("all_profiles",allprofileColumns);
        Log.e("RegistrationAct_TBL","all_profiles table created");

        // createTable implementation for allcontacts is different as we are fetching data in table creation here.
        LinkedHashMap<String,String> contactsColumns = new LinkedHashMap<>();
        contactsColumns.put("phone_no","VARCHAR");
        contactsColumns.put("name","VARCHAR");
        contactsColumns.put("status","INTEGER");
        dbOperations.createTable("allcontacts",contactsColumns);
    }
}
