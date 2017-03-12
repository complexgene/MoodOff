package com.moodoff.validations;

import com.moodoff.exceptions.ValidationException;

/**
 * Created by Santanu on 3/11/2017.
 */

public interface UserManagementValidationInterface {
    boolean validateRegistrationData (String userName,String userPhoneNumber, String userBirthday, String userTextStatus) throws ValidationException;
    boolean validateOTPLength(String otp) throws ValidationException;
}
