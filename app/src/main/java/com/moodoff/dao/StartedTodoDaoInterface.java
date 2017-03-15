package com.moodoff.dao;

import android.content.Context;

import com.moodoff.helper.DBHelper;
import com.moodoff.model.User;

import java.util.LinkedHashMap;

/**
 * Created by Santanu on 3/11/2017.
 */

public interface StartedTodoDaoInterface {
    void dropAllInternalTables();
    void createTable(String tableName,LinkedHashMap<String,String> columnNameAndDataType);
    boolean checkEntryOfPlaylistInInternalTableAndReadIfRequired(DBHelper dbOpr, String todaysDate, boolean moodsAndSongsFetchNotComplete);
    LinkedHashMap<String, String> getContactsTableData(LinkedHashMap<String,String> allContacts, DBHelper dbOpr, User singleTonUserObject);
}
