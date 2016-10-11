package com.moodoff;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegistrationActivity extends AppCompatActivity {

    EditText userName, phoneNumber, eMail;
    DatePicker dateOfBirth;
    Button registerNow;
    StoreRetrieveDataInterface rd=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registerNow = (Button)findViewById(R.id.register);
        registerNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = (EditText) findViewById(R.id.uname);
                phoneNumber = (EditText) findViewById(R.id.phoneNumber);
                eMail = (EditText) findViewById(R.id.email);
                //dateOfBirth = (DatePicker)findViewById(R.id.)

                String uN = userName.getText().toString(),
                        pN = phoneNumber.getText().toString(),
                        eM = eMail.getText().toString();

                try {

                    // Write the filled-up data to a local file Now
                    rd = new StoreRetrieveDataImpl("UserData.txt");
                    rd.beginWriteTransaction();
                    rd.createNewData("user", uN);
                    rd.createNewData("phoneNo", pN);
                    rd.createNewData("email", eM);
                    rd.endWriteTransaction();
                    // Writing Done

                   /*
                    // Populate UserDetails Class
                    rd.beginReadTransaction();
                    UserDetails.setUserName(rd.getValueFor("user"));
                    UserDetails.setPhoneNumber(rd.getValueFor("phoneNo"));
                    UserDetails.setEmailId(rd.getValueFor("email"));
                    rd.endReadTransaction();
                    // Populating the UserDetails class is done.
                    */

                    Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();

                    Intent ii = new Intent(RegistrationActivity.this,Start.class);
                    startActivity(ii);

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Ex:" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
        });
    }
}

