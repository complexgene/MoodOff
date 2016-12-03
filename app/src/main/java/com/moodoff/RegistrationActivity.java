package com.moodoff;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.BoolRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegistrationActivity extends AppCompatActivity {

    EditText name,mobile_number,birthday,email;
    TextView error;
    DatePicker datePicker;
    Calendar calendar;
    int year, month, day;
    ProgressBar spinner;
    StoreRetrieveDataInterface rd=null;
    boolean status = false;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        name = (EditText) findViewById(R.id.name);
        mobile_number = (EditText) findViewById(R.id.phone_number);
        birthday = (EditText) findViewById(R.id.date_of_birth);
        email = (EditText) findViewById(R.id.email_id);
        spinner = (ProgressBar) findViewById(R.id.progressBarRegistration);
        error = (TextView) findViewById(R.id.error_message);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                setDate();
            }
        });
        registerButton = (Button) findViewById(R.id.newregistration);
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
        registerButton.setVisibility(View.GONE);
        spinner.setVisibility(View.VISIBLE);

        if (validateRegistrationData() == true) {

            String nm = name.getText().toString().replaceAll(" ","_"),
                    pn = mobile_number.getText().toString(),
                    dob = birthday.getText().toString(),
                    em = email.getText().toString(),
                    userProfileString;

            userProfileString = nm+"/"+pn+"/"+em+"/"+dob;

//            User user_object = new User();
//            user_object.setName(name.getText().toString());
//            user_object.setName(mobile_number.getText().toString());
//            user_object.setName(birthday.getText().toString());
//            user_object.setName(email.getText().toString());

            if (sendRegistrationInfo(userProfileString) == true) {
                Log.e("RegistrationAct_Reg","Tried Registering...");
                if (saveUserProfile() == true) {
                    spinner.setVisibility(View.GONE);
                    error.setText("Registration Successful.");
                    Intent ii = new Intent(RegistrationActivity.this, Start.class);
                    startActivity(ii);
                } else {
                    Log.e("Kutta","file not saved");
                    spinner.setVisibility(View.GONE);
                    registerButton.setVisibility(View.VISIBLE);
                    //error.setText("Registeration Unsuccessfull.");
                }
            } else {
                Log.e("Kutta","NR");
            }
        }
    }

    public boolean saveUserProfile() {
        try {
            rd = new StoreRetrieveDataImpl("UserData.txt");
            rd.beginWriteTransaction();
            rd.createNewData("user", name.getText().toString().replaceAll(" ","_"));
            rd.createNewData("phoneNo", mobile_number.getText().toString());
            rd.createNewData("dob", birthday.getText().toString());
            rd.createNewData("email", email.getText().toString());
            rd.endWriteTransaction();
            return true;
        } catch (IOException e) {
            error.setText("Could n't save the file.");
            return false;
        }


//        Context context = getApplicationContext();
//        File profileDir = new File(File.separator+"data"+File.separator+getPackageName()+File.separator+"profile");
//        if (!profileDir.exists()){
//            profileDir.mkdir();
//        }
//        File fileName = new File(profileDir.getAbsolutePath()+"myProfile.xml");
//        error.setText(fileName.getAbsolutePath());
//        try {
//            fileName.createNewFile();
//            return true;
//        } catch (IOException e) {
//            //error.setText("Could not save file !!!");
//            return false;
//        }
    }

//    public User getUserProfile() {
//
//    }

    public boolean sendRegistrationInfo(final String user_string) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection=null;
                try {
                    // Proide the URL fto which you would fire a post
                    URL url = new URL("http://hipilab.com/moodoff/users/"+user_string);
                    Log.e("RegistrationActivity",url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoOutput(true);
                    int responseCode = urlConnection.getResponseCode();
                    Log.e("RegActivity_resp",""+responseCode);
                    if(responseCode==200){
                        status = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"Successfully Registered",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"Registration Failed!!",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                catch(Exception ee){
                    Log.e("RegistrationActivity",ee.getMessage());
                    ee.printStackTrace();
                }
                // Close the Http Connection that you started in finally.
                finally {
                    if(urlConnection!=null)
                        urlConnection.disconnect();
                }
            }
        }).start();
        while(status==false);
        return status;
    }

}
