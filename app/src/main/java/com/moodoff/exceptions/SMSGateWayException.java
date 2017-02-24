package com.moodoff.exceptions;

import android.util.Log;

/**
 * Created by Shan on 2/25/2017.
 */

public class SMSGateWayException extends RuntimeException {
    public SMSGateWayException(String module, String errorMessage){
        super(errorMessage);
        Log.e(module, errorMessage);
    }
}
