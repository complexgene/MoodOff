package com.moodoff.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moodoff.R;
import com.moodoff.helper.AllAppData;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.LoggerBaba;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.helper.ValidateMediaPlayer;
import com.moodoff.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.moodoff.helper.LoggerBaba.printMsg;

public class NotificationFragment extends Fragment implements ViewPager.OnPageChangeListener,AudioManager.OnAudioFocusChangeListener{

    private int i = 0, idOfTheLastPlayButtonClicked = -1, currentPlayButtonId = -1, leftButtonHeight,leftButtonWidth,rightButtonHeight,rightButtonWidth,textViewWidth,textViewHeight;
    private View view;
    private FrameLayout mainParentLayout;
    private ArrayList<String> allNotifications;
    private LayoutInflater mainInflater;
    private ViewGroup mainContainer;
    private AudioManager mAudioManager;
    private StoreRetrieveDataInterface fileOpr = new StoreRetrieveDataImpl(AllAppData.userDetailsFileName);
    private Context ctx;
    private ServerManager serverManager;
    public static SeekBar currentSeekBar;
    Handler seekHandler = new Handler();
    public static int oldCountOfNotifications = 0;
    User userData = User.getInstance();
    public static ImageButton playOrStopButton;
    public static ProgressBar currentNotificationSpinner;
    public static int totalNumberOfNotifications = 0;
    private String serverSongURL = AllAppData.serverSongURL;
    private DBHelper dbOperations;
    private User singleTonUser;
    public static MediaPlayer mp;
    public static ImageButton currentPlayingButton;
    public static HashMap<String,String> allReadContacts;
    public static boolean changeDetected = false;

    public void init(){
        allReadContacts = AllAppData.allReadContacts;
        dbOperations = new DBHelper(ctx);
        singleTonUser = User.getInstance();
        serverManager = new ServerManager(ctx);
    }

