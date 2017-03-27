package com.moodoff.dao;

import android.app.Activity;
import android.widget.TextView;

import com.moodoff.model.User;
import com.moodoff.model.UserLiveMoodDetailsPojo;

import java.util.HashMap;

/**
 * Created by Santanu on 3/11/2017.
 */

public interface UserManagementdaoInterface {
    boolean storeUserDataToCloudDB(User singleTonUser);
    void voteForTextStatus(final String forUserMobileNumber, final String currentUserMobileNumber, final Activity profileActivity);
    void voteForAudioStatus(final String forUserMobileNumber, final String currentUserMobileNumber, final Activity profileActivity);
    void likeCurrentMood(final String userMobileNumber, String currentUserMobileNumber, Activity profileActivity);
    void loveCurrentMood(final String userMobileNumber, String currentUserMobileNumber, Activity profileActivity);
    void sadCurrentMood(final String userMobileNumber, String currentUserMobileNumber, Activity profileActivity);
    void setLiveMood(String userMobileNumber, UserLiveMoodDetailsPojo userLiveMoodDetails);
    void writeTextStatusChange(String userMobileNumber, final String newTextStatus, Activity curActivity, final TextView userTextStatus);
    void writeAudioStatusChange(String userMobileNumber, final String newTextStatus, Activity curActivity, final TextView userTextStatus);
    void setRebuildNotificationPanelNodeInCloud(String userMobileNumber);
}
