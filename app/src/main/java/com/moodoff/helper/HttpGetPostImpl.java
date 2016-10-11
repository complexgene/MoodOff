package com.moodoff.helper;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by snaskar on 10/12/2016.
 */

public class HttpGetPostImpl implements HttpGetPostInterface {
    private String serverURL = HttpGetPostInterface.serverURL;

    public void postDataToServer(final String Url, HashMap<String,String> extraParameters){
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
                    //Log.e("ResponseCode",responseCode+"");

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

    public String getNotificationsFromServer(String phoneNumber){
                HttpURLConnection urlConnection=null;
                StringBuilder response=new StringBuilder("");
                try {
                    URL url = new URL("http://192.168.2.5:5002/controller/moodoff/notifications/"+phoneNumber);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    // Now as the data would start coming asociate that with an InputStream to store it.
                    InputStream is = urlConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    int data = isr.read();
                    // Declare a string variable inside which the entire data would be stored.
                    response = new StringBuilder("");
                    // Until we don't encounter the end of data keep reading the data, end is marked by -1
                    while(data!=-1){
                        response.append((char)data);
                        data = isr.read();
                    }

                }catch(Exception ee){
                    Log.e("Todayerror",Log.getStackTraceString(ee));
                    ee.printStackTrace();
                }
                // Close the Http Connection that you started in finally.
                finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
        return response.toString();
    }
}
