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
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.DBHelper;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;

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
    ProgressBar progressRegistration;
    TextView error;
    Calendar calendar;
    int year, month, day;
    StoreRetrieveDataInterface rd=null;
    Button register;
    DBHelper dbOperations;
    boolean userExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initComponents();

        // Need to do this without any lag..
        createAllNecessaryTablesForAppOperation();

        name.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});
        mobile_number.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});
        email.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {error.setText("");}});

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
                if (validateRegistrationData()){
                    Toast.makeText(getApplicationContext(),"Registering...Wait!!",Toast.LENGTH_LONG).show();
                    progressRegistration.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            checkIfUserExists(v,mobile_number.getText().toString());
                        }
                    }).start();
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
        email = (EditText) findViewById(R.id.email_id);
        status_text = (EditText) findViewById(R.id.status_text);
        error = (TextView) findViewById(R.id.error_message);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        progressRegistration = (ProgressBar)findViewById(R.id.progressBarRegistration);
        progressRegistration.setVisibility(View.INVISIBLE);
    }

    private void checkIfUserExists(final View v,String mobNo) {
        final String Url = serverURL + "/users/check/" + mobNo;
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    Log.e("RegAct_Not", "Start checking if user exists");
                    URL url = new URL(Url);
                    Log.e("RegistrAct_checkUserURL", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream is = urlConnection.getInputStream();
                    isr = new InputStreamReader(is);
                    Log.e("RegAct_data","here");
                    int data = isr.read();
                    final StringBuilder response = new StringBuilder("");
                    while (data != -1) {
                        response.append((char) data);
                        data = isr.read();
                    }
                    Log.e("RegAct_usrChkStatus",response.toString());
                    if(response.toString().equals("true")){
                        userExists = true;
                    }
                    //doNotContinue = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(userExists){
                                error.setText("!! This phone number is already registered !!");
                                progressRegistration.setVisibility(View.INVISIBLE);
                            }
                            else{
                                register(v);
                            }
                        }
                    });

                } catch (Exception ee) {
                    Log.e("RegAct_chkUsrErr", ee.getMessage());
                }
                finally {
                    try{
                        isr.close();
                    }
                    catch(Exception ee){
                        Log.e("RegAct_Usrchk_Err", "ISR couldn't be closed");
                    }
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private void createAllNecessaryTablesForAppOperation(){
        DBHelper dboperations = new DBHelper(getApplicationContext());
        dbOperations.dropAllTables();
        LinkedHashMap<String,String> contactsColumns = new LinkedHashMap<>();
        contactsColumns.put("phone_no","VARCHAR");
        contactsColumns.put("name","VARCHAR");
        dboperations.createTable("allcontacts",contactsColumns);
        Log.e("RegistrationAct_TBL","allcontacts table created.");
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
        /*HashMap<String,String> wnotificationsColumns = new HashMap<>();
        wnotificationsColumns.put("from_user_id","VARCHAR");
        wnotificationsColumns.put("to_user_id","VARCHAR");
        wnotificationsColumns.put("file_name","VARCHAR");
        wnotificationsColumns.put("type","VARCHAR");
        wnotificationsColumns.put("send_done","TINYINT(1)");
        wnotificationsColumns.put("create_ts","TIMESTAMP");
        dboperations.createTable("rnotifications",wnotificationsColumns);
        Log.e("RegistrationAct_TBL","Write Notifications table created");*/
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
            error.setText("Error: Phone number is mandatory.");return false;
        } else if (birthday.getText().toString().isEmpty()) {
            error.setText("Error: Birthday is mandatory.");return false;
        } else if (isValidEmail(email.getText()) == false) {
            error.setText("Error: Invalid Email Format.");return false;
        } else {
            return true;
        }
    }

    public void register(View view) {
        error.setVisibility(View.VISIBLE);

            final String nm = name.getText().toString().replaceAll(" ", "_"),
                    pn = mobile_number.getText().toString(),
                    dob = birthday.getText().toString(),
                    em = email.getText().toString(),
                    textStatus = status_text.getText().toString().replaceAll(" ","_"),
                    userProfileString;

            userProfileString = nm + "/" + pn + "/" + em + "/" + dob + "/" + textStatus;
            //final String Url = serverURL+"/users/" + userProfileString;
            final String Url = "http://192.168.2.5:5679/controller/users/" + userProfileString;
            dbOperations.todoWorkEntry(Url);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection urlConnection = null;
                    try {
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              Toast.makeText(getApplicationContext(), "Setting up things..!!", Toast.LENGTH_SHORT).show();
                                          }
                                      });
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
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                                    finish(); // destroy current activity..
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

    public boolean saveUserProfile() {
        try {
            rd = new StoreRetrieveDataImpl("UserData.txt");
            rd.beginWriteTransaction();
            rd.createNewData("user", name.getText().toString().replaceAll(" ", "_"));
            rd.createNewData("phoneNo", mobile_number.getText().toString());
            rd.createNewData("dob", birthday.getText().toString());
            rd.createNewData("email", email.getText().toString());
            rd.createNewData("textStatus","Using MoodOff");
            rd.createNewData("audioStatus","Using MoodOff");
            rd.endWriteTransaction();
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
