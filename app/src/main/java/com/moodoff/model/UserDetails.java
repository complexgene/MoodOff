package com.moodoff.model;

import com.moodoff.helper.HttpGetPostInterface;

/**
 * Created by snaskar on 10/11/2016.
 */

public class UserDetails {
    private String userName;
    private String userMobileNumber;
    private String userDateOfBirth;
    private String userTextStatus;
    private String userAudioStatusSong;
    private int userScore;
    private int userNumberOfOldNotifications;
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
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserMobileNumber() {
        return userMobileNumber;
    }
    public void setUserMobileNumber(String userMobileNumber) {
        this.userMobileNumber = userMobileNumber;
    }
    public String getUserDateOfBirth() {
        return userDateOfBirth;
    }
    public void setUserDateOfBirth(String userDateOfBirth) {
        this.userDateOfBirth = userDateOfBirth;
    }
    public String getUserTextStatus() {
        return userTextStatus;
    }
    public void setUserTextStatus(String userTextStatus) {
        this.userTextStatus = userTextStatus;
    }
    public String getUserAudioStatusSong() {
        return userAudioStatusSong;
    }
    public void setUserAudioStatusSong(String userAudioStatusSong) {
        this.userAudioStatusSong = userAudioStatusSong;
    }
    public int getUserScore() {
        return userScore;
    }
    public void setUserScore(int userScore) {
        this.userScore = userScore;
    }
    public int getUserNumberOfOldNotifications() {
        return userNumberOfOldNotifications;
    }
    public void setUserNumberOfOldNotifications(int userNumberOfOldNotifications) {
        this.userNumberOfOldNotifications = userNumberOfOldNotifications;
    }
}
