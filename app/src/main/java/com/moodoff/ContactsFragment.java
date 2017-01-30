package com.moodoff;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.DBInternal;
import com.moodoff.helper.Messenger;
import com.moodoff.model.UserDetails;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    View mainView, profileDialogView;
    ViewGroup mainContainer;
    LayoutInflater mainInflater;
    Context ctx;
    SQLiteDatabase mydatabase;
    EditText tableName;
    TextView tv = null;
    TextView contacts;
    ProgressBar spinner;
    FloatingActionButton refreshContactButton;
    HashMap<String,String> allC = new HashMap<>();
    boolean contactReadingStatusNotComplete = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainContainer = container;
        mainInflater = inflater;

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ctx = mainView.getContext();

        addOwnProfileAndRefreshButton();

        //spinner = (ProgressBar)mainView.findViewById(R.id.refreshSpin);
        //DBInternal dbInternal = new DBInternal();
        allC = getOrStoreContactsTableData(0,allC);

        return mainView;
    }
    private View getVerticalLine(int width){
        View v = new View(getContext());
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                width
        ));
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));
        return v;
    }
    LinearLayout vertically;
    private void addOwnProfileAndRefreshButton(){
        RelativeLayout refershAndProfile = (RelativeLayout)mainView.findViewById(R.id.allContactDisplay);

        //View divide = new View(getContext());
        vertically = new LinearLayout(getContext());
        vertically.setOrientation(LinearLayout.VERTICAL);
        vertically.addView(getVerticalLine(5));

        LinearLayout refreshAndMyProfile = new LinearLayout(getContext());
        LinearLayout.LayoutParams designDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        designDetails.topMargin = 10;
        designDetails.bottomMargin = 10;
        refreshAndMyProfile.setLayoutParams(designDetails);

        designDetails = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        FloatingActionButton refreshContactButton = new FloatingActionButton(getContext());
        refreshContactButton.setImageResource(R.drawable.refresh_contacts);
        refreshContactButton.setBackgroundTintList(ColorStateList.valueOf(Color.YELLOW));
        refreshContactButton.setSize(FloatingActionButton.SIZE_MINI);
        designDetails.gravity = Gravity.CENTER;
        designDetails.leftMargin = 10;
        designDetails.rightMargin = 10;
        refreshContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                spinner.setVisibility(View.VISIBLE);
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
//                                spinner.setVisibility(View.INVISIBLE);
                                populatePageWithContacts(vertically);
                            }
                        });
                    }
                }).start();
            }
        });
        refreshContactButton.setLayoutParams(designDetails);
        Button myProfile = new Button(getContext());
        myProfile.setText("My Profile");
        //myProfile.setBackgroundColor(Color.WHITE);
        myProfile.setBackgroundResource(R.drawable.profilecontactdesignown);
        designDetails = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        designDetails.rightMargin = 10;
        designDetails.weight = 1;
        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProfile(UserDetails.getPhoneNumber());
            }
        });
        myProfile.setLayoutParams(designDetails);

        refreshAndMyProfile.addView(refreshContactButton);
        refreshAndMyProfile.addView(myProfile);

        vertically.addView(refreshAndMyProfile);
        vertically.addView(getVerticalLine(5));
        refershAndProfile.addView(vertically);
        populatePageWithContacts(vertically);
    }

    public void populatePageWithContacts(LinearLayout layout){
        ScrollView contactsScroll = new ScrollView(getContext());
        contactsScroll.removeAllViews();
        LinearLayout eachContact = new LinearLayout(ctx);
        eachContact.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        eachContact.setLayoutParams(layoutDetails);
        allC = ContactsManager.allReadContacts;
        ArrayList<String> validUsers = ContactsManager.friendsWhoUsesApp;
        Log.e("CONTCCCCCTSSSS",allC.size()+" "+validUsers.size());
        for(final String validCntct:validUsers){
                LinearLayout eachContactLayout = new LinearLayout(getContext());
                eachContactLayout.setBackgroundColor(Color.WHITE);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                //layoutDetails.topMargin = 20;
                eachContactLayout.setLayoutParams(layoutDetails);
                Button contactNameAndNumber = new Button(ctx);
                contactNameAndNumber.setText(allC.get(validCntct) + "\n" + validCntct);
                contactNameAndNumber.setBackgroundResource(R.drawable.profilecontactdesign);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutDetails.topMargin = 40;
                layoutDetails.bottomMargin = 40;
                layoutDetails.leftMargin = 25;
                layoutDetails.rightMargin = 25;
                contactNameAndNumber.setLayoutParams(layoutDetails);
                contactNameAndNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadProfile(validCntct);
                    }
                });

                eachContactLayout.addView(contactNameAndNumber);
                eachContact.addView(eachContactLayout);
            allC.remove(validCntct);
        }
        int noOfContacts = allC.size();
        for(final String eachCntct:allC.keySet()){
            LinearLayout eachContactLayout = new LinearLayout(getContext());
            eachContactLayout.setBackgroundColor(Color.WHITE);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.topMargin = 20;
            eachContactLayout.setLayoutParams(layoutDetails);
            Button contactNameAndNumber = new Button(ctx);
            contactNameAndNumber.setText(allC.get(eachCntct)+"\n"+eachCntct);
            contactNameAndNumber.setBackgroundResource(R.drawable.profilecontactdesign);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.topMargin = 40;
            layoutDetails.bottomMargin = 40;
            layoutDetails.leftMargin = 25;
            layoutDetails.rightMargin = 25;
            contactNameAndNumber.setLayoutParams(layoutDetails);
            contactNameAndNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Messenger.print(getContext(),"Not yet using the app Moodoff");
                    //loadProfile(eachCntct);
                }
            });

            eachContactLayout.addView(contactNameAndNumber);
            eachContact.addView(eachContactLayout);
        }
        contactsScroll.addView(eachContact);
        layout.addView(contactsScroll);
    }
    private void loadProfile(String contactNumber){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment newFragment = Profile.newInstance(contactNumber,"b");
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.allContactDisplay, newFragment);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
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
    public LinkedHashMap<String,String> getOrStoreContactsTableData(int status, HashMap<String,String> allContacts){
        LinkedHashMap<String,String> allContactsPresent = new LinkedHashMap<>();
        mydatabase = getActivity().openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
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
                String createQuery = "CREATE TABLE IF NOT EXISTS allcontacts(user_id VARCHAR,phone_no VARCHAR);";
                mydatabase.execSQL(createQuery);
                String deleteQuery = "DELETE FROM allcontacts;";
                mydatabase.execSQL(deleteQuery);
                String insertQuery = "";
                for(String eachContact:allContacts.keySet()){
                    Log.e("ContactsFragment_CntErr",eachContact);
                    insertQuery = "INSERT INTO allcontacts(phone_no,name) values('"+eachContact+"','"+allContacts.get(eachContact)+"');";
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
