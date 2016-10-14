package com.moodoff.helper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by snaskar on 10/12/2016.
 */

public interface HttpGetPostInterface {
    final String serverURL = "http://192.168.2.7:5002/controller/moodoff";
    public void postDataToServer(String Url, HashMap<String,String> extraParameters);
    public String getNotificationsFromServer(String phoneNumber);
}
