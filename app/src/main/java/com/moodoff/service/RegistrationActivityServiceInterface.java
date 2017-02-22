package com.moodoff.service;

import android.content.Context;

/**
 * Created by santanu on 22/2/17.
 */

public interface RegistrationActivityServiceInterface {
    // This method creates all the tables for app operation before the app starts.
    void createAllNecessaryTablesForAppOperation(Context context);

}
