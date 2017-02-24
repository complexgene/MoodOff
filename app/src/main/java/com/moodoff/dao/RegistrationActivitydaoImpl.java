package com.moodoff.dao;

import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.exceptions.IncorrectOTPException;
import com.moodoff.helper.Messenger;
import com.moodoff.model.UserDetails;

import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by Shan on 2/24/2017.
 */

public class RegistrationActivitydaoImpl implements RegistrationActivitydaoInterface {
    //----------------------------Google Firebase-------------------------------------------------
        private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        private DatabaseReference mRootRef = firebaseDatabase.getReference().child("allusers");
        private DatabaseReference dbRef;
    //--------------------------------------------------------------------------------------------
        private String userOTPValue;
        private boolean userExists;
    //--------------------------------------------------------------------------------------------
    //--------------VOID METHODS-----------------------
    public void storeUserDataToCloudDB(UserDetails singleTonUser){
        dbRef = mRootRef.child(singleTonUser.getUserMobileNumber());
        dbRef.setValue(singleTonUser);
    }
    //--------------------------------------------------------------------------------
    //------------NON-VOID METHODS-----------------------------------------------------
    public boolean checkIfUserExists(final String userMobileNumber) {
        mRootRef.child(userMobileNumber).child("userMobileNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    userExists = true;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        return userExists;
    }
}
