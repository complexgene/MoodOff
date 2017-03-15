package com.moodoff.service;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moodoff.dao.StartedTodoDaoImpl;
import com.moodoff.dao.StartedTodoDaoInterface;
import com.moodoff.helper.DBHelper;
import com.moodoff.model.User;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Created by Santanu on 3/11/2017.
 */

public class StartedTodoService {
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = firebaseDatabase.getReference().child("errors");
    private DatabaseReference dbRef;

    private Context context;
    private StartedTodoDaoInterface startedTodoDBOperations;

    public StartedTodoService(Context context){
        this.context = context;
        startedTodoDBOperations = new StartedTodoDaoImpl(context);
    }

    public void createAllNecessaryTablesForAppOperation(){
        startedTodoDBOperations.dropAllInternalTables();

        LinkedHashMap<String,String> worktodoColumns = new LinkedHashMap<>();
        worktodoColumns.put("id","INTEGER PRIMARY KEY AUTOINCREMENT");
        worktodoColumns.put("api","VARCHAR");
        startedTodoDBOperations.createTable("worktodo",worktodoColumns);
        Log.e("StartedTodoService", "worktodo table created.");

        LinkedHashMap<String,String> profilesColumns = new LinkedHashMap<>();
        profilesColumns.put("id","VARCHAR PRIMARY KEY");
        profilesColumns.put("name","VARCHAR");
        profilesColumns.put("mailid","VARCHAR");
        profilesColumns.put("dob","VARCHAR");
        profilesColumns.put("textstatus","VARCHAR");
        profilesColumns.put("audiostatus","VARCHAR");
        startedTodoDBOperations.createTable("profiles",profilesColumns);
        Log.e("StartedTodoService", "profiles table created.");

        LinkedHashMap<String,String> rnotificationsColumns = new LinkedHashMap<>();
        rnotificationsColumns.put("from_user_id","VARCHAR");
        rnotificationsColumns.put("to_user_id","VARCHAR");
        rnotificationsColumns.put("file_name","VARCHAR");
        rnotificationsColumns.put("type","VARCHAR");
        rnotificationsColumns.put("send_done","INTEGER");
        rnotificationsColumns.put("create_ts","VARCHAR");
        startedTodoDBOperations.createTable("rnotifications",rnotificationsColumns);
        Log.e("StartedTodoService", "rnotifications table created.");

        LinkedHashMap<String,String> playListColumns = new LinkedHashMap<>();
        playListColumns.put("date","VARCHAR");
        playListColumns.put("mood_type","VARCHAR");
        playListColumns.put("song_name","VARCHAR");
        playListColumns.put("artist_name","VARCHAR");
        playListColumns.put("movie_or_album_name","VARCHAR");
        startedTodoDBOperations.createTable("playlist",playListColumns);
        Log.e("StartedTodoService", "playlist table created.");

        LinkedHashMap<String,String> allprofileColumns = new LinkedHashMap<>();
        allprofileColumns.put("phno","VARCHAR");
        allprofileColumns.put("name","VARCHAR");
        allprofileColumns.put("text_status","VARCHAR");
        allprofileColumns.put("audio_status","VARCHAR");
        allprofileColumns.put("text_status_likes","INTEGER");
        allprofileColumns.put("audio_status_likes","INTEGER");
        startedTodoDBOperations.createTable("all_profiles",allprofileColumns);
        Log.e("StartedTodoService", "all_profiles table created");

        // createTable implementation for allcontacts is different as we are fetching data in table creation here.
        LinkedHashMap<String,String> contactsColumns = new LinkedHashMap<>();
        contactsColumns.put("phone_no","VARCHAR");
        contactsColumns.put("name","VARCHAR");
        contactsColumns.put("status","INTEGER");
        startedTodoDBOperations.createTable("allcontacts",contactsColumns);
    }
    public boolean checkEntryOfPlaylistInInternalTableAndReadIfRequired(DBHelper dbOperations, String todaysDate, boolean moodsAndSongsFetchNotComplete){
        return startedTodoDBOperations.checkEntryOfPlaylistInInternalTableAndReadIfRequired(dbOperations, todaysDate, moodsAndSongsFetchNotComplete);
    }
    public LinkedHashMap<String,String> getContactsTableData(LinkedHashMap<String,String> allContacts, DBHelper dbOpr, User singleTonUserObject){
        return startedTodoDBOperations.getContactsTableData(allContacts, dbOpr, singleTonUserObject);
    }

    // Error sending to Firebase
    public void writeProblemTrace(String message){
        dbRef = mRootRef.child("errorMsg");
        dbRef.setValue(new Date().toString() + "->" + message);
    }
}
