package com.moodoff;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.moodoff.helper.AppData;
import com.moodoff.helper.DBInternal;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.model.UserDetails;

import java.lang.reflect.Field;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Moods.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Moods#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Moods extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    MoodsListAdapter moodsListAdapter;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Moods() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Moods newInstance(String param1, String param2) {
        Moods fragment = new Moods();
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
        //Intent ii = new Intent(Moods.this, GenericMood.class);

    }
    View rootView;
    Button btnRomantic,btnParty,btnOnTour,btnInLove,btnDance,btnSad,btnWorkOut,btnFriends,btnMissU,btnOldEra,btnCrazy,btnCalm;
    RelativeLayout layout;
    ViewGroup mainContainer;
    LayoutInflater mainInflater;
    static boolean notitifcationClicked = false;
    static String moodToEnter="";
    static FragmentManager fm;
    static Fragment fragment;
    static Activity activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainContainer = container;
        mainInflater = inflater;
        fm = getFragmentManager();
        activity = getActivity();
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_moods, container,
                false);
        //rootView.setBackgroundColor(Color.WHITE);
        btnOnTour = (Button)rootView.findViewById(R.id.btn_ontour);
        btnParty = (Button)rootView.findViewById(R.id.btn_party);
        btnRomantic = (Button)rootView.findViewById(R.id.btn_romantic);
        btnSad = (Button)rootView.findViewById(R.id.btn_sad);
        btnOldEra = (Button)rootView.findViewById(R.id.btn_old_era);
        btnWorkOut = (Button)rootView.findViewById(R.id.btn_workout);
        btnDance = (Button)rootView.findViewById(R.id.btn_dance);
        btnCalm = (Button)rootView.findViewById(R.id.btn_calm);
        btnCrazy = (Button)rootView.findViewById(R.id.btn_crazy);
        btnFriends = (Button)rootView.findViewById(R.id.btn_friends);
        btnMissU = (Button)rootView.findViewById(R.id.btn_missu);
        btnInLove = (Button)rootView.findViewById(R.id.btn_inlove);

        showAllButtons();
        if(notitifcationClicked)
            cameFromNotification();

        btnOnTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("on_tour",1);
            }
        });
        btnParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("party",1);
            }
        });
        btnRomantic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("romantic",1);
            }
        });
        btnSad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("sad",1);
            }
        });
        btnOldEra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("old_era",1);
            }
        });
        btnWorkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("work_out",1);
            }
        });
        btnDance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openInternalDBActivity = new Intent(getActivity(), DBInternal.class);
                startActivity(openInternalDBActivity);
            }
        });
        btnCalm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("calm",1);
            }
        });
        btnCrazy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("crazy",1);
            }
        });
        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("friends",1);
            }
        });
        btnMissU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("missu",1);
            }
        });
        btnInLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startParticularMood("in_love",1);
            }
        });

        return rootView;
    }

    private void cameFromNotification(){
       startParticularMood(moodToEnter,0);
    }

    UserDetails userData = UserDetails.getInstance();

    private void startParticularMood(String moodType, int status){
        /*if(status == 0)fm=AllTabs.moodsFragmentManager;
        else */
        fm=getFragmentManager();
        if(ContactsFragment.openedAProfile){
            Log.e("MOODS",super.getId()+"");
            ContactsFragment.openedAProfile = false;
        }
        ServerManager serverManager = new ServerManager();
        serverManager.setLiveMood(userData.getMobileNumber(),moodType);
        if(AppData.allMoodPlayList.containsKey(moodType)) {
            FragmentTransaction transaction = fm.beginTransaction();
            Fragment newFragment=GenericMood.newInstance(moodType, "b");
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack if needed
            transaction.replace(R.id.allmoods, newFragment);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
            putAllButtonsOff();
        /*Intent ii = new Intent(getContext(),GenericSelectedMood.class);
        Bundle bundle = new Bundle();
        bundle.putString("selectedmood",moodType);
        startActivity(ii);*/
        }
        else{
            Messenger.print(getContext(),"Sorry!! There is no songs yet in this mood..");
        }
    }

    public void putAllButtonsOff(){
        btnRomantic.setVisibility(View.INVISIBLE);
        btnParty.setVisibility(View.INVISIBLE);
        btnOnTour.setVisibility(View.INVISIBLE);
        btnInLove.setVisibility(View.INVISIBLE);
        btnDance.setVisibility(View.INVISIBLE);
        btnSad.setVisibility(View.INVISIBLE);
//        layout.setVisibility(View.VISIBLE);
    }
    public void showAllButtons(){
        btnRomantic.setVisibility(View.VISIBLE);
        btnParty.setVisibility(View.VISIBLE);
        btnOnTour.setVisibility(View.VISIBLE);
        btnInLove.setVisibility(View.VISIBLE);
        btnDance.setVisibility(View.VISIBLE);
        btnSad.setVisibility(View.VISIBLE);
        //layout.setVisibility(View.VISIBLE);
    }
/*
    public void putmeoff(View v){
        Button b = (Button)rootView.findViewById(R.id.btn_romantic);
        b.setVisibility(View.GONE);
    }*/

    @Override
    public void onResume() {
        if(rootView != null)
            showAllButtons();
        super.onResume();
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
        try {
                super.onDetach();
                mListener = null;

        }catch (Exception ee){}

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
