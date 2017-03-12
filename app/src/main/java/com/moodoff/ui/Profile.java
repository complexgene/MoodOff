package com.moodoff.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.R;
import com.moodoff.helper.AppData;
import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.helper.ValidateMediaPlayer;
import com.moodoff.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Profile extends Fragment implements AudioManager.OnAudioFocusChangeListener{

    private AudioManager mAudioManager;

    @Override
    public void onAudioFocusChange(int i) {
        if(mediaPlayer!=null && mediaPlayer.isPlaying()) {
            if (i <= 0 && AllTabs.mViewPager.getCurrentItem()==2) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
        }
    }

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
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
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
        playAudioStatusButton = (ImageButton)view.findViewById(R.id.playAudioStatus);
        loveTextStatus = (ImageButton)view.findViewById(R.id.loveTextStatus);
        loveAudioStatus = (ImageButton)view.findViewById(R.id.loveAudioStatus);
        txtViewCurrentMood = (TextView)view.findViewById(R.id.txtView_currentMood);
        txtViewUserStatus = (TextView)view.findViewById(R.id.userStatus);
        lastMoodListened = (TextView)view.findViewById(R.id.lastMoodListened);
        btnCurrentMoodPic = (Button)view.findViewById(R.id.btn_currentMood);
        editBasicInfo = (ImageButton)view.findViewById(R.id.editBasicInfo);
        myAudioStatusSong = new String();
        editAudioStatus = (ImageButton)view.findViewById(R.id.editAudioStatus);
        editTextStatus = (ImageButton)view.findViewById(R.id.editTextStatus);
        backbutton = (ImageButton)view.findViewById(R.id.backbutton);
        seekBar_Profile = (SeekBar)view.findViewById(R.id.myAudioStatusProgressBar);
        seekBar_Profile.setEnabled(false);
        spinner = (ProgressBar) view.findViewById(R.id.profileProgressBar);
    }

    View view;
    ImageView profileImage;
    TextView myName, myPhNo, myEmail, myDob, myTextStatus, statusChangeTitle, textStatusLoveCount, audioStatusLoveCount;
    TextView txtViewCurrentMood,txtViewUserStatus, lastMoodListened;
    ImageButton editAudioStatus, editTextStatus,okButton,cancelButton,okButtonWidth,cancelButtonWidth;
    Button  btnCurrentMoodPic;
    ImageButton loveTextStatus, loveAudioStatus, editBasicInfo, backbutton;
    int screenHeight, screenWidth;
    ViewGroup mainContainer;
    LayoutInflater mainInflater;
    LinearLayout dialogContainer;
    StoreRetrieveDataInterface fileOperations;
    String profileOfUser;
    public static View dialogView;
    public static ImageButton playAudioStatusButton;
    public static MediaPlayer mediaPlayer = null;
    public static Boolean isSongPlaying = false;
    public static ProgressBar spinner;
    public static SeekBar seekBar_Profile;
    public static Handler seekHandler = new Handler();
    public static int ifSelectingAudioStatus = 0;
    public static String myAudioStatusSong;
    User userData = User.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainContainer = container;
        mainInflater = inflater;

        init();

        profileOfUser = mParam1;
        String p=ContactsManager.allReadContacts.get(profileOfUser);

        Toast.makeText(getContext(),"Loading profile of: "+ (p==null?userData.getUserName():p),Toast.LENGTH_SHORT).show();

        // Check if its someone else's profile, then remove the edit button
        if(!profileOfUser.equals(userData.getUserMobileNumber())){
            editAudioStatus.setVisibility(View.GONE);editTextStatus.setVisibility(View.GONE);editBasicInfo.setVisibility(View.GONE);
        }

        setUserProfileData(profileOfUser);

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate();
            }
        });
        editTextStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStatus(0);
            }
        });
        editAudioStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer != null ) {
                    if (isSongPlaying == true) {
                        playAudioStatusSong(myAudioStatusSong);
                    }
                }
                editStatus(1);
            }
        });

        playAudioStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Profile_AudioSong",myAudioStatusSong.toString());

                Messenger.print(getContext(),myAudioStatusSong.toString());
                playAudioStatusSong(myAudioStatusSong);
            }
        });
        loveTextStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               loveTheTextStatus(profileOfUser, textStatusLoveCount, getActivity());
            }
        });
        seekBar_Profile.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar_Profile) {
                if (mediaPlayer!=null) {
                    mediaPlayer.seekTo(seekBar_Profile.getProgress());
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar_Profile) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar_Profile, int progress, boolean fromUser) {
            }
        });

        return view;
    }

    public static Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    public static void seekUpdation() {
        if (mediaPlayer!=null) {
            if(seekBar_Profile.getMax()!=0) {
                seekBar_Profile.setProgress(mediaPlayer.getCurrentPosition());
                seekHandler.postDelayed(run, 10);
            }
        }
    }

    public static void playAudioStatusSong(String myAudioStatusSongURL){
        // Write the code to play the song and handle the seekbar too
        showSpinner();
        String songURL = HttpGetPostInterface.serverSongURL+myAudioStatusSongURL.replaceAll("@","/");
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
                    ifSelectingAudioStatus = 0;
                    ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
                    validateMediaPlayer.initialiseAndValidateMediaPlayer("profile","play");
                    mediaPlayer.start();
                    if(mediaPlayer!=null) {
                        seekBar_Profile.setMax(mediaPlayer.getDuration());
                        seekUpdation();
                        seekBar_Profile.setEnabled(true);
                    }
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    showPlayStopButton("play");
                    seekBar_Profile.setMax(0);
                    seekBar_Profile.setEnabled(false);
                }
            });
        } else {
            Log.e("Profile_MP","Playing stop player");
            ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
            validateMediaPlayer.initialiseAndValidateMediaPlayer("profile","stop");
            showPlayStopButton("play");
            if (mediaPlayer!=null) {
                mediaPlayer.stop();
                seekBar_Profile.setMax(0);
                seekBar_Profile.setEnabled(false);
            }
        }
    }

    /*set play or pause button for display*/
    public static void showPlayStopButton(String playOrPause) {
        try {
            playOrPause = playOrPause.toLowerCase();
            if (playOrPause == "play") {
                isSongPlaying = false;
                playAudioStatusButton.setImageResource(R.drawable.playdedicate);
            } else {
                isSongPlaying = true;
                playAudioStatusButton.setImageResource(R.drawable.stopdedicate);
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
    public static void showSpinner() {
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
        okButton = (ImageButton)dialogView.findViewById(R.id.songselectok);
        cancelButton = (ImageButton)dialogView.findViewById(R.id.songselectcancel);
        if(textOrAudioStatus==0){
            editUserTextStatus(fbDialogue);
        }
        else{
            editUserAudioStatus(fbDialogue);
        }
        //getAndSetScreenSizes();
        //setWidthOfButtonAcrossScreen();
        fbDialogue.setContentView(dialogView);
        fbDialogue.setCancelable(true);
        fbDialogue.show();

        fbDialogue.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(mediaPlayer != null) {
                    if(mediaPlayer.isPlaying()) {
                        playAudioStatusSelectionSong();
                    }
                }
            }
        });
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
                    Toast.makeText(getContext(),"Looks like nothing's changed..!!",Toast.LENGTH_SHORT).show();
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
            if(type==0)userData.setUserTextStatus(newStatus);
            else if(type==1){myAudioStatusSong=newStatus;userData.setUserAudioStatusSong(newStatus);}
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
        statusChangeTitle.setText("Change Audio Status");
        dialogContainer.removeAllViews();
        HashMap<String,ArrayList<String>> allSongs = AppData.allMoodPlayList;
        final RadioGroup rg = new RadioGroup(getContext());

        for(final String eachMood : allSongs.keySet()) {
            int i=0;
            for(String eachSong : allSongs.get(eachMood)) {
                if(i++<5)
                allSongsInMap.add(eachMood+" : "+eachSong);
                else break;
            }
        }
        RadioButton[] allSongsRadio = new RadioButton[allSongsInMap.size()];

        for(int i=0;i<allSongsInMap.size();i++){
            final String eachMood = allSongsInMap.get(i).split(" : ")[0];
            final String eachSong = allSongsInMap.get(i).split(" : ")[1];
            rg.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.leftMargin = 20;
            layoutDetails.rightMargin = 20;
            rg.setLayoutParams(layoutDetails);
            rg.setBackgroundResource(R.drawable.songnamedisplaypanel);
            rg.setGravity(Gravity.CENTER_VERTICAL);

            rg.addView(getHorizontalLine(1));

            allSongsRadio[i] = new RadioButton(getContext());
            rg.addView(allSongsRadio[i]);
            TextView songName = new TextView(dialogView.getContext());
            songName.setPadding(50,0,0,0);
            songName.setText(eachSong.replaceAll("\\.mp3","").replaceAll("_"," "));
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.leftMargin = 40;
            songName.setTypeface(Typeface.DEFAULT_BOLD);
            songName.setLayoutParams(layoutDetails);
            rg.addView(songName);

            LinearLayout playButtonAndSeekBar = new LinearLayout(getContext());
            playButtonAndSeekBar.setGravity(Gravity.CENTER);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.leftMargin = 20;
            layoutDetails.rightMargin = 20;
            playButtonAndSeekBar.setLayoutParams(layoutDetails);

            //Spinner
            final ProgressBar audioStatusSelectionProgressBar = new ProgressBar(view.getContext());
            audioStatusSelectionProgressBar.setId((i+1)+2000000);
            audioStatusSelectionProgressBar.setVisibility(View.GONE);

            //PlayButton
            final ImageButton playButton = new ImageButton(getContext());
            playButton.setImageResource(R.drawable.playdedicate);
            playButton.setBackgroundResource(0);
            playButton.setId(playButtonId++);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String songURL = HttpGetPostInterface.serverSongURL+eachMood+"/"+eachSong;
                    //Messenger.print(getContext(),songURL);
                    Log.e("ProfilePLAYBTN",songURL);
                    currentPlayOrStopButtonId = v.getId();
                    currentView = v;
                    songFileName = songURL;
                    playAudioStatusSelectionSong();
                }
            });
            //Seekbar
            SeekBar seekBar_ProfileForEachSong = new SeekBar(getContext());
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            seekBar_ProfileForEachSong.setEnabled(false);
            seekBar_ProfileForEachSong.setLayoutParams(layoutDetails);
            seekBar_ProfileForEachSong.setId((i+1)+1000000);
            seekBar_ProfileForEachSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if(mediaPlayer!=null)
                        mediaPlayer.seekTo(seekBar.getProgress());
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }
            });
            playButtonAndSeekBar.addView(playButton);
            playButtonAndSeekBar.addView(audioStatusSelectionProgressBar);
            playButtonAndSeekBar.addView(seekBar_ProfileForEachSong);

            rg.addView(playButtonAndSeekBar);
        }
        dialogContainer.addView(rg);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog dialog = new ProgressDialog(getContext());
                dialog.setMessage("Updating the audio status...");
                dialog.show();
                dialog.setCancelable(false);
                String moodAndSong = allSongsInMap.get(rg.getCheckedRadioButtonId()-1);
                String songStorePattern = moodAndSong.replace(" : ","@");
                Messenger.print(getContext(),songStorePattern);
                if(writeTheStatusChangeToServerAndFile(1,songStorePattern)){
                    myAudioStatusSong = songStorePattern;
                    Messenger.print(getContext(),"Audio Status Updated..");
                    fbDialogue.dismiss();
                }
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbDialogue.dismiss();
                if(mediaPlayer != null) {
                    if(mediaPlayer.isPlaying()) {
                        playAudioStatusSelectionSong();
                    }
                }
            }
        });
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

    public static ImageButton currentAudioStatusSelectionPlayOrStopButton;
    public static ProgressBar currentAudioStatusSelectionSpinner;
    public static SeekBar currentAudioStatusSelectionSeekbar;
    public static int currentPlayOrStopButtonId = -1, idOfTheLastPlayButtonClicked = -1;
    public static View currentView, lastView;
    public static String songFileName;
    public static Handler seekHandlerForAudioStatusSelection = new Handler();

    Runnable runAudioStatuSelection = new Runnable() {
        @Override
        public void run() {
            seekUpdationForAudioStatusSelection();
        }
    };

    public void seekUpdationForAudioStatusSelection() {
        if (mediaPlayer!=null) {
            if(currentAudioStatusSelectionSeekbar.getMax()!=0) {
                currentAudioStatusSelectionSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekHandlerForAudioStatusSelection.postDelayed(runAudioStatuSelection, 10);
            }
        }
    }

    public static void playAudioStatusSelectionSong(){
//        Log.e("Profile_SelectStatus", currentView.getId() + "");
        currentAudioStatusSelectionPlayOrStopButton = (ImageButton) currentView.findViewById(currentPlayOrStopButtonId);
        currentAudioStatusSelectionSpinner = (ProgressBar) dialogView.findViewById((currentPlayOrStopButtonId+1)+2000000);
        currentAudioStatusSelectionSeekbar = (SeekBar) dialogView.findViewById((currentPlayOrStopButtonId+1)+1000000);
        Log.e("Profile_SelectStatus","I started the spinner");
        currentAudioStatusSelectionPlayOrStopButton.setVisibility(View.GONE);
        currentAudioStatusSelectionSpinner.setVisibility(View.VISIBLE);
        if(currentPlayOrStopButtonId != idOfTheLastPlayButtonClicked) {
            if (idOfTheLastPlayButtonClicked != -1) {
                ImageButton lastPlayedButton = (ImageButton) lastView.findViewById(idOfTheLastPlayButtonClicked);
                lastPlayedButton.setImageResource(R.drawable.playdedicate);
                SeekBar lastSeekBar = (SeekBar) dialogView.findViewById((idOfTheLastPlayButtonClicked+1)+1000000);
                lastSeekBar.setProgress(0);
                lastSeekBar.setEnabled(false);
            }
            //if(mp!=null)mp.reset();
            currentAudioStatusSelectionPlayOrStopButton.setImageResource(R.drawable.stopdedicate);
            idOfTheLastPlayButtonClicked = currentPlayOrStopButtonId;
            lastView = currentView;
            ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
            validateMediaPlayer.initialiseAndValidateMediaPlayer("profile","play");
            play(songFileName);
        }
        else {
            if(mediaPlayer.isPlaying()){
                currentAudioStatusSelectionSeekbar.setProgress(0);
                currentAudioStatusSelectionSeekbar.setEnabled(false);
                mediaPlayer.reset();
                currentAudioStatusSelectionPlayOrStopButton.setImageResource(R.drawable.playdedicate);
                ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
                validateMediaPlayer.initialiseAndValidateMediaPlayer("profile","stop");
                currentAudioStatusSelectionPlayOrStopButton.setVisibility(View.VISIBLE);
                currentAudioStatusSelectionSpinner.setVisibility(View.GONE);
            }
            else {
                currentAudioStatusSelectionSeekbar.setMax(mediaPlayer.getDuration());
                currentAudioStatusSelectionSeekbar.setEnabled(true);
                seekUpdation();
                ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
                validateMediaPlayer.initialiseAndValidateMediaPlayer("profile","play");
                play(songFileName);
                currentAudioStatusSelectionPlayOrStopButton.setImageResource(R.drawable.stopdedicate);
            }
        }
    }

    public static void play(String songUrl){
        releaseMediaPlayerObject(mediaPlayer);
        mediaPlayer = new MediaPlayer();
//        Log.e("Profile_SongFile",songFileName);
//        String[] moodTypeAndSong = songFileName.split("@");
//        String url = serverSongURL + moodTypeAndSong[0]+"/"+moodTypeAndSong[1];
        Log.e("Profile_SelectStatusURL", songUrl.toString()+" sB id:"+currentAudioStatusSelectionSeekbar.getId());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setLooping(false);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if(mediaPlayer!=null) {
                        currentAudioStatusSelectionSeekbar.setMax(mediaPlayer.getDuration());
                        seekUpdation();
                        Log.e("Profile_SelectStatus","I stopped the spinner");
                        currentAudioStatusSelectionPlayOrStopButton.setVisibility(View.VISIBLE);
                        currentAudioStatusSelectionSpinner.setVisibility(View.GONE);
                        currentAudioStatusSelectionSeekbar.setEnabled(true);
                    }
                    ifSelectingAudioStatus = 1;
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    currentAudioStatusSelectionSeekbar.setMax(0);
                    currentAudioStatusSelectionPlayOrStopButton.setImageResource(R.drawable.playdedicate);
                }
            });
        } catch (IllegalArgumentException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mediaPlayer); e.printStackTrace();
        } catch (IllegalStateException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mediaPlayer); e.printStackTrace();
        } catch (IOException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mediaPlayer); e.printStackTrace();
        } catch (Exception e) {toastError(e.getMessage()); releaseMediaPlayerObject(mediaPlayer); e.printStackTrace();
        }
    }

    public static boolean profileDetailsNotRetrievedYet = true;
    HashMap<String,String> profileDataParsed = new HashMap<>();
    DatabaseReference dbRef;
    public static String currentMood = "Not Live";
    private void setUserProfileData(final String userPhoneNumber){
        dbRef = FirebaseDatabase.getInstance().getReference().child(profileOfUser);
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
                    ServerManager serverManager = new ServerManager();
                    serverManager.getLiveMood(userPhoneNumber);
                    while(profileDetailsNotRetrievedYet);
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

    @Override
    public void onStart() {
        super.onStart();
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myName.setText(dataSnapshot.child("user").getValue(String.class).replaceAll("_"," "));
                myPhNo.setText(dataSnapshot.child("phNo").getValue(String.class));
                profileImage.setImageResource(getPicFor(dataSnapshot.child("gender").getValue(String.class)));
                myTextStatus.setText(profileDataParsed.get("textStatus").replaceAll("_"," "));
                myAudioStatusSong=profileDataParsed.get("audioStatusURL");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void showProfileData(){
        /*String email = profileDataParsed.get("email");
        String dob = profileDataParsed.get("dob");
        email = dbRef.child("email").toString();
        dob = dbRef.child("dob").toString();
*/
        //myEmail.setText(email);
        //myDob.setText(dob);
        //profileImage.setImageResource(getPicFor(profileDataParsed.get("genderpic")));
        myTextStatus.setText(profileDataParsed.get("textStatus").replaceAll("_"," "));
        myAudioStatusSong=profileDataParsed.get("audioStatusURL");
        //Messenger.print(getContext(),myAudioStatusSong);
        textStatusLoveCount.setText(profileDataParsed.get("textStatusLoveCount")+" people loved the status");
        audioStatusLoveCount.setText(profileDataParsed.get("audioStatusLoveCount")+" people loved the status");
        Log.e("Profile____","gonna check live..."+currentMood);
        String mood = currentMood.split("_")[0].trim();
        boolean userIsNotLive = currentMood.split("_")[1].equals("0");
        Log.e("Profile__",currentMood);
        String userStatusText="";int userStatusColor=0;
       if(userIsNotLive){
           userStatusText = "[Offline]:";
           userStatusColor = Color.rgb(255,0,0);
           lastMoodListened.setText("Last Mood Listened..");
       }
        else{
           userStatusText = "[Live]:";
           userStatusColor = Color.rgb(255,0,255);
           lastMoodListened.setText("Listening To Mood..");
       }
        txtViewUserStatus.setTextColor(userStatusColor);
        txtViewUserStatus.setText(userStatusText);
        btnCurrentMoodPic.setBackgroundResource(getResImage(mood));
        txtViewCurrentMood.setText(Character.toUpperCase(mood.charAt(0))+mood.substring(1));
        profileDetailsNotRetrievedYet = true;
    }
    private int getResImage(String currentMood){
        switch(currentMood){
            case "crazy":{return R.drawable.mood_crazy;}
            case "on_tour":{return R.drawable.mood_ontour;}
            case "old_era":{return R.drawable.mood_oldera;}
            case "party":{return R.drawable.mood_party;}
            case "workout":{return R.drawable.mood_workout;}
            case "friends":{return R.drawable.mood_friends;}
            case "romantic":{return R.drawable.mood_romantic1;}
            case "dance":{return R.drawable.mood_dance;}
            case "sad":{return R.drawable.mood_sad;}
            case "missu":{return R.drawable.mood_missu;}
        }
        return 0;
    }

    private void getAndSetScreenSizes(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    public void setWidthOfButtonAcrossScreen(){
        /*okButtonWidth = (ImageButton)dialogView.findViewById(R.id.songselectok);
        cancelButtonWidth = (ImageButton)dialogView.findViewById(R.id.songselectcancel);
        okButtonWidth.setWidth((int)Math.floor(0.5*screenWidth));
        cancelButtonWidth.setWidth((int)Math.floor(0.5*screenWidth));
*/
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
        Log.e("Profile_onDetach", "OnDetach");
        if(AllTabs.mViewPager.getCurrentItem()==2) {
            ContactsFragment.openedAProfile = false;
            releaseMediaPlayerObject(mediaPlayer);
            mListener = null;
        }
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        Log.e("Profile_onDestroyView","onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.e("Profile_onDestroy","onDestroy");
        if(AllTabs.mViewPager.getCurrentItem()==2) {
            ContactsFragment.openedAProfile = false;
            mAudioManager.abandonAudioFocus(this);
            if (mediaPlayer != null) {
                releaseMediaPlayerObject(mediaPlayer);
                mediaPlayer = null;
            }
        }
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
