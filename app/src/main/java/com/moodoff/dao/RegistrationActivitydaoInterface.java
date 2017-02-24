package com.moodoff.dao;

import com.moodoff.model.UserDetails;

/**
 * Created by Shan on 2/24/2017.
 */

public interface RegistrationActivitydaoInterface {
    boolean checkIfUserExists(final String userMobileNumber);
    void storeUserDataToCloudDB(UserDetails singleTonUser);
}
