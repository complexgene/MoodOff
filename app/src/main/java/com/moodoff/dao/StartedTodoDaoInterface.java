package com.moodoff.dao;

import android.content.Context;

import java.util.LinkedHashMap;

/**
 * Created by Santanu on 3/11/2017.
 */

public interface StartedTodoDaoInterface {
    void dropAllInternalTables();
    void createTable(String tableName,LinkedHashMap<String,String> columnNameAndDataType);
}
