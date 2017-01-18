package com.moodoff;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class PermissionRequest extends AppCompatActivity {
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_request);
        Log.e("ABC","abc");
        askForPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Intent ii = new Intent(this,Start.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                    finish(); // destroy current activity..
                    startActivity(ii);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Sorry!! The app needs all permissions to go ahead!!",Toast.LENGTH_LONG).show();
                }
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    Intent ii = new Intent(this,Start.class);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                    finish(); // destroy current activity..
                    startActivity(ii);
                }
                else{
                    Toast.makeText(getApplicationContext(),"Sorry!! The app needs all permissions to go ahead!!",Toast.LENGTH_LONG).show();
                }
        }
    }

    private boolean askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int contactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
            int extStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            if(cameraPermission != PackageManager.PERMISSION_GRANTED && contactsPermission != PackageManager.PERMISSION_GRANTED && extStoragePermission != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
            else{
                Log.e("yooo","aaaaa");

                Intent ii = new Intent(this,Start.class);
                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ii.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clears all previous activities task
                finish(); // destroy current activity..
                startActivity(ii);
            }
            Log.e("Start_permission","Asking done..");
            return true;
        }
        else {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
        return false;
    }
}
