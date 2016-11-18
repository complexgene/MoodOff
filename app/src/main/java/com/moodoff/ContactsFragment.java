package com.moodoff;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.DBInternal;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    View v;Context ctx;
    SQLiteDatabase mydatabase;
    EditText tableName;
    TextView tv = null;
    TextView contacts;
    ProgressBar spinner;
    FloatingActionButton refreshContacts;
    HashMap<String,String> allC = new HashMap<>();
    boolean contactReadingStatusNotComplete = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_contacts, container, false);
        ctx = v.getContext();

        spinner = (ProgressBar)v.findViewById(R.id.refreshSpin);
        //DBInternal dbInternal = new DBInternal();
        if(checkIfATableExists("allcontacts")){
            Log.e("ContactsFragment_TBLEXT","table exists");
            allC = getOrStoreContactsTableData(0,allC);
            populatePageWithContacts();
        }
        else{
            Log.e("ContactsFragment_cntcts","Not present");
            Toast.makeText(getContext(),"Reading your Contacts. Wait.",Toast.LENGTH_LONG).show();
            spinner.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    allC = ContactList.getContactNames(ctx.getContentResolver());
                    getOrStoreContactsTableData(1,allC);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),"Contacts Reading Finished..",Toast.LENGTH_SHORT).show();
                            spinner.setVisibility(View.INVISIBLE);
                            populatePageWithContacts();
                        }
                    });
                }
            }).start();
        }

//        mainLayout.addView(contactsScroll);
        //contacts.setText(allC.get(0));
        refreshContacts = (FloatingActionButton)v.findViewById(R.id.refreshContacts);
        refreshContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(),"Reading your Contacts. Wait.",Toast.LENGTH_LONG).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        allC = ContactList.getContactNames(ctx.getContentResolver());
                        getOrStoreContactsTableData(1,allC);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),"Contacts Reading Finished..",Toast.LENGTH_SHORT).show();
                                spinner.setVisibility(View.INVISIBLE);
                                populatePageWithContacts();
                            }
                        });
                    }
                }).start();
            }
        });

        return v;
    }

    public void populatePageWithContacts(){
        RelativeLayout mainLayout = (RelativeLayout)v.findViewById(R.id.allContactDisplay);
        ScrollView contactsScroll = (ScrollView)v.findViewById(R.id.contactsScroll);
        contactsScroll.removeAllViews();
        LinearLayout eachContact = new LinearLayout(ctx);
        eachContact.setOrientation(LinearLayout.VERTICAL);
        eachContact.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        int noOfContacts = allC.size();
        for(String eachCntct:allC.keySet()){
            TextView tv = new TextView(ctx);
            tv.setTextSize(16.0f);
            tv.setText(allC.get(eachCntct)+" "+eachCntct);
            eachContact.addView(tv);
        }
        contactsScroll.addView(eachContact);
    }

    public boolean checkIfATableExists(String tableName){
        mydatabase = ctx.openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
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
    public HashMap<String,String> getOrStoreContactsTableData(int status, HashMap<String,String> allContacts){
        HashMap<String,String> allContactsPresent = new HashMap<>();
        mydatabase = getActivity().openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
            // status = 0 is for READ and RETURN as it means TABLE ALREADY EXISTS
            if(status == 0){
                //READ and RETURN data
                Cursor resultSet = mydatabase.rawQuery("Select * from allcontacts", null);
                resultSet.moveToFirst();
                while (!resultSet.isAfterLast()) {
                    allContactsPresent.put(resultSet.getString(0),resultSet.getString(1));
                    resultSet.moveToNext();
                }
            }
            // First time conatct table create or REFRESH done.
            else{
                String createQuery = "CREATE TABLE IF NOT EXISTS allcontacts(user_id VARCHAR,phone_no VARCHAR);";
                mydatabase.execSQL(createQuery);
                String deleteQuery = "DELETE FROM allcontacts;";
                mydatabase.execSQL(deleteQuery);
                String insertQuery = "";
                for(String eachContact:allContacts.keySet()){
                    Log.e("ContactsFragment_CntErr",eachContact);
                    insertQuery = "INSERT INTO allcontacts(user_id,phone_no) values('"+eachContact+"','"+allContacts.get(eachContact)+"');";
                    Log.e("ContactsFragment_CntErr",insertQuery);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
