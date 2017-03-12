package com.moodoff.dao;

import com.moodoff.helper.DBHelper;
import com.moodoff.model.User;

import java.util.LinkedHashMap;

/**
 * Created by Santanu on 3/11/2017.
 */

public interface StartdaoInterface {
    boolean checkEntryOfPlaylistInInternalTableAndReadIfRequired(DBHelper dbOpr, String todaysDate, boolean moodsAndSongsFetchNotComplete);
    LinkedHashMap<String, String> getContactsTableData(LinkedHashMap<String,String> allContacts, DBHelper dbOpr, User singleTonUserObject);
}
