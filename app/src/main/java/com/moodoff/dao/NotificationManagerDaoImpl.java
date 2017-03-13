package com.moodoff.dao;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moodoff.helper.LoggerBaba;
import com.moodoff.helper.Messenger;
import com.moodoff.model.User;

import java.net.HttpURLConnection;
import java.net.URL;

import static com.moodoff.helper.HttpGetPostInterface.serverURL;
import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by Santanu on 3/14/2017.
 */

public class NotificationManagerDaoImpl implements NotificationManagerDaoInterface {
    //----------------------------Google Firebase-------------------------------------------------
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = firebaseDatabase.getReference().child("allNotifications");
    private DatabaseReference dbRef;
    //--------------------------------------------------------------------------------------------

    public boolean writeSongDedicateToCloudDB(String ts, String fromUser, final String toUser, String currentMood, String currentSong, String type) {
        try{
            printMsg("NotificationManagerDaoImpl", "Came to create nodes for dedicator and dedicate :P in cloud DB..");
            // New dedicate entry in current user Node
            dbRef = mRootRef.child(fromUser).child(ts);
            dbRef.setValue(fromUser+"#"+toUser+"#"+(currentMood+"@"+currentSong)+"#"+type+"#"+ts);
            // New dedicate entry in dedicated person's Node
            dbRef = mRootRef.child(toUser).child(ts);
            dbRef.setValue(fromUser+"#"+toUser+"#"+(currentMood+"@"+currentSong)+"#"+type+"#"+ts);
            printMsg("NotificationManagerDaoImpl", "Nodes for dedicator and dedicate :P in cloud DB successful..");
            return true;
        }catch(Exception ee){
            printMsg("NotificationManagerDaoImpl", "ERROR!! Dedicating module broke while creating nodes in cloud DB..");
            return false;
        }
    }
}
