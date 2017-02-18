package com.moodoff.helper;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by snaskar on 12/21/2016.
 */

public class ContactsManager {
    //<KEY:Number,VALUE:Name>
    public static LinkedHashMap<String,String> allReadContacts = new LinkedHashMap<>();
    public static ArrayList<String> allReadContactsFromDBServer = new ArrayList<>();
    public static ArrayList<String> friendsWhoUsesApp = new ArrayList<>();
    public static ArrayList<String> friendsWhoDoesntUseApp = new ArrayList<>();
    public static int countFriendsUsingApp = 0;
    public static int countFriendsNotUsingApp = 0;

}
