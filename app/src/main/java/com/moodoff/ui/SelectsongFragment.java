package com.moodoff.ui;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.R;
import com.moodoff.helper.AllAppData;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectsongFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectsongFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectsongFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SelectsongFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectsongFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectsongFragment newInstance(String param1, String param2) {
        SelectsongFragment fragment = new SelectsongFragment();
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

    View view;
    ImageButton okButtonWidth,cancelButtonWidth;
    int screenHeight, screenWidth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_selectsong, container, false);

        //getAndSetScreenSizes();
        //setWidthOfButtonAcrossScreen();
        //retrieveAndShowSongs();
        return view;

    }
    static int playButtonId = 0;
    private void retrieveAndShowSongs(){
        LinearLayout allSongsContainer = (LinearLayout)view.findViewById(R.id.eachRingToneSong);
        HashMap<String,ArrayList<String>> allSongs = AllAppData.allMoodPlayList;
        for(final String eachMood : allSongs.keySet()){
            TextView moodType = new TextView(getContext());
            moodType.setText(eachMood);
            allSongsContainer.addView(moodType);
            for(final String eachSong : allSongs.get(eachMood)){
                LinearLayout eachSongPanel = new LinearLayout(getContext());
                eachSongPanel.setGravity(Gravity.CENTER_VERTICAL);
                eachSongPanel.setOrientation(LinearLayout.HORIZONTAL);
                TextView songName = new TextView(getContext());
                songName.setText(eachSong);
                final FloatingActionButton playButton = new FloatingActionButton(getContext());
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

                eachSongPanel.addView(playButton);
                eachSongPanel.addView(songName);
                allSongsContainer.addView(eachSongPanel);
            }
        }


    }

    private void getAndSetScreenSizes(){
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }
    /*public void setWidthOfButtonAcrossScreen(){
        okButtonWidth = (ImageButton)view.findViewById(R.id.songselectok);
        cancelButtonWidth = (ImageButton)view.findViewById(R.id.songselectcancel);
        okButtonWidth.setWidth((int)Math.floor(0.5*screenWidth));
        cancelButtonWidth.setWidth((int)Math.floor(0.5*screenWidth));

    }*/

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
