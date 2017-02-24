package com.moodoff.ui;

import android.app.Activity;
import android.media.MediaPlayer;

/**
 * Created by snaskar on 11/18/2016.
 */

public class SingleTonMediaPlayer extends Activity{
    private static MediaPlayer singleTonMediaPlayer;
    private SingleTonMediaPlayer(){}
    public static MediaPlayer getSingleTonMediaPlayerInstance(){
        if(singleTonMediaPlayer == null){
            singleTonMediaPlayer = new MediaPlayer();
        }
        return singleTonMediaPlayer;
    }
}
