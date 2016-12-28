package com.moodoff;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import java.util.ArrayList;

public class AllTabs extends AppCompatActivity implements ViewPager.OnPageChangeListener,SelectsongFragment.OnFragmentInteractionListener,Profile.OnFragmentInteractionListener,SingSong.OnFragmentInteractionListener,Moods.OnFragmentInteractionListener,GenericMood.OnFragmentInteractionListener,NotificationFragment.OnFragmentInteractionListener,KaraokeFragment.OnFragmentInteractionListener,ContactsFragment.OnFragmentInteractionListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    //private ViewPager mViewPager;
    public static ViewPager mViewPager;
    public static ArrayList<String> tabNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tabs);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        tabNames.clear();tabNames.add("Moods");tabNames.add("ACTIVITY");tabNames.add("PROFILE");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new NotificationFragment());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Log.e("ALLTABS","I am called again..");
        mViewPager.setCurrentItem(Start.switchToTab);
        mViewPager.getAdapter().notifyDataSetChanged();

        //Request all the dangerous permissions over here

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_tabs, menu);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // Just to control the nullPointerException
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_all_tabs, container, false);

            Log.e("ALLTABS","I am called again..");

            /*mViewPager.setCurrentItem(Start.switchToTab);
            mViewPager.getAdapter().notifyDataSetChanged();*/

            return rootView;
        }
    }
    private boolean doorClosed = true;
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter{

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if(position == 0)return Moods.newInstance("a","b");
            else if(position == 1)return NotificationFragment.newInstance("x","y");
            //else if(position == 2)return SingSong.newInstance("p","q");
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
    public void onBackPressed() {
        super.onBackPressed();
        Log.e("Start","BACK Pressed..");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
