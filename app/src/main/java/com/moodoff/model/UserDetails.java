package com.moodoff.model;

import com.moodoff.helper.HttpGetPostInterface;

/**
 * Created by snaskar on 10/11/2016.
 */

public class UserDetails {
    private String userName;
    private String phoneNumber;
    private String dateOfBirth;
    private String userTextStatus;
    private String userAudioStatusSong;
    private int score;
    private int numberOfOldNotifications;
    private static UserDetails instance;

    private UserDetails(){
        this.userAudioStatusSong = HttpGetPostInterface.serverSongURL+"romantic/HERO.mp3";
    }
    public static UserDetails getInstance(){
        if(instance == null){
            instance = new UserDetails();
        }
        return instance;
    }

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
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String pN) {
        phoneNumber = pN;
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
