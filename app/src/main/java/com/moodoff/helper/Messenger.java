package com.moodoff.helper;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by snaskar on 12/31/2016.
 */

public class Messenger {
    public static void print(Context ctx,String msg){
        Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show();
    }
    public static void printCenter(Context ctx,String msg){
        Toast toast = Toast.makeText(ctx,msg,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }
}
