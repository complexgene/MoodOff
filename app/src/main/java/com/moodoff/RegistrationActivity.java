package com.moodoff;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;

import static com.moodoff.helper.HttpGetPostInterface.serverURL;

public class RegistrationActivity extends AppCompatActivity {

    EditText name,mobile_number,birthday,email, status_text;
    RadioGroup genderType;String genderOfUser;
    ProgressBar progressRegistration;
    TextView error;
    Calendar calendar;
    int year, month, day;
    StoreRetrieveDataInterface rd=null;
    Button register;
    DBHelper dbOperations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initComponents();
        // Need to do this without any lag..
        createAllNecessaryTablesForAppOperation();

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
                    checkIfUserExists(mobile_number.getText().toString());
                }
            }
        });
    }

    private void initComponents(){
        dbOperations = new DBHelper(this);
        register = (Button)findViewById(R.id.btn_register);
        name = (EditText) findViewById(R.id.name);
        mobile_number = (EditText) findViewById(R.id.phone_number);
        birthday = (EditText) findViewById(R.id.date_of_birth);
        status_text = (EditText) findViewById(R.id.status_text);
        error = (TextView) findViewById(R.id.error_message);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        progressRegistration = (ProgressBar)findViewById(R.id.progressBarRegistration);
        progressRegistration.setVisibility(View.INVISIBLE);
    }

    private void checkIfUserExists(final String userMobileNumber) {
        mRootRef.child(userMobileNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    error.setText("Oops!! You are already registered!! :(");
                }
                else {
                    Toast.makeText(getApplicationContext(), "Registering...Wait!!", Toast.LENGTH_SHORT).show();
                    progressRegistration.setVisibility(View.VISIBLE);
                    register();
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void createAllNecessaryTablesForAppOperation(){
        DBHelper dboperations = new DBHelper(getApplicationContext());
        dbOperations.dropAllTables();

        LinkedHashMap<String,String> worktodoColumns = new LinkedHashMap<>();
        worktodoColumns.put("id","INTEGER PRIMARY KEY AUTOINCREMENT");
        worktodoColumns.put("api","VARCHAR");
        dboperations.createTable("worktodo",worktodoColumns);
        Log.e("RegistrationAct_TBL","worktodo table created.");

        LinkedHashMap<String,String> profilesColumns = new LinkedHashMap<>();
        profilesColumns.put("id","VARCHAR PRIMARY KEY");
        profilesColumns.put("name","VARCHAR");
        profilesColumns.put("mailid","VARCHAR");
        profilesColumns.put("dob","VARCHAR");
        profilesColumns.put("textstatus","VARCHAR");
        profilesColumns.put("audiostatus","VARCHAR");
        dboperations.createTable("profiles",profilesColumns);
        Log.e("RegistrationAct_TBL","profiles table created.");

        LinkedHashMap<String,String> rnotificationsColumns = new LinkedHashMap<>();
        rnotificationsColumns.put("from_user_id","VARCHAR");
        rnotificationsColumns.put("to_user_id","VARCHAR");
        rnotificationsColumns.put("file_name","VARCHAR");
        rnotificationsColumns.put("type","VARCHAR");
        rnotificationsColumns.put("send_done","INTEGER");
        rnotificationsColumns.put("create_ts","VARCHAR");
        dboperations.createTable("rnotifications",rnotificationsColumns);
        Log.e("RegistrationAct_TBL","rnotifications table created");

        LinkedHashMap<String,String> playListColumns = new LinkedHashMap<>();
        playListColumns.put("date","VARCHAR");
        playListColumns.put("mood_type","VARCHAR");
        playListColumns.put("song_name","VARCHAR");
        playListColumns.put("artist_name","VARCHAR");
        playListColumns.put("movie_or_album_name","VARCHAR");
        dboperations.createTable("playlist",playListColumns);
        Log.e("RegistrationAct_TBL","playlist table created");

        LinkedHashMap<String,String> allprofileColumns = new LinkedHashMap<>();
        allprofileColumns.put("phno","VARCHAR");
        allprofileColumns.put("name","VARCHAR");
        allprofileColumns.put("text_status","VARCHAR");
        allprofileColumns.put("audio_status","VARCHAR");
        allprofileColumns.put("text_status_likes","INTEGER");
        allprofileColumns.put("audio_status_likes","INTEGER");
        dboperations.createTable("all_profiles",allprofileColumns);
        Log.e("RegistrationAct_TBL","all_profiles table created");

        // createTable implementation for allcontacts is different as we are fetching data in table creation here.
        LinkedHashMap<String,String> contactsColumns = new LinkedHashMap<>();
        contactsColumns.put("phone_no","VARCHAR");
        contactsColumns.put("name","VARCHAR");
        contactsColumns.put("status","INTEGER");
        dboperations.createTable("allcontacts",contactsColumns);
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
    public boolean validateRegistrationData () {
        if (name.getText().toString().isEmpty()) {
            error.setText("Error: Name is mandatory.");return false;
        } else if (mobile_number.getText().toString().isEmpty()) {
            error.setText("Error: Phone number is mandatory.");return false;
        } else if (birthday.getText().toString().isEmpty()) {
            error.setText("Error: Birthday is mandatory.");return false;
        }  else {
            return true;
        }
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
            String userName = name.getText().toString();
            String userMobileNumber = mobile_number.getText().toString();
            String userBday = birthday.getText().toString();
            String userTextStatus = status_text.getText().toString();
            String userAudioStatus = "romantic@HERO.mp3";
            String userScore = "0";
            String userOldNotificationCount = "0";

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

            UserDetails userData = UserDetails.getInstance();
            userData.setUserName(rd.getValueFor("userName"));
            userData.setPhoneNumber(rd.getValueFor("userPhoneNo"));
            userData.setDateOfBirth(rd.getValueFor("userDob"));
            userData.setUserTextStatus(rd.getValueFor("userTextStatus"));
            userData.setUserAudioStatusSong(rd.getValueFor("userAudioStatus"));
            userData.setScore(Integer.parseInt(rd.getValueFor("userScore")));
            userData.setNumberOfOldNotifications(Integer.parseInt(rd.getValueFor("userNumberOfOldNotifications")));
            rd.endReadTransaction();

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
