package com.moodoff.validations;

import com.moodoff.exceptions.ValidationException;

/**
 * Created by Santanu on 3/11/2017.
 */

public class UserManagementValidationImpl implements UserManagementValidationInterface{
        public boolean validateRegistrationData (String userName,String userMobileNumber, String userBirthday, String userTextStatus) throws ValidationException {
            if (userName.isEmpty()) {
                throw new ValidationException("UserManagementValidationImpl","Error: Enter Your Name!!");
            } else if (userMobileNumber.isEmpty()) {
                throw new ValidationException("UserManagementValidationImpl","Error: Enter Your Phone Number!!");
            } else if (userBirthday.isEmpty()) {
                throw new ValidationException("UserManagementValidationImpl","Error: Enter Your Birthday!!");
            } else if (userTextStatus.isEmpty()) {
                throw new ValidationException("UserManagementValidationImpl","Error: Enter Your Text Status!!");
            }  else {
                return true;
            }
        }
        public boolean validateOTPLength(String otp) throws ValidationException{
            if(otp.length()<6 || otp.length()>6)
                throw new ValidationException("UserManagementValidationImpl"," Invalid length of OTP");
            return true;
        }
}
