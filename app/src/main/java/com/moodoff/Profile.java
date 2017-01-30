package com.moodoff;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.AppData;
import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.helper.ValidateMediaPlayer;
import com.moodoff.model.UserDetails;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        profileImage = (ImageView)view.findViewById(R.id.profileImage);
        myName = (TextView)view.findViewById(R.id.username);
        myPhNo = (TextView)view.findViewById(R.id.userPhNo);
        myEmail = (TextView)view.findViewById(R.id.useremailId);
        myDob = (TextView)view.findViewById(R.id.userdob);
        myTextStatus = (TextView)view.findViewById(R.id.myTextStatus);
        textStatusLoveCount = (TextView)view.findViewById(R.id.textStatusLoveCount);
        audioStatusLoveCount = (TextView)view.findViewById(R.id.audioStatusLoveCount);
        playAudioStatusButton = (FloatingActionButton)view.findViewById(R.id.playAudioStatus);
        loveTextStatus = (FloatingActionButton)view.findViewById(R.id.loveTextStatus);
        loveAudioStatus = (FloatingActionButton)view.findViewById(R.id.loveAudioStatus);
        txtViewCurrentMood = (TextView)view.findViewById(R.id.txtView_currentMood);
        editBasicInfo = (FloatingActionButton)view.findViewById(R.id.editBasicInfo);
        myAudioStatusSong = new String();
        editAudioStatus = (ImageButton)view.findViewById(R.id.editAudioStatus);
        editTextStatus = (ImageButton)view.findViewById(R.id.editTextStatus);
        seekBar = (SeekBar)view.findViewById(R.id.myAudioStatusProgressBar);
        seekBar.setClickable(false);
        spinner = (ProgressBar) view.findViewById(R.id.profileProgressBar);
    }

    View view,dialogView;
    ImageView profileImage;
    TextView myName, myPhNo, myEmail, myDob, myTextStatus, statusChangeTitle, textStatusLoveCount, audioStatusLoveCount;
    TextView txtViewCurrentMood;
    String myAudioStatusSong;
    ImageButton editAudioStatus, editTextStatus;
    Button okButtonWidth,cancelButtonWidth,okButton,cancelButton;
    FloatingActionButton loveTextStatus, loveAudioStatus, editBasicInfo;
    int screenHeight, screenWidth;
    ViewGroup mainContainer;
    LayoutInflater mainInflater;
    LinearLayout dialogContainer;
    StoreRetrieveDataInterface fileOperations;
    String profileOfUser;
    public static FloatingActionButton playAudioStatusButton;
    public static MediaPlayer mediaPlayer = null;
    public static Boolean isSongPlaying = false;
    public static ProgressBar spinner;
    public static SeekBar seekBar;
    Handler seekHandler = new Handler();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainContainer = container;
        mainInflater = inflater;

        init();

        profileOfUser = mParam1;
        String p=ContactsManager.allReadContacts.get(profileOfUser);

        Toast.makeText(getContext(),"Loading profile of: "+ (p==null?UserDetails.getUserName():p),Toast.LENGTH_SHORT).show();

        // Check if its someone else's profile, then remove the edit button
        if(!profileOfUser.equals(UserDetails.getPhoneNumber())){
            editAudioStatus.setVisibility(View.GONE);editTextStatus.setVisibility(View.GONE);editBasicInfo.setVisibility(View.GONE);
        }

        setUserProfileData(profileOfUser);

        editTextStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStatus(0);
            }
        });
        editAudioStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStatus(1);
            }
        });

        playAudioStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Profile_AudioSong",myAudioStatusSong.toString());
                String myAudioStatusSongURL = HttpGetPostInterface.serverSongURL+myAudioStatusSong.replaceAll("@","/");
                Messenger.print(getContext(),myAudioStatusSongURL);
                playAudioStatusSong(myAudioStatusSongURL);
            }
        });
        loveTextStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               loveTheTextStatus(profileOfUser, textStatusLoveCount, getActivity());
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });

        return view;
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    public void seekUpdation() {
        if (mediaPlayer!=null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            if(seekBar.getMax()!=0) {
                seekHandler.postDelayed(run, 10);
            }
        }
    }

    private void playAudioStatusSong(String songURL){
        // Write the code to play the song and handle the seekbar too
        showSpinner();
        releaseMediaPlayerObject(mediaPlayer);
        mediaPlayer = new MediaPlayer();
        Log.e("Profile_SongPlayURL",songURL.toString());
        if(isSongPlaying==false) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try{
                //mp = MediaPlayer.create(this, Uri.parse(url));
                mediaPlayer.setDataSource(songURL);
                mediaPlayer.prepareAsync();
            } catch (IllegalArgumentException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mediaPlayer); e.printStackTrace();
            } catch (IllegalStateException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mediaPlayer); e.printStackTrace();
            } catch (IOException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mediaPlayer); e.printStackTrace();
            } catch (Exception e) {toastError(e.getMessage()); releaseMediaPlayerObject(mediaPlayer); e.printStackTrace();
            }
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    showPlayStopButton("stop");
                    seekBar.setClickable(true);
                    if(mediaPlayer!=null) {
                        seekBar.setMax(mediaPlayer.getDuration());
                        seekUpdation();
                    }
                    ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
                    validateMediaPlayer.initialiseAndValidateMediaPlayer("profile","play");
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    showPlayStopButton("play");
                    seekBar.setMax(0);
                    seekBar.setClickable(false);
                }
            });
        } else {
            Log.e("Profile_MP","Playing stop player");
            ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
            validateMediaPlayer.initialiseAndValidateMediaPlayer("profile","stop");
            showPlayStopButton("play");
            mediaPlayer.stop();
            seekBar.setMax(0);
            seekBar.setClickable(false);
        }
    }

    /*set play or pause button for display*/
    public static void showPlayStopButton(String playOrPause) {
        try {
            playOrPause = playOrPause.toLowerCase();
            if (playOrPause == "play") {
                isSongPlaying = false;
                playAudioStatusButton.setImageResource(R.drawable.play);
            } else {
                isSongPlaying = true;
                playAudioStatusButton.setImageResource(R.drawable.stop);
            }
            spinner.setVisibility(ProgressBar.GONE);
            playAudioStatusButton.setVisibility(Button.VISIBLE);
        } catch(Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject(mediaPlayer);
        }
    }

    public static void releaseMediaPlayerObject(MediaPlayer mp) {
        try {
            if (mp != null) {
                if(mp.isPlaying()){mp.stop();}
                mp.release();
                mp = null;
            }
        } catch(Exception e){e.fillInStackTrace();e.printStackTrace();}
    }

    /*show spinner in place of play/pause button*/
    public void showSpinner() {
        try {
            playAudioStatusButton.setVisibility(Button.GONE);
            spinner.setVisibility(ProgressBar.VISIBLE);
        } catch(Exception e){toastError(e.getMessage());}
    }

    /*Toast error message*/
    public static void toastError(String error) {
        //Toast.makeText(view.getContext(), "Oops! Somehing went wrong\n"+error.toString(), Toast.LENGTH_LONG).show();
        Log.e("GenericMood_MPissue",error);
    }

    private void loveTheTextStatus(String user, TextView txtViewToChange, Activity curActivity){
        ServerManager serverManager = new ServerManager();
        serverManager.loveTextStatus(user, txtViewToChange, curActivity);
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
            editUserTextStatus(fbDialogue);
        }
        else{
            editUserAudioStatus(fbDialogue);
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
                    if(writeTheStatusChangeToServerAndFile(0,tv.getText().toString())){
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

    private boolean writeTheStatusChangeToServerAndFile(int type, String newStatus){
        try {
            String statusType = (type==0)?"textStatus":"audioStatus";
            ServerManager serverManager = new ServerManager();
            serverManager.writeStatusChange(type, newStatus, getActivity(), myTextStatus);
            Log.e("Profile_"+statusType+"Chng","Server Change DONE");
            fileOperations = new StoreRetrieveDataImpl("UserData.txt");
            fileOperations.beginWriteTransaction();
            if(fileOperations.getValueFor(statusType)==null){
                fileOperations.createNewData(statusType,newStatus);
            }
            else{
                fileOperations.updateValueFor(statusType,newStatus);
            }
            fileOperations.endWriteTransaction();
            if(type==0)UserDetails.setUserTextStatus(newStatus);
            else if(type==1){myAudioStatusSong=newStatus;UserDetails.setUserAudioStatusSong(newStatus);}
            Log.e("Profile_"+statusType+"Chng","Internal File Change DONE");
            return true;
        } catch (IOException e) {
            Log.e("Profile_writeToFile_Err","Couldn't save the file:"+e.getMessage());
            return false;
        }

    }

    ArrayList<String> allSongsInMap = new ArrayList<>();
    private void editUserAudioStatus(final Dialog fbDialogue){
        int playButtonId = 0;
        statusChangeTitle.setText("Edit your Audio Status");
        dialogContainer.removeAllViews();
        HashMap<String,ArrayList<String>> allSongs = AppData.allMoodPlayList;
        final RadioGroup rg = new RadioGroup(getContext());

        for(final String eachMood : allSongs.keySet()) {
            for(String eachSong : allSongs.get(eachMood)) {
                allSongsInMap.add(eachMood+" : "+eachSong);
            }
        }
        RadioButton[] allSongsRadio = new RadioButton[allSongsInMap.size()];

        for(int i=0;i<allSongsInMap.size();i++){
            final String eachMood = allSongsInMap.get(i).split(" : ")[0];
            final String eachSong = allSongsInMap.get(i).split(" : ")[1];
            rg.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.topMargin = 25;
            rg.setLayoutParams(layoutDetails);
            rg.setBackgroundColor(Color.RED);
            rg.setGravity(Gravity.CENTER_VERTICAL);
            TextView songName = new TextView(dialogView.getContext());
            songName.setText(eachSong.replaceAll("\\.mp3",""));
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.topMargin = 15;
            songName.setGravity(Gravity.CENTER_HORIZONTAL);
            songName.setTypeface(Typeface.DEFAULT_BOLD);
            songName.setLayoutParams(layoutDetails);
            rg.addView(songName);

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
                    String songURL = HttpGetPostInterface.serverSongURL+eachMood+"/"+eachSong;
                    Messenger.print(getContext(),songURL);
                    Log.e("ProfilePLAYBTN",songURL);
                    //playSong(songURL);
                }
            });
            SeekBar seekBarForEachSong = new SeekBar(getContext());
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.rightMargin = 50;
            seekBarForEachSong.setLayoutParams(layoutDetails);
            allSongsRadio[i] = new RadioButton(getContext());
            rg.addView(allSongsRadio[i]);
            rg.addView(playButton);
            rg.addView(seekBarForEachSong);
            }
            dialogContainer.addView(rg);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String moodAndSong = allSongsInMap.get(rg.getCheckedRadioButtonId()-1);
                    String songStorePattern = moodAndSong.replace(" : ","@");
                    Messenger.print(getContext(),songStorePattern);
                    if(writeTheStatusChangeToServerAndFile(1,songStorePattern)){
                        myAudioStatusSong = songStorePattern;
                        Messenger.print(getContext(),"Audio Status Updated..");
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

    boolean profileDetailsNotRetrievedYet = true;
    HashMap<String,String> profileDataParsed = new HashMap<>();
    private void setUserProfileData(final String userPhoneNumber){
        final String serverURL = HttpGetPostInterface.serverURL;
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    URL url = new URL(serverURL + "/users/" + userPhoneNumber);
                    Log.e("Profile_ContactURL", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream is = urlConnection.getInputStream();
                    isr = new InputStreamReader(is);
                    int data = isr.read();
                    final StringBuilder response = new StringBuilder("");
                    while (data != -1) {
                        response.append((char) data);
                        data = isr.read();
                    }
                    Log.e("Profile_Data", response.toString());
                    profileDataParsed = ParseNotificationData.parseAndGetProfileData(response.toString());
                    Log.e("Profile_parsedHM",profileDataParsed.get("name")+" "+profileDataParsed.get("genderpic"));
                    profileDetailsNotRetrievedYet = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProfileData();
                        }
                    });

                } catch (Exception ee) {
                    Log.e("Profile_Err", ee.getMessage());
                }
            }
        }).start();
        //showProfileData();
    }
    private int getPicFor(String genderType){
        return (genderType.equals("0"))?R.drawable.man:R.drawable.woman;
    }
    private void showProfileData(){
        String name = profileDataParsed.get("name");
        if(name.length()>17){name=name.substring(0,17)+"...";}
        String phNo = profileDataParsed.get("phoneNumber");
        String email = profileDataParsed.get("email");
        if(email.length()>17){email=email.substring(0,15)+"...";}
        String dob = profileDataParsed.get("dob");
        myName.setText(name);
        myPhNo.setText(phNo);
        //myEmail.setText(email);
        //myDob.setText(dob);
        profileImage.setImageResource(getPicFor(profileDataParsed.get("genderpic")));
        new UserDetails();
        myTextStatus.setText(profileDataParsed.get("textStatus").replaceAll("_"," "));
        Typeface font = AppData.getAppFont(getContext());
        myTextStatus.setTypeface(font);
        myTextStatus.setTextSize(20);
        myAudioStatusSong=profileDataParsed.get("audioStatusURL");
        Messenger.print(getContext(),myAudioStatusSong);
        textStatusLoveCount.setText(profileDataParsed.get("textStatusLoveCount")+" people loved the status");
        audioStatusLoveCount.setText(profileDataParsed.get("audioStatusLoveCount")+" people loved the status");
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
