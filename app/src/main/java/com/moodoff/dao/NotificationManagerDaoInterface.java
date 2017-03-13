package com.moodoff.dao;

/**
 * Created by Santanu on 3/14/2017.
 */

public interface NotificationManagerDaoInterface {
    boolean writeSongDedicateToCloudDB(String ts, String fromUser, final String toUser, String currentMood, String currentSong, String type);
}
