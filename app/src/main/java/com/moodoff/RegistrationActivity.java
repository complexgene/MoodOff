package com.moodoff;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.exceptions.ValidationException;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;
import com.moodoff.service.RegistrationActivityServiceImpl;
import com.moodoff.service.RegistrationActivityServiceInterface;
import com.moodoff.validations.RegistrationActivityBusinessImpl;
import com.moodoff.validations.RegistrationActivityBusinessInterface;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {
    //---------------------------------------------------------------------------------
    // Declaration space for all helper classes
    //---------------------------------------------------------------------------------
    RegistrationActivityServiceInterface registrationServiceBot = new RegistrationActivityServiceImpl();
    RegistrationActivityBusinessInterface registrationValidationBot = new RegistrationActivityBusinessImpl();
    StoreRetrieveDataInterface rd=null;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mRootRef = firebaseDatabase.getReference().child("allusers");
    DatabaseReference dbRef;
    UserDetails singleTonUser;
    //---------------------------------------------------------------------------------
    // Declartion for all helper classes is done
    //---------------------------------------------------------------------------------

    //--------------------------------------------------
    // Declaration space for all UI variables
    //--------------------------------------------------
    private EditText et_userName,et_userMobileNumber,et_userBirthday,et_userTextStatus;
    private String userName, userMobileNumber, userBirthday, userTextStatus, userAudioStatus;
    private int userScore, userOldNotificationCount;
    private ProgressBar progressRegistration;
    private TextView error;
    private Calendar calendar;
    private int year, month, day;
    private Button register;
    private DBHelper dbOperations;
    private Context currentContext;
    // -------------------------------------------------
    // Declaration of all varaibles complete
    //--------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initilaize all the UI Components
        initComponents();
        registrationServiceBot.createAllNecessaryTablesForAppOperation(currentContext);

        // Clicking on any textbox should remove the red error line if any displayed earlier
        // as the user is going to rectify the value now.
        et_userName.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});
        et_userMobileNumber.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});
        et_userBirthday.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
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
                if (validateRegistrationData()) {
                    showAndValidateOTP();
                     /***
                      showOTPDialog
                      if(OTP successful) populatesingleTonUser();
                      else backoff;
                     */
                    //showOTPdialog
                    //checkIfUserExistsAndProceed(mobile_number.getText().toString());
                    Log.e("RegistrationActivity","Perfect");
                }
            }
        });
    }

    private void initComponents(){
        currentContext = getApplicationContext();
        singleTonUser = UserDetails.getInstance();
        dbOperations = new DBHelper(this);
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
            userAudioStatus = "romantic@HERO.com";
            userScore = 0;
            userOldNotificationCount = 0;

            // Call for validation of the fetched data
            registrationValidationBot.validateRegistrationData(userName,userMobileNumber,userBirthday,userTextStatus);
            return true;
        }catch(ValidationException ve){
            error.setText(ve.getMessage());
        }
        return false;
    }
    private void showAndValidateOTP(){
        Dialog dialog = new Dialog(currentContext);
        dialog.setContentView(R.layout.gamerules);
        dialog.show();
    }

    private void checkIfUserExists(final String userMobileNumber) {
        mRootRef.child(userMobileNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    error.setText("Seems you used this app before!! Wait while we fetch your old data!!");
                }
                else {
                    Messenger.printCenter(currentContext,"Registering.. Wait!!");
                    progressRegistration.setVisibility(View.VISIBLE);
                    register();
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }


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



    public void register() {
        try{
            if(saveUserProfileIsSuccesful()) {
                Toast.makeText(getApplicationContext(), "Successfully Registered", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(RegistrationActivity.this, Start.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                finish(); // destroy current activity..
                startActivity(mainIntent);
            }
            else{
                error.setText("Sorry!! Seems there is a technical issue now!! ");
            }
        } catch (Exception ee) {
            Log.e("RegistrationAct_Err", ee.getMessage());
            ee.printStackTrace();
        }
    }

    public boolean saveUserProfileIsSuccesful() {
        try {

            // Storing all the registration details into the text file
            rd = new StoreRetrieveDataImpl("singleTonUser.txt");
            rd.beginWriteTransaction();
            rd.createNewData("userName", userName);
            rd.createNewData("userPhoneNo", userMobileNumber);
            rd.createNewData("userDob", userBirthday);
            rd.createNewData("userTextStatus",userTextStatus);
            rd.createNewData("userAudioStatus",userAudioStatus);
            rd.createNewData("userScore",String.valueOf(userScore));
            rd.createNewData("userNumberOfOldNotifications",String.valueOf(userOldNotificationCount));
            rd.endWriteTransaction();

            // Populate the POJO
            singleTonUser.setUserName(userName);
            singleTonUser.setmobileNumber(userMobileNumber);
            singleTonUser.setDateOfBirth(userBirthday);
            singleTonUser.setUserTextStatus(userTextStatus);
            singleTonUser.setUserAudioStatusSong(userAudioStatus);
            singleTonUser.setScore(userScore);
            singleTonUser.setNumberOfOldNotifications(userOldNotificationCount);

            // Store the details onto the cloud
            dbRef = mRootRef.child(userMobileNumber);
            dbRef.child("userName").setValue(singleTonUser.getUserName());
            dbRef.child("userPhoneNo").setValue(singleTonUser.getMobileNumber());
            dbRef.child("userDob").setValue(singleTonUser.getDateOfBirth());
            dbRef.child("userTextStatus").setValue(singleTonUser.getUserTextStatus());
            dbRef.child("userAudioStatus").setValue(singleTonUser.getUserAudioStatusSong());
            dbRef.child("userScore").setValue(singleTonUser.getScore());
            dbRef.child("userNumberOfOldNotifications").setValue(singleTonUser.getNumberOfOldNotifications());

            return true;
        } catch (IOException e) {
            error.setText("Couldn't save the file.");
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