    @Override
    public void onAudioFocusChange(int i) {
        if(mp!=null && mp.isPlaying()) {
            if (i <= 0 && AllTabs.mViewPager.getCurrentItem()==1) {
                mp.pause();
            } else {
                mp.start();
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        Log.e("SMNotFrag","Page selected..");
        ViewPager viewPager = AllTabs.mViewPager;
        AllTabs.tabNames.clear();
        AllTabs.tabNames.add("Moods");AllTabs.tabNames.add("Activity");AllTabs.tabNames.add("Profiles");
        viewPager.getAdapter().notifyDataSetChanged();
        fileOpr.beginWriteTransaction();
        fileOpr.updateValueFor("numberOfOldNotifications","0");
        fileOpr.endWriteTransaction();
    }
    @Override
    public void onPageScrollStateChanged(int state) {

    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    public NotificationFragment() {
        // Required empty public constructor
    }
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainInflater = inflater;
        mainContainer = container;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        ctx = view.getContext();

        init();

        fetchNotifications();

        monitorLiveNotifications();

        return view;
    }

    private void fetchNotifications() {
        try {
            Log.e("NotificationFragment","fetchNotifications(): Start loading notifications from Internal DB");
            ArrayList<String> allNotificationsFromDB = dbOperations.readNotificationsFromInternalDB();
            AllAppData.allNotifications = allNotificationsFromDB;
            AllAppData.totalNoOfNot = allNotificationsFromDB.size();
            NotificationFragment.totalNumberOfNotifications = allNotificationsFromDB.size();
            Log.e("NotificationFragment","fetchNotifications(): Fetched " + AllAppData.totalNoOfNot + " notifications from internal DB..");
            designNotPanel(view);
        } catch (Exception ee) {
            Log.e("NotificationFragmentErr", "fetchNotifications():" + ee.getMessage());
            ee.printStackTrace();
        }
    }

    public void designNotPanel(final View view){

        allReadContacts = AllAppData.allReadContacts;
        allNotifications = AllAppData.allNotifications;

        Log.e("Not_Design","called..:"+currentPlayButtonId);
        changeDetected = false;
        mainParentLayout = (FrameLayout) view.findViewById(R.id.containsallN);
        mainParentLayout.removeAllViews();
        //mainParentLayout.setBackgroundResource(R.drawable.moodon_bg_notpanel);
        ScrollView mainParent = new ScrollView(view.getContext());
        mainParent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout ll = new LinearLayout(view.getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        int difference = allNotifications.size() - oldCountOfNotifications;
        Log.e("NotFrag","Updating notification view:"+difference+" "+allReadContacts.size());
        for (i = 0; i < allNotifications.size(); i++) {
            // Input Parsing
            String[] componentsInNotification = allNotifications.get(i).split(" ");
            final String fromUserNumber = componentsInNotification[0];
            String fromUserName = allReadContacts.get(fromUserNumber);
            if(fromUserNumber.equals(userData.getUserMobileNumber())){
                    fromUserName = "You";
            }
            else{
                    if(fromUserName == null)
                        fromUserName = fromUserNumber;
            }
            final String date = componentsInNotification[2];
            final String time = componentsInNotification[3];
            final String type = componentsInNotification[4];
            final String toUserNumber = componentsInNotification[1];
            String toUserName = allReadContacts.get(toUserNumber);

            if(toUserNumber.equals(userData.getUserMobileNumber())){
                    toUserName = "You";
            }
            else{
                    if(toUserName == null)
                        toUserName = toUserNumber;
            }

            final String songName = componentsInNotification[5];

            // Each notification layout
            boolean isCurrentUser = fromUserName.trim().equals("You");

            LinearLayout parent = new LinearLayout(view.getContext());
            //parent.setPadding(10,10,10,10);
            LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutDetails.weight=1;
            //layoutDetails.topMargin=15;

            if(isCurrentUser) {
                parent.setBackgroundResource(R.drawable.eachnotificationfile);
                layoutDetails.leftMargin = 120;
                layoutDetails.rightMargin = 2;
            }
            else {
                parent.setBackgroundResource(R.drawable.eachnotificationfileothers);
                layoutDetails.leftMargin = 2;
                layoutDetails.rightMargin = 120;
            }

            parent.setLayoutParams(layoutDetails);

            // Love Button
            final ImageButton loveButton = new ImageButton(view.getContext());
            loveButton.setBackgroundResource(0);
            final String fromNumberToSend = fromUserNumber;
            loveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(fromUserNumber.equals(userData.getUserMobileNumber()))
                        Messenger.print(getContext(),"You can't like your own dedicated songs!!");
                    else {
                        String urlToFire = fromUserNumber + "/" + toUserNumber + "/" + date + "_" + time + "/5";
                        //loadProfile(toUserNumber);
                        voteLove(urlToFire, loveButton);
                    }
                }
            });
            if(type.equals("5")){
                loveButton.setImageResource(R.drawable.like_s);
                loveButton.setEnabled(false);
            }
            else {
                if (fromUserNumber.equals(userData.getUserMobileNumber())) {
                    loveButton.setVisibility(View.INVISIBLE);
                }
                loveButton.setImageResource(R.drawable.like_ns);
            }

            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.gravity=Gravity.CENTER_VERTICAL;
            loveButton.setLayoutParams(layoutDetails);

            // Text Notification and SeekBar
            LinearLayout nameAndTimeAndSeekBar = new LinearLayout(view.getContext());
            nameAndTimeAndSeekBar.setGravity(Gravity.CENTER);
            nameAndTimeAndSeekBar.setOrientation(LinearLayout.VERTICAL);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.weight=1;
            nameAndTimeAndSeekBar.setLayoutParams(layoutDetails);

            LinearLayout nameAndTime = new LinearLayout(view.getContext());
            nameAndTime.setOrientation(LinearLayout.VERTICAL);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            nameAndTime.setLayoutParams(layoutDetails);

            TextView name = new TextView(view.getContext());
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.leftMargin = 20;
            //layoutDetails.weight = 1;
            name.setLayoutParams(layoutDetails);
            name.setTextColor(Color.BLACK);
            name.setGravity(Gravity.CENTER_HORIZONTAL);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            //Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/teen.ttf");
            //name.setTypeface(Typeface.DEFAULT_BOLD..BOLD);
            name.setTextSize(14);

            String nameToDisplay = "",timeToDisplay="";

            if(fromUserName.equals("You"))
            {
                nameToDisplay = toUserName;
                timeToDisplay = time.substring(0,time.lastIndexOf(":"));
            }
                //textToDisplay = "[ "+date+" at "+time.substring(0,time.lastIndexOf(":"))+"]\n"+toUserName;
            else {
                nameToDisplay = fromUserName;
                timeToDisplay = time.substring(0,time.lastIndexOf(":"));
                //textToDisplay = "[ "+date+" at "+time.substring(0,time.lastIndexOf(":"))+"]\n"+fromUserName;
            }
            name.setText(nameToDisplay);

            LinearLayout timeLayout = new LinearLayout(view.getContext());
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.rightMargin = 15;
            timeLayout.setLayoutParams(layoutDetails);
            timeLayout.setGravity(Gravity.RIGHT);
            TextView timeDone = new TextView(view.getContext());
            timeDone.setTextColor(ColorStateList.valueOf(Color.rgb(103,102,103)));
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            timeDone.setLayoutParams(layoutDetails);
            int timeOfDay = Integer.parseInt(timeToDisplay.split(":")[0]);
            if(timeOfDay>12){
                timeToDisplay = getProcessedDate(date)+" 0"+(timeOfDay-12)+":"+timeToDisplay.split(":")[1]+" PM";
            }
            else{
                timeToDisplay = getProcessedDate(date)+" "+timeToDisplay+" AM";
            }
            timeDone.setTextSize(11);
            timeDone.setText(timeToDisplay);
            timeLayout.addView(timeDone);

            nameAndTime.addView(name);
            //nameAndTime.addView(timeLayout);

            final SeekBar seekBar = new SeekBar(view.getContext());
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.gravity = Gravity.CENTER;
            layoutDetails.weight = 1;
            seekBar.setEnabled(false);
            seekBar.setId((i+1)+1000000);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if(mp!=null)
                        mp.seekTo(seekBar.getProgress());
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }
            });
            seekBar.setLayoutParams(layoutDetails);

            nameAndTimeAndSeekBar.addView(nameAndTime);
            nameAndTime.addView(seekBar);
            //nameAndTimeAndSeekBar.addView(nameAndTime);
            nameAndTime.addView(timeLayout);
            // Text notification and seekbar

            //Spinner
            final ProgressBar notificationProgressBar = new ProgressBar(view.getContext());
            notificationProgressBar.setId((i+1)+2000000);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutDetails.gravity = Gravity.CENTER;
            layoutDetails.topMargin = 10;
            layoutDetails.bottomMargin = 10;
            layoutDetails.rightMargin = 10;
            layoutDetails.leftMargin = 10;
            notificationProgressBar.setLayoutParams(layoutDetails);
            notificationProgressBar.setVisibility(View.GONE);

            // Play button
            final String songFileName = allNotifications.get(i).substring(allNotifications.get(i).lastIndexOf(" ")).trim();
            final ImageButton playImageButton = new ImageButton(view.getContext());
            playImageButton.setBackgroundResource(0);
            playImageButton.setId(i);
            playImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPlayButtonId = v.getId();
                    currentSeekBar = (SeekBar) view.findViewById(((currentPlayButtonId)+1)+1000000);
                    currentNotificationSpinner = (ProgressBar) view.findViewById(((currentPlayButtonId)+1)+2000000);
                    currentNotificationSpinner.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                    playSong(playImageButton,v,currentSeekBar,songFileName,currentNotificationSpinner);
                }
            });
            playImageButton.setImageResource(R.drawable.playdedicate);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.gravity = Gravity.CENTER_VERTICAL;
            layoutDetails.topMargin = 10;
            layoutDetails.bottomMargin = 10;
            layoutDetails.rightMargin = 10;
            playImageButton.setLayoutParams(layoutDetails);

            parent.addView(playImageButton);
            parent.addView(notificationProgressBar);
            parent.addView(nameAndTimeAndSeekBar);
            parent.addView(loveButton);


            ll.addView(parent);
        }
        mainParent.addView(ll);
        mainParentLayout.addView(mainParent);
        if(mp!=null && mp.isPlaying()){
            Log.e("Nota_Frag1","HERE "+currentSeekBar.getId()+" "+currentPlayButtonId+" "+difference);
            currentSeekBar = (SeekBar) view.findViewById(((currentPlayButtonId+difference)+1)+1000000);
            currentSeekBar.setMax(mp.getDuration());
            currentSeekBar.setEnabled(true);
            seekUpdation();
            currentPlayingButton = (ImageButton)view.findViewById(currentPlayButtonId+difference);
            currentPlayingButton.setImageResource(R.drawable.stop);
            //currentPlayingButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,0,0)));
            idOfTheLastPlayButtonClicked = currentPlayButtonId+difference;
        }
    }

    private void voteLove(String urlToFire, ImageButton loveButton){
        ServerManager serverManager = new ServerManager();
        serverManager.voteLove(urlToFire,getActivity(),loveButton);
    }

    public void playSong(ImageButton playButton, View currentClickedButton, SeekBar currentSeekBar, String songFileName, ProgressBar spinner){
        Log.e("Nota_Frag2", currentClickedButton.getId() + "");
        playOrStopButton = playButton;
        currentNotificationSpinner = spinner;
        Log.e("Notification_Visibility","I started the spinner");
        playOrStopButton.setVisibility(View.GONE);
        currentNotificationSpinner.setVisibility(View.VISIBLE);
        if(currentClickedButton.getId() != idOfTheLastPlayButtonClicked) {
            if (idOfTheLastPlayButtonClicked != -1) {
                ImageButton lastPlayedButton = (ImageButton) view.findViewById(idOfTheLastPlayButtonClicked);
                lastPlayedButton.setImageResource(R.drawable.playdedicate);
                SeekBar lastSeekBar = (SeekBar) view.findViewById((idOfTheLastPlayButtonClicked+1)+1000000);
                lastSeekBar.setProgress(0);
                lastSeekBar.setEnabled(false);
            }
            //if(mp!=null)mp.reset();
            playOrStopButton.setImageResource(R.drawable.stopdedicate);
            idOfTheLastPlayButtonClicked = currentClickedButton.getId();
            currentPlayButtonId = idOfTheLastPlayButtonClicked;
            oldCountOfNotifications = allNotifications.size();
            ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
            validateMediaPlayer.initialiseAndValidateMediaPlayer("notification","play");
            play(songFileName,currentPlayButtonId);
        }
        else {
            if(mp.isPlaying()){
                    currentSeekBar.setProgress(0);
                    currentSeekBar.setEnabled(false);
                    mp.reset();
                    playOrStopButton.setImageResource(R.drawable.playdedicate);
                    ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
                    validateMediaPlayer.initialiseAndValidateMediaPlayer("notification","stop");
                    playOrStopButton.setVisibility(View.VISIBLE);
                    currentNotificationSpinner.setVisibility(View.GONE);
                }
            else {
                currentSeekBar.setMax(mp.getDuration());
                currentSeekBar.setEnabled(true);
                seekUpdation();
                ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
                validateMediaPlayer.initialiseAndValidateMediaPlayer("notification","play");
                play(songFileName,currentPlayButtonId);
                playOrStopButton.setImageResource(R.drawable.stopdedicate);
            }
        }
    }

    public void play(String songFileName,int currentPlayButtonId){
        final ImageButton currentPlayButton = (ImageButton) view.findViewById(currentPlayButtonId);
        releaseMediaPlayerObject(mp);
        mp = new MediaPlayer();
        String[] moodTypeAndSong = songFileName.split("@");
        String url = serverSongURL + moodTypeAndSong[0]+"/"+moodTypeAndSong[1];
        Log.e("Not_Frag_SongURL", url.toString()+" sB id:"+currentSeekBar.getId());
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(url);
            mp.prepareAsync();
            mp.setLooping(false);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if(mediaPlayer!=null) {
                        currentSeekBar.setMax(mediaPlayer.getDuration());
                        seekUpdation();
                        currentPlayButton.setVisibility(View.VISIBLE);
                        currentNotificationSpinner.setVisibility(View.GONE);
                        currentSeekBar.setEnabled(true);
                    }
                    mediaPlayer.start();
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    currentSeekBar.setMax(0);
                    currentPlayButton.setImageResource(R.drawable.playdedicate);
                }
            });
        } catch (IllegalArgumentException e) {printMsg("NotificationFragment_Err",e.getMessage()); releaseMediaPlayerObject(mp); e.printStackTrace();
        } catch (IllegalStateException e) {printMsg("NotificationFragment_Err",e.getMessage()); releaseMediaPlayerObject(mp); e.printStackTrace();
        } catch (IOException e) {printMsg("NotificationFragment_Err",e.getMessage()); releaseMediaPlayerObject(mp); e.printStackTrace();
        } catch (Exception e) {printMsg("NotificationFragment_Err",e.getMessage()); releaseMediaPlayerObject(mp); e.printStackTrace();
        }
    }

    public void monitorLiveNotifications() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference allNotificationsNode = firebaseDatabase.getReference().child("allNotifications");
        final DatabaseReference
                    dbRefForallNotificationsNode = allNotificationsNode.child(singleTonUser.getUserMobileNumber());

        dbRefForallNotificationsNode.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    printMsg("NotificationFragment", "New notifications added, so lets invoke the READ from Cloud..");
                    serverManager.readNotificationsFromServerAndWriteToInternalDB();
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

        final DatabaseReference
                dbRefForrebuildPanelStateNode = allNotificationsNode.child("rebuildPanelState").child(singleTonUser.getUserMobileNumber());
        dbRefForrebuildPanelStateNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                printMsg("NotificationFragment", "Gonna invoke the designNotPanel() if rebuildPanelState is 1");
                if(dataSnapshot.getValue(Integer.class) == 1) {
                    printMsg("NotificationFragment", "rebuildPanelState is 1");
                    dbRefForrebuildPanelStateNode.setValue(0);
                    designNotPanel(view);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /*release and return the nullified mediaplayer object*/
    public static void releaseMediaPlayerObject(MediaPlayer mp) {
        try {
            if (mp != null) {
                if(mp.isPlaying()){mp.stop();}
                mp.release();
                mp = null;
            }
        } catch(Exception e){e.fillInStackTrace();e.printStackTrace();}
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    public void seekUpdation() {
        try {
            if (mp!=null || currentSeekBar!=null) {
                currentSeekBar.setProgress(mp.getCurrentPosition());
                if(currentSeekBar.getMax()!=0) {
                    seekHandler.postDelayed(run, 10);
                }
            }
        } catch(Exception e) {
        }
    }

    private String getProcessedDate(String date){
        String[] months = {"Jan","Feb","Mar","Apr","may","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        String[] YMD = date.split("-");
        return YMD[2]+months[Integer.parseInt(YMD[1])-1]+"'"+YMD[0].substring(2);
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
        Log.e("Notification_onDetach", "Notification on Detach");
        if(AllTabs.mViewPager.getCurrentItem() == 1) {
            releaseMediaPlayerObject(mp);
            mListener = null;
        }
        super.onDetach();
    }
    @Override
    public void onDestroy() {
        Log.e("Notification_onDestroy", "Notification on Destroy");
        if(AllTabs.mViewPager.getCurrentItem() == 1) {
            mAudioManager.abandonAudioFocus(this);
            if (mp != null) {
                releaseMediaPlayerObject(mp);
                mp = null;
            }
        }
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //-------------------------Some Extra Code, Might be required later----------------------------------------
    /*public void setSizes(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.e("NotFrag",height+"");
        leftButtonWidth = (int)Math.floor(0.15*width);
        textViewWidth = (int)Math.floor(0.70*width);
        rightButtonWidth = (int)Math.floor(0.2*width);
        leftButtonHeight = rightButtonHeight = textViewHeight = (int)Math.ceil(.0625*height);

    }*/
}
