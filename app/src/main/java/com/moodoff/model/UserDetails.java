package com.moodoff.model;

import com.moodoff.helper.HttpGetPostInterface;

/**
 * Created by snaskar on 10/11/2016.
 */

public class UserDetails {
    private static String userName;
    private static String phoneNumber;
    private static String emailId;
    private static String dateOfBirth;
    private static String userTextStatus;
    private static String userAudioStatusSong;

    public UserDetails(){
        userAudioStatusSong = HttpGetPostInterface.serverSongURL+"romantic/HERO.mp3";
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String uN) {
        userName = uN;
    }

    public static String getPhoneNumber() {
        return phoneNumber;
    }

    public static void setPhoneNumber(String pN) {
        phoneNumber = pN;
    }

    public static String getEmailId() {
        return emailId;
    }

    public static void setEmailId(String mailId) {
        emailId = mailId;
    }

    public static String getDateOfBirth() {
        return dateOfBirth;
    }

    public static void setDateOfBirth(String dateOfBirth) {
        UserDetails.dateOfBirth = dateOfBirth;
    }

    public static String getUserTextStatus(){return userTextStatus;}

    public static void setUserTextStatus(String userTextStatus) {
        UserDetails.userTextStatus = userTextStatus;
    }

    public static String getUserAudioStatusSong() {
        return userAudioStatusSong;
    }

    public static void setUserAudioStatusSong(String userAudioStatusSong) {
        UserDetails.userAudioStatusSong = userAudioStatusSong;
    }
}
