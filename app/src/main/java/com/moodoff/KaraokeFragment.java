package com.moodoff;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KaraokeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KaraokeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KaraokeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public KaraokeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KaraokeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KaraokeFragment newInstance(String param1, String param2) {
        KaraokeFragment fragment = new KaraokeFragment();
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
    private MediaRecorder myRec;
    private String outputFile;
    Button rec,play,stop,newRecord;
    MediaPlayer mpp;
    MediaPlayer mp;
    Handler myHandler;
    // Added code
    SeekBar seekBarKaraoke;

    Runnable updateSongTime = new Runnable() {
        public void run() {
            int startTime = mpp.getCurrentPosition();
            Log.e("cp",mpp.getCurrentPosition()+"");
            seekBarKaraoke.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_karaoke, container, false);

        rec = (Button) view.findViewById(R.id.record);
        play = (Button) view.findViewById(R.id.play);
        stop = (Button) view.findViewById(R.id.stop);
        newRecord = (Button) view.findViewById(R.id.newRecord);
        seekBarKaraoke = (SeekBar)view.findViewById(R.id.seekBar_karaoke);




        outputFile = Environment.getExternalStorageDirectory().getAbsoluteFile().toString()+File.separator+"/moodoff"+File.separator+"sam.mp3";

        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("What We Suggest")
                            .setMessage("Set the volume level at 50% for better recording.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        myRec = new MediaRecorder();
                                        myRec.setAudioSource(MediaRecorder.AudioSource.MIC);
                                        myRec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                                        myRec.setAudioEncoder(MediaRecorder.OutputFormat.MPEG_4);
                                        myRec.setOutputFile(outputFile);
                                        mpp = new MediaPlayer();
                                        AssetFileDescriptor afd = view.getContext().getAssets().openFd("BDNZ.mp3");
                                        mpp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                                        mpp.prepare();
                                        myRec.prepare();
                                        afd.close();
                                        mpp.start();
                                        myRec.start();
                                        Log.e("mediaPlayer2","kkk");
                                                        if(mpp!=null){
                                                            Log.e("cp",mpp.getCurrentPosition()+"");
                                                            seekBarKaraoke.setProgress(mpp.getCurrentPosition());
                                                            myHandler.postDelayed(updateSongTime,100);
                                                    }

                                    } catch (Exception ee) {
                                        ee.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();

                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                rec.setEnabled(false);
                stop.setEnabled(true);
                Toast.makeText(view.getContext(), "Recording Started", Toast.LENGTH_LONG).show();
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mpp.stop();
                    myRec.stop();
                    myRec.release();
                    myRec = null;
                }
                catch(Exception ee){ee.printStackTrace();}
                stop.setEnabled(false);
                play.setEnabled(true);
                Toast.makeText(view.getContext(), "Audio Recorded Successfully",Toast.LENGTH_LONG).show();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(outputFile);
                    mp.prepare();
                    mp.start();
                }
                catch(Exception ee){ee.printStackTrace();}
                stop.setEnabled(false);
                play.setEnabled(true);
                Toast.makeText(view.getContext(), "Playing Audio", Toast.LENGTH_LONG).show();
            }
        });

        newRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp!=null)mp.stop();
                play.setEnabled(false);
                //MediaPlayer mp = new MediaPlayer();
                try {
                    myRec = new MediaRecorder();
                    myRec.setAudioSource(MediaRecorder.AudioSource.MIC);
                    myRec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    myRec.setAudioEncoder(MediaRecorder.OutputFormat.MPEG_4);
                    myRec.setOutputFile(outputFile);
                    mpp = new MediaPlayer();
                    AssetFileDescriptor afd = view.getContext().getAssets().openFd("BDNZ.mp3");
                    mpp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mpp.prepare();
                    myRec.prepare();
                    afd.close();
                    mpp.start();
                    myRec.start();
                    stop.setEnabled(true);
                }
                catch(Exception ee){ee.printStackTrace();}

                Toast.makeText(view.getContext(), "Playing Audio", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    // ABCD

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
