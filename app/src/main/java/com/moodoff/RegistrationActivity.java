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
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.DBHelper;
import com.moodoff.helper.DBInternal;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {

    EditText name,mobile_number,birthday,email;
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

        birthday.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                setDate();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(v);
            }
        });
    }

    private void initComponents(){
        dbOperations = new DBHelper(this);
        register = (Button)findViewById(R.id.newregistration);
        name = (EditText) findViewById(R.id.name);
        mobile_number = (EditText) findViewById(R.id.phone_number);
        birthday = (EditText) findViewById(R.id.date_of_birth);
        email = (EditText) findViewById(R.id.email_id);
        error = (TextView) findViewById(R.id.error_message);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }


    private void createAllNecessaryTablesForAppOperation(){
        DBHelper dboperations = new DBHelper(getApplicationContext());
        HashMap<String,String> contactsColumns = new HashMap<>();
        contactsColumns.put("phone_no","VARCHAR");
        contactsColumns.put("name","VARCHAR");
        dboperations.createTable("allcontacts",contactsColumns);
        Log.e("RegistrationAct_TBL","allcontacts table created.");
        HashMap<String,String> worktodoColumns = new HashMap<>();
        worktodoColumns.put("id","INTEGER PRIMARY KEY AUTOINCREMENT");
        worktodoColumns.put("api","VARCHAR");
        dboperations.createTable("worktodo",worktodoColumns);
        Log.e("RegistrationAct_TBL","worktodo table created.");
        HashMap<String,String> profilesColumns = new HashMap<>();
        profilesColumns.put("id","VARCHAR PRIMARY KEY");
        profilesColumns.put("name","VARCHAR");
        profilesColumns.put("mailid","VARCHAR");
        profilesColumns.put("dob","VARCHAR");
        profilesColumns.put("textstatus","VARCHAR");
        profilesColumns.put("audiostatus","VARCHAR");
        dboperations.createTable("profiles",profilesColumns);
        Log.e("RegistrationAct_TBL","profiles table created.");
        HashMap<String,String> rnotificationsColumns = new HashMap<>();
        rnotificationsColumns.put("from_user_id","VARCHAR");
        rnotificationsColumns.put("to_user_id","VARCHAR");
        rnotificationsColumns.put("file_name","VARCHAR");
        rnotificationsColumns.put("type","VARCHAR");
        rnotificationsColumns.put("send_done","INTEGER");
        rnotificationsColumns.put("create_ts","VARCHAR");
        dboperations.createTable("rnotifications",rnotificationsColumns);
        Log.e("RegistrationAct_TBL","Read Notifications table created");
        HashMap<String,String> wnotificationsColumns = new HashMap<>();
        wnotificationsColumns.put("from_user_id","VARCHAR");
        wnotificationsColumns.put("to_user_id","VARCHAR");
        wnotificationsColumns.put("file_name","VARCHAR");
        wnotificationsColumns.put("type","VARCHAR");
        wnotificationsColumns.put("send_done","TINYINT(1)");
        wnotificationsColumns.put("create_ts","TIMESTAMP");
        dboperations.createTable("rnotifications",wnotificationsColumns);
        Log.e("RegistrationAct_TBL","Write Notifications table created");
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

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public boolean validateRegistrationData () {
        if (name.getText().toString().isEmpty()) {
            error.setText("Error: Name is mandatory.");return false;
        } else if (mobile_number.getText().toString().isEmpty()) {
            error.setText("Error: phone number is mandatory.");return false;
        } else if (birthday.getText().toString().isEmpty()) {
            error.setText("Error: birthday is mandatory.");return false;
        } else if (isValidEmail(email.getText()) == false) {
            error.setText("Invalid Email.");return false;
        } else {
            return true;
        }
    }

    public void register(View view) {
        error.setVisibility(View.VISIBLE);
        if (validateRegistrationData() == true) {

            final String nm = name.getText().toString().replaceAll(" ", "_"),
                    pn = mobile_number.getText().toString(),
                    dob = birthday.getText().toString(),
                    em = email.getText().toString(),
                    userProfileString;

            userProfileString = nm + "/" + pn + "/" + em + "/" + dob;
            final String Url = "http://hipilab.com/moodoff/users/" + userProfileString;
            dbOperations.todoWorkEntry(Url);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection urlConnection = null;
                    try {
                        // Proide the URL fto which you would fire a post
                        URL url = new URL(Url);
                        Log.e("RegistrationActivity", url.toString());
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setDoOutput(true);
                        int responseCode = urlConnection.getResponseCode();
                        Log.e("RegAct_RESCODE", "" + responseCode);
                        if (responseCode == 200) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    saveUserProfile();
                                    Toast.makeText(getApplicationContext(), "Successfully Registered", Toast.LENGTH_SHORT).show();
                                    Intent mainIntent = new Intent(RegistrationActivity.this, Start.class);
                                    startActivity(mainIntent);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Registration Failed!! Try after sometime..", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (Exception ee) {
                        Log.e("RegistrationAct_Err", ee.getMessage());
                        ee.printStackTrace();
                    }
                    // Close the Http Connection that you started in finally.
                    finally {
                        if (urlConnection != null)
                            urlConnection.disconnect();
                    }
                }
            }).start();
        }
    }

    public boolean saveUserProfile() {
        try {
            rd = new StoreRetrieveDataImpl("UserData.txt");
            rd.beginWriteTransaction();
            rd.createNewData("user", name.getText().toString().replaceAll(" ", "_"));
            rd.createNewData("phoneNo", mobile_number.getText().toString());
            rd.createNewData("dob", birthday.getText().toString());
            rd.createNewData("email", email.getText().toString());
            rd.createNewData("textStatus","Using MoodOff");
            rd.endWriteTransaction();
            return true;
        } catch (IOException e) {
            error.setText("Couldn't save the file.");
            return false;
        }
    }

}
