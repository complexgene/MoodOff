package com.moodoff.model;

import com.moodoff.helper.HttpGetPostInterface;

/**
 * Created by snaskar on 10/11/2016.
 */

public class UserDetails {
    private String userName;
    private String mobileNumber;
    private String dateOfBirth;
    private String userTextStatus;
    private String userAudioStatusSong;
    private int score;
    private int numberOfOldNotifications;
    private static UserDetails instance;

    private UserDetails(){
        this.userAudioStatusSong = HttpGetPostInterface.serverSongURL+"romantic/HERO.mp3";
    } // To maintain singleTon property across app
    public static UserDetails getInstance(){
        if(instance == null){
            instance = new UserDetails();
        }
        return instance;
    } // same instance will be returned everytime a call is made to this

    public int getNumberOfOldNotifications() {
        return numberOfOldNotifications;
    }
    public void setNumberOfOldNotifications(int numberOfOldNotifications) {
        this.numberOfOldNotifications = numberOfOldNotifications;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String uN) {
        userName = uN;
    }
    public String getMobileNumber() {
        return mobileNumber;
    }
    public void setMobileNumber(String pN) {
        mobileNumber = pN;
    }
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    public String getUserTextStatus(){return userTextStatus;}
    public void setUserTextStatus(String userTextStatus) {
        this.userTextStatus = userTextStatus;
    }
    public String getUserAudioStatusSong() {
        return userAudioStatusSong;
    }
    public void setUserAudioStatusSong(String userAudioStatusSong) {
        this.userAudioStatusSong = userAudioStatusSong;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
}
