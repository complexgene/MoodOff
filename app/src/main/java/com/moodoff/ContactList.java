package com.moodoff;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.moodoff.helper.DBInternal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ContactList extends AppCompatActivity {

    private ListView lstNames;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    String val=null;
    SQLiteDatabase mydatabase;
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

    HashMap<String,String> allC = new HashMap<>();
    public void showContacts() {
        // Check the SDK version and whether the permission is already granted or not.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            if(checkIfATableExists("allcontacts")) {
                Log.e("ContactList","Tab exists");
                allC = getOrStoreContactsTableData(0, allC);
            }
            else {
                Log.e("ContactList","Tab doesn't exists");
                allC = ContactList.getContactNames(getContentResolver());
                getOrStoreContactsTableData(1,allC);
            }
            ArrayList<String> contactsInList = new ArrayList<>();
            for(String eachContact:allC.keySet()){
                contactsInList.add(allC.get(eachContact)+" "+eachContact);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.displaycontactsfordedicate, contactsInList);
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
    public static LinkedHashMap<String,String> getContactNames(ContentResolver contentResolver) {
        LinkedHashMap<String,String> contacts = new LinkedHashMap<>();
        // Get the ContentResolver
        ContentResolver cr = contentResolver;
        // Get the Cursor of all the contacts
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        // Move the cursor to first. Also check whether the cursor is empty or not.
        if (cursor.moveToFirst()) {
            // Iterate through the cursor
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if(name==null)
                    name=" ";
                name = name.replaceAll("'", "\'");
                //Log.i("Names", name);
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // Query phone here. Covered next
                    Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumber = phoneNumber.replaceAll("[\\-]", "");
                        if ( phoneNumber.replaceAll("[^0-9]", "").length()>=10 && Pattern.matches("^((0091)|(\\+91)|0?)[789]{1}\\d{9}$",phoneNumber) ){
                            phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                            contacts.put(phoneNumber,name);
                            //Log.i("Number", phoneNumber);
                        }
                    }
                    //Collections.sort(contacts);
                    phones.close();
                }
            }
        }
        // Close the curosor
        cursor.close();
        //Log.e("ContactList",contacts.get(0));
        return contacts;
    }

    public boolean checkIfATableExists(String tableName){
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            Cursor allTables = mydatabase.rawQuery("SELECT name from sqlite_master WHERE type='table' and name='"+tableName+"'", null);
            if(allTables.getCount()==1) {
                Log.e("ContactsFragment_chkTbl",tableName+" exists");
                mydatabase.close();
                return true;
            }
            else{
                Log.e("ContactsFragment_chkTbl",tableName+" doesn't exist");
                mydatabase.close();
                return false;
            }
        }
        catch(Exception ee){
            Log.e("ContactsFragment_chkEr",ee.getMessage());
        }
        mydatabase.close();
        return false;
    }
    public LinkedHashMap<String,String> getOrStoreContactsTableData(int status, HashMap<String,String> allContacts){
        LinkedHashMap<String,String> allContactsPresent = new LinkedHashMap<>();
        mydatabase = openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            // status = 0 is for READ and RETURN as it means TABLE ALREADY EXISTS
            if(status == 0){
                //READ and RETURN data
                Cursor resultSet = mydatabase.rawQuery("Select * from allcontacts order by name", null);
                resultSet.moveToFirst();
                while (!resultSet.isAfterLast()) {
                    allContactsPresent.put(resultSet.getString(0),resultSet.getString(1));
                    resultSet.moveToNext();
                }
            }
            // First time conatct table create or REFRESH done.
            else{
                String createQuery = "CREATE TABLE IF NOT EXISTS allcontacts(phone_no VARCHAR,name VARCHAR);";
                mydatabase.execSQL(createQuery);
                String deleteQuery = "DELETE FROM allcontacts;";
                mydatabase.execSQL(deleteQuery);
                String insertQuery = "";
                for(String eachContact:allContacts.keySet()){
                    //Log.e("ContactsFragment_CntErr",eachContact);
                    insertQuery = "INSERT INTO allcontacts values('"+eachContact+"','"+allContacts.get(eachContact)+"');";
                    //Log.e("ContactsFragment_CntErr",insertQuery);
                    mydatabase.execSQL(insertQuery);
                }
                return null;
            }
            mydatabase.close();
        }catch (Exception ee){
            Log.e("ContactsFragment_StrErr",ee.getMessage());
            ee.fillInStackTrace();
        }
        mydatabase.close();
        //allContactsPresent.add("santanu");
        return allContactsPresent;
    }
}

