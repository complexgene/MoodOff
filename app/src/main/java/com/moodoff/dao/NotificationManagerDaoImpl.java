package com.moodoff.dao;

import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.R;
import com.moodoff.helper.ServerManager;
import com.moodoff.model.User;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.moodoff.helper.AllAppData.serverURL;
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

    /*public void detectChangeInNotificationNode(String userMobileNumber){
        mRootRef.child(userMobileNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                printMsg("NotificationManagerDaoImpl", "Some new notifications appeared in cloud..So calling SM.readNotifications..()");
                ServerManager sm = new ServerManager();
                sm.readNotificationsFromServerAndWriteToInternalDB();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/

    public boolean writeSongDedicateToCloudDB(String ts, String fromUser, final String toUser, String currentMood, String currentSong, String type) {
        try{
            printMsg("NotificationManagerDaoImpl", "Came to create nodes for dedicator and dedicate :P in cloud DB..");
            // New dedicate entry in current user Node
            dbRef = mRootRef.child(fromUser).child(fromUser+"@"+toUser+"@"+ts);
            dbRef.setValue(fromUser+"#"+toUser+"#"+(currentMood+"@"+currentSong)+"#"+type+"#"+ts);
            // New dedicate entry in dedicated person's Node
            dbRef = mRootRef.child(toUser).child(fromUser+"@"+toUser+"@"+ts);
            dbRef.setValue(fromUser+"#"+toUser+"#"+(currentMood+"@"+currentSong)+"#"+type+"#"+ts);
            printMsg("NotificationManagerDaoImpl", "Nodes for dedicator and dedicate :P in cloud DB successful..");

            // Causes Invoke of NotificationBuilder from async listener----------------------------------------
            dbRef = mRootRef.child("rebuildPanelState").child(fromUser);
            dbRef.setValue(1);
            dbRef = mRootRef.child("rebuildPanelState").child(toUser);
            dbRef.setValue(1);

            return true;
        }catch(Exception ee){
            printMsg("NotificationManagerDaoImpl", "ERROR!! Dedicating module broke while creating nodes in cloud DB..");
            return false;
        }
    }

    public void likeTheDedicatedSong(String fromUserNumber, String toUserNumber, String currentMoodType, String currentSong, String timeStamp) {
        try {
            printMsg("NotificationManagerDaoImpl", "likedTheDedicatedSong():Came to love the dedicated song with details:" + fromUserNumber+" "+toUserNumber+" "+timeStamp+" ");
            dbRef = mRootRef.child(toUserNumber).child(fromUserNumber+"@"+toUserNumber+"@"+timeStamp);
            dbRef.setValue(fromUserNumber+"#"+toUserNumber+"#"+(currentMoodType+"@"+currentSong)+"#5#"+timeStamp);
            printMsg("NotificationManagerDaoImpl", "likedTheDedicatedSong():Loving the dedicated song with details:" + fromUserNumber+" "+toUserNumber+" "+timeStamp+" is DONE!!");
        } catch (Exception ee) {
            printMsg("NotificationManagerDaoImpl", "ERROR!! Liking the dedicated song experienced ISSUE!!");
        }
    }
}
