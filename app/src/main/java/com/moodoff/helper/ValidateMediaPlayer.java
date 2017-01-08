package com.moodoff.helper;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.Log;

import com.moodoff.GenericMood;
import com.moodoff.NotificationFragment;
import com.moodoff.Profile;
import com.moodoff.R;

import java.util.ArrayList;

public class ValidateMediaPlayer {

    //private MediaPlayer currentMediaPlayer = null;
    private String currentTab = null;

    private static ValidateMediaPlayer validateMediaPlayer;
    private ValidateMediaPlayer(){}
    public static ValidateMediaPlayer getValidateMediaPlayerInstance(){
        if(validateMediaPlayer == null) {
            validateMediaPlayer = new ValidateMediaPlayer();
        }
        return validateMediaPlayer;
    }

    GenericMood genericMoodTab = new GenericMood();
    NotificationFragment notificationTab = new NotificationFragment();
    Profile profileTab = new Profile();

    ListOfTabs tabListObj = new ListOfTabs();
    ArrayList<String> listOfTabs = tabListObj.getListOfTabs();

    public void initialiseAndValidateMediaPlayer(String changedTab, String action) {
        Log.e("ValidateMP","CurrTab:"+currentTab+"\tChanTab:"+changedTab);
        if (action == "play") {
            //Pause other mp if playing
            Log.e("ValidateMP","Changed Tab;Size:"+listOfTabs.size());
            for(int i=0; i<listOfTabs.size(); i++) {
                if(listOfTabs.get(i)!=changedTab.toLowerCase()) {
                    Log.e("ValidateMP_pause","frag:"+listOfTabs.get(i));
                    switch(listOfTabs.get(i)) {
                        case "mood":
                            //Log.e("ValidateMP_pause","mood:mp="+mediaPlayer.toString());
                            if(genericMoodTab.mp!=null) {
                                if(genericMoodTab.mp.isPlaying()){
                                    Log.e("ValidateMP","mood-pause");
                                    genericMoodTab.mp.pause();
                                    genericMoodTab.isPlayOrPauseFromGM = 0;
                                    genericMoodTab.showPlayPauseButton("play");
                                }
                            }
                            break;
                        case "notification":
                            //Log.e("ValidateMP_pause","notification:mp="+mediaPlayer.toString());
                            if(notificationTab.mp!=null){
                                if(notificationTab.mp.isPlaying()){
                                    Log.e("ValidateMP","notification-stop");
                                    notificationTab.mp.stop();
                                    if(notificationTab.playOrStopButton!=null){
                                        notificationTab.playOrStopButton.setImageResource(R.drawable.play);
                                        notificationTab.playOrStopButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(229,152,245)));
                                    }
                                    if(notificationTab.currentSeekBar!=null){
                                        notificationTab.currentSeekBar.setMax(0);
                                        notificationTab.currentSeekBar.setEnabled(false);
                                    }
                                }
                            }
                            break;
                        case "profile":
                            //Log.e("ValidateMP_pause","profile:mp="+mediaPlayer.toString());
                            if(profileTab.mediaPlayer!=null){
                                if(profileTab.mediaPlayer.isPlaying()){
                                    Log.e("ValidateMP","profile-stop");
                                    profileTab.mediaPlayer.stop();
                                    profileTab.releaseMediaPlayerObject(profileTab.mediaPlayer);
                                    profileTab.showPlayStopButton("play");
                                    profileTab.seekBar.setMax(0);
                                    profileTab.seekBar.setClickable(false);
                                }
                            }
                            break;
                    }
                } else {
                    Log.e("ValidateMP_init","frag:"+listOfTabs.get(i));
                    switch(listOfTabs.get(i)) {
                        case "mood":
                            Log.e("ValidateMP_init","mood");
                            break;
                        case "notification":
                            Log.e("ValidateMP_init","notification");
                            break;
                        case "profile":
                            Log.e("ValidateMP_init","profile");
                            break;
                    }
                }
            }
        } else {
            if(changedTab=="notification"||changedTab=="profile") {
                Log.e("ValidateMP","else-resume");
                if(genericMoodTab.mp!=null) {
                    Log.e("ValidateMP","else-not-null");
                    if(!genericMoodTab.mp.isPlaying()){
                        if(genericMoodTab.isPlayOrPauseFromGM==0) {
                            Log.e("ValidateMP","else-not-playing");
                            Log.e("ValidateMP","mood-resume");
                            genericMoodTab.mp.start();
                            genericMoodTab.showPlayPauseButton("pause");
                        }
                    }
                }
            }
        }
        currentTab = changedTab;
    }

    /*release and return the nullified mediaplayer object*/
    public static void releaseMediaPlayerObject(MediaPlayer mp) {
        try {
            if (mp != null) {
                if(mp.isPlaying()){mp.stop();}
                mp.release();
                mp = null;
            }
        } catch(Exception e){e.fillInStackTrace();e.printStackTrace();}
    }

}
