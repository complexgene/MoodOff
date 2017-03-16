package com.moodoff.dao;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.helper.ServerManager;

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
            dbRef = mRootRef.child(fromUser).child(toUser+"@"+ts);
            dbRef.setValue(fromUser+"#"+toUser+"#"+(currentMood+"@"+currentSong)+"#"+type+"#"+ts);
            // New dedicate entry in dedicated person's Node
            dbRef = mRootRef.child(toUser).child(fromUser+"@"+ts);
            dbRef.setValue(fromUser+"#"+toUser+"#"+(currentMood+"@"+currentSong)+"#"+type+"#"+ts);
            printMsg("NotificationManagerDaoImpl", "Nodes for dedicator and dedicate :P in cloud DB successful..");
            return true;
        }catch(Exception ee){
            printMsg("NotificationManagerDaoImpl", "ERROR!! Dedicating module broke while creating nodes in cloud DB..");
            return false;
        }
    }
}
