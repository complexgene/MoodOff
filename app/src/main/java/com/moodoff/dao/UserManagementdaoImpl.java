package com.moodoff.dao;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private DatabaseReference dbRefForAllUserDetailsNode, dbRefForOnlyUserPhoneNumbersNode;
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
            printMsg("UserManagementdaoImpl","User Data uploaded to cloud DB at 1.allUserDetailsNode 2.OnlyUserPhNoNode..");
            return true;
        }
        catch(Exception cloudDBException){
            Log.e("UserManagementdaoImpl","DBException raised in methiod storeUserDataToCloudDB");
            return false;
        }
    }
}
