package com.moodoff.dao;

import android.widget.ImageButton;

/**
 * Created by Santanu on 3/14/2017.
 */

public interface NotificationManagerDaoInterface {
    //void detectChangeInNotificationNode(String userMobileNumber);
    boolean writeSongDedicateToCloudDB(String ts, String fromUser, final String toUser, String currentMood, String currentSong, String type);
    void likeTheDedicatedSong(String fromUserNumber, String toUserNumber, String currentMoodType, String currentSong, String timeStamp);
}
