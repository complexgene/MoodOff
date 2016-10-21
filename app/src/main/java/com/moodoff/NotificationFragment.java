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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private String serverURL = HttpGetPostInterface.serverURL;

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
    FrameLayout mainParentLayout;
    ArrayList<String> allNotifications = new ArrayList<>();
    boolean setDoorClosed=true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        try {
            mainParentLayout = (FrameLayout) view.findViewById(R.id.containsallN);

                fetchNotifications();

            Log.e("Door","K");
                //Infinitely wait here until the arraylist gets populated.
                while(setDoorClosed);
                //Once the door is open go and create the dynamic views.
            Log.e("Door","K1");

                LinearLayout mainParent = new LinearLayout(getContext());
                mainParent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                mainParent.setOrientation(LinearLayout.VERTICAL);

                for (int i = 0; i < allNotifications.size(); i++) {
                    LinearLayout parent = new LinearLayout(getContext());
                    parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    parent.setOrientation(LinearLayout.HORIZONTAL);


                    EditText allN = new EditText(getContext());
                    allN.setText(allNotifications.get(i));
                    parent.addView(allN);

                    mainParent.addView(parent);
                }

                mainParentLayout.addView(mainParent);
        }
        catch (Exception ei){
            Log.e("Dynamic",ei.getMessage());
        }
        return view;
    }

    public void fetchNotifications(){
        // Read the mobile number of the current user from stored file
        final String userMobileNumber = UserDetails.getPhoneNumber();
        // Let suppose i want to populate a textview on the screen whose name is allNotitifactions
        // Remember that i would get the response in json format finally in the variable response,
        // which can be parsed for retrievng the actual values.
        allNotificationsTextView = (TextView)view.findViewById(R.id.getresponse);
        allNotificationsTextView.setText("");

        // Start a separate thread for Http Connection for DB entry for VOTE
        new Thread(new Runnable() {
            HttpURLConnection urlConnection=null;
            @Override
            public void run() {
                    try {
                        URL url = new URL(serverURL+"/notifications/" + userMobileNumber);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        // Now as the data would start coming asociate that with an InputStream to store it.

                        // LINES ADDED
                        Log.e("Door",urlConnection.getReadTimeout()+"");
                        if(urlConnection.getReadTimeout()==0)throw new Exception("abc");
                        // Above 2 lines

                        InputStream is = urlConnection.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        int data = isr.read();
                        // Declare a string variable inside which the entire data would be stored.
                        final StringBuilder response = new StringBuilder("");
                        // Until we don't encounter the end of data keep reading the data, end is marked by -1
                        while (data != -1) {
                            response.append((char) data);
                            data = isr.read();
                            Log.e("Door","lol");
                        }

                        // When you will like to print the data on any UI object you have to use the thread that is asscoiated
                        // with the UI, not the current new thread that you have started.
                        // UI thread can be accesed in this way.

                        //getActivity().runOnUiThread(new Runnable() {
                         //   @Override
                          //  public void run() {
                        Log.e("Door","CT");
                                allNotifications = ParseNotificationData.getNotification(response.toString());
                                // All notification retrived, now open the door for display
                                setDoorClosed=false;
                        Log.e("Door","OT");
                        //    }
                        //});
                        // If you want to see the output in the console uncomment the next line.
                        //  Log.i("TAG","Response:"+response.toString());
                    } catch (Exception ee) {
                        ee.printStackTrace();
                        setDoorClosed=false;

                    }
                    // Close the Http Connection that you started in finally.
                    finally {
                        if (urlConnection != null)
                            urlConnection.disconnect();
                        setDoorClosed=false;
                    }
                }
        }).start();
        // This is the entire code that would give you the json response inside the variable response.
        // Remember that response is a StringBuilder variable type, its little different from String variable
        // How to get a string representation of the StringBuilder variable then? Just use "VARIABLE_NAME.toString()"

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
