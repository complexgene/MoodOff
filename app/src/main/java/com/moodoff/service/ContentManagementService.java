package com.moodoff.service;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moodoff.model.User;

import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by Santanu on 3/11/2017.
 */
// This module is responsible to fetch and store content like songs, stories etc etc.
public class ContentManagementService {
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootRef = firebaseDatabase.getReference().child("allNotifications");
    private DatabaseReference dbRef;

    private Context context;
    public ContentManagementService(Context context){
        this.context = context;
    }

    public String getNotifications(User singleTonUser){
        dbRef = mRootRef.child(singleTonUser.getUserMobileNumber());
        printMsg("ContentManagementService",dbRef.toString());
        return dbRef.toString();
    }
}
