package com.moodoff.helper;

import java.util.ArrayList;

/**
 * Created by Saurav on 1/1/2017.
 */

public class ListOfTabs {
    public static ArrayList<String> listOfAllTabs = new ArrayList<>();

    public void addListOfTabs() {
        listOfAllTabs.add("mood");
        listOfAllTabs.add("notification");
        listOfAllTabs.add("profile");
    }

    public ArrayList<String> getListOfTabs() {
        addListOfTabs();
        return listOfAllTabs;
    }

}
