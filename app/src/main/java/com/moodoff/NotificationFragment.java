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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.AppData;
import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.ServerManager;
import com.moodoff.model.UserDetails;

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
public class NotificationFragment extends Fragment implements ViewPager.OnPageChangeListener{
    @Override
    public void onPageSelected(int position) {
        Log.e("SMNotFrag","Page selected..");
        ViewPager viewPager = AllTabs.mViewPager;
        AllTabs.tabNames.clear();
        AllTabs.tabNames.add("Moods");AllTabs.tabNames.add("Activity");AllTabs.tabNames.add("Profiles");
        viewPager.getAdapter().notifyDataSetChanged();
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    int i= 0;
    View view,dialogView;
    MediaPlayer mp;
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
            Log.e("NotificationFrag_Er2",ei.getMessage());
        }

        return view;
    }

    Activity act;
    int currentPlayButtonId = -1;
    FloatingActionButton currentPlayingButton;
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

    SeekBar currentSeekBar;
    Handler seekHandler = new Handler();

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    public void seekUpdation() {
        if (mp!=null) {
            currentSeekBar.setProgress(mp.getCurrentPosition());
            seekHandler.postDelayed(run, 10);
        }
    }

    public static int oldCountOfNotifications = 0;

    public void designNotPanel(final View view){
        Log.e("Not_Design","called..:"+currentPlayButtonId);
        changeDetected = false;
        mainParentLayout = (FrameLayout) view.findViewById(R.id.containsallN);
        mainParentLayout.removeAllViews();
        mainParentLayout.setBackgroundResource(R.drawable.moodon_bg_notpanel);
        ScrollView mainParent = new ScrollView(view.getContext());
        mainParent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout ll = new LinearLayout(view.getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        allNotifications = AppData.allNotifications;
        int difference = allNotifications.size() - oldCountOfNotifications;
        Log.e("NotFrag","Updating notification view:"+difference);
        for (i = 0; i < allNotifications.size(); i++) {
            // Input Parsing
            String[] componentsInNotification = allNotifications.get(i).split(" ");
            String fromUserNumber = componentsInNotification[0];
            String fromUserName = allReadContacts.get(fromUserNumber);
            if(fromUserName == null){
                if(fromUserNumber.equals(UserDetails.getPhoneNumber())){
                    fromUserName = "You";
                }
                else{
                    fromUserName = fromUserNumber;
                }
            }
            String date = componentsInNotification[2];
            final String time = componentsInNotification[3];
            final String toUserNumber = componentsInNotification[1];
            String toUserName = allReadContacts.get(toUserNumber);
            if(toUserName == null){
                if(toUserNumber.equals(UserDetails.getPhoneNumber())){
                    toUserName = "You";
                }
                else{
                    toUserName = toUserNumber;
                }
            }
            final String songName = componentsInNotification[5];

            // Each notification layout
            LinearLayout parent = new LinearLayout(view.getContext());
            parent.setBackgroundResource(R.color.deep_orange);
            parent.setGravity(Gravity.CENTER_VERTICAL);
            parent.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutDetails.topMargin=25;
            layoutDetails.leftMargin = 10;
            layoutDetails.rightMargin = 10;
            parent.setLayoutParams(layoutDetails);
            parent.setOrientation(LinearLayout.HORIZONTAL);

            // Love Button
            final FloatingActionButton loveButton = new FloatingActionButton(view.getContext());
            final String fromNumberToSend = fromUserNumber;
            loveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(view.getContext(),toUserNumber,Toast.LENGTH_SHORT).show();
                    //loadProfile(toUserNumber);
                    voteLove(fromNumberToSend,toUserNumber,time);
                }
            });
            loveButton.setImageResource(R.drawable.love_ns);
            loveButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(21,214,191)));
            loveButton.setSize(FloatingActionButton.SIZE_MINI);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.weight=1;
            layoutDetails.gravity=Gravity.CENTER_VERTICAL;
            layoutDetails.leftMargin=20;
            loveButton.setLayoutParams(layoutDetails);
            parent.addView(loveButton);

            // Text Notification and SeekBar
            LinearLayout linearLayout = new LinearLayout(view.getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView allN = new TextView(view.getContext());
            allN.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
            allN.setGravity(Gravity.TOP);
            boolean isCurrentUser = fromUserName.trim().equals("You");
            if(isCurrentUser) {
                parent.setBackgroundResource(R.drawable.eachnotificationfile);
            }
            else {
                parent.setBackgroundResource(R.drawable.registrationdatabox);
            }
            allN.setPadding(22,0,10,0);
            allN.setTypeface(Typeface.DEFAULT_BOLD);
            //allN.setBackgroundResource(R.color.light_yellow);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.weight=1;
            layoutDetails.width=textViewWidth;
            layoutDetails.height=textViewHeight;
            layoutDetails.topMargin = 15;
            layoutDetails.rightMargin = 15;
            layoutDetails.leftMargin = 15;
            allN.setLayoutParams(layoutDetails);
            allN.setTextColor(Color.BLACK);
            //allN.setTypeface(Typeface.createFromAsset(view.getContext().getApplicationContext().getAssets(),"fonts/BLKCHCRY.ttf"));
            allN.setTypeface(Typeface.SERIF);
            String textToDisplay = "[ "+date+" at "+time.substring(0,time.lastIndexOf(":"))+"]\n"+fromUserName+" > "+toUserName;
            allN.setText(textToDisplay);
            linearLayout.addView(allN);
            final SeekBar seekBar = new SeekBar(view.getContext());
            seekBar.setEnabled(false);
            seekBar.setId((i+1)*1000000);
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
            linearLayout.addView(seekBar);
            parent.addView(linearLayout);

            // Play button
            final String songFileName = allNotifications.get(i).substring(allNotifications.get(i).lastIndexOf(" ")).trim();
            //Log.e("NotFrag_songFile",songFileName);
            final FloatingActionButton playFloatingButton = new FloatingActionButton(view.getContext());
            playFloatingButton.setId(i);
            playFloatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPlayButtonId = v.getId();
                    currentSeekBar = (SeekBar) view.findViewById(((currentPlayButtonId)+1)*1000000);
                    currentSeekBar.setEnabled(true);
                    //currentSeekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_IN));
                    playSong(playFloatingButton,v,currentSeekBar,songFileName);

                }
            });
            playFloatingButton.setImageResource(R.drawable.play);
            playFloatingButton.setSize(FloatingActionButton.SIZE_MINI);
            playFloatingButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(229,152,245)));
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.weight=1;
            layoutDetails.gravity = Gravity.CENTER_VERTICAL;
            layoutDetails.rightMargin=20;
            playFloatingButton.setLayoutParams(layoutDetails);
            parent.addView(playFloatingButton);

            ll.addView(parent);
        }
        mainParent.addView(ll);
        mainParentLayout.addView(mainParent);
        if(mp!=null && mp.isPlaying()){
            Log.e("Nota_Frag1","HERE "+currentSeekBar.getId()+" "+currentPlayButtonId+" "+difference);
            currentSeekBar = (SeekBar) view.findViewById(((currentPlayButtonId+difference)+1)*1000000);
            currentSeekBar.setMax(mp.getDuration());
            currentSeekBar.setEnabled(true);
            seekUpdation();
            currentPlayingButton = (FloatingActionButton)view.findViewById(currentPlayButtonId+difference);
            currentPlayingButton.setImageResource(R.drawable.stop);
            currentPlayingButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,0,0)));
            idOfTheLastPlayButtonClicked = currentPlayButtonId+difference;
        }
    }

    private void voteLove(String from,String to, String ts){
        ServerManager serverManager = new ServerManager();
        //serverManager.voteLove(from,to,ts);
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
    public void playSong(FloatingActionButton playButton, View currentClickedButton, SeekBar currentSeekBar, String songFileName){
        Log.e("Nota_Frag2", currentClickedButton.getId() + "");
        if(currentClickedButton.getId() != idOfTheLastPlayButtonClicked) {
            if (idOfTheLastPlayButtonClicked != -1) {
                FloatingActionButton lastPlayedButton = (FloatingActionButton) view.findViewById(idOfTheLastPlayButtonClicked);
                lastPlayedButton.setImageResource(R.drawable.play);
                lastPlayedButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(229,152,245)));
                SeekBar lastSeekBar = (SeekBar) view.findViewById((idOfTheLastPlayButtonClicked+1)*1000000);
                lastSeekBar.setProgress(0);
                lastSeekBar.setEnabled(false);
            }
            if(mp!=null)mp.reset();
            playButton.setImageResource(R.drawable.stop);
            playButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,0,0)));
            idOfTheLastPlayButtonClicked = currentClickedButton.getId();
            currentPlayButtonId = idOfTheLastPlayButtonClicked;
            oldCountOfNotifications = allNotifications.size();
            play(songFileName,currentPlayButtonId);
        }
        else {
            if(mp.isPlaying()){
                currentSeekBar.setProgress(0);
                currentSeekBar.setEnabled(false);
                mp.reset();
                playButton.setImageResource(R.drawable.play);
                playButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(229,152,245)));
            } else {
                currentSeekBar.setMax(mp.getDuration());
                currentSeekBar.setEnabled(true);
                seekUpdation();
                play(songFileName,currentPlayButtonId);
                //mp.start();
                playButton.setImageResource(R.drawable.stop);
                playButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255,0,0)));
            }
        }
    }

    public void play(String songFileName,int currentPlayButtonId){
        final FloatingActionButton currentPlayButton = (FloatingActionButton) view.findViewById(currentPlayButtonId);
        mp = SingleTonMediaPlayer.getSingleTonMediaPlayerInstance();
        String url = serverSongURL + "romantic/" + songFileName;
        Log.e("Not_Frag_SongURL", url.toString()+" sB id:"+currentSeekBar.getId());
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mp.setDataSource(url);
            mp.prepare();
            currentSeekBar.setMax(mp.getDuration());
            seekUpdation();
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    currentSeekBar.setMax(0);
                    currentPlayButton.setImageResource(R.drawable.play);
                }
            });
        } catch (Exception ee) {
            Log.e("Not_Frag_Err", "abc" + ee.getMessage());
        }
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
        if(mp!=null) {
            mp.reset();
            mp.release();
            mp = null;
        }
        super.onDetach();
        mListener = null;
    }
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
