package com.moodoff;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Moods.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Moods#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Moods extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Local variables
    MoodsListAdapter moodsListAdapter;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Moods() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Moods.
     */
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
    Button btnRomantic,btnParty,btnOnTour,btnInLove,btnDance,btnMissU;
    ExpandableListView moodlist;
    RelativeLayout layout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_moods, container,
                false);
        btnRomantic = (Button)rootView.findViewById(R.id.btn_romantic);
        btnParty = (Button)rootView.findViewById(R.id.btn_party);
        btnOnTour = (Button)rootView.findViewById(R.id.btn_ontour);
        btnInLove = (Button)rootView.findViewById(R.id.btn_inlove);
        btnDance = (Button)rootView.findViewById(R.id.btn_dance);
        btnMissU = (Button)rootView.findViewById(R.id.btn_missu);
        layout = (RelativeLayout)rootView.findViewById(R.id.relativeLayout);

        btnRomantic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new GenericMood();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack if needed
                transaction.replace(R.id.allmoods, newFragment);
                transaction.addToBackStack(null);
                putAllButtonsOff();

// Commit the transaction
                transaction.commit();
            }
        });

        moodlist = (ExpandableListView)rootView.findViewById(R.id.moodlist);
        moodsListAdapter = new MoodsListAdapter(getContext());
        moodlist.setAdapter(moodsListAdapter);

        return rootView;
    }
    public void putAllButtonsOff(){
        btnRomantic.setVisibility(View.INVISIBLE);
        btnParty.setVisibility(View.INVISIBLE);
        btnOnTour.setVisibility(View.INVISIBLE);
        btnInLove.setVisibility(View.INVISIBLE);
        btnDance.setVisibility(View.INVISIBLE);
        btnMissU.setVisibility(View.INVISIBLE);
        layout.setVisibility(View.VISIBLE);
    }
/*
    public void putmeoff(View v){
        Button b = (Button)rootView.findViewById(R.id.btn_romantic);
        b.setVisibility(View.GONE);
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
