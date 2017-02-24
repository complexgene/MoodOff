package com.moodoff.service;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.dao.RegistrationActivitydaoImpl;
import com.moodoff.dao.RegistrationActivitydaoInterface;
import com.moodoff.exceptions.IncorrectOTPException;
import com.moodoff.exceptions.SMSGateWayException;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.Messenger;
import com.moodoff.model.UserDetails;

import java.util.LinkedHashMap;

import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by santanu on 22/2/17.
 */

public class RegistrationActivityServiceImpl implements RegistrationActivityServiceInterface{
    //--------------------------------------------------------------------------------------------
        RegistrationActivitydaoInterface regAct_daoBot = new RegistrationActivitydaoImpl();
    //--------------------------------------------------------------------------------------------
    //-----VOID Methods-----
    public void createAllNecessaryTablesForAppOperation(Context context){
        DBHelper dbOperations = new DBHelper(context);
        dbOperations.dropAllTables();

        LinkedHashMap<String,String> worktodoColumns = new LinkedHashMap<>();
        worktodoColumns.put("id","INTEGER PRIMARY KEY AUTOINCREMENT");
        worktodoColumns.put("api","VARCHAR");
        dbOperations.createTable("worktodo",worktodoColumns);
        Log.e("RegistrationAct_TBL","worktodo table created.");

        LinkedHashMap<String,String> profilesColumns = new LinkedHashMap<>();
        profilesColumns.put("id","VARCHAR PRIMARY KEY");
        profilesColumns.put("name","VARCHAR");
        profilesColumns.put("mailid","VARCHAR");
        profilesColumns.put("dob","VARCHAR");
        profilesColumns.put("textstatus","VARCHAR");
        profilesColumns.put("audiostatus","VARCHAR");
        dbOperations.createTable("profiles",profilesColumns);
        Log.e("RegistrationAct_TBL","profiles table created.");

        LinkedHashMap<String,String> rnotificationsColumns = new LinkedHashMap<>();
        rnotificationsColumns.put("from_user_id","VARCHAR");
        rnotificationsColumns.put("to_user_id","VARCHAR");
        rnotificationsColumns.put("file_name","VARCHAR");
        rnotificationsColumns.put("type","VARCHAR");
        rnotificationsColumns.put("send_done","INTEGER");
        rnotificationsColumns.put("create_ts","VARCHAR");
        dbOperations.createTable("rnotifications",rnotificationsColumns);
        Log.e("RegistrationAct_TBL","rnotifications table created");

        LinkedHashMap<String,String> playListColumns = new LinkedHashMap<>();
        playListColumns.put("date","VARCHAR");
        playListColumns.put("mood_type","VARCHAR");
        playListColumns.put("song_name","VARCHAR");
        playListColumns.put("artist_name","VARCHAR");
        playListColumns.put("movie_or_album_name","VARCHAR");
        dbOperations.createTable("playlist",playListColumns);
        Log.e("RegistrationAct_TBL","playlist table created");

        LinkedHashMap<String,String> allprofileColumns = new LinkedHashMap<>();
        allprofileColumns.put("phno","VARCHAR");
        allprofileColumns.put("name","VARCHAR");
        allprofileColumns.put("text_status","VARCHAR");
        allprofileColumns.put("audio_status","VARCHAR");
        allprofileColumns.put("text_status_likes","INTEGER");
        allprofileColumns.put("audio_status_likes","INTEGER");
        dbOperations.createTable("all_profiles",allprofileColumns);
        Log.e("RegistrationAct_TBL","all_profiles table created");

        // createTable implementation for allcontacts is different as we are fetching data in table creation here.
        LinkedHashMap<String,String> contactsColumns = new LinkedHashMap<>();
        contactsColumns.put("phone_no","VARCHAR");
        contactsColumns.put("name","VARCHAR");
        contactsColumns.put("status","INTEGER");
        dbOperations.createTable("allcontacts",contactsColumns);
    }
    public String generateAndSendOTP(String userMobileNumber) throws SMSGateWayException{
        String otpGotFromSMSGateway;
        try{
            // Logic to Access SMS Gateway API to initiate OTP message and get the new OTP Value
            otpGotFromSMSGateway = "123456";

        }catch (SMSGateWayException smsException){
            throw smsException;
        }
        return otpGotFromSMSGateway;
    }
    public void storeUserDataToCloudDB(UserDetails singleTonUser){
        printMsg("RegAct_storeUser","Storing user object to cloud DB..");
        regAct_daoBot.storeUserDataToCloudDB(singleTonUser);
        printMsg("RegAct_storeUser","Storing user object to cloud DB complete..");
    }
    //-----That's all---------------------

    //-----Boolean return methods--------
    public boolean checkIfOTPIsCorrect(String generatedOTP, String user_OTP_From_UI) throws IncorrectOTPException{
        try{
                if(!generatedOTP.equals(user_OTP_From_UI.trim()))
                    throw  new IncorrectOTPException("RegistrationActivity"," Wrong OTP!! Please try again!!");

        }catch (IncorrectOTPException ioe){
            throw ioe;
        }
        return true;
    }
    public boolean checkIfUserExistsAndDecideWhatToDoNext(final String userMobileNumber) {
        printMsg("RegAct_chkExists","Checking if user exists..");
        boolean userExists = regAct_daoBot.checkIfUserExists(userMobileNumber);
        printMsg("RegAct_chkExists","Checking if user exists resulted in : "+userExists);
        return userExists;
    }
}
