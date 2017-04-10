package com.moodoff.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.R;
import com.moodoff.helper.AllAppData;
import com.moodoff.helper.LoggerBaba;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static android.content.Context.MODE_PRIVATE;

public class ContactsFragment extends Fragment{
    // All Variables declaration -----------------------------------------------------------------------
    public static boolean openedAProfile = false;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    View mainView;
    ProgressBar spinner;
    TextView lastActiveTS;
    ViewGroup mainContainer;
    LayoutInflater mainInflater;
    Context ctx;
    SQLiteDatabase mydatabase;
    HashMap<String,String> allC = new HashMap<>();
    User singleTonUser = User.getInstance();
    static HashMap<String, HashMap<String, String>> userAndMood = new HashMap<>();
    int countOfIterationsToFetchMoodDetailsForEachAppUserFriend = 0;
    public static boolean updateViewCalled = false;
    Button myProfile;
    ServerManager serverManager;
    // Declaration of all variables complete------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainContainer = container;
        mainInflater = inflater;

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ctx = mainView.getContext();

        // Live monitoring of the mood status changes
        detailsForLiveMoodRelatedFeeds();

        spinner = (ProgressBar)mainView.findViewById(R.id.tempSpinner);

        // Check if somebody joins our app and reload the profile view then..
        serverManager = new ServerManager();
        serverManager.resursiveFetchContactsFromServer();

        // Load all the contacts in the hashMap
        allC = AllAppData.allReadContacts;

