package com.moodoff.validations;
import com.moodoff.exceptions.ValidationException;

import static com.moodoff.helper.LoggerBaba.printMsg;

/**
 * Created by santanu on 22/2/17.
 */

public class RegistrationActivityBusinessImpl implements RegistrationActivityBusinessInterface {
    public boolean validateRegistrationData (String userName,String userMobileNumber, String userBirthday, String userTextStatus) throws ValidationException{
        if (userName.isEmpty()) {
            throw new ValidationException("RegistrationActivity","Error: Enter Your Name!!");
        } else if (userMobileNumber.isEmpty()) {
            throw new ValidationException("RegistrationActivity","Error: Enter Your Phone Number!!");
        } else if (userBirthday.isEmpty()) {
            throw new ValidationException("RegistrationActivity","Error: Enter Your Birthday!!");
        } else if (userTextStatus.isEmpty()) {
            throw new ValidationException("RegistrationActivity","Error: Enter Your Text Status!!");
        }  else {
            return true;
        }
    }
    public boolean validateOTPLength(String otp) throws ValidationException{
        if(otp.length()<6 || otp.length()>6 || otp.matches("((A-Z)|(a-z))+"))
            throw new ValidationException("RegistrationActivity"," Invalid length of OTP");
        return true;
    }
}
