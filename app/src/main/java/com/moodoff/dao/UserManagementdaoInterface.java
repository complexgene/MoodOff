package com.moodoff.dao;

import com.moodoff.model.User;

/**
 * Created by Santanu on 3/11/2017.
 */

public interface UserManagementdaoInterface {
    boolean storeUserDataToCloudDB(User singleTonUser);
}
