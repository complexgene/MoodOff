package com.moodoff.errors;

/**
 * Created by Shan on 2/21/2017.
 */

public enum Errors {
    // General Errors
    USER_EXISTS("MO0001","Seems the user already exists!!"),
    STREAM_ERROR("MO0002","BufferedReader couldn't be closed.");

    // Code
    private String errorCode;
    private String errorMessage;
    private String extraInfo;
    Errors(String errorCode, String errorMessage){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
