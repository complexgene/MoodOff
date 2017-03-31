package com.moodoff.service;

import android.app.Activity;
import android.widget.Button;
import android.widget.ImageButton;

import com.moodoff.helper.ServerManager;

/**
 * Created by Santanu on 3/14/2017.
 */

public class NotificationService {
    Activity activity;
    public NotificationService(Activity activity){
        this.activity = activity;
    }
    public boolean writeSongDedicateToCloudDB(String ts, String fromUser, final String toUser, String currentMood, String currentSong, String type){
        ServerManager serverManager = new ServerManager(activity);
        boolean serverWriteSuccessOrFailure = serverManager.writeSongDedicateToCloudDB(ts, fromUser, toUser, currentMood, currentSong, type);
        return serverWriteSuccessOrFailure;
    }

    public void likeTheDedicatedSong(String fromUserNumber, String toUserNumber, String currentMoodType, String currentSong, String timeStamp) {
        ServerManager serverManager = new ServerManager();
        serverManager.likeTheDedicatedSong(fromUserNumber, toUserNumber, currentMoodType, currentSong, timeStamp);
    }
}
