package com.moodoff.helper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by snaskar on 11/14/2016.
 */

public class PlaylistSongs {
    public static HashMap<String,ArrayList<String>> allMoodPlayList = new HashMap<>();

    public static HashMap<String, ArrayList<String>> getAllMoodPlayList() {
        return allMoodPlayList;
    }

    public static void setAllMoodPlayList(HashMap<String, ArrayList<String>> allMoodPlayList) {
        PlaylistSongs.allMoodPlayList = allMoodPlayList;
    }
}
