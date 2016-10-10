package com.moodoff;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.moodoff.helper.StoreRetrieveDataImpl;
import com.moodoff.helper.StoreRetrieveDataInterface;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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

    View view;
    TextView allNotificationsTextView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);

        fetchNotifications();

        return view;
    }

    public void fetchNotifications(){
        // Read the mobile number of the current user from stored file
        StoreRetrieveDataInterface userData = new StoreRetrieveDataImpl("UserData.txt");
        userData.beginReadTransaction();
        final String userMobileNumber = userData.getValueFor("user");
        userData.endReadTransaction();
        //if(userMobileNumber=="")userMobileNumber="9620332800";
        // FETCHING NOTIFICATIONS FROM THE SERVER IN JSON FORMAT.
        // Let suppose i want to populate a textview on the screen whose name is allNotitifactions
        // Remember that i would get the response in json format finally in the variable response,
        // which can be parsed for retrievng the actual values.
        allNotificationsTextView = (TextView)view.findViewById(R.id.getresponse);
        allNotificationsTextView.setText("");
        // Start a separate thread for Http Connection
        new Thread(new Runnable() {
            HttpURLConnection urlConnection=null;
            @Override
            public void run() {
                try {
                    // Proide the URL from which you would get the JSON response
                    URL url = new URL("http://192.168.2.5:5002/controller/moodoff/notifications/"+userMobileNumber);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    // Now as the data would start coming asociate that with an InputStream to store it.
                    InputStream is = urlConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    int data = isr.read();
                    // Declare a string variable inside which the entire data would be stored.
                    final StringBuilder response = new StringBuilder("");
                    // Until we don't encounter the end of data keep reading the data, end is marked by -1
                    while(data!=-1){
                        response.append((char)data);
                        data = isr.read();
                    }

                    // When you will like to print the data on any UI object you have to use the thread that is asscoiated
                    // with the UI, not the current new thread that you have started.
                    // UI thread can be accesed in this way.
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<String> allNotifications = ParseNotificationData.getNotification(response.toString());
                            for(String eachNotification:allNotifications) {
                                allNotificationsTextView.setText(allNotificationsTextView.getText() + eachNotification + "\n----------------------------------------------------------------------------------------\n");
                            }

                        }
                    });
                    // If you want to see the output in the console uncomment the next line.
                    //  Log.i("TAG","Response:"+response.toString());
                }catch(Exception ee){
                    ee.printStackTrace();
                }
                // Close the Http Connection that you started in finally.
                finally {
                    if(urlConnection!=null)
                        urlConnection.disconnect();
                }
            }
        }).start();
        // This is the entire code that would give you the json response inside the variable response.
        // Remember that response is a StringBuilder variable type, its little different from String variable
        // How to get a string representation of the StringBuilder variable then? Just use "VARIABLE_NAME.toString()"

        // When the server is ON, you can represent the above URL with this ti get the actual notifications for the number 9681578989.
        //URL url = new URL("http://192.168.2.5:5213/controller/moodoff/notifications/9681578989");

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
