package com.moodoff;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ContactList extends AppCompatActivity {


    private ListView lstNames;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    String val=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);
        Intent intent=getIntent();
        this.lstNames = (ListView) findViewById(R.id.contactlist);

        // Read and show the contacts
        showContacts();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        lstNames.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                val=lstNames.getItemAtPosition(position).toString();
                Log.e("sam",val.toString());
                // Toast.makeText(MainActivity.this,val, Toast.LENGTH_SHORT).show();
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                Intent it = new Intent();
                                it.putExtra("selectedContact",val);
                                setResult(RESULT_OK, it);
                                finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked Do Nothing
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ContactList.this);
                builder.setTitle("Confirm your dedication");
                Dialog d=builder.setMessage("This song will be dedicated to"+" "+ val).setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                int dividerId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
                View divider = d.findViewById(dividerId);
                builder.setView(divider);


            }
        });

    }



    public void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            List<String> contacts = getContactNames();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contacts);
            lstNames.setAdapter(adapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts();
            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Read the name of all the contacts.
     *
     * @return a list of names+mobile_number.
     */
    private List<String> getContactNames() {
        List<String> contacts = new ArrayList<>();
        // Get the ContentResolver
        ContentResolver cr = getContentResolver();
        // Get the Cursor of all the contacts
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        // Move the cursor to first. Also check whether the cursor is empty or not.
        if (cursor.moveToFirst()) {
            // Iterate through the cursor
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                //Log.i("Names", name);
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // Query phone here. Covered next
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumber = phoneNumber.replaceAll("[\\-]", "");
                        if ( phoneNumber.replaceAll("[^0-9]", "").length()>=10 && Pattern.matches("^((0091)|(\\+91)|0?)[789]{1}\\d{9}$",phoneNumber) ){
                            phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                            contacts.add(name + "\n" + phoneNumber);
                            //Log.i("Number", phoneNumber);
                        }
                    }
                    phones.close();
                }
            }
        }
        // Close the curosor
        cursor.close();

        return contacts;
    }
}

