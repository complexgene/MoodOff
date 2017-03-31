package com.moodoff.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.R;
import com.moodoff.exceptions.IncorrectOTPException;
import com.moodoff.exceptions.ValidationException;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.User;
import com.moodoff.service.StartedTodoService;
import com.moodoff.service.UserManagementService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.moodoff.helper.LoggerBaba.printMsg;

public class RegistrationActivity extends AppCompatActivity {
    // ---------------------------------------------------------------------------------
    //     Declaration space for all helper classes
    // ---------------------------------------------------------------------------------
    private StartedTodoService startedTodoService;
    private UserManagementService userManagementService;
    private User singleTonUser;
    private String userName, userMobileNumber, userBirthday, userTextStatus, userAudioStatus, valueOfOTP, generatedOTP;
    private int userScore, userOldNotificationCount, year, month, day;
    private Calendar calendar;

    // ---------------------------------------------------------------------------------
    //     Declartion for all helper classes is done
    // ---------------------------------------------------------------------------------

    // --------------------------------------------------
    //      Declaration space for all UI variables
    // --------------------------------------------------
    private EditText et_userName,et_userMobileNumber,et_userBirthday,et_userTextStatus,et_otp;
    private ProgressBar progressRegistration;
    private TextView error, statusMsg;
    private Button register, btn_resendotp;
    private Context currentContext;
    private ImageButton btn_okButton, btn_cancelButton;
    private Dialog dialog;
    // -------------------------------------------------
    //     Declaration of all varaibles complete
    // --------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        // Initilaize all the UI Components
        initComponents();
        startedTodoService.createAllNecessaryTablesForAppOperation();
        // Clicking on any textbox should remove the red error line if any displayed earlier
        // as the user is going to rectify the value now.
        et_userName.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});
        et_userMobileNumber.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});
        et_userBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                error.setText("");
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                setDate();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                printMsg("RegistrationActivity","Checking for registration validation data..");
                if (validateRegistrationData()) {
                    printMsg("RegistrationActivity","Registration validation data is valid..");
                    printMsg("RegistrationActivity","OTP from SMS Gateway to cloud..");
                    generatedOTP = userManagementService.generateOTPForUser();
                    printMsg("RegistrationActivity","OTP successfully added to cloud..");
                    printMsg("RegistrationActivity","Initiate OTP Panel..");
                    showAndValidateOTP();
                }
            }
        });
    }
    private void initComponents(){
        currentContext = getApplicationContext();
        startedTodoService = new StartedTodoService(currentContext);
        userManagementService = new UserManagementService(currentContext);
        singleTonUser = User.getInstance();
        register = (Button)findViewById(R.id.btn_register);
        et_userName = (EditText) findViewById(R.id.name);
        et_userMobileNumber = (EditText) findViewById(R.id.et_phone_number);
        et_userBirthday = (EditText) findViewById(R.id.et_date_of_birth);
        et_userTextStatus = (EditText) findViewById(R.id.status_text);
        error = (TextView) findViewById(R.id.error_message);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        progressRegistration = (ProgressBar)findViewById(R.id.progressBarRegistration);
        progressRegistration.setVisibility(View.INVISIBLE);
    }
    private boolean validateRegistrationData(){
        try {
            // Get the values from UI, and populate reference variable fields
            userName = et_userName.getText().toString();
            userMobileNumber = et_userMobileNumber.getText().toString();
            userBirthday = et_userBirthday.getText().toString();
            userTextStatus = et_userTextStatus.getText().toString();
            userAudioStatus = "romantic@HERO.mp3";
            userScore = 0;
            userOldNotificationCount = 0;
            // Call for validation of the fetched data
            userManagementService.validateRegistrationData(userName,userMobileNumber,userBirthday,userTextStatus);
            return true;
        }
        catch(ValidationException ve){
            error.setText(ve.getMessage());
        }
        return false;
    }
    private void showAndValidateOTP(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_otp_validation);
        dialog.setCancelable(false);
        dialog.show();

        btn_okButton = (ImageButton)dialog.findViewById(R.id.btn_otp_layout_ok);
        btn_cancelButton = (ImageButton)dialog.findViewById(R.id.btn_otp_layout_cancel);
        btn_resendotp = (Button)dialog.findViewById(R.id.btn_otp_layout_resendotp);
        btn_resendotp.setVisibility(View.GONE);
        statusMsg = (TextView)dialog.findViewById(R.id.tv_status_msg);
        statusMsg.setVisibility(View.GONE);
        et_otp = (EditText)dialog.findViewById(R.id.et_otp);

        et_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusMsg.setVisibility(View.GONE);
                btn_resendotp.setVisibility(View.GONE);
            }
        });

        btn_okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    valueOfOTP = et_otp.getText().toString();
                    printMsg("RegistrationActivity","Validation of OTP initiated..");
                    userManagementService.validateOTPLength(valueOfOTP);
                    printMsg("RegistrationActivity","Validation of OTP successful..");
                    printMsg("RegistrationActivity","Checking if OTP is correct..");
                    if(userManagementService.checkIfOTPIsCorrect(generatedOTP,valueOfOTP)){
                        printMsg("RegistrationActivity","OTP entered is correct..");
                        printMsg("RegistrationActivity","Creating the User POJO..");
                        // Populate the POJO
                        singleTonUser.setUserName(userName);
                        singleTonUser.setUserMobileNumber(userMobileNumber);
                        singleTonUser.setUserDateOfBirth(userBirthday);
                        singleTonUser.setUserTextStatus(userTextStatus);
                        singleTonUser.setUserAudioStatusSong(userAudioStatus);
                        singleTonUser.setUserScore(userScore);
                        singleTonUser.setUserNumberOfOldNotifications(0);
                        singleTonUser.setUserNumberOfOldLikedDedicates(0);
                        printMsg("RegistrationActivity","Creating User POJO done..");
                        if(userManagementService.createUserProfileFileinLocal(singleTonUser)){
                            if(userManagementService.registerUser(singleTonUser)){
                                Messenger.print(getApplicationContext(),"Registration Successful!!");
                                dialog.dismiss();
                                Intent ii = new Intent(RegistrationActivity.this, Start.class);
                                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                                finish(); // destroy current activity..
                                startActivity(ii);
                            }
                        }
                    }
                }catch (ValidationException ve){
                    statusMsg.setVisibility(View.VISIBLE);
                    et_otp.setText("");
                    et_otp.setHint("Enter OTP Again");
                    statusMsg.setText(ve.getMessage());
                }catch(IncorrectOTPException ioe){
                    statusMsg.setVisibility(View.VISIBLE);
                    statusMsg.setText(ioe.getMessage());
                    btn_resendotp.setVisibility(View.VISIBLE);
                }
            }
        });
        btn_cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_resendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_resendotp.setVisibility(View.GONE);
                et_otp.setText("");
                printMsg("RegistrationActivity","Initiating OTP addition to cloud DB..");
                generatedOTP = userManagementService.generateOTPForUser();
                printMsg("RegistrationActivity","New OTP added to cloud DB..");
                statusMsg.setText("New OTP Sent To You.");
            }
        });
    }

    //--------Calendar init codes and calendar representation----------NO NEED TO MODIFY THIS----//
    public void setDate() {
        new DatePickerDialog(this, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };
    private void updateLabel() {
        String myFormat = "dd-MM-yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        et_userBirthday.setText(sdf.format(calendar.getTime()));
    }
    //--------Calendar thing ends------------------------------------------//

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