        return mainView;
    }

    private View getHorizontalLine(int width){
        View v = new View(ctx);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, width);
        layoutParams.leftMargin = 50;
        layoutParams.rightMargin = 50;
        v.setLayoutParams(layoutParams);
        v.setBackgroundColor(Color.parseColor("#B3B3B3"));
        return v;
    }
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

        lastActiveTS = (TextView)mainView.findViewById(R.id.lastActiveTS);
        boolean currentUserIsLive = userAndMood.get(singleTonUser.getUserMobileNumber()).get("liveNow").equals("1")?true:false;
        String userMoodType = userAndMood.get(singleTonUser.getUserMobileNumber()).get("moodType");
        String userLastTS = userAndMood.get(singleTonUser.getUserMobileNumber()).get("lastActiveTS");
        TextView lastListenedOrLiveLabel = (TextView)mainView.findViewById(R.id.lastListenedOrLiveLabel);
        lastListenedOrLiveLabel.setTypeface(Typeface.DEFAULT_BOLD);
        if(currentUserIsLive) {
            lastListenedOrLiveLabel.setText("Live On : ");
            lastListenedOrLiveLabel.setTextColor(Color.GREEN);
            lastActiveTS.setText(userMoodType);
        }
        else {
            lastListenedOrLiveLabel.setText("Last listened at : ");
            lastListenedOrLiveLabel.setTextColor(Color.RED);
            lastActiveTS.setText(userLastTS);
        }
        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openedAProfile = true;
                loadProfile(singleTonUser.getUserMobileNumber());
            }
        });
        // Populating all contacts who uses app and who doesn't
        ScrollView contactsScroll = (ScrollView)mainView.findViewById(R.id.allContacts);
        contactsScroll.removeAllViews();
        LinearLayout eachContact = new LinearLayout(ctx);
        eachContact.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        eachContact.setLayoutParams(layoutDetails);
        allC = AllAppData.allReadContacts;
        ArrayList<String> friendWhoUsesApp = AllAppData.friendsWhoUsesApp;
        Log.e("CONTCCCCCTSSSS",allC.size()+" "+friendWhoUsesApp.size());

        eachContact.addView(getHorizontalLine(1));
        LinearLayout titleAppUsers = new LinearLayout(ctx);
        titleAppUsers.setGravity(Gravity.CENTER);
        layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutDetails.topMargin = 10;
        layoutDetails.bottomMargin = 10;
        layoutDetails.leftMargin = 20;
        layoutDetails.rightMargin = 20;
        titleAppUsers.setLayoutParams(layoutDetails);
        titleAppUsers.setBackgroundResource(R.drawable.poster);
        TextView tvv = new TextView(ctx);
        tvv.setGravity(Gravity.CENTER);
        tvv.setPadding(3,3,3,3);
        tvv.setTextColor(Color.WHITE);
        tvv.setText(" Friends Using MoodOff ");
        titleAppUsers.addView(tvv);
        eachContact.addView(titleAppUsers);
        for(final String eachFriendContact : friendWhoUsesApp) {
            String contactName = allC.get(eachFriendContact);
            if (contactName!=null && !isNotHavingAnyCharacter(contactName)) {
                LinearLayout eachContactLayout = new LinearLayout(ctx);
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

                // This gets the value of the current mood details(moodType, isLIVE, atTtime) which was populated
                // from MAIN before this function call

                String moodType = userAndMood.get(eachFriendContact).get("moodType");
                boolean userIsLive = userAndMood.get(eachFriendContact).get("liveNow").equals("1")?true:false;

                LinearLayout moodStatusAndMood = new LinearLayout(ctx);
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
                    moodStatus.setText("Live on Mood");
                }
                else{
                    moodStatus.setTextColor(Color.RED);
                    moodStatus.setTypeface(Typeface.DEFAULT_BOLD);
                    moodStatus.setText("Last Listened Mood");
                }
                //moodStatus.setBackgroundResource(R.drawable.contactsusingappdesign);
                moodStatus.setLayoutParams(layoutDetails);
                moodStatusAndMood.addView(moodStatus);

                TextView moodName = new TextView(ctx);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                moodName.setAllCaps(false);
                moodName.setText(" : " + moodType.replaceAll("_"," "));
               // moodName.setBackgroundResource(R.drawable.contactsusingappdesign);
                moodName.setLayoutParams(layoutDetails);
                moodStatusAndMood.addView(moodName);

                LinearLayout atTime = new LinearLayout(ctx);
                atTime.setGravity(Gravity.CENTER);
                if(!userIsLive) {
                    TextView aT = new TextView(ctx);
                    aT.setTextColor(Color.RED);
                    aT.setTypeface(Typeface.DEFAULT_BOLD);
                    aT.setText("Last Listened At ");
                    aT.setLayoutParams(layoutDetails);
                    //moodStatusAndMood.addView(aT);

                    TextView lastActiveTime = new TextView(ctx);
                    String lastActiveTimeStamp = (userAndMood.get(eachFriendContact).get("lastActiveTS")).toString();
                    Log.e("ContactsFragment", "Retrieved Here is:" + lastActiveTimeStamp);
                    layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    lastActiveTime.setAllCaps(false);
                    lastActiveTime.setText(": " + lastActiveTimeStamp);
                    // moodName.setBackgroundResource(R.drawable.contactsusingappdesign);
                    lastActiveTime.setLayoutParams(layoutDetails);
                    //moodStatusAndMood.addView(lastActiveTime);
                    atTime.addView(aT);
                    atTime.addView(lastActiveTime);

                }

                atTime.setPadding(0,0,0,20);
                eachContactLayout.addView(moodStatusAndMood);
                eachContactLayout.addView(atTime);

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
        LinearLayout titleNotAppUsers = new LinearLayout(ctx);
        titleNotAppUsers.setGravity(Gravity.CENTER);
        layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutDetails.topMargin = 10;
        layoutDetails.bottomMargin = 10;
        layoutDetails.leftMargin = 20;
        layoutDetails.rightMargin = 20;
        layoutDetails.gravity = Gravity.CENTER;
        titleNotAppUsers.setLayoutParams(layoutDetails);
        titleNotAppUsers.setBackgroundResource(R.drawable.poster);
        TextView tvv1 = new TextView(ctx);
        tvv1.setPadding(3,3,3,3);
        tvv1.setTextColor(Color.WHITE);
        tvv1.setText(" Friends Not Using MoodOff ");
        titleNotAppUsers.addView(tvv1);
        eachContact.addView(titleNotAppUsers);
        for(final String eachCntct: AllAppData.friendsWhoDoesntUseApp){
            final String contactName = allC.get(eachCntct);
            if(contactName!=null && !isNotHavingAnyCharacter(contactName)){
                eachContact.addView(getHorizontalLine(1));
                LinearLayout eachContactLayout = new LinearLayout(ctx);
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
                TextView personName = new TextView(ctx);
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
                                        Messenger.print(ctx,"App Link Sent");
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        Messenger.print(ctx,"App Link Not Sent");
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
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
        spinner.setVisibility(View.INVISIBLE);
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

    boolean liveStatusLoaded = false, liveMoodTypeLoaded = false, liveLastActiveTSLoaded = false;
    // Async Listeners for Live Feed In Profile
    private void detailsForLiveMoodRelatedFeeds(){
        AllAppData.friendsWhoUsesApp.add(singleTonUser.getUserMobileNumber());
        final int limit = AllAppData.friendsWhoUsesApp.size();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference liveFeedNode = firebaseDatabase.getReference().child("livefeed");
        final DatabaseReference checkAliveNode = firebaseDatabase.getReference().child("checkAlive");

        for(final String eachAppUsingFriend : AllAppData.friendsWhoUsesApp){
            DatabaseReference
              dbRefForLiveFeedMoodType = liveFeedNode.child(eachAppUsingFriend)
                                                             .child(AllAppData.moodLiveFeedNode)
                                                             .child(AllAppData.userLiveMood);

            final HashMap<String, String> moodTypeAndLiveStatus = new HashMap<>();

            dbRefForLiveFeedMoodType.addChildEventListener(new ChildEventListener() {
                  @Override
                  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                      Log.e("ContactsFragment", "Called for monitoring the children..");
                      String value = dataSnapshot.getValue().toString();
                      if(value.length() == 1) {
                          moodTypeAndLiveStatus.put("liveNow",value);
                          userAndMood.put(eachAppUsingFriend, moodTypeAndLiveStatus);
                      }
                      else {
                          moodTypeAndLiveStatus.put("moodType",value);
                          userAndMood.put(eachAppUsingFriend, moodTypeAndLiveStatus);
                          liveMoodTypeLoaded = true;
                      }

                      countOfIterationsToFetchMoodDetailsForEachAppUserFriend++;
                      if(countOfIterationsToFetchMoodDetailsForEachAppUserFriend >= (limit + limit + limit)) {
                          addOwnProfileAndRefreshButton();
                      }

                      LoggerBaba.printMsg("ContactsFragment", userAndMood.toString());
                  }
                  @Override
                  public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                      String value = dataSnapshot.getValue().toString();
                      if(value.length() == 1) {
                          moodTypeAndLiveStatus.put("liveNow",value);
                          if(!eachAppUsingFriend.equals(singleTonUser.getUserMobileNumber()) && value.equals("1")) {
                              serverManager.displayAlertNotificationOnTopBarOfPhone(ctx, "Some friends came live");
                              addOwnProfileAndRefreshButton();
                          }
                          else {
                              checkAliveNode.child(eachAppUsingFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(DataSnapshot dataSnapshot) {
                                      String tsValue = dataSnapshot.getValue(String.class);
                                      String processedTime = getProcessedTime(tsValue.split("_")[0], tsValue.split("_")[1]);
                                      moodTypeAndLiveStatus.put("lastActiveTS", processedTime);
                                      userAndMood.put("lastActiveTS", moodTypeAndLiveStatus);
                                      userAndMood.put(eachAppUsingFriend, moodTypeAndLiveStatus);
                                      addOwnProfileAndRefreshButton();
                                  }
                                  @Override
                                  public void onCancelled(DatabaseError databaseError) { }
                              });
                          }
                      }
                      else {
                          moodTypeAndLiveStatus.put("moodType",value);
                          userAndMood.put(eachAppUsingFriend, moodTypeAndLiveStatus);
                          addOwnProfileAndRefreshButton();
                      }
                      LoggerBaba.printMsg("ContactsFragment", userAndMood.toString());
                  }
                  @Override
                  public void onChildRemoved(DataSnapshot dataSnapshot) {

                  }
                  @Override
                  public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                  }
                  @Override
                  public void onCancelled(DatabaseError databaseError) {

                  }
              });

            DatabaseReference
                    dbRefForCheckAlive = checkAliveNode.child(eachAppUsingFriend);
            dbRefForCheckAlive.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String[] tsValue = dataSnapshot.getValue(String.class).split("_");
                    String date = tsValue[0];
                    String time = tsValue[1];

                    moodTypeAndLiveStatus.put("lastActiveTS", getProcessedTime(date, time));
                    userAndMood.put(eachAppUsingFriend, moodTypeAndLiveStatus);

                    Log.e("ContactsFragment", "Time Changed:" + userAndMood.get(eachAppUsingFriend).get("lastActiveTS"));

                    countOfIterationsToFetchMoodDetailsForEachAppUserFriend++;
                    if(countOfIterationsToFetchMoodDetailsForEachAppUserFriend >= (limit + limit + limit)) {
                        addOwnProfileAndRefreshButton();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private String getProcessedDate(String date){
        String[] months = {"Jan","Feb","Mar","Apr","may","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        String[] YMD = date.split("-");
        Log.e("NotFrag", date);
        return YMD[0]+"-"+months[Integer.parseInt(YMD[1])-1]+"'"+YMD[2].substring(2);
    }
    private String getProcessedTime(String date, String time) {
        String timeToDisplay = "";
        String hrsMins = time.substring(0, time.lastIndexOf(":"));
        Log.e("ContactsFragment", hrsMins);
        int hrs = Integer.parseInt(hrsMins.split(":")[0]);
        String mins = hrsMins.split(":")[1];
        if(hrs > 12){
            timeToDisplay = getProcessedDate(date)+ " " + (hrs-12) +  ":" + mins + "PM";
        }
        else{
            timeToDisplay = getProcessedDate(date) + " " + hrs + ":" + mins +"AM";
        }
        return timeToDisplay;
    }

    // Fragment specific methods -- NOT USED --------------------------------------------------------------
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
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
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
}
