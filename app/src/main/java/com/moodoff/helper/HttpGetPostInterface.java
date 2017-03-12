package com.moodoff.helper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by snaskar on 10/12/2016.
 */

public interface HttpGetPostInterface {
    //final String serverURL = "http://192.168.2.3:5789/controller/moodoff";
    //final String serverURL = "http://hipilab.com/moodoff";
    final String serverURL = "https://moodoff-ff2cf.firebaseio.com";
    //final String serverSongURL = "http://hipilab.com/data/songs/";
    final String serverSongURL = "https://moodoff.000webhostapp.com/data/songs/";
    //final String serverStoriesURL = "http://hipilab.com/data/stories/";
    final String serverStoriesURL = "https://moodoff.000webhostapp.com/data/stories/";
    public void postDataToServer(String Url, HashMap<String,String> extraParameters);
    public String getNotificationsFromServer(String phoneNumber);
}
