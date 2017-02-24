package com.moodoff.service;

import android.content.Context;

import com.moodoff.exceptions.IncorrectOTPException;
import com.moodoff.model.UserDetails;

/**
 * Created by santanu on 22/2/17.
 */

public interface RegistrationActivityServiceInterface {
    // This method creates all the tables for app operation before the app starts.
    void createAllNecessaryTablesForAppOperation(Context context);
    boolean checkIfOTPIsCorrect(String mobileNumber, String otp) throws IncorrectOTPException;
    String generateAndSendOTP(String userMobileNumber);
    boolean checkIfUserExistsAndDecideWhatToDoNext(final String userMobileNumber);
    void storeUserDataToCloudDB(UserDetails singleTonUser);
}
