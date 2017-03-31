package com.moodoff.dao;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.helper.AllAppData;
import com.moodoff.helper.Messenger;
import com.moodoff.model.User;
import com.moodoff.model.UserLiveMoodDetailsPojo;

import java.util.HashMap;

import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by Santanu on 3/11/2017.
 */

public class UserManagementdaoImpl implements UserManagementdaoInterface {
    //----------------------------Google Firebase-------------------------------------------------
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference allNotificationsNode = firebaseDatabase.getReference().child("allNotifications");
    private DatabaseReference allUserDetailsNode = firebaseDatabase.getReference().child("allusers");
    private DatabaseReference onlyUserPhoneNumbersNode = firebaseDatabase.getReference().child("userlist");
    private DatabaseReference liveFeedNode = firebaseDatabase.getReference().child("livefeed");
    private DatabaseReference checkAliveNode = firebaseDatabase.getReference().child("checkAlive");

    private DatabaseReference dbRefForAllNotificationsNode, dbRefForAllUserDetailsNode, dbRefForOnlyUserPhoneNumbersNode, dbRefForLiveFeed, dbRefForCheckAlive;
    //--------------------------------------------------------------------------------------------

    public boolean storeUserDataToCloudDB(User singleTonUser){
        try{
            printMsg("UserManagementdaoImpl","Came to create two nodes to cloud DB..");
            // Entry to allUserDetailsNode
            dbRefForAllUserDetailsNode = allUserDetailsNode.child(singleTonUser.getUserMobileNumber());
            dbRefForAllUserDetailsNode.setValue(singleTonUser);
            // Entry to onlyUserPhoneNumbersNode
            dbRefForOnlyUserPhoneNumbersNode = onlyUserPhoneNumbersNode.child(singleTonUser.getUserMobileNumber());
            dbRefForOnlyUserPhoneNumbersNode.setValue(1);
            // Entry to LiveFeed Node for love counts
            /*dbRefForLiveFeed = liveFeedNode.child(singleTonUser.getUserMobileNumber()).child(AllAppData.userTextStatusLoveCount);
            dbRefForLiveFeed.setValue(0);
            dbRefForLiveFeed = liveFeedNode.child(singleTonUser.getUserMobileNumber()).child(AllAppData.userAudioStatusLoveCount);
            dbRefForLiveFeed.setValue(0);
            */printMsg("UserManagementdaoImpl","User Data uploaded to cloud DB at 1.allUserDetailsNode 2.OnlyUserPhNoNode..");
            return true;
        }
        catch(Exception cloudDBException){
            Log.e("UserManagementdaoImpl","DBException raised in method storeUserDataToCloudDB");
            return false;
        }
    }
    public void voteForTextStatus(final String forUserMobileNumber, final String currentUserMobileNumber, final Activity profileActivity){
        try{
            printMsg("UserManagementDaoImpl", "Came to vote for the text status of user:" + forUserMobileNumber);
            dbRefForLiveFeed = liveFeedNode.child(forUserMobileNumber).child(AllAppData.userTextStatusLoveCount);
            dbRefForLiveFeed.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(currentUserMobileNumber)) {
                        profileActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Messenger.print(profileActivity, "You have already voted for this status");
                            }
                        });
                        printMsg("UserManagementDaoImpl", "You have already voted..");
                    }
                    else{
                        dbRefForLiveFeed = dbRefForLiveFeed.child(currentUserMobileNumber);
                        dbRefForLiveFeed.setValue(1);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            printMsg("UserManagementDaoImpl", "Voting for text status of user:" + forUserMobileNumber +" is done.");
            //return true;
        }catch (Exception cloudDBException){
            printMsg("UserManagementDaoImpl", "ERROR!! Voting couldn't be done for text status in cloud DB!!");
            //return false;
        }
    }
    public void voteForAudioStatus(final String forUserMobileNumber, final String currentUserMobileNumber, final Activity profileActivity){
        try{
            printMsg("UserManagementDaoImpl", "Came to vote for the text status of user:" + forUserMobileNumber);
            dbRefForLiveFeed = liveFeedNode.child(forUserMobileNumber).child(AllAppData.userAudioStatusLoveCount);
            dbRefForLiveFeed.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(currentUserMobileNumber)) {
                        profileActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Messenger.print(profileActivity, "You have already voted for this status");
                            }
                        });
                        printMsg("UserManagementDaoImpl", "You have already voted..");
                    }
                    else{
                        dbRefForLiveFeed = dbRefForLiveFeed.child(currentUserMobileNumber);
                        dbRefForLiveFeed.setValue(1);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            printMsg("UserManagementDaoImpl", "Voting for audio status of user:" + forUserMobileNumber +" is done.");
        }catch (Exception cloudDBException){
            printMsg("UserManagementDaoImpl", "ERROR!! Voting couldn't be done for audio status in cloud DB!!");
        }
    }
    public void writeTextStatusChange(String userMobileNumber, final String newTextStatus, final Activity profileActivity, final TextView userTextStatus){
        printMsg("UserManagementDaoImpl", "Came to change the text status value:"+newTextStatus+" for user:"+userMobileNumber+" in cloud DB");
        dbRefForAllUserDetailsNode = allUserDetailsNode.child(userMobileNumber).child(AllAppData.userTextStatus);
        dbRefForAllUserDetailsNode.setValue(newTextStatus);
        profileActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userTextStatus.setText(newTextStatus);
                Messenger.print(profileActivity, "Your new text status is super awesome.");
            }
        });
        printMsg("UserManagementDaoImpl", "Changing of the text status value to the cloud DB done.");
    }
    public void writeAudioStatusChange(String userMobileNumber, final String newAudioStatus, final Activity profileActivity, final TextView userTextStatus){
        printMsg("UserManagementDaoImpl", "Came to change the audio status value:"+newAudioStatus+" for user:"+userMobileNumber+" in cloud DB");
        dbRefForAllUserDetailsNode = allUserDetailsNode.child(userMobileNumber).child(AllAppData.userAudioStatus);
        dbRefForAllUserDetailsNode.setValue(newAudioStatus);
        profileActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Messenger.print(profileActivity, "Your new audio status is super awesome.");
            }
        });
        printMsg("UserManagementDaoImpl", "Changing of the audio status value to the cloud DB done.");
    }
    public void likeCurrentMood(final String forUserMobileNumber, final String currentUserMobileNumber, final Activity profileActivity){
        try{
            printMsg("UserManagementDaoImpl", "Came to like the current mood of  user:" + forUserMobileNumber);
            dbRefForLiveFeed = liveFeedNode.child(forUserMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodLikeCount);
            dbRefForLiveFeed.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(currentUserMobileNumber)) {
                        profileActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Messenger.print(profileActivity, "Already voted Like for this mood");
                            }
                        });
                        printMsg("UserManagementDaoImpl", "Already voted Like for this mood..");
                    }
                    else{
                        dbRefForLiveFeed = dbRefForLiveFeed.child(currentUserMobileNumber);
                        dbRefForLiveFeed.setValue(1);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            printMsg("UserManagementDaoImpl", "Liking the mood of user:" + forUserMobileNumber +" is done.");
            //return true;
        }catch (Exception cloudDBException){
            printMsg("UserManagementDaoImpl", "ERROR!! Liking the mood couldn't be done in cloud DB!!");
            //return false;
        }
    }
    public void loveCurrentMood(final String forUserMobileNumber, final String currentUserMobileNumber, final Activity profileActivity){
        try{
            printMsg("UserManagementDaoImpl", "Came to love the current mood of  user:" + forUserMobileNumber);
            dbRefForLiveFeed = liveFeedNode.child(forUserMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodLoveCount);
            dbRefForLiveFeed.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(currentUserMobileNumber)) {
                        profileActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Messenger.print(profileActivity, "Already voted Love for this mood");
                            }
                        });
                        printMsg("UserManagementDaoImpl", "Already voted Love for this mood.");
                    }
                    else{
                        dbRefForLiveFeed = dbRefForLiveFeed.child(currentUserMobileNumber);
                        dbRefForLiveFeed.setValue(1);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            printMsg("UserManagementDaoImpl", "Loving the mood of user:" + forUserMobileNumber +" is done.");
            //return true;
        }catch (Exception cloudDBException){
            printMsg("UserManagementDaoImpl", "ERROR!! Loving the mood couldn't be done in cloud DB!!");
            //return false;
        }
    }
    public void sadCurrentMood(final String forUserMobileNumber, final String currentUserMobileNumber, final Activity profileActivity){
        try{
            printMsg("UserManagementDaoImpl", "Came to vote for sad for the current mood of user:" + forUserMobileNumber);
            dbRefForLiveFeed = liveFeedNode.child(forUserMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodSadCount);
            dbRefForLiveFeed.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(currentUserMobileNumber)) {
                        profileActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Messenger.print(profileActivity, "Already voted Sad for this mood.");
                            }
                        });
                        printMsg("UserManagementDaoImpl", "Already voted Sad for this mood..");
                    }
                    else{
                        dbRefForLiveFeed = dbRefForLiveFeed.child(currentUserMobileNumber);
                        dbRefForLiveFeed.setValue(1);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            printMsg("UserManagementDaoImpl", "Showing sadness for the mood of user:" + forUserMobileNumber +" is done.");
        }catch (Exception cloudDBException){
            printMsg("UserManagementDaoImpl", "ERROR!! Showing sadness for the mood couldn't be done in cloud DB!!");
        }
    }
    public void setLiveMood(String userMobileNumber, UserLiveMoodDetailsPojo userLiveMoodDetails) {
        try {

            printMsg("UserManagementDaoImpl", "Came to set the mood for the current user:" + userMobileNumber);
            dbRefForCheckAlive = checkAliveNode.child(userMobileNumber);
            dbRefForCheckAlive.setValue(AllAppData.getTodaysDateAndTime());

            dbRefForLiveFeed = liveFeedNode.child(userMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.userLiveMood).child("moodType");
            dbRefForLiveFeed.setValue(userLiveMoodDetails.getMoodType());
            dbRefForLiveFeed = liveFeedNode.child(userMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.userLiveMood).child("liveNow");
            dbRefForLiveFeed.setValue(userLiveMoodDetails.getLiveNow());
            dbRefForLiveFeed = liveFeedNode.child(userMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.timeStamp);
            dbRefForLiveFeed.setValue(AllAppData.getTodaysDateAndTime());
            dbRefForLiveFeed = liveFeedNode.child(userMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodLikeCount);
            dbRefForLiveFeed.removeValue();
            dbRefForLiveFeed = liveFeedNode.child(userMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodLoveCount);
            dbRefForLiveFeed.removeValue();
            dbRefForLiveFeed = liveFeedNode.child(userMobileNumber).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodSadCount);
            dbRefForLiveFeed.removeValue();
            printMsg("UserManagementDaoImpl", "Setting the mood for the current user done.");

        }
        catch(Exception ee) {
            printMsg("UserManagementDaoImpl", "ERROR!! Setting current mood couldn't be done in cloud DB!!");
        }
    }
    public void setRebuildNotificationPanelNodeInCloud(String userMobileNumber) {
        dbRefForAllNotificationsNode = allNotificationsNode.child("rebuildStatePanel").
                child(userMobileNumber);
        dbRefForAllNotificationsNode.setValue(1);
    }
    public void keepPingingToStayAlive(String userMobileNumber) {
        printMsg("UserManagementDaoImpl", "Came here to ping and keep the live mood alive..");
        dbRefForCheckAlive = checkAliveNode.child(userMobileNumber);
        dbRefForCheckAlive.setValue(AllAppData.getTodaysDateAndTime());
        printMsg("UserManagementDaoImpl", "Ping and keeping the live mood alive DONE..");
    }
}
