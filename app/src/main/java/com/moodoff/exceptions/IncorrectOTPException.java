package com.moodoff.exceptions;

import android.util.Log;

/**
 * Created by Shan on 2/24/2017.
 */

public class IncorrectOTPException extends RuntimeException {
    public IncorrectOTPException(String module, String errorMessage){
        super(errorMessage);
        Log.e(module, errorMessage);
    }
}
