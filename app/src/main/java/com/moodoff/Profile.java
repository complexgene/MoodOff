package com.moodoff;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.moodoff.model.UserDetails;

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

    View view,tempView;
    TextView myName, myPhNo, myEmail, myDob;
    ImageButton selectRingTone;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        tempView = inflater.inflate(R.layout.fragment_selectsong, container, false);
        myName = (TextView)view.findViewById(R.id.username);
        myPhNo = (TextView)view.findViewById(R.id.userPhNo);
        myEmail = (TextView)view.findViewById(R.id.useremailId);
        myDob = (TextView)view.findViewById(R.id.userdob);
        selectRingTone = (ImageButton)view.findViewById(R.id.selectRingTone);
        setUserProfileData();
        //setUserTextStatus();
        //setUserAudioStatus();
        selectRingTone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //chooseAndSetRingTone();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment newFragment = SelectsongFragment.newInstance("party","b");
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack if needed
                transaction.replace(R.id.entireProfile, newFragment);
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();

            }
        });


        return view;
    }

    private void chooseAndSetRingTone(){
        final Dialog fbDialogue = new Dialog(view.getContext(), android.R.style.Theme_Black);
        fbDialogue.getWindow().setTitle("Select your audio status song");
        fbDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(100, 0, 0, 0)));
        fbDialogue.setContentView(R.layout.fragment_selectsong);
        getSongsAndFillDialogue();
        fbDialogue.setCancelable(true);
        fbDialogue.show();
    }
    private void getSongsAndFillDialogue(){
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout container = (LinearLayout) tempView.findViewById(R.id.eachRingToneSong);
        TextView tv = new TextView(tempView.getContext());
        tv.setText("Hi hello");
        Button b = new Button(tempView.getContext());
        b.setText("abcde");
        container.addView(tv);
        container.addView(b);

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
