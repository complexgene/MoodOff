package com.moodoff.exceptions;

import android.util.Log;

/**
 * Created by santanu on 22/2/17.
 */

public class ValidationException extends RuntimeException {
    public ValidationException(String module, String errorMessage){
        super(errorMessage);
        Log.e(module, errorMessage);
    }
    public ValidationException(String module, String errorMessage, Exception e){
        super(errorMessage);
        Log.e(module, errorMessage + "\nException Message:" + e.getMessage());
    }
}
