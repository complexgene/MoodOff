package com.moodoff;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.AllNotifications;
import com.moodoff.helper.HttpGetPostImpl;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.model.UserDetails;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
public class NotificationFragment extends Fragment {
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
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
    View view;
    MediaPlayer mp;
    TextView allNotificationsTextView;
    FrameLayout mainParentLayout;
    ArrayList<String> allNotifications = AllNotifications.allNotifications;
    int idOfTheLastPlayButtonClicked=-1;
    boolean isPlaying = false;
    HashMap<String,String> allC = new HashMap<>();
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);

        setSizes();

        try {
            designNotPanel(0);
        }
        catch (Exception ei){
            Log.e("NotificationFragment_Er",ei.getMessage());
        }

        checkNot();
        return view;
    }

    public void designNotPanel(int k){
        mainParentLayout = (FrameLayout) view.findViewById(R.id.containsallN);


        ScrollView mainParent = new ScrollView(getContext());
        mainParent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);

        for (i = 0; i < allNotifications.size(); i++) {
            LinearLayout parent = new LinearLayout(getContext());
            parent.setBackgroundColor(Color.GREEN);
            parent.setGravity(Gravity.CENTER_VERTICAL);
            parent.setGravity(Gravity.CENTER_HORIZONTAL);

            LinearLayout.LayoutParams layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutDetails.topMargin=15;
            parent.setLayoutParams(layoutDetails);
            parent.setOrientation(LinearLayout.HORIZONTAL);

            final ImageButton floatingActionButton = new ImageButton(getContext());
                    /*final String mobNo = allNotifications.get(i).substring(0,10);
                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getContext(),mobNo,Toast.LENGTH_SHORT).show();
                        }
                    });*/
            floatingActionButton.setBackgroundResource(R.drawable.snaskar_9620332800);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.width=leftButtonWidth;
            layoutDetails.height=leftButtonHeight;
            layoutDetails.weight=1;
            layoutDetails.rightMargin=15;
            layoutDetails.topMargin = 25;
            layoutDetails.leftMargin=15;
            floatingActionButton.setLayoutParams(layoutDetails);
            parent.addView(floatingActionButton);

            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            TextView allN = new TextView(getContext());
            SeekBar seekBar = new SeekBar(getContext());
            allN.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
            allN.setGravity(Gravity.TOP);
            boolean isCurrentUser = allNotifications.get(i).split(" ")[0].equals("You");
            if(isCurrentUser) {
                allN.setBackgroundColor(Color.CYAN);
                seekBar.setBackgroundColor(Color.CYAN);
            }
            else {
                allN.setBackgroundColor(Color.YELLOW);
                seekBar.setBackgroundColor(Color.YELLOW);
            }
            allN.setPadding(22,0,10,0);
            allN.setTypeface(Typeface.DEFAULT_BOLD);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.weight=1;
            layoutDetails.width=textViewWidth;
            layoutDetails.height=textViewHeight;
            allN.setLayoutParams(layoutDetails);
            allN.setTextColor(Color.BLACK);
            String textToDisplay = allNotifications.get(i).substring(0,allNotifications.get(i).lastIndexOf(" "));
            allN.setText(textToDisplay);
            linearLayout.addView(allN);
            linearLayout.addView(seekBar);
            parent.addView(linearLayout);

            final FloatingActionButton floatingActionButton2 = new FloatingActionButton(getContext());
            floatingActionButton2.setId(i);
            final String songFileName = allNotifications.get(i).substring(allNotifications.get(i).lastIndexOf(" ")).trim();

            floatingActionButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e("Not_Frag", v.getId() + "");
                    if (mp != null) {
                        for (int j = 0; j < allNotifications.size(); j++) {
                            FloatingActionButton otherButon = (FloatingActionButton) view.findViewById(j);
                            otherButon.setImageResource(R.drawable.play);
                        }
                    }

                    if (v.getId() != idOfTheLastPlayButtonClicked) {
                        if(mp!=null)mp.reset();
                        mp = SingleTonMediaPlayer.getSingleTonMediaPlayerInstance();
                        String url = serverSongURL + "romantic/" + songFileName;
                        Log.e("Not_Frag_SongURL", url.toString());
                        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        try {
                            //mp = MediaPlayer.create(this, Uri.parse(url));
                            mp.setDataSource(url);
                            mp.prepare();
                            mp.start();
                            floatingActionButton2.setImageResource(R.mipmap.pause);
                            idOfTheLastPlayButtonClicked = v.getId();
                            isPlaying = true;
                        } catch (Exception ee) {
                            Log.e("Not_Frag_Err", "abc" + ee.getMessage());
                        }
                    }
                    else{
                        if(isPlaying){
                            mp.pause();
                            isPlaying = false;
                        }
                        else{
                            mp.start();
                            floatingActionButton2.setImageResource(R.mipmap.pause);
                            isPlaying = true;
                        }
                    }
                }
            });
            floatingActionButton2.setImageResource(R.drawable.play);
            floatingActionButton2.setSize(FloatingActionButton.SIZE_MINI);
            layoutDetails = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutDetails.weight=1;
            layoutDetails.width=rightButtonWidth;
            layoutDetails.height=rightButtonHeight;
            layoutDetails.rightMargin=20;
            layoutDetails.topMargin = 25;
            layoutDetails.leftMargin=10;
            floatingActionButton2.setLayoutParams(layoutDetails);
            parent.addView(floatingActionButton2);

            ll.addView(parent);
        }
        mainParent.addView(ll);
        mainParentLayout.addView(mainParent);
        if(k==1)mainParentLayout.removeAllViews();
    }


    public void checkNot(){
        new Handler().postDelayed(new Runnable() {
            HttpURLConnection urlConnection = null;
            InputStreamReader isr = null;
            @Override
            public void run() {
                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                URL url = new URL(serverURL + "/notifications/" + UserDetails.getPhoneNumber());
                                Log.e("NotFrag_Notf_Read", url.toString());
                                urlConnection = (HttpURLConnection) url.openConnection();
                                InputStream is = urlConnection.getInputStream();
                                isr = new InputStreamReader(is);
                                int data = isr.read();
                                final StringBuilder response = new StringBuilder("");
                                while (data != -1) {
                                    response.append((char) data);
                                    data = isr.read();
                                }
                                Log.e("NotFrag_Response",response.toString());
                                ArrayList<String> allYourNotificationFromServer = ParseNotificationData.getNotification(response.toString());
                                ArrayList<String> allYourNotification = new ArrayList<String>();
                                int newSize = allYourNotificationFromServer.size();
                                for(String eachNotification : allYourNotificationFromServer){
                                    String[] allData = eachNotification.split(" ");
                                    String fromUser = allData[0];
                                    String toUser = allData[1];
                                    String ts = allData[2];
                                    String timeSplit[] = ts.split("_");
                                    ts = "on "+timeSplit[0] + " at "+timeSplit[1].substring(0,5);
                                    String type = allData[3];
                                    String songName = allData[4];


                                    if(fromUser.equals(UserDetails.getPhoneNumber())){
                                        if (allC.get(toUser) != null) {
                                            allYourNotification.add("You dedicated a song to " + allC.get(toUser) + "\n" + ts +" "+songName);
                                        } else {
                                            allYourNotification.add("You dedicated a song to " + toUser + "\n" + ts +" "+songName);
                                        }
                                    }
                                    else {
                                        if (allC.get(fromUser) != null) {
                                            allYourNotification.add(allC.get(fromUser) + " dedicated you a song\n" + ts +" "+songName);
                                        } else {
                                            allYourNotification.add(fromUser + " dedicated you a song\n" + ts +" "+songName);
                                        }
                                    }
                                }
                                Log.e("NotFrag_AllParsedNot",allYourNotification.toString());
                                AllNotifications.allNotifications = allYourNotification;
                                Log.e("NotFrag_Size",totalNumberOfNotifications+" -- "+newSize);
                                if(newSize>totalNumberOfNotifications) {
                                    totalNumberOfNotifications = newSize;
                                    Activity currActivity = getActivity();
                                    NotificationCompat.Builder builder =
                                            new NotificationCompat.Builder(view.getContext())
                                                    .setSmallIcon(R.drawable.btn_dedicate)
                                                    .setColor(001500)
                                                    .setContentTitle("MoodOff")
                                                    .setContentText(UserDetails.getUserName()+ "!! You got new notifications!!");

                                    Intent notificationIntent;
                                            if(getActivity()==null){
                                                notificationIntent = new Intent(view.getContext(), Start.class);
                                                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            }
                                            else{
                                                currActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(getContext(),"Hi New one",Toast.LENGTH_SHORT).show();
                                                        designNotPanel(1);
                                                    }
                                                });
                                                notificationIntent = new Intent(view.getContext(), NotificationFragment.class);
                                                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            }
                                    PendingIntent contentIntent = PendingIntent.getActivity(view.getContext(), 0, notificationIntent,
                                            PendingIntent.FLAG_CANCEL_CURRENT);
                                    builder.setContentIntent(contentIntent);
                                    builder.setAutoCancel(true);

                                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    Ringtone r = RingtoneManager.getRingtone(view.getContext(), notification);
                                    r.play();

                                    // Add as notification
                                    NotificationManager manager = (NotificationManager) view.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    manager.notify(0, builder.build());
                                }
                            }
                            catch(Exception ee){
                                Log.e("NotificationFrag_Err","Some issues.."+ee.getMessage());
                            }
                            finally {
                                try {
                                    isr.close();
                                } catch (Exception ee) {
                                    Log.e("NotificationFrag_Err", "InputStreamReader couldn't be closed");
                                }
                                urlConnection.disconnect();

                            }
                        }
                    }).start();
                    checkNot();
                } catch (Exception ee) {
                    Log.e("NotificationFrag_Err","Some issues.."+ee.getMessage());
                }
            }
        },5000);
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
