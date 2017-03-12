package com.moodoff.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.R;
import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.Messenger;
import com.moodoff.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

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
    public static boolean openedAProfile = false;
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
    User userData = User.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainContainer = container;
        mainInflater = inflater;

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ctx = mainView.getContext();

        addOwnProfileAndRefreshButton();

        monitorViewChange();

        //spinner = (ProgressBar)mainView.findViewById(R.id.refreshSpin);
        //DBInternal dbInternal = new DBInternal();
        allC = ContactsManager.allReadContacts;


        return mainView;
    }

    public static boolean updateViewCalled = false;
    private void monitorViewChange(){
        Log.e("Contacts_viewChange","Monitor is running to detect a view change");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(updateViewCalled){
                            updateViewCalled = false;
                            Log.e("Contacts_viewChange","View change detected..");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addOwnProfileAndRefreshButton();
                                }
                            });
                        }
                     }
                }).start();
                monitorViewChange();
            }
        },5000);
    }


    private View getHorizontalLine(int width){
        View v = new View(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, width);
        layoutParams.leftMargin = 50;
        layoutParams.rightMargin = 50;
        v.setLayoutParams(layoutParams);
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));
        return v;
    }
    private View getVerticalLine(int width){
        View v = new View(getContext());
        v.setLayoutParams(new LinearLayout.LayoutParams(width,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));
        return v;
    }
    Button myProfile;
    private void addOwnProfileAndRefreshButton(){
        ImageButton refreshContactButton = (ImageButton) mainView.findViewById(R.id.btn_refreshContact);
        refreshContactButton.setImageResource(R.drawable.refresh_contacts);
        refreshContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Reading your Contacts. Wait.",Toast.LENGTH_LONG).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        allC = ContactList.getContactNames(ctx.getContentResolver());
                        getOrStoreContactsTableData(allC);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(),"Contacts Reading Finished..",Toast.LENGTH_SHORT).show();
                                addOwnProfileAndRefreshButton();
                            }
                        });
                    }
                }).start();
            }
        });
        myProfile = (Button)mainView.findViewById(R.id.ownProfile);//new Button(getContext());
        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openedAProfile = true;
                loadProfile(userData.getUserMobileNumber());
            }
        });
        // Populating all contacts who uses app and who doesn't
        ScrollView contactsScroll = (ScrollView)mainView.findViewById(R.id.allContacts);
        contactsScroll.removeAllViews();
        LinearLayout eachContact = new LinearLayout(ctx);
        eachContact.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        eachContact.setLayoutParams(layoutDetails);
        allC = ContactsManager.allReadContacts;
        ArrayList<String> friendWhoUsesApp = ContactsManager.friendsWhoUsesApp;
        Log.e("CONTCCCCCTSSSS",allC.size()+" "+friendWhoUsesApp.size());

        eachContact.addView(getHorizontalLine(1));
        LinearLayout titleAppUsers = new LinearLayout(getContext());
        titleAppUsers.setGravity(Gravity.CENTER);
        layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutDetails.topMargin = 10;
        layoutDetails.bottomMargin = 10;
        layoutDetails.leftMargin = 20;
        layoutDetails.rightMargin = 20;
        titleAppUsers.setLayoutParams(layoutDetails);
        titleAppUsers.setBackgroundResource(R.drawable.poster);
        TextView tvv = new TextView(getContext());
        tvv.setGravity(Gravity.CENTER);
        tvv.setPadding(3,3,3,3);
        tvv.setTextColor(Color.WHITE);
        tvv.setText(" Friends Using App ");
        titleAppUsers.addView(tvv);
        eachContact.addView(titleAppUsers);
        for(final String eachFriendContact : friendWhoUsesApp) {
            String contactName = allC.get(eachFriendContact);
            if (contactName!=null && !isNotHavingAnyCharacter(contactName)) {
                LinearLayout eachContactLayout = new LinearLayout(getContext());
                eachContactLayout.setOrientation(LinearLayout.VERTICAL);
                eachContactLayout.setBackgroundColor(Color.WHITE);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                eachContactLayout.setLayoutParams(layoutDetails);

                eachContactLayout.addView(getHorizontalLine(1));
                //eachContactLayout.setPadding(0,20,0,20);
                TextView name = new TextView(ctx);
                name.setPadding(0,20,0,0);
                name.setTextColor(Color.BLACK);
                name.setTypeface(Typeface.DEFAULT_BOLD);
                name.setGravity(Gravity.CENTER);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                name.setAllCaps(false);
                name.setText(allC.get(eachFriendContact));
                //name.setBackgroundResource(R.drawable.contactsusingappdesign);
                name.setLayoutParams(layoutDetails);
                eachContactLayout.addView(name);

                boolean userIsLive = (new Random().nextInt(2)==1)?true:false;

                LinearLayout moodStatusAndMood = new LinearLayout(getContext());
                moodStatusAndMood.setGravity(Gravity.CENTER);
                moodStatusAndMood.setOrientation(LinearLayout.HORIZONTAL);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                moodStatusAndMood.setLayoutParams(layoutDetails);

                TextView moodStatus = new TextView(ctx);
                moodStatus.setGravity(Gravity.CENTER);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                moodStatus.setAllCaps(false);
                if(userIsLive){
                    moodStatus.setTextColor(Color.GREEN);
                    moodStatus.setTypeface(Typeface.DEFAULT_BOLD);
                    moodStatus.setText("Live on");
                }
                else{
                    moodStatus.setTextColor(Color.RED);
                    moodStatus.setTypeface(Typeface.DEFAULT_BOLD);
                    moodStatus.setText("Last Listened");
                }
                //moodStatus.setBackgroundResource(R.drawable.contactsusingappdesign);
                moodStatus.setLayoutParams(layoutDetails);
                moodStatusAndMood.addView(moodStatus);

                TextView moodName = new TextView(ctx);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                moodName.setAllCaps(false);
                moodName.setText(" : Party");
               // moodName.setBackgroundResource(R.drawable.contactsusingappdesign);
                moodName.setLayoutParams(layoutDetails);
                moodStatusAndMood.addView(moodName);
                moodStatusAndMood.setPadding(0,0,0,20);
                eachContactLayout.addView(moodStatusAndMood);
                eachContactLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openedAProfile = true;
                        loadProfile(eachFriendContact);
                    }
                });
                eachContact.addView(eachContactLayout);
            }
        }

        eachContact.addView(getHorizontalLine(2));
        LinearLayout titleNotAppUsers = new LinearLayout(getContext());
        titleNotAppUsers.setGravity(Gravity.CENTER);
        layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutDetails.topMargin = 10;
        layoutDetails.bottomMargin = 10;
        layoutDetails.leftMargin = 20;
        layoutDetails.rightMargin = 20;
        layoutDetails.gravity = Gravity.CENTER;
        titleNotAppUsers.setLayoutParams(layoutDetails);
        titleNotAppUsers.setBackgroundResource(R.drawable.poster);
        TextView tvv1 = new TextView(getContext());
        tvv1.setPadding(3,3,3,3);
        tvv1.setTextColor(Color.WHITE);
        tvv1.setText(" Friends Not Using App ");
        titleNotAppUsers.addView(tvv1);
        eachContact.addView(titleNotAppUsers);
        for(final String eachCntct:ContactsManager.friendsWhoDoesntUseApp){
            final String contactName = allC.get(eachCntct);
            if(contactName!=null && !isNotHavingAnyCharacter(contactName)){
                eachContact.addView(getHorizontalLine(1));
                LinearLayout eachContactLayout = new LinearLayout(getContext());
                eachContactLayout.setBackgroundResource(R.drawable.contactsnotusingappdesign);
                eachContactLayout.setGravity(Gravity.CENTER);
                eachContactLayout.setPadding(20,20,20,20);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutDetails.weight = 1;
                layoutDetails.topMargin = 10;
                layoutDetails.bottomMargin = 10;
                layoutDetails.leftMargin = 20;
                layoutDetails.rightMargin = 20;
                eachContactLayout.setLayoutParams(layoutDetails);
                TextView personName = new TextView(getContext());
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                personName.setLayoutParams(layoutDetails);
                layoutDetails.weight=1;
                personName.setGravity(Gravity.CENTER);
                personName.setTypeface(Typeface.DEFAULT_BOLD);
                personName.setText(contactName);
                TextView sendInvite = new TextView(ctx);
                sendInvite.setAllCaps(false);
                sendInvite.setPadding(10,10,10,10);
                sendInvite.setText("Send Invite");
                sendInvite.setTextColor(Color.WHITE);
                sendInvite.setTransformationMethod( null);
                sendInvite.setBackgroundResource(R.drawable.invitebuttonlayout);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutDetails.rightMargin = 10;
                sendInvite.setLayoutParams(layoutDetails);
                sendInvite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        Messenger.print(getContext(),"App Link Sent");
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        Messenger.print(getContext(),"App Link Not Sent");
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Confirm Sending App Link");
                        Dialog d=builder.setMessage("Your friend "+contactName+" will receive an app link to install. Proceed?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        int dividerId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
                        View divider = d.findViewById(dividerId);
                        builder.setView(divider);
                        //loadProfile(eachCntct);
                    }
                });
                eachContactLayout.addView(personName);
                eachContactLayout.addView(sendInvite);
                eachContact.addView(eachContactLayout);
            }
            }
        contactsScroll.addView(eachContact);
    }
    private static boolean isNotHavingAnyCharacter(String name){
        for(int i=0;i<name.length();i++){
            if(Character.isLetter(name.charAt(i))){
                return false;
            }
        }
        return true;
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
    public LinkedHashMap<String,String> getOrStoreContactsTableData(HashMap<String,String> allContacts){
        LinkedHashMap<String,String> allContactsPresent = new LinkedHashMap<>();
        mydatabase = getActivity().openOrCreateDatabase("moodoff", MODE_PRIVATE, null);
        try {
                HashMap<String,String> detailsWithStatus1 = new HashMap<>();
                Cursor resultSet = mydatabase.rawQuery("Select * from allcontacts", null);
                resultSet.moveToFirst();
                while (!resultSet.isAfterLast()) {
                    if(resultSet.getInt(2)==1){
                        Log.e("ContactsFrag_GotOne","No:"+resultSet.getString(1));
                        detailsWithStatus1.put(resultSet.getString(0),"1");
                    }
                    resultSet.moveToNext();
                }

                String dropQuery = "DROP TABLE allcontacts;";
                mydatabase.execSQL(dropQuery);
                Log.e("ContactsFragment_DROP","allconatcts table dropped..");
                String createQuery = "CREATE TABLE IF NOT EXISTS allcontacts(phone_no VARCHAR,name VARCHAR,status INTEGER);";
                mydatabase.execSQL(createQuery);
                Log.e("ContactsFragment_CREATE","allconatcts table created..");
                String insertQuery = "";
                for(String eachContact:allContacts.keySet()){
                    String name = allContacts.get(eachContact);
                    name = name.replaceAll("'", "''");
                    if(detailsWithStatus1.containsKey(eachContact)) {
                        insertQuery = "INSERT INTO allcontacts(phone_no,name,status) values('" + eachContact + "','" + name + "',1);";
                        Log.e("ContactsFragment_Query3",insertQuery);
                    }
                    else
                        insertQuery = "INSERT INTO allcontacts(phone_no,name,status) values('"+eachContact+"','"+name+"',0);";
                    mydatabase.execSQL(insertQuery);
                }
            mydatabase.close();
        }catch (Exception ee){
            Log.e("ContactsFragment_Err2",ee.getMessage());
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
        Log.e("ContactsFragment","ContactsFragment onDetach");
        mListener = null;
        super.onDetach();
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
