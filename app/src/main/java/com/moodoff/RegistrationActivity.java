package com.moodoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;

public class RegistrationActivity extends AppCompatActivity {

    EditText userName, phoneNumber, eMail;
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
                userName = (EditText)findViewById(R.id.uname);
                phoneNumber = (EditText)findViewById(R.id.phoneNumber);
                eMail = (EditText)findViewById(R.id.email);

                String uN = userName.getText().toString(),
                        pN = phoneNumber.getText().toString(),
                        eM = eMail.getText().toString();

                try {
                    rd = new StoreRetrieveDataImpl("UserData.txt");
                    rd.beginWriteTransaction();
                    rd.createNewData("user", pN);
                    rd.createNewData("email", eM);
                    Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
                    Intent ii = new Intent(RegistrationActivity.this,Start.class);
                    startActivity(ii);
                    rd.endWriteTransaction();
                }catch(Exception e){
                    Toast.makeText(getApplicationContext(), "Ex:"+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
