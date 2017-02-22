package com.moodoff;

import android.app.Activity;
import android.app.DatePickerDialog;
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
    //---------------------------------------------------------------------------------
    // Declartion for all helper classes is done
    //---------------------------------------------------------------------------------

    //--------------------------------------------------
    // Declaration space for all the variables
    //--------------------------------------------------
    private EditText name,mobile_number,birthday,status_text;
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
        name.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});
        mobile_number.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});
        birthday.setOnClickListener(new View.OnClickListener() {
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
                    /***
                      showOTPDialog
                      if(OTP successful) populateUserData();
                      else backoff;
                     */
                    //showOTPdialog
                    //checkIfUserExists(mobile_number.getText().toString());
                }
            }
        });
    }

    private void initComponents(){
        currentContext = getApplicationContext();
        dbOperations = new DBHelper(this);
        register = (Button)findViewById(R.id.btn_register);
        name = (EditText) findViewById(R.id.name);
        mobile_number = (EditText) findViewById(R.id.et_phone_number);
        birthday = (EditText) findViewById(R.id.et_date_of_birth);
        status_text = (EditText) findViewById(R.id.status_text);
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
            // Get the values from UI
            String  userName = name.getText().toString(),
                    userPhoneNumber = mobile_number.getText().toString(),
                    userBirthday = birthday.getText().toString(),
                    userTextStatus = status_text.getText().toString();

            // Call for validation of the fetched data
            registrationValidationBot.validateRegistrationData(userName,userPhoneNumber,userBirthday,userTextStatus);
            return true;
        }catch(ValidationException ve){
            error.setText(ve.getMessage());
        }
        return false;
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
        birthday.setText(sdf.format(calendar.getTime()));
    }

    private String getGenderNumber(String gender){
        switch(gender){
            case "Male":{return "0";}
            case "Female":{return "1";}
            case "NA":{return "2";}
        }
        return "2";
    }
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mRootRef = firebaseDatabase.getReference().child("allusers");
    DatabaseReference dbRef;

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
            // Get all the values from UI
            String userName = name.getText().toString();
            String userMobileNumber = mobile_number.getText().toString();
            String userBday = birthday.getText().toString();
            String userTextStatus = status_text.getText().toString();
            String userAudioStatus = "romantic@HERO.mp3";
            String userScore = "0";
            String userOldNotificationCount = "0";

            // Storing all the registration details into the text file
            rd = new StoreRetrieveDataImpl("UserData.txt");
            rd.beginWriteTransaction();
            rd.createNewData("userName", userName);
            rd.createNewData("userPhoneNo", userMobileNumber);
            rd.createNewData("userDob", userBday);
            rd.createNewData("userTextStatus",userTextStatus);
            rd.createNewData("userAudioStatus",userAudioStatus);
            rd.createNewData("userScore",userScore);
            rd.createNewData("userNumberOfOldNotifications",userOldNotificationCount);
            rd.endWriteTransaction();

            // Populate the POJO
            UserDetails userData = UserDetails.getInstance();
            userData.setUserName(rd.getValueFor("userName"));
            userData.setPhoneNumber(rd.getValueFor("userPhoneNo"));
            userData.setDateOfBirth(rd.getValueFor("userDob"));
            userData.setUserTextStatus(rd.getValueFor("userTextStatus"));
            userData.setUserAudioStatusSong(rd.getValueFor("userAudioStatus"));
            userData.setScore(Integer.parseInt(rd.getValueFor("userScore")));
            userData.setNumberOfOldNotifications(Integer.parseInt(rd.getValueFor("userNumberOfOldNotifications")));
            rd.endReadTransaction();

            // Store the details onto the cloud
            dbRef = mRootRef.child(mobile_number.getText().toString());
            dbRef.child("userName").setValue(userData.getUserName());
            dbRef.child("userPhoneNo").setValue(userData.getPhoneNumber());
            dbRef.child("userDob").setValue(userData.getDateOfBirth());
            dbRef.child("userTextStatus").setValue(userData.getUserTextStatus());
            dbRef.child("userAudioStatus").setValue(userData.getUserAudioStatusSong());
            dbRef.child("userScore").setValue(userData.getScore());
            dbRef.child("userNumberOfOldNotifications").setValue(userData.getNumberOfOldNotifications());

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
