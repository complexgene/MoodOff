package com.moodoff.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.moodoff.R;
import com.moodoff.helper.LoggerBaba;
import com.moodoff.helper.ServerManager;
import com.moodoff.model.User;

import java.util.ArrayList;

public class AllTabs extends AppCompatActivity implements SelectsongFragment.OnFragmentInteractionListener,Profile.OnFragmentInteractionListener,Moods.OnFragmentInteractionListener,GenericMood.OnFragmentInteractionListener,NotificationFragment.OnFragmentInteractionListener,ContactsFragment.OnFragmentInteractionListener{

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static ViewPager mViewPager;
    public static ArrayList<String> tabNames = new ArrayList<>();
    private  TabLayout tabLayout;
    User singleTonUser = User.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tabs);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        tabNames.clear();tabNames.add("MOODS");tabNames.add("ACTIVITY");tabNames.add("PROFILE");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new NotificationFragment());

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Log.e("ALLTABS","I am called again..");
        mViewPager.setCurrentItem(Start.switchToTab);
        mViewPager.getAdapter().notifyDataSetChanged();

        mViewPager.setOffscreenPageLimit(2);
        //setUpTabIcons();
    }

    /*private int[] tabIcons = {
            R.drawable.changemood,
            R.drawable.btn_dedicate,
            R.drawable.tab_profile
    };
    private void setUpTabIcons(){
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_tabs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_all_tabs, container, false);

            Log.e("ALLTABS","I am called again..");

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter{

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if(position == 0)return Moods.newInstance("a","b");
            else if(position == 1)return NotificationFragment.newInstance("x","y");
            else if(position == 2)return ContactsFragment.newInstance("p","q");
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return tabNames.get(0);
                case 1: {
                    return tabNames.get(1);
                }
                case 2: {
                    return tabNames.get(2);
                }
            }
            return null;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // Just to control the nullPointerException
    }
    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoggerBaba.printMsg("AllTabs", "In onDestroy() of AllTabs activity trying to exit from the live Mood..");
        GenericMood.AmStillHere = false;
        GenericMood.releaseMediaPlayerObject();
        NotificationFragment.releaseMediaPlayerObject(NotificationFragment.mp);
        Profile.releaseMediaPlayerObject(Profile.mediaPlayer);
    }
}
