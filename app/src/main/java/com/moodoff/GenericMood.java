package com.moodoff;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.AppData;
import com.moodoff.helper.DBHelper;
import com.moodoff.helper.HangManWords;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;
import com.moodoff.helper.ValidateMediaPlayer;
import com.moodoff.model.UserDetails;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GenericMood.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GenericMood#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenericMood extends Moods implements View.OnClickListener,AudioManager.OnAudioFocusChangeListener{

    private AudioManager mAudioManager;
    @Override
    public void onAudioFocusChange(int i) {
        if(i<=0){
            mp.pause();
        }
        else{
            mp.start();
        }
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String serverURL = HttpGetPostInterface.serverURL;
    private String serverSongURL = HttpGetPostInterface.serverSongURL;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GenericMood() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GenericMood.
     */
    // TODO: Rename and change types and number of parameters
    public static GenericMood newInstance(String param1, String param2) {
        GenericMood fragment = new GenericMood();
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
    View view;
    Bitmap bitmap;
    String imageFilePath;
    ImageView photoView;

    //Player variables
    public static MediaPlayer mp;
    Context context;
    //Buttons
    Button stopBtn, nextBtn, prevBtn, repBtn, shuffleBtn;
    public static Button playPauseBtn;
    FloatingActionButton dedicateButton,playListButton, menuButton;
    ProgressBar storyLoadSpinner;
    public static ProgressBar spinner;
    SeekBar seekBar;
    TextView songName = null, storyTitleTV, storyBodyTV, duration;
    Handler seekHandler = new Handler();
    ArrayList<String> currentplayList = null;
    String currentSong = "", currentMood = "", playListFilePath = "";
    public static int playOrPauseParm = 0, isPlayOrPauseFromGM = 0;
    int currentIndex = 0, repParm = 0, timeElapsedOrTimeLeft = 0;
    int numberOfChances = 5;
    DBHelper dbOperations;
    StoreRetrieveDataInterface fileOperations = new StoreRetrieveDataImpl("UserData.txt");
    LinearLayout gamePanel,likeAndStoryTitle;

    public void init(){
        dbOperations = new DBHelper(getContext());
        songName = (TextView) view.findViewById(R.id.nameOfSong);
        storyTitleTV = (TextView) view.findViewById(R.id.tv_storytitle);
        storyBodyTV = (TextView) view.findViewById(R.id.tv_story);
        playPauseBtn = (Button) view.findViewById(R.id.playPauseBtn);
        stopBtn = (Button) view.findViewById(R.id.stopButton);
        nextBtn = (Button) view.findViewById(R.id.nextButton);
        prevBtn = (Button) view.findViewById(R.id.prevButton);
        repBtn = (Button) view.findViewById(R.id.repeatButton);
        shuffleBtn = (Button) view.findViewById(R.id.shuffleButton);
        dedicateButton = (FloatingActionButton) view.findViewById(R.id.btn_dedicate);
        playListButton = (FloatingActionButton)view.findViewById(R.id.btn_playlist);
        //changeMoodButton = (FloatingActionButton) view.findViewById(R.id.btn_changemood);
        menuButton = (FloatingActionButton)view.findViewById(R.id.btn_menu);
        spinner = (ProgressBar) view.findViewById(R.id.progressBar);
        storyLoadSpinner = (ProgressBar) view.findViewById(R.id.load_story_spinner);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        duration = (TextView) view.findViewById(R.id.duration);
        photoView = (ImageView) view.findViewById(R.id.photoView);
        gamePanel = (LinearLayout)view.findViewById(R.id.gamePanel);
        likeAndStoryTitle = (LinearLayout)view.findViewById(R.id.likeAndStoryTitle);
        gamePanel.setVisibility(View.GONE);
        playPauseBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        prevBtn.setOnClickListener(this);
        repBtn.setOnClickListener(this);
        shuffleBtn.setOnClickListener(this);
        dedicateButton.setOnClickListener(this);
        playListButton.setOnClickListener(this);
        seekBar.setOnClickListener(this);
        duration.setOnClickListener(this);
        disableButton(prevBtn);
        currentMood = mParam1;

        // Game init
        newGame = (Button)view.findViewById(R.id.newgame);
        checkTheLetter = (Button)view.findViewById(R.id.checkTheLetter);
        tvv = (TextView)view.findViewById(R.id.tv_storytitle);
        pointsEarned = (TextView)view.findViewById(R.id.totalScore);
        selectedLetters = (TextView)view.findViewById(R.id.selectedLetters);
        chosenLetter = (EditText)view.findViewById(R.id.guessedLetter);
        cartoon = (ImageView)view.findViewById(R.id.cartoon);
        //loveStory = (FloatingActionButton) view.findViewById(R.id.like_story);
        gamerules = (FloatingActionButton) view.findViewById(R.id.gamerules);
    }

    public void showMenu(){
        PopupMenu popupMenu = new PopupMenu(view.getContext(),menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.genericmoodbg_popup,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String selectedOption = item.getTitle().toString();
                if(selectedOption.equalsIgnoreCase("Play Game")){
                    showItemInMiddle(5);
                }
                else if(selectedOption.equalsIgnoreCase("Show Story")){
                    showItemInMiddle(1);
                }
                else {
                    if (selectedOption.equalsIgnoreCase("Choose From gallery")) {
                        showItemInMiddle(2);
                    } else {
                        if (selectedOption.equalsIgnoreCase("Capture using Camera")) {
                            showItemInMiddle(3);
                        }
                        else {
                            if (selectedOption.equalsIgnoreCase("Show last Image")) {
                                showItemInMiddle(4);
                            }
                        }
                    }
                }
                return true;
            }
        });
        popupMenu.show();
    }

    TextView txtView_selectedWord,points;
    char[] wordToFill;
    int totalScore=UserDetails.getScore();
    public void showItemInMiddle(final int selectedOption){
        switch(selectedOption){
            case 1:{
                likeAndStoryTitle.setVisibility(View.VISIBLE);
                storyBodyTV.setVisibility(View.VISIBLE);
                gamePanel.setVisibility(View.GONE);
                photoView.setVisibility(View.GONE);
                storyLoadSpinner.setVisibility(View.VISIBLE);
                ServerManager serverManager = new ServerManager();
                serverManager.loadStory(currentMood,getActivity(),storyTitleTV,storyBodyTV,storyLoadSpinner);
                break;

            }
            case 2:{
                likeAndStoryTitle.setVisibility(View.GONE);
                gamePanel.setVisibility(View.GONE);
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),2);
                break;
            }
            case 3:{
                likeAndStoryTitle.setVisibility(View.GONE);
                gamePanel.setVisibility(View.GONE);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File pictureDirectory = new File(Environment.getExternalStorageDirectory().getAbsoluteFile().toString()+"/moodoff/"+currentMood+"/");
                pictureDirectory.mkdirs();
                String pictureName = getPictureName();
                File imageFile = new File(pictureDirectory,pictureName);
                // Directory creation complete
                Uri picture = Uri.fromFile(imageFile);
                // We have to create an URI resource because putExtra expects URI resource as the second argument.
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,picture);
                // Start the Activity Now
                startActivityForResult(cameraIntent,0);
                break;
            }
            case 4:{
                gamePanel.setVisibility(View.GONE);
                likeAndStoryTitle.setVisibility(View.GONE);
                imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/moodoff/"+currentMood+"/"+getPictureName();
                if(!(new File(imageFilePath).exists())){
                    Toast.makeText(getContext(), "Not yet taken any photo for this mood", Toast.LENGTH_SHORT).show();
                }
                else {
                    bitmap = BitmapFactory.decodeFile(imageFilePath);
                    photoView.setImageBitmap(bitmap);
                    photoView.setVisibility(View.VISIBLE);
                }
                break;
            }
            case 5: {
                newGame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        resetGameView();
                        launchGame();
                    }
                });

                resetGameView();
                launchGame();

            }
        }
    }

    private void resetGameView(){
        likeAndStoryTitle.setVisibility(View.VISIBLE);
        newGame.setEnabled(false);
        gamePanel.setVisibility(View.VISIBLE);
        photoView.setVisibility(View.GONE);
        storyBodyTV.setVisibility(View.GONE);
        //loveStory.setVisibility(View.INVISIBLE);
        tvv.setText("Guess The Word");
        chosenLetter.setText("");
        chosenLetter.setEnabled(true);
        selectedLetters.setText("");
        txtView_selectedWord = (TextView)view.findViewById(R.id.selectedWord);
        points = (TextView)view.findViewById(R.id.totalScore);
    }
    // Game Variables

    Button newGame,checkTheLetter;
    FloatingActionButton loveStory, gamerules;
    TextView tvv,pointsEarned,selectedLetters;
    EditText chosenLetter;
    ImageView cartoon;
    // Finished Declaration

    private  void launchGame(){
        final HashSet<Character> lettersPicked = new HashSet<>();
        numberOfChances=5;
        ArrayList<String> words = HangManWords.getAllWords();
        int randomNo = new Random().nextInt(words.size());
        final String selectedWord = words.get(randomNo);
        Log.e("GM_SELECTEDWORD",selectedWord);
        pointsEarned.setText(""+totalScore); // Get the points from DB
        cartoon.setImageResource(R.drawable.c1);
        StringBuilder sb = new StringBuilder("");
        for(int i=0;i<selectedWord.length();i++){
            sb.append(" _");
        }
        wordToFill = sb.toString().toCharArray();
        txtView_selectedWord.setText(String.valueOf(wordToFill));

        chosenLetter.setFilters(new InputFilter[]{new InputFilter.AllCaps(),new InputFilter.LengthFilter(1)});

        gamerules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.gamerules);
                dialog.show();
            }
        });

        checkTheLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredLetter = chosenLetter.getText().toString();
                if(enteredLetter.equals("")) {
                    Messenger.print(getContext(), "Add some letter to try");
                }
                else {
                    char letterPressed = enteredLetter.charAt(0);
                    if (lettersPicked.contains(letterPressed)) {
                        Messenger.print(getContext(), "!!Letter already used!!");
                        chosenLetter.setText("");
                    } else {
                        lettersPicked.add(letterPressed);
                        selectedLetters.setText(selectedLetters.getText()+" "+letterPressed);
                        Log.e("GM_LETTERFOUND",""+letterPressed);
                        if(checkIfLetterIsInWordAndUpdateCartoon(selectedWord, letterPressed)){
                            //char matchedLetter = s.toString().charAt(s.toString().length()-1);
                            ArrayList<Integer> getPos = fillPlacesOfWord(selectedWord,letterPressed);
                            Log.e("GM_POSEXPOSED",getPos.toString());
                            //char[] wordToFill = selectedWord.toCharArray();
                            for(Integer eachPosition : getPos){
                                wordToFill[eachPosition+eachPosition+1] = letterPressed;
                            }
                            //points.setText(""+totalScore);
                            txtView_selectedWord.setText(String.valueOf(wordToFill));
                            int idx = -1;
                            idx = txtView_selectedWord.getText().toString().indexOf("_");
                            if(idx==-1){
                                chosenLetter.setEnabled(false);
                                totalScore+=(selectedWord.length()*3);
                                points.setText(""+totalScore);
                                UserDetails.setScore(totalScore);
                                fileOperations.beginWriteTransaction();
                                fileOperations.updateValueFor("score",String.valueOf(totalScore));
                                fileOperations.endWriteTransaction();
                                newGame.setEnabled(true);
                            }

                        }
                        else{
                            int pic1 = R.drawable.c1,pic2 = R.drawable.c2,
                                    pic3 = R.drawable.c3,pic4 = R.drawable.c4,
                                    pic5 = R.drawable.c5,pic6 = R.drawable.c6;
                            Log.e("GM_FAILED",""+numberOfChances);
                            switch (numberOfChances){
                                case 5:{
                                    cartoon.setImageResource(pic2);
                                    break;
                                }
                                case 4:{
                                    cartoon.setImageResource(pic3);
                                    break;
                                }
                                case 3:{
                                    cartoon.setImageResource(pic4);
                                    break;
                                }
                                case 2:{
                                    cartoon.setImageResource(pic5);
                                    break;
                                }
                                case 1:{
                                    cartoon.setImageResource(pic6);
                                    newGame.setEnabled(true);
                                    chosenLetter.setEnabled(false);
                                    break;
                                }
                            }
                            numberOfChances--;
                        }
                        chosenLetter.setText("");
                    }
                }
            }
        });
    }


    private ArrayList<Integer> fillPlacesOfWord(String word, char matchedLetter){
        ArrayList<Integer> characterPositions = new ArrayList<>();
        for(int i=0;i<word.length();i++){
            if((word.charAt(i))==(matchedLetter))
            characterPositions.add(i);
        }
        Log.e("GM_CHARSFOUNDAT",characterPositions.toString());
        return characterPositions;
    }

    private boolean checkIfLetterIsInWordAndUpdateCartoon(String selectedWord, char c){
        char charToCompare = c;
        for(int i=0;i<selectedWord.length();i++){
            if((selectedWord.charAt(i))==charToCompare){
                Log.e("GM_MATCHED","MATCHED_"+charToCompare+"_"+selectedWord);
                return true;
            }
        }
        return false;
    }

    LayoutInflater mainInflater;
    ViewGroup mainContainer;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        mainInflater = inflater;
        mainContainer = container;
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_generic_mood, container, false);
        //view.setBackgroundColor(Color.WHITE);

        init();
        //Toast.makeText(getContext(),"You selected mood: "+(char)(mParam1.charAt(0)-32)+mParam1.substring(1),Toast.LENGTH_LONG).show();

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        storyLoadSpinner.setVisibility(View.VISIBLE);
        ServerManager serverManager = new ServerManager();
        serverManager.loadStory(currentMood,getActivity(),storyTitleTV,storyBodyTV,storyLoadSpinner);

        if (currentMood != "") {
            currentplayList = readList(currentMood);
            checkRepeatButtonStatus(currentIndex);
        }
        if (currentplayList != null) {
            //onClickShuffleButton(view);
            Log.e("GenericMood_SongCur",currentplayList.get(0));
            currentSong = songNameFromList(currentplayList, currentIndex);
            Log.e("GenericMood_SongCur",currentSong);
            displaySongName(songName, "Tap play...");
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(seekBar.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });

        final FloatingActionButton loveButton = (FloatingActionButton)view.findViewById(R.id.btn_love);
        loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUser = UserDetails.getPhoneNumber();
                char type = '1';
                if(mp==null || !mp.isPlaying()){
                    Toast.makeText(getActivity().getApplicationContext(),"Please play a song to like it!!",Toast.LENGTH_SHORT).show();
                }
                else {
                    final String Url = serverURL+"/notifications/"+currentUser+"/"+currentSong+"/"+type;

                    if(dbOperations.todoWorkEntry(Url)){
                        for(int i=0;i<10;i++){
                            dbOperations.todoWorkEntry(Url);
                        }
                        loveButton.setImageResource(R.drawable.love_s);
                        Toast.makeText(getActivity().getApplicationContext(),"You loved this song",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(),"Sorry!! Please try after sometime!!",Toast.LENGTH_SHORT).show();
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HttpURLConnection urlConnection=null;
                            try {
                                // Proide the URL to which you would fire a post
                                URL url = new URL(Url);
                                Log.e("GenericMood_LOVEButton",url.toString());
                                urlConnection = (HttpURLConnection) url.openConnection();
                                urlConnection.setDoOutput(true);
                                int responseCode = urlConnection.getResponseCode();
                                if(responseCode==200){
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity().getApplicationContext(),"You loved this song",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else{
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity().getApplicationContext(),"Sorry!! Please try after sometime!!",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            catch(Exception ee){
                                Log.e("GenericMood_LoveButton",ee.getMessage());
                                ee.printStackTrace();
                            }
                            // Close the Http Connection that you started in finally.
                            finally {
                                if(urlConnection!=null)
                                    urlConnection.disconnect();
                            }
                        }
                    }).start();
                }
            }
        });

        duration.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                String Value = duration.getText().toString().split(":")[0];
                if (Integer.valueOf(Value) > 100) {
                    duration.setText("00:00");
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

//        songName.setSelected(true);
//        songName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//        songName.setSingleLine(true);

        return view;
    }

    public void navigateContacts(View v){
        if(mp==null){
            Toast.makeText(getActivity().getApplicationContext(),"You have to play a song to dedicate.",Toast.LENGTH_SHORT).show();
        }
        else {
            Log.e("GenericMood","willstart");
            Intent it = new Intent(getContext(), ContactList.class);
            startActivityForResult(it, 1);
        }
    }

    static String getPictureName(){return "mogambo.jpg";}

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String currentUser = UserDetails.getPhoneNumber();
                char type = '1';
                final String stredittext=data.getStringExtra("selectedContact");

                final String Url = "notifications/"+currentUser+"/"+stredittext.substring(stredittext.lastIndexOf(" ")+1)+"/"+currentMood+"@"+currentSong+"/"+type;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection=null;
                        try {
                            // Proide the URL fto which you would fire a post
                            URL url = new URL(serverURL+"/"+Url);
                            Log.e("GM_DedicateURL",url.toString());
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setDoOutput(true);
                            int responseCode = urlConnection.getResponseCode();
                            if(responseCode==200){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity().getApplicationContext(),"Dedicated to "+stredittext,Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else{
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity().getApplicationContext(),"Sorry!! Please try after sometime!!",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        catch(Exception ee){
                            ee.printStackTrace();
                        }
                        // Close the Http Connection that you started in finally.
                        finally {
                            if(urlConnection!=null)
                                urlConnection.disconnect();
                        }
                    }
                }).start();
            }
        }
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/moodoff/"+currentMood+"/"+getPictureName();
                Log.e("Geeeeeeneeerd",imageFilePath.toString());
                bitmap = BitmapFactory.decodeFile(imageFilePath);
                photoView.setImageBitmap(bitmap);
                photoView.setVisibility(View.VISIBLE);
                Log.e("Geeeeeeneeerd","GGGGGGGGGGGGGGGGMMMMMMM");
            }
        }
        if (requestCode == 2)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if (data != null)
                {
                    try
                    {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                        photoView.setImageBitmap(bitmap);
                        photoView.setVisibility(View.VISIBLE);
                        photoView.setAdjustViewBounds(true);
                        Log.e("GM_gallery","Photo set");

                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED)
            {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playPauseBtn:
                onClickPlayButton(view);
                break;
            case R.id.prevButton:
                onClickPrevButton(view);
                break;
            case R.id.nextButton:
                onClickNextButton(view);
                break;
            case R.id.stopButton:
                onClickStopButton(view);
                break;
            case R.id.repeatButton:
                onClickRepeatButton(view);
                break;
            case R.id.shuffleButton:
                onClickShuffleButton(view);
                break;
            case R.id.btn_dedicate:
                navigateContacts(view);
                break;
            case R.id.duration:
                onClickDuration(view);
                break;
            case R.id.btn_playlist:
                showPlayList();
                break;
        }
    }

    private void showPlayList(){
        ArrayList<String> allSongsOfCurrentMood = AppData.allMoodPlayList.get(currentMood);
        Log.e("GM_UGETSONGS",allSongsOfCurrentMood.toString());
        PopupMenu popupMenu = new PopupMenu(view.getContext(),playListButton);
        popupMenu.getMenuInflater().inflate(R.menu.genericmoodplaylist_popup,popupMenu.getMenu());

        for(int i=0;i<allSongsOfCurrentMood.size();i++) {
            String songName = allSongsOfCurrentMood.get(i).replaceAll("_"," ");
            popupMenu.getMenu().add(songName.substring(0, songName.lastIndexOf(".")));
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String selectedOption = item.getTitle().toString();
                int songIndex = currentplayList.indexOf(selectedOption.replaceAll(" ","_")+".mp3");
                Log.e("GM_PLAYLIST",selectedOption+" index:"+songIndex);

                onClickStopButton(view);
                currentIndex = songIndex;
                onClickPlayButton(view);

                return true;
            }
        });

        popupMenu.show();

    }

    public void onClickDuration(View v) {
        timeElapsedOrTimeLeft = (timeElapsedOrTimeLeft > 0) ? 0 : 1;
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            seekUpdation();
        }
    };

    public void seekUpdation() {
        if (mp!=null) {
            seekBar.setProgress(mp.getCurrentPosition());
            if(seekBar.getMax()!=0) {
                duration.setText(getCurrentSongElapsedOrLeftDuration(mp, timeElapsedOrTimeLeft));
                seekHandler.postDelayed(run, 10);
            }
        }
    }

    /*Return current playing song's duration in mm:ss format*/
    public String getCurrentSongElapsedOrLeftDuration(MediaPlayer mediaPlayer, int timeElapsedOrTimeLeft) {
        try {
            int duration = 0, elapsedOrLeftTimeInMiliSeconds = 0;
            if (mediaPlayer != null) {
                if(timeElapsedOrTimeLeft == 0) {
                    elapsedOrLeftTimeInMiliSeconds = mediaPlayer.getDuration()-mediaPlayer.getCurrentPosition();
                } else {
                    elapsedOrLeftTimeInMiliSeconds = mediaPlayer.getCurrentPosition();
                }
                if (elapsedOrLeftTimeInMiliSeconds != 0) {
                    try{
                        duration = ((elapsedOrLeftTimeInMiliSeconds) / 1000);
                    }
                    catch(ArithmeticException e){
                        duration = 0;
                    }
                }
            }
            return ((duration/60) + ":" + ((duration%60 > 9) ? (duration % 60) : ("0"+(duration%60)) ));
        } catch (Exception e) {
            toastError(e.getMessage());
            return "00:00";
        }
    }

    /*Return current playing song's duration in mm:ss format*/
    public String getCurrentSongDuration(MediaPlayer mediaPlayer) {
        try {
            int duration = 0;
            if (mediaPlayer != null) {
                duration = (mediaPlayer.getDuration()/1000);
            }
            return ((duration/60) + ":" + ((duration%60 > 9) ? (duration % 60) : ("0"+(duration%60)) ));
        } catch (Exception e) {
            toastError(e.getMessage());
            return "00:00";
        }
    }

    /*On click method for previous button: play previous song*/
    public void onClickPrevButton(View v) {
        try{
            onClickStopButton(v);
            currentIndex = decrementIndex(currentIndex);
            showSpinner();
            onClickPlayButton(v);
        } catch (Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }
    /*On click method for next button: play next song*/
    public void onClickNextButton(View v) {
        try{
            onClickStopButton(v);
            currentIndex = incrementIndex(currentIndex);
            showSpinner();
            onClickPlayButton(v);
        } catch (Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }
    /*On click method for play/pause button*/
    public void onClickPlayButton(View v) {
        try{
            if (playOrPauseParm == 0) {
                playSong();
            } else if (playOrPauseParm == 1) {
                pauseSong();
            } else {
                toastError("Unexpected condition !!!");
            }
        } catch (Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }

    /*play a new or currently paused song*/
    public void playSong() {
        Log.e("GenericMood_MPPlaySong","MediaPlayer Play Initiated");
        try {
            if (mp == null) {
                //for playing new song
                showSpinner();
                currentSong = (String) songNameFromList(currentplayList, currentIndex);
                displaySongName(songName, "Loading...");
                setSongSource(currentIndex, currentMood);
                mp.setLooping(false);
                //after the song is prepared in asynchronous mode
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        isPlayOrPauseFromGM = 1;
                        showPlayPauseButton("pause");
                        displaySongName(songName, currentSong.substring(0,currentSong.lastIndexOf(".")));
                        if(mediaPlayer!=null) {
                            seekBar.setMax(mediaPlayer.getDuration());
                            seekUpdation();
                        }
                        ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
                        validateMediaPlayer.initialiseAndValidateMediaPlayer("mood","play");
                        mediaPlayer.start();
                    }
                });
                Log.e("GenericMood_MPPause","MediaPlayer PLay Done");
                //call next on completion of the song
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        currentIndex = incrementIndex(currentIndex);
                        onClickNextButton(view);
                    }
                });

            } else {
                //for playing paused song
                if(!mp.isPlaying()) {
                    isPlayOrPauseFromGM = 1;
                    showPlayPauseButton("pause");
                    currentSong = (String)songNameFromList(currentplayList,currentIndex);
                    displaySongName(songName, currentSong.substring(0,currentSong.lastIndexOf(".")));
                    if(mp!=null) {
                        seekBar.setMax(mp.getDuration());
                        seekUpdation();
                    }
                    ValidateMediaPlayer validateMediaPlayer = ValidateMediaPlayer.getValidateMediaPlayerInstance();
                    validateMediaPlayer.initialiseAndValidateMediaPlayer("mood","play");
                    mp.start();
                }
            }
        } catch (Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }

    /*pause the currently playing song*/
    public void pauseSong() {
        Log.e("GenericMood_MPPause","MediaPlayer Paused Initiated");
        try {
            if(mp.isPlaying()) {
                mp.pause();
            }
            isPlayOrPauseFromGM = 1;
            showPlayPauseButton("play");
            Log.e("GenericMood_MPPause","MediaPlayer Paused Done");
        } catch (Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }

    /*On click method for stop button: stops music here*/
    public void onClickStopButton(View v) {
        try{
            releaseMediaPlayerObject();
            isPlayOrPauseFromGM = 1;
            showPlayPauseButton("play");
            seekBar.setMax(0);
        } catch (Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }

    /*show spinner in place of play/pause button*/
    public void showSpinner() {
        try {
            playPauseBtn.setVisibility(Button.GONE);
            spinner.setVisibility(ProgressBar.VISIBLE);
        } catch(Exception e){toastError(e.getMessage());}
    }

    /*set play or pause button for display*/
    public static void showPlayPauseButton(String playOrPause) {
        try {
            playOrPause = playOrPause.toLowerCase();
            if (playOrPause == "play") {
                playOrPauseParm = 0;
                playPauseBtn.setBackgroundResource(R.mipmap.play);
            } else {
                playOrPauseParm = 1;
                playPauseBtn.setBackgroundResource(R.mipmap.pause);
            }
            playPauseBtn.setVisibility(Button.VISIBLE);
            spinner.setVisibility(ProgressBar.GONE);
        } catch(Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }

    /*On click method for shuffle button: Change repeat button and status*/
    public void onClickRepeatButton(View v) {
        try {
            repParm = (repParm+1)%3;
            checkRepeatButtonStatus(currentIndex);
            if (repParm == 1) {
                repBtn.setBackgroundResource(R.mipmap.repeat_one);
            } else if (repParm == 2) {
                repBtn.setBackgroundResource(R.mipmap.repeat_all);
            } else {
                repBtn.setBackgroundResource(R.mipmap.repeat_none);
            }
        } catch(Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }

    /*On click method for shuffle button: Shuffle the playlist*/
    public void onClickShuffleButton(View v) {
        try {
            //Shuffle the collection
            String currentPlayingSongs = currentplayList.get(currentIndex);
            Collections.shuffle(currentplayList);
            currentIndex = currentplayList.indexOf(currentPlayingSongs);
        } catch(Exception e){
            toastError(e.getMessage());
            releaseMediaPlayerObject();
        }
    }
    boolean doorClosed = true;
    ArrayList<String> listOfSong = new ArrayList<String>();
    //Read the text file similar to activityName and return an listOfSong
    public ArrayList<String> readList(final String mood) {
        try{
            final String userMobileNumber = UserDetails.getPhoneNumber();
            final String serverURL = HttpGetPostInterface.serverURL;

            // Access the collection of songs that has already been read in Start.java and stored in variable of file PlaylistSongs.java
            listOfSong = AppData.allMoodPlayList.get(mood);

            Collections.shuffle(listOfSong);
            return(listOfSong);
        } catch(Exception e){toastError(e.getMessage()); return(null);}
    }

    /*Set data source with the currentSong*/
    public void setSongSource(int index, String mood) {
        String currentSong = currentplayList.get(index);
        releaseMediaPlayerObject();
        mp = new MediaPlayer();
        Log.e("GenericMood_MPInst",""+(mp==null));
        String url = serverSongURL + mood + "/" + currentSong;
        Log.e("GenericMood_SongPlayURL",url.toString());
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            //mp = MediaPlayer.create(this, Uri.parse(url));
            mp.setDataSource(url);
            mp.prepareAsync();
        } catch (IllegalArgumentException e) {toastError(e.getMessage()); releaseMediaPlayerObject(); e.printStackTrace();
        } catch (IllegalStateException e) {toastError(e.getMessage()); releaseMediaPlayerObject(); e.printStackTrace();
        } catch (IOException e) {toastError(e.getMessage()); releaseMediaPlayerObject(); e.printStackTrace();
        } catch (Exception e) {toastError(e.getMessage()); releaseMediaPlayerObject(); e.printStackTrace();
        }
    }

    /*Return the currentSong at an index from the playList*/
    public String songNameFromList(ArrayList<String> playList, int index) {
        try {
            return playList.get(index);
        } catch(Exception e){toastError(e.getMessage()); return null;}
    }

    /*Set the text with currentSong*/
    public void displaySongName(TextView textToDisplaySong, String currentSong) {
        try {
            if (currentSong.contains("_")) {
                currentSong = currentSong.split(".", 1)[0].replaceAll("_", " ");
            } else if (!currentSong.contains("_")) {
                currentSong = currentSong.split(".", 1)[0];
            }
            textToDisplaySong.setText(currentSong);
        } catch(Exception e){
            Log.e("GenericMood_displaySong",e.getMessage());
        }
    }

    /*check repeat button status and enable/disable next/previous button*/
    public void checkRepeatButtonStatus(int index) {
        try {
            if (repParm == 0 || repParm == 1) {
                if (index == 0) {
                    disableButton(prevBtn);
                } else if (index == getLastIndex(currentplayList)) {
                    disableButton(nextBtn);
                } else {
                    enableButton(nextBtn);
                    enableButton(prevBtn);
                }
            } else {
                enableButton(nextBtn);
                enableButton(prevBtn);
            }
        } catch(Exception e){toastError(e.getMessage());}
    }

    /*return the decremented index as per repeat button status*/
    public int decrementIndex(int index) {
        try {
            int lastIndex = getLastIndex(currentplayList);
            index--;
            if (repParm == 0 || repParm == 1) {
                if (index <= 0) {
                    index = 0;
                }
            } else {
                //decrement index in a circular queue order
                index = (index+(lastIndex+1))%(lastIndex+1);
            }
            checkRepeatButtonStatus(index);
            return index;
        } catch(Exception e){toastError(e.getMessage()); return -1;}
    }

    /*return the incremented index as per repeat button status*/
    public int incrementIndex(int index) {
        try {
            int lastIndex = getLastIndex(currentplayList);
            index++;
            if (repParm == 0 || repParm == 1) {
                if (index >= lastIndex) {
                    index = lastIndex;
                }
            } else {
                //increment index in a circular queue order
                index = (index+(lastIndex+1))%(lastIndex+1);
            }
            checkRepeatButtonStatus(index);
            return index;
        } catch(Exception e){toastError(e.getMessage()); return -1;}

    }

    /*release and return the nullified mediaplayer object*/
    public static void releaseMediaPlayerObject() {
        try {
            if (mp != null) {
                if(mp.isPlaying()){mp.stop();}
                mp.release();
                mp = null;
            }
        } catch(Exception e){e.fillInStackTrace();e.printStackTrace();}
    }

    /*return the last index of the playlist*/
    public  int getLastIndex(ArrayList<String> playList) {
        try {
            int lastIndex;
            lastIndex = (playList.size() - 1);
            return lastIndex;
        } catch(Exception e){toastError(e.getMessage()); return -1;}
    }

    /*enable clickability of a button*/
    public void enableButton (Button button) {
        try {
            button.setClickable(true);
            button.setEnabled(true);
        } catch(Exception e){toastError(e.getMessage());}
    }

    /*disable clickability of a button*/
    public void disableButton (Button button) {
        try {
            button.setClickable(false);
            button.setEnabled(false);
        } catch(Exception e){toastError(e.getMessage());}
    }

    /*Toast error message*/
    public static void toastError(String error) {
        //Toast.makeText(view.getContext(), "Oops! Somehing went wrong\n"+error.toString(), Toast.LENGTH_LONG).show();
        Log.e("GenericMood_MPissue",error);
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
        releaseMediaPlayerObject();
        super.onDetach();
        mListener = null;
        playOrPauseParm=0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.abandonAudioFocus(this);
        ServerManager serverManager = new ServerManager();
        serverManager.exitLiveMood(UserDetails.getPhoneNumber());
        Log.e("GenericMood","GM on Destroy");
        if(mp!=null) {
            releaseMediaPlayerObject();
            mp = null;
        }
        playOrPauseParm=0;
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
