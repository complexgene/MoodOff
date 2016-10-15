package com.moodoff;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.moodoff.helper.DBInternal;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

public class Start extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        StoreRetrieveDataInterface rd=null;
        try {
            rd = new StoreRetrieveDataImpl("UserData.txt");
            if(rd.fileExists()){

                rd.beginReadTransaction();
                UserDetails.setUserName(rd.getValueFor("user"));
                UserDetails.setPhoneNumber(rd.getValueFor("phoneNo"));
                UserDetails.setEmailId(rd.getValueFor("email"));
                rd.endReadTransaction();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Intent mainIntent = new Intent(Start.this, AllTabs.class);
                        Start.this.startActivity(mainIntent);
                        Start.this.finish();
                    }
                }, 2500);
            }
            else{

                Intent ii = new Intent(this,RegistrationActivity.class);
                startActivity(ii);
            }
            }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Ex:"+e.getMessage(), Toast.LENGTH_LONG).show();
        }



    }

}
