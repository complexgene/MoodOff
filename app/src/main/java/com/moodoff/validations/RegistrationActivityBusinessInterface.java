package com.moodoff.validations;

import com.moodoff.exceptions.ValidationException;

/**
 * Created by santanu on 22/2/17.
 */

public interface RegistrationActivityBusinessInterface {
    boolean validateRegistrationData (String userName,String userPhoneNumber, String userBirthday, String userTextStatus) throws ValidationException;
    boolean validateOTPLength(String otp) throws ValidationException;
}
