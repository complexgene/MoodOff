package com.moodoff.exceptions;

import android.util.Log;

/**
 * Created by santanu on 22/2/17.
 */

public class GenericException extends Exception {
    public GenericException(String module, String errorMessage){
        super(errorMessage);
        Log.e(module, errorMessage);
    }
    public GenericException(String module, String errorMessage, Exception e){
        super(errorMessage);
        Log.e(module, errorMessage);
    }
}
