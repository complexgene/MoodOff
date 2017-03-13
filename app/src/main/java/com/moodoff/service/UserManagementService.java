package com.moodoff.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.moodoff.dao.UserManagementdaoImpl;
import com.moodoff.dao.UserManagementdaoInterface;
import com.moodoff.exceptions.IncorrectOTPException;
import com.moodoff.exceptions.SMSGateWayException;
import com.moodoff.exceptions.ValidationException;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.User;
import com.moodoff.ui.RegistrationActivity;
import com.moodoff.validations.UserManagementValidationImpl;
import com.moodoff.validations.UserManagementValidationInterface;

import java.io.IOException;

import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by Santanu on 3/11/2017.
 */
// Handles all the user management tasks like registration, fetching data of user etc etc.
public class UserManagementService {
    private Context context;
    public UserManagementService(Context context){
        this.context = context;
    }
    //--------------------------------------------------------------------------------------------
    UserManagementValidationInterface userManagementValidation = new UserManagementValidationImpl();
    UserManagementdaoInterface userManagementDao = new UserManagementdaoImpl();
    private StoreRetrieveDataInterface rd=null;
    private User singleTonUserObject = User.getInstance();
    //--------------------------------------------------------------------------------------------

    // Validation Operations
    public boolean validateRegistrationData (String userName,String userMobileNumber, String userBirthday, String userTextStatus) throws ValidationException {
        return userManagementValidation.validateRegistrationData(userName, userMobileNumber, userBirthday, userTextStatus);
    }

    // OTP Operations
    public String generateOTPForUser() throws SMSGateWayException {
        String otpGotFromSMSGateway;
        try{
            // Logic to Access SMS Gateway API to initiate OTP message and get the new OTP Value
            otpGotFromSMSGateway = "123456";

        }catch (SMSGateWayException smsException){
            throw smsException;
        }
        return otpGotFromSMSGateway;
    }
    public boolean validateOTPLength(String otp){
        return userManagementValidation.validateOTPLength(otp);
    }
    public boolean checkIfOTPIsCorrect(String generatedOTP, String user_OTP_From_UI) throws IncorrectOTPException {
        try{
            if(!generatedOTP.equals(user_OTP_From_UI.trim()))
                throw  new IncorrectOTPException("UserManagementService"," Wrong OTP!! Please try again!!");
        }catch (IncorrectOTPException ioe){
            throw ioe;
        }
        return true;
    }

    // User Related Operations
    public boolean createUserProfileFileinLocal(User singleTonUser) {
        try {
            // Storing all the registration details into the text file
            printMsg("UserManagementService","Creating UserData.txt file and writing user data into that..");
            rd = new StoreRetrieveDataImpl("UserData.txt");
            rd.beginWriteTransaction();
            rd.createNewData("userName", singleTonUser.getUserName());
            rd.createNewData("userMobileNumber", singleTonUser.getUserMobileNumber());
            rd.createNewData("userDateOfBirth", singleTonUser.getUserDateOfBirth());
            rd.createNewData("userTextStatus", singleTonUser.getUserTextStatus());
            rd.createNewData("userAudioStatus", singleTonUser.getUserAudioStatusSong());
            rd.createNewData("userScore",String.valueOf(singleTonUser.getUserScore()));
            rd.createNewData("userNumberOfOldNotifications",String.valueOf(singleTonUser.getUserNumberOfOldNotifications()));
            rd.endWriteTransaction();
            printMsg("UserManagementService","Creating text file in local and writing user data to that completed..");
            return true;
        } catch (IOException e) {
            printMsg("UserManagementService_Err01", "Couldn't save the local file..");
            return false;
        }
    }
    public boolean registerUser(User singleTonUser){
        printMsg("UserManagementService","Calling UserManagamentServiceDao to store data to cloud DB..");
        return userManagementDao.storeUserDataToCloudDB(singleTonUser);
    }
    public boolean populateUserData(){
        StoreRetrieveDataInterface rd = null;
        try {
            printMsg("UserManagementService","Reading Userdata.txt file from local and populating the POJO");
            rd = new StoreRetrieveDataImpl("UserData.txt");
            if (rd.fileExists()) {
                rd.beginReadTransaction();
                singleTonUserObject.setUserName(rd.getValueFor("userName"));
                singleTonUserObject.setUserMobileNumber(rd.getValueFor("userMobileNumber"));
                singleTonUserObject.setUserDateOfBirth(rd.getValueFor("userDateOfBirth"));
                singleTonUserObject.setUserTextStatus(rd.getValueFor("userTextStatus"));
                singleTonUserObject.setUserAudioStatusSong(rd.getValueFor("userAudioStatus"));
                singleTonUserObject.setUserScore(Integer.parseInt(rd.getValueFor("userScore")));
                singleTonUserObject.setUserNumberOfOldNotifications(Integer.parseInt(rd.getValueFor("userNumberOfOldNotifications")));
                rd.endReadTransaction();
                printMsg("UserManagementService","User object populated:\n" + singleTonUserObject.toString());
                return true;
            }
            else {

                return false;
            }
        }
        catch(Exception ee){
            printMsg("UserManagementService","Reading file from local encountered some problem..");
        }
        return true;
    }


}