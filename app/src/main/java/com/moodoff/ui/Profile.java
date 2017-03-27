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
import android.view.WindowManager;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.R;
import com.moodoff.helper.AllAppData;
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

import static com.moodoff.helper.LoggerBaba.printMsg;

public class Profile extends Fragment implements AudioManager.OnAudioFocusChangeListener{

    private AudioManager mAudioManager;
    private User singleTonUser;

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
        singleTonUser = User.getInstance();
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
        txtView_likeCurrentMoodCount = (TextView)view.findViewById(R.id.txtView_likeCurrentMoodCount);
        txtView_loveCurrentMoodCount = (TextView)view.findViewById(R.id.txtView_loveCurrentMoodCount);
        txtView_sadCurrentMoodCount = (TextView)view.findViewById(R.id.txtView_sadCurrentMoodCount);
        imgBtn_LikeCurrentMood = (ImageButton)view.findViewById(R.id.imgBtn_likeCurrentMood);
        imgBtn_LoveCurrentMood = (ImageButton)view.findViewById(R.id.imgBtn_loveCurrentMood);
        imgBtn_SadCurrentMood = (ImageButton)view.findViewById(R.id.imgBtn_sadCurrentMood);

        txtViewUserLiveMoodStatus = (TextView)view.findViewById(R.id.userLiveMoodStatus);
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
    TextView txtViewUserLiveMoodStatus, lastMoodListened;
    TextView txtViewCurrentMood, txtView_likeCurrentMoodCount, txtView_loveCurrentMoodCount, txtView_sadCurrentMoodCount;
    ImageButton imgBtn_LikeCurrentMood, imgBtn_LoveCurrentMood, imgBtn_SadCurrentMood;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainContainer = container;
        mainInflater = inflater;

        init();

        profileOfUser = mParam1;
        String p= AllAppData.allReadContacts.get(profileOfUser);

        //Toast.makeText(getContext(),"Loading profile of: "+ (p==null?singleTonUser.getUserName():p),Toast.LENGTH_SHORT).show();

        // Check if its someone else's profile, then remove the edit button
        if(!profileOfUser.equals(singleTonUser.getUserMobileNumber())){
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
                Log.e("Profile_AudioSong","Ready to play the audio status song:" + myAudioStatusSong.toString());
                playAudioStatusSong(myAudioStatusSong);
            }
        });
        loveTextStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               loveTheTextStatusOfPersonWhoseProfileIsOpen();
            }
        });
        loveAudioStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loveTheAudioStatusOfThePersonWhoseProfileIsOpen();
            }
        });
        imgBtn_LikeCurrentMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeTheCurrentMoodOfThePersonWhoseProfileIsOpen();
            }
        });
        imgBtn_LoveCurrentMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loveTheCurrentMoodOfThePersonWhoseProfileIsOpen();
            }
        });
        imgBtn_SadCurrentMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sadTheCurrentMoodOfThePersonWhoseProfileIsOpen();
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
        String songURL = AllAppData.serverSongURL+myAudioStatusSongURL.replaceAll("@","/");
        releaseMediaPlayerObject(mediaPlayer);
        mediaPlayer = new MediaPlayer();
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

    private void loveTheTextStatusOfPersonWhoseProfileIsOpen() {
        ServerManager serverManager = new ServerManager();
        serverManager.loveTextStatus(profileOfUser, singleTonUser.getUserMobileNumber(), getActivity());
    }
    private void loveTheAudioStatusOfThePersonWhoseProfileIsOpen() {
        ServerManager serverManager = new ServerManager();
        serverManager.loveAudioStatus(profileOfUser, singleTonUser.getUserMobileNumber(), getActivity());
    }
    private void likeTheCurrentMoodOfThePersonWhoseProfileIsOpen() {
        ServerManager serverManager = new ServerManager();
        serverManager.likeCurrentMood(profileOfUser, singleTonUser.getUserMobileNumber(), getActivity());
    }
    private void loveTheCurrentMoodOfThePersonWhoseProfileIsOpen() {
        ServerManager serverManager = new ServerManager();
        serverManager.loveCurrentMood(profileOfUser, singleTonUser.getUserMobileNumber(), getActivity());
    }
    private void sadTheCurrentMoodOfThePersonWhoseProfileIsOpen() {
        ServerManager serverManager = new ServerManager();
        serverManager.sadCurrentMood(profileOfUser, singleTonUser.getUserMobileNumber(), getActivity());
    }

    private void editStatus(int textOrAudioStatus){

        final Dialog fbDialogue = new Dialog(view.getContext(), android.R.style.Theme_Black);
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
        fbDialogue.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        statusChangeTitle.setText("Edit your Text Status");
        dialogContainer.removeAllViews();
        // Show the old text status
        final EditText tv = new EditText(dialogView.getContext());
        tv.setPadding(30,10,10,30);
        tv.setBackgroundColor(Color.WHITE);
        tv.setText(myTextStatus.getText());
        dialogContainer.addView(tv);
        // If status is changed and Ok button in the dialog is clicked
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldStatus = myTextStatus.getText().toString();
                String newStatus = tv.getText().toString();
                if(oldStatus.equals(newStatus)){
                    Toast.makeText(getContext(),"Looks like nothing's changed..!!",Toast.LENGTH_SHORT).show();
                }
                else{
                    writeTheTextStatusChangeToServerAndFile(tv.getText().toString());
                    fbDialogue.dismiss();
                }
        }});
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbDialogue.dismiss();
            }
        });
    }
    private boolean writeTheTextStatusChangeToServerAndFile(String newStatus){
        try {
            ServerManager serverManager = new ServerManager();
            serverManager.writeTextStatusChange(singleTonUser.getUserMobileNumber(), newStatus, getActivity(), myTextStatus);

            fileOperations = new StoreRetrieveDataImpl(AllAppData.userDetailsFileName);
            fileOperations.beginWriteTransaction();
            fileOperations.updateValueFor(AllAppData.userTextStatus,newStatus);
            fileOperations.endWriteTransaction();

            singleTonUser.setUserTextStatus(newStatus);

            return true;
        } catch (Exception e) {
            Log.e("Profile_writeToFile_Err","Couldn't save the file:"+e.getMessage());
            return false;
        }
    }
    private boolean writeTheAudioStatusChangeToServerAndFile(String newAudioStatus){
        try {
            ServerManager serverManager = new ServerManager();
            serverManager.writeAudioStatusChange(singleTonUser.getUserMobileNumber(), newAudioStatus, getActivity(), myTextStatus);

            fileOperations = new StoreRetrieveDataImpl(AllAppData.userDetailsFileName);
            fileOperations.beginWriteTransaction();
            fileOperations.updateValueFor(AllAppData.userAudioStatus,newAudioStatus);
            fileOperations.endWriteTransaction();

            myAudioStatusSong=newAudioStatus;
            singleTonUser.setUserAudioStatusSong(newAudioStatus);

            return true;
        } catch (Exception e) {
            Log.e("Profile_writeToFile_Err","Couldn't save the file:"+e.getMessage());
            return false;
        }
    }
    ArrayList<String> allSongsInMap = new ArrayList<>();
    private void editUserAudioStatus(final Dialog fbDialogue){
        int playButtonId = 0;
        statusChangeTitle.setText("Change Audio Status");
        dialogContainer.removeAllViews();
        HashMap<String,ArrayList<String>> allSongs = AllAppData.allMoodPlayList;
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
                    String songURL = AllAppData.serverSongURL+eachMood+"/"+eachSong;
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
                if(writeTheAudioStatusChangeToServerAndFile(songStorePattern)){
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
    HashMap<String,String> profileDataParsed = new HashMap<>(), profileDataParsed2 = new HashMap<>();

    public static String currentMood = "Not Live";

    private void setUserProfileData(final String userPhoneNumber){
        final String serverURL = AllAppData.serverURL;
        new Thread(new Runnable() {
            HttpURLConnection urlConnection = null, urlConnection2 = null;
            InputStreamReader isr = null, isr2 = null;
            @Override
            public void run() {
                try {
                    URL userDetailsUrl = new URL(serverURL + "/allusers/" + userPhoneNumber +".json");
                    URL userLiveFeedDetailsUrl = new URL(serverURL + "/livefeed/" + userPhoneNumber + ".json");
                    Log.e("Profile_userDetailsURL", userDetailsUrl.toString());
                    Log.e("Profile_userDetailsURL", userLiveFeedDetailsUrl.toString());
                    urlConnection = (HttpURLConnection) userDetailsUrl.openConnection();
                    urlConnection2 = (HttpURLConnection) userLiveFeedDetailsUrl.openConnection();
                    InputStream is = urlConnection.getInputStream();
                    InputStream is2 = urlConnection2.getInputStream();
                    isr = new InputStreamReader(is);
                    isr2 = new InputStreamReader(is2);
                    int data = isr.read();
                    final StringBuilder response = new StringBuilder("");
                    while (data != -1) {
                        response.append((char) data);
                        data = isr.read();
                    }
                    int data2 = isr2.read();
                    final StringBuilder response2 = new StringBuilder("");
                    while (data2 != -1) {
                        response2.append((char) data2);
                        data2 = isr2.read();
                    }
                    printMsg("Profile", "Got from serverBasic:" + response.toString());
                    printMsg("Profile", "Got from serverLive:" + response2.toString());
                    profileDataParsed = ParseNotificationData.parseAndGetBasicProfileData(response.toString());
                    profileDataParsed2 = ParseNotificationData.parseAndGetLiveProfileData(response2.toString());


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
    }

    private void showProfileData(){
        populateDetails();

        // Start async listener
        liveFeedForStatusRelatedFeedsAndMoodRelatedFeeds();

    }

    // Live data monitoring, will only be called for realtime data changes
    private void populateLiveDetails(){
        final String serverURL = AllAppData.serverURL;
        new Thread(new Runnable() {
            HttpURLConnection  urlConnection2 = null;
            InputStreamReader isr2 = null;
            @Override
            public void run() {
                try {
                    URL userLiveFeedDetailsUrl = new URL(serverURL + "/livefeed/" + profileOfUser + ".json");
                    Log.e("Profile_userDetailsURL", userLiveFeedDetailsUrl.toString());
                    urlConnection2 = (HttpURLConnection) userLiveFeedDetailsUrl.openConnection();
                    InputStream is2 = urlConnection2.getInputStream();
                    isr2 = new InputStreamReader(is2);
                    int data2 = isr2.read();
                    final StringBuilder response2 = new StringBuilder("");
                    while (data2 != -1) {
                        response2.append((char) data2);
                        data2 = isr2.read();
                    }
                    printMsg("Profile", "Got from serverLive:" + response2.toString());
                    profileDataParsed2 = ParseNotificationData.parseAndGetLiveProfileData(response2.toString());

                    // THINK OF LIVE MOOD NOW

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateDetails();
                        }
                    });

                } catch (Exception ee) {
                    Log.e("Profile_Err", ee.getMessage());
                }
            }
        }).start();
    }
    private void populateDetails(){
        myName.setText(profileDataParsed.get(AllAppData.userName));
        myPhNo.setText(profileDataParsed.get(AllAppData.userMobileNumber));
        myDob.setText(profileDataParsed.get(AllAppData.userDateOfBirth));
        myDob.setVisibility(View.GONE);
        myTextStatus.setText(profileDataParsed.get(AllAppData.userTextStatus));
        myAudioStatusSong = profileDataParsed.get(AllAppData.userAudioStatus);
        textStatusLoveCount.setText(profileDataParsed2.get(AllAppData.userTextStatusLoveCount) + AllAppData.likedTextStatusLine);
        audioStatusLoveCount.setText(profileDataParsed2.get(AllAppData.userAudioStatusLoveCount) + AllAppData.likedAudioStatusLine);
        txtViewCurrentMood.setText(profileDataParsed2.get(AllAppData.userLiveMood));
        txtView_likeCurrentMoodCount.setText(profileDataParsed2.get(AllAppData.userMoodLikeCount));
        txtView_loveCurrentMoodCount.setText(profileDataParsed2.get(AllAppData.userMoodLoveCount));
        txtView_sadCurrentMoodCount.setText(profileDataParsed2.get(AllAppData.userMoodSadCount));
    }

    //Extra items required for the Activity
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
    private int getPicFor(String genderType){
        return (genderType.equals("0"))?R.drawable.man:R.drawable.woman;
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






    // Async Listeners for Live Feed In Profile
    private void liveFeedForStatusRelatedFeedsAndMoodRelatedFeeds(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference liveFeedNode = firebaseDatabase.getReference().child("livefeed");
        DatabaseReference dbRefForLiveFeedTextStatus = liveFeedNode.child(profileOfUser).child(AllAppData.userTextStatusLoveCount);
        dbRefForLiveFeedTextStatus.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                printMsg("Profile", "Some value changed in text status like node..");
                populateLiveDetails();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {  }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                printMsg("Profile", "Some value changed in text status like node..");
                populateLiveDetails();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {  }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
        DatabaseReference dbRefForLiveFeedAudioStatus = liveFeedNode.child(profileOfUser).child(AllAppData.userAudioStatusLoveCount);
        dbRefForLiveFeedAudioStatus.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                printMsg("Profile", "Some value changed in audio status like node..");
                populateLiveDetails();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                printMsg("Profile", "Some value changed in text status like node..");
                populateLiveDetails();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
        DatabaseReference dbRefForLiveFeedOfMood = liveFeedNode.child(profileOfUser).child(AllAppData.moodLiveFeedNode);
        dbRefForLiveFeedOfMood.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        DatabaseReference dbRefForLiveMoodOfUser = liveFeedNode.child(profileOfUser).child(AllAppData.moodLiveFeedNode).child(AllAppData.userLiveMood);
        dbRefForLiveMoodOfUser.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        DatabaseReference dbRefForMoodLikeCount = liveFeedNode.child(profileOfUser).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodLikeCount);
        dbRefForMoodLikeCount.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        DatabaseReference dbRefForMoodLoveCount = liveFeedNode.child(profileOfUser).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodLoveCount);
        dbRefForMoodLoveCount.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        DatabaseReference dbRefForMoodSadCount = liveFeedNode.child(profileOfUser).child(AllAppData.moodLiveFeedNode).child(AllAppData.userMoodSadCount);
        dbRefForMoodSadCount.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                printMsg("Profile", "Some value changed in livemood node..");
                populateLiveDetails();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

}
