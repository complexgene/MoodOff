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
    private DatabaseReference mRootRef = firebaseDatabase.getReference().child("allusers");
    private DatabaseReference dbRef;
    //--------------------------------------------------------------------------------------------
    private String userOTPValue;
    private boolean userExists;
    //--------------------------------------------------------------------------------------------
    //--------------VOID METHODS-----------------------
    public boolean storeUserDataToCloudDB(User singleTonUser){
        try{
            dbRef = mRootRef.child(singleTonUser.getUserMobileNumber());
            dbRef.setValue(singleTonUser);
            printMsg("UserManagementdaoImpl","User Data uploaded to cloud DB..");
            return true;
        }
        catch(Exception cloudDBException){
            Log.e("UserManagementdaoImpl","DBException raised in methiod storeUserDataToCloudDB");
            return false;
        }

    }
}
