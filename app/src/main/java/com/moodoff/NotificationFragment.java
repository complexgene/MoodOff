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
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
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
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment implements ViewPager.OnPageChangeListener,AudioManager.OnAudioFocusChangeListener{
    @Override
    public void onAudioFocusChange(int i) {
        if(i<=0){
            mp.pause();
        }
        else{
            mp.start();
        }
    }

    private AudioManager mAudioManager;
    StoreRetrieveDataInterface fileOpr = new StoreRetrieveDataImpl("Userdata.txt");
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

    public static int totalNumberOfNotifications = 0;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String serverURL = HttpGetPostInterface.serverURL,serverSongURL = HttpGetPostInterface.serverSongURL;

    // TODO: Rename and change types of parameters
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    int i= 0;
    View view,dialogView;
    public static MediaPlayer mp;
    TextView allNotificationsTextView;
    FrameLayout mainParentLayout;
    ArrayList<String> allNotifications;
    int idOfTheLastPlayButtonClicked=-1;
    boolean isPlaying = false;
    static HashMap<String,String> allReadContacts = ContactsManager.allReadContacts;
    LayoutInflater mainInflater;
    ViewGroup mainContainer;
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainInflater = inflater;
        mainContainer = container;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        setSizes();
        Log.e("Nota_Frag3","being called..");

        try {
            while(Start.notificationFetchNotComplete);
            allNotifications = AppData.allNotifications;
            oldCountOfNotifications = allNotifications.size();
            Log.e("NotificationFrag_SIZE",allNotifications.size()+"");
            designNotPanel(view);
            showNotPanel();

        }
        catch (Exception ei){
            Log.e("NotificationFrag_Er2",ei.toString());
        }

        return view;
    }

    Activity act;
    int currentPlayButtonId = -1;
    public static ImageButton currentPlayingButton;
    public static boolean changeDetected = false;
    private void showNotPanel(){
        act = (Activity)view.getContext();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(changeDetected) {
                                    designNotPanel(view);
                                }
                            }
                        });
                    }
                }).start();
                showNotPanel();
            }
        },2000);

    }

    public static SeekBar currentSeekBar;
    Handler seekHandler = new Handler();

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

    public static int oldCountOfNotifications = 0;

    int colorOfLoveFloatingActionButton = Color.rgb(0,255,0);

    public void designNotPanel(final View view){
        allReadContacts = ContactsManager.allReadContacts;
        Log.e("Not_Design","called..:"+currentPlayButtonId);
        changeDetected = false;
        mainParentLayout = (FrameLayout) view.findViewById(R.id.containsallN);
        mainParentLayout.removeAllViews();
        //mainParentLayout.setBackgroundResource(R.drawable.moodon_bg_notpanel);
        ScrollView mainParent = new ScrollView(view.getContext());
        mainParent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout ll = new LinearLayout(view.getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        allNotifications = AppData.allNotifications;
        int difference = allNotifications.size() - oldCountOfNotifications;
        Log.e("NotFrag","Updating notification view:"+difference+" "+allReadContacts.size());
        for (i = 0; i < allNotifications.size(); i++) {
            // Input Parsing
            String[] componentsInNotification = allNotifications.get(i).split(" ");
            final String fromUserNumber = componentsInNotification[0];
            String fromUserName = allReadContacts.get(fromUserNumber);
            Log.e("NotFrag",fromUserName+" is this");
            if(fromUserNumber.equals(UserDetails.getPhoneNumber())){
                    fromUserName = "You";
            }
            else{
                    if(fromUserName == null)
                        fromUserName = fromUserNumber;
            }

            final String date = componentsInNotification[2];
            final String time = componentsInNotification[3];
            final String type = componentsInNotification[4];
            Log.e("Not_fragTYPEEEMAN",type);
            final String toUserNumber = componentsInNotification[1];
            String toUserName = allReadContacts.get(toUserNumber);
            Log.e("NotFrag",toUserName+" is this2");

            if(toUserNumber.equals(UserDetails.getPhoneNumber())){
                    toUserName = "You";
            }
            else{
                    if(toUserName == null)
                        toUserName = toUserNumber;
            }

            final String songName = componentsInNotification[5];

            Log.e("NOTEFRAGGG",fromUserName+" "+toUserName+" yaaaa ");

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
                    if(fromUserNumber.equals(UserDetails.getPhoneNumber()))
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
                if (fromUserNumber.equals(UserDetails.getPhoneNumber())) {
                    loveButton.setVisibility(View.INVISIBLE);
                }
                loveButton.setImageResource(R.drawable.likenot_ns);
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

    private String getProcessedDate(String date){
        String[] months = {"Jan","Feb","Mar","Apr","may","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        String[] YMD = date.split("-");
        return YMD[2]+months[Integer.parseInt(YMD[1])-1]+"'"+YMD[0].substring(2);
    }

    private void voteLove(String urlToFire, ImageButton loveButton){
        ServerManager serverManager = new ServerManager();
        serverManager.voteLove(urlToFire,getActivity(),loveButton);
    }

    private void loadProfile(String contactNumber){
        /*FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment newFragment = Profile.newInstance(contactNumber,"b");
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.allContactDisplay, newFragment);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();*/
        final Dialog fbDialogue = new Dialog(view.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fbDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));

        dialogView = mainInflater.inflate(R.layout.fragment_profile, mainContainer, false);
    }

    public static ImageButton playOrStopButton;
    public static ProgressBar currentNotificationSpinner;

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
        } catch (IllegalArgumentException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mp); e.printStackTrace();
        } catch (IllegalStateException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mp); e.printStackTrace();
        } catch (IOException e) {toastError(e.getMessage()); releaseMediaPlayerObject(mp); e.printStackTrace();
        } catch (Exception e) {toastError(e.getMessage()); releaseMediaPlayerObject(mp); e.printStackTrace();
        }
    }

    /*Toast error message*/
    public static void toastError(String error) {
        //Toast.makeText(view.getContext(), "Oops! Somehing went wrong\n"+error.toString(), Toast.LENGTH_LONG).show();
        Log.e("Notification_issue",error);
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

    public int leftButtonHeight,leftButtonWidth,rightButtonHeight,rightButtonWidth,textViewWidth,textViewHeight;
    public void setSizes(){
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
        releaseMediaPlayerObject(mp);
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.abandonAudioFocus(this);
        Log.e("Notification","Notification on Destroy");
        if(mp!=null) {
            releaseMediaPlayerObject(mp);
            mp = null;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
