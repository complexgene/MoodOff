package com.moodoff;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.PlaylistSongs;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Profile extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Profile() {
        // Required empty public constructor
    }

    public static Profile newInstance(String param1, String param2) {
        Profile fragment = new Profile();
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

    private void init(){
        view = mainInflater.inflate(R.layout.fragment_profile, mainContainer, false);
        myName = (TextView)view.findViewById(R.id.username);
        myPhNo = (TextView)view.findViewById(R.id.userPhNo);
        myEmail = (TextView)view.findViewById(R.id.useremailId);
        myDob = (TextView)view.findViewById(R.id.userdob);
        myTextStatus = (TextView)view.findViewById(R.id.myTextStatus);
        playAudioStatusButton = (FloatingActionButton)view.findViewById(R.id.playAudioStatus);
        myAudioStatusSong = new String();
        selectRingTone = (ImageButton)view.findViewById(R.id.selectRingTone);
        editTextStatus = (ImageButton)view.findViewById(R.id.editTextStatus);
    }

    View view,dialogView;
    TextView myName, myPhNo, myEmail, myDob, myTextStatus, statusChangeTitle;
    String myAudioStatusSong;
    ImageButton selectRingTone, editTextStatus;
    Button okButtonWidth,cancelButtonWidth,okButton,cancelButton;
    FloatingActionButton playAudioStatusButton;
    int screenHeight, screenWidth;
    ViewGroup mainContainer;
    LayoutInflater mainInflater;
    LinearLayout dialogContainer;
    StoreRetrieveDataInterface fileOperations;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainContainer = container;
        mainInflater = inflater;

        init();

        setUserProfileData();

        selectRingTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStatus(0);
            }
        });
        editTextStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStatus(1);
            }
        });
        playAudioStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong(myAudioStatusSong);
            }
        });

        return view;
    }

    private void playSong(String songURL){
        // Write the code to play the song and handle the seekbar too
    }

    private void editStatus(int textOrAudioStatus){
        final Dialog fbDialogue = new Dialog(view.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fbDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));

        dialogView = mainInflater.inflate(R.layout.fragment_selectsong, mainContainer, false);
        dialogContainer = (LinearLayout)dialogView.findViewById(R.id.eachRingToneSong);
        statusChangeTitle = (TextView)dialogView.findViewById(R.id.statusChangeTitle);
        okButton = (Button)dialogView.findViewById(R.id.songselectok);
        cancelButton = (Button)dialogView.findViewById(R.id.songselectcancel);

        if(textOrAudioStatus==0){
            editUserAudioStatus(fbDialogue);
        }
        else{
            editUserTextStatus(fbDialogue);
        }
        getAndSetScreenSizes();
        setWidthOfButtonAcrossScreen();
        fbDialogue.setContentView(dialogView);
        fbDialogue.setCancelable(true);
        fbDialogue.show();
    }

    private void editUserTextStatus(final Dialog fbDialogue){
        statusChangeTitle.setText("Edit your Text Status");
        dialogContainer.removeAllViews();
        final EditText tv = new EditText(dialogView.getContext());
        tv.setText(myTextStatus.getText());
        dialogContainer.addView(tv);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldStatus = myTextStatus.getText().toString();
                String newStatus = tv.getText().toString();
                if(oldStatus.equals(newStatus)){
                    Toast.makeText(getContext(),"Same as previous status",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(writeTheStatusChangeToFile(tv.getText().toString())){
                        myTextStatus.setText(tv.getText());
                    }
                    else{
                        Toast.makeText(getContext(),"Some error occured!! Please try later!!",Toast.LENGTH_SHORT).show();
                    }
                    fbDialogue.dismiss();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbDialogue.dismiss();
            }
        });

    }

    private boolean writeTheStatusChangeToFile(String newStatus){
        try {
            fileOperations = new StoreRetrieveDataImpl("UserData.txt");
            fileOperations.beginWriteTransaction();
            if(fileOperations.getValueFor("textStatus")==null){
                fileOperations.createNewData("textStatus",newStatus);
            }
            else{
                fileOperations.updateValueFor("textStatus",newStatus);
            }
            fileOperations.endWriteTransaction();
            UserDetails.setUserTextStatus(newStatus);
            return true;
        } catch (IOException e) {
            Log.e("Profile_writeToFile_Err","Couldn't save the file:"+e.getMessage());
            return false;
        }

    }

    private void editUserAudioStatus(final Dialog fbDialogue){
        int playButtonId = 0;
        statusChangeTitle.setText("Edit your Audio Status");
        dialogContainer.removeAllViews();
        HashMap<String,ArrayList<String>> allSongs = PlaylistSongs.getAllMoodPlayList();
        for(final String eachMood : allSongs.keySet()){
            /*LinearLayout moodTypeLayout = new LinearLayout(dialogView.getContext());
            moodTypeLayout.setBackgroundColor(Color.CYAN);
            TextView moodType = new TextView(dialogView.getContext());
            moodType.setText(eachMood);
            moodTypeLayout.addView(moodType);
            dialogContainer.addView(moodTypeLayout);
            */for(final String eachSong : allSongs.get(eachMood)){
                LinearLayout eachSongPanel = new LinearLayout(getContext());
                eachSongPanel.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutDetails.topMargin = 25;
                eachSongPanel.setLayoutParams(layoutDetails);
                eachSongPanel.setBackgroundColor(Color.RED);
                eachSongPanel.setGravity(Gravity.CENTER_VERTICAL);
                TextView songName = new TextView(dialogView.getContext());
                songName.setText(eachSong.replaceAll("_"," ").replaceAll("\\.mp3",""));
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutDetails.topMargin = 15;
                songName.setGravity(Gravity.CENTER_HORIZONTAL);
                songName.setTypeface(Typeface.DEFAULT_BOLD);
                songName.setLayoutParams(layoutDetails);
                eachSongPanel.addView(songName);

                LinearLayout playButtonAndSeekBar = new LinearLayout(getContext());
                playButtonAndSeekBar.setGravity(Gravity.CENTER_VERTICAL);
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutDetails.leftMargin = 50;
                layoutDetails.bottomMargin = 15;
                playButtonAndSeekBar.setLayoutParams(layoutDetails);
                final FloatingActionButton playButton = new FloatingActionButton(getContext());
                playButton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                playButton.setImageResource(R.drawable.play);
                playButton.setSize(FloatingActionButton.SIZE_MINI);
                playButton.setId(++playButtonId);
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //playSong(eachSong);
                        Toast.makeText(getContext(),eachSong,Toast.LENGTH_SHORT).show();
                    }
                });
                SeekBar seekBarForEachSong = new SeekBar(getContext());
                layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutDetails.rightMargin = 50;
                seekBarForEachSong.setLayoutParams(layoutDetails);
                playButtonAndSeekBar.addView(playButton);
                playButtonAndSeekBar.addView(seekBarForEachSong);

                eachSongPanel.addView(playButtonAndSeekBar);
                dialogContainer.addView(eachSongPanel);
            }
        }
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbDialogue.dismiss();
            }
        });

    }

    private void setUserProfileData(){
        String name = UserDetails.getUserName();
        if(name.length()>17){name=name.substring(0,17)+"...";}
        String phNo = UserDetails.getPhoneNumber();
        String email = UserDetails.getEmailId();
        if(email.length()>17){email=email.substring(0,17)+"...";}
        String dob = UserDetails.getDateOfBirth();
        myName.setText(name);
        myPhNo.setText(phNo);
        myEmail.setText(email);
        myDob.setText(dob);
        new UserDetails();
        myTextStatus.setText(UserDetails.getUserTextStatus());
        myTextStatus.setTextSize(20);
        myAudioStatusSong=UserDetails.getUserAudioStatusSong();
    }
    private void getAndSetScreenSizes(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }
    public void setWidthOfButtonAcrossScreen(){
        okButtonWidth = (Button)dialogView.findViewById(R.id.songselectok);
        cancelButtonWidth = (Button)dialogView.findViewById(R.id.songselectcancel);
        okButtonWidth.setWidth((int)Math.floor(0.5*screenWidth));
        cancelButtonWidth.setWidth((int)Math.floor(0.5*screenWidth));

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
