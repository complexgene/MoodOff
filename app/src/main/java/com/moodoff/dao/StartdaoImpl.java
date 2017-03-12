package com.moodoff.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.moodoff.helper.AppData;
import com.moodoff.helper.ContactsManager;
import com.moodoff.helper.DBHelper;
import com.moodoff.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/*
 * Created by Santanu on 3/11/2017.
 */

public class StartdaoImpl implements StartdaoInterface {
    public boolean checkEntryOfPlaylistInInternalTableAndReadIfRequired(DBHelper dbOpr, String todaysDate, boolean moodsAndSongsFetchNotComplete) {
        SQLiteDatabase readData = dbOpr.getReadableDatabase();
        Cursor resultSet = readData.rawQuery("Select * from playlist where date='" + todaysDate + "'", null);
        // If today's playlist file is not downloaded yet
        if (resultSet.getCount() == 0) return false;
        // File downloaded
        HashMap<String, ArrayList<String>> allSongs = new HashMap<>();
        resultSet.moveToFirst();
        Log.e("Start_SongRead_IntDB", resultSet.getCount() + " no of rows..");
        while (!resultSet.isAfterLast()) {
            String moodType = resultSet.getString(1);
            String songName = resultSet.getString(2);
            String artistName = resultSet.getString(3);
            String movieOrAlbumname = resultSet.getString(3);
            if (allSongs.containsKey(moodType)) {
                allSongs.get(moodType).add(songName);
            } else {
                ArrayList<String> songs = new ArrayList<>();
                songs.add(songName);
                allSongs.put(moodType, songs);
            }
            resultSet.moveToNext();
        }
        AppData.allMoodPlayList = allSongs;
        return true;
    }

    public LinkedHashMap<String,String> getContactsTableData(LinkedHashMap<String,String> allContacts, DBHelper dbOpr, User singleTonUserObject) {
        SQLiteDatabase mydatabase = dbOpr.getWritableDatabase();
        try {
            Log.e("Start_CONTACTS_DATAB4", ContactsManager.friendsWhoUsesApp + " \n" + ContactsManager.friendsWhoDoesntUseApp);
            int countOfNonAppUsers = 0, countOfAppUsers = 0;
            Cursor resultSet = mydatabase.rawQuery("Select * from allcontacts order by name", null);
            resultSet.moveToFirst();
            while (!resultSet.isAfterLast()) {
                String phone_no = resultSet.getString(0);
                String name = resultSet.getString(1).replaceAll("'", "\'");
                int appUsingStatus = resultSet.getInt(2);
                if (appUsingStatus == 1) {
                    countOfAppUsers++;
                    if (!phone_no.equals(singleTonUserObject.getUserMobileNumber()))
                        ContactsManager.friendsWhoUsesApp.add(phone_no);
                } else {
                    countOfNonAppUsers++;
                    ContactsManager.friendsWhoDoesntUseApp.add(phone_no);
                }
                ContactsManager.allReadContacts.put(phone_no, name);
                //allContacts.put(phone_no,name);
                resultSet.moveToNext();
                //ContactsManager.allReadContacts = allReadContacts;
            }
            allContacts = ContactsManager.allReadContacts;
            ContactsManager.countFriendsUsingApp = countOfAppUsers;
            ContactsManager.countFriendsNotUsingApp = countOfNonAppUsers;
            Log.e("Start_CONTACTS_DATAA4", ContactsManager.friendsWhoUsesApp + " \n" + ContactsManager.friendsWhoDoesntUseApp);
            Log.e("Start_COUNTS_USERS", "[Using:" + ContactsManager.countFriendsUsingApp + "] [NotUsing:" + ContactsManager.countFriendsNotUsingApp + "]");
            mydatabase.close();
        } catch (Exception ee) {
            Log.e("StartFragment_TBLErr", ee.getMessage());
            ee.fillInStackTrace();
        }
        mydatabase.close();
        return allContacts;
    }
}
