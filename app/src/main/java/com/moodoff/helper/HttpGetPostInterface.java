package com.moodoff.helper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by snaskar on 10/12/2016.
 */

public interface HttpGetPostInterface {
    final String serverURL = "http://hipilab.com/moodoff";
    final String serverSongURL = "http://hipilab.com/data/songs";
    final String serverStoriesURL = "http://hipilab.com/data/stories";
    public void postDataToServer(String Url, HashMap<String,String> extraParameters);
    public String getNotificationsFromServer(String phoneNumber);
}
