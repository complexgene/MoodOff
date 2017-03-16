package com.moodoff.dao;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moodoff.helper.AllAppData;
import com.moodoff.model.User;

import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by Santanu on 3/11/2017.
 */

public class UserManagementdaoImpl implements UserManagementdaoInterface {
    //----------------------------Google Firebase-------------------------------------------------
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference allUserDetailsNode = firebaseDatabase.getReference().child("allusers");
    private DatabaseReference onlyUserPhoneNumbersNode = firebaseDatabase.getReference().child("userlist");
    private DatabaseReference liveFeedNode = firebaseDatabase.getReference().child("livefeed");
    private DatabaseReference dbRefForAllUserDetailsNode, dbRefForOnlyUserPhoneNumbersNode, dbRefForLiveFeed;
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
            dbRefForLiveFeed = liveFeedNode.child(singleTonUser.getUserMobileNumber()).child(AllAppData.userTextStatusLoveCount);
            dbRefForLiveFeed.setValue(0);
            dbRefForLiveFeed = liveFeedNode.child(singleTonUser.getUserMobileNumber()).child(AllAppData.userAudioStatusLoveCount);
            dbRefForLiveFeed.setValue(0);
            printMsg("UserManagementdaoImpl","User Data uploaded to cloud DB at 1.allUserDetailsNode 2.OnlyUserPhNoNode..");
            return true;
        }
        catch(Exception cloudDBException){
            Log.e("UserManagementdaoImpl","DBException raised in methiod storeUserDataToCloudDB");
            return false;
        }
    }
}
