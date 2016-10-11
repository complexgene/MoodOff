package com.moodoff.model;

/**
 * Created by snaskar on 10/11/2016.
 */

public class UserDetails {
    private static String userName;
    private static String phoneNumber;
    private static String emailId;

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
}
