package com.moodoff;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.moodoff.helper.ExpressionsImpl;
import com.moodoff.helper.HttpGetPostInterface;
import com.moodoff.model.UserDetails;

import java.io.DataOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GenericMood.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GenericMood#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenericMood extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String serverURL = HttpGetPostInterface.serverURL;

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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }
    View view;
    Bitmap bitmap;
    String folder;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_generic_mood, container, false);
        moodpageBG = (ImageView) view.findViewById(R.id.photoView);
        folder=Environment.getExternalStorageDirectory().getAbsolutePath()+"/moodoff/mogambo.jpg";
        bitmap = BitmapFactory.decodeFile(folder);
        moodpageBG.setImageBitmap(bitmap);

        FloatingActionButton cameraButton = (FloatingActionButton)view.findViewById(R.id.btn_camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File pictureDirectory = new File(Environment.getExternalStorageDirectory().getAbsoluteFile().toString()+"/moodoff/");
                pictureDirectory.mkdirs();
                String pictureName = getPictureName();
                File imageFile = new File(pictureDirectory,pictureName);
                // Directory creation complete
                Uri picture = Uri.fromFile(imageFile);
                // We have to create an URI resource because putExtra expects URI resource as the second argument.
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,picture);
                // Start the Activity Now
                startActivityForResult(cameraIntent,0);
            }
        });

        final FloatingActionButton loveButton = (FloatingActionButton)view.findViewById(R.id.btn_love);
        loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUser = UserDetails.getPhoneNumber();
                String currentSong = "turu_turu"+new Random().nextInt(1000)+".mp3";
                char type = '0';
                final String Url = "notifications/"+currentUser+"/"+currentSong+"/"+type;
                loveButton.setImageResource(R.drawable.love_s);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection urlConnection=null;
                        try {
                            // Proide the URL fto which you would fire a post
                            URL url = new URL(serverURL+"/"+Url);
                            urlConnection = (HttpURLConnection) url.openConnection();

                            // Method is POSt, need to specify that
                            //urlConnection.setDoOutput(false);
                            //urlConnection.setRequestMethod("POST");
                            //urlConnection.setRequestProperty("User-Agent","Mozilla/5.0");
                            //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

                            // Send post request
                            urlConnection.setDoOutput(true);

                            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                            //wr.writeBytes(urlParameters);  //FOR EXTRA DATA
                            wr.flush();
                            wr.close();

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
                            Log.e("ResponseCode",responseCode+"");

                        }catch(Exception ee){
                            Log.e("Todayerror",Log.getStackTraceString(ee));
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
        });

        return view;
    }

    ImageView moodpageBG;
    static String getPictureName(){
        return "mogambo.jpg";
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        folder=Environment.getExternalStorageDirectory().getAbsolutePath()+"/moodoff/mogambo.jpg";
        bitmap = BitmapFactory.decodeFile(folder);
        moodpageBG.setImageBitmap(bitmap);

        //iv.setImageBitmap(bitmap);

        // Either you can take the captured image as biitmap or you can save it to external directory.
        // Now choose what you want to do.
        // I wanted to save the image in the External HDD so i wrote the above code.


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
