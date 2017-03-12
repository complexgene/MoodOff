package com.moodoff.ui;

import android.app.NotificationManager;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
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
import android.widget.RemoteViews;

import com.moodoff.R;
import com.moodoff.helper.AppData;
import com.moodoff.helper.Messenger;
import com.moodoff.helper.ServerManager;
import com.moodoff.model.User;

import java.util.ArrayList;

public class AllTabs extends AppCompatActivity implements SelectsongFragment.OnFragmentInteractionListener,Profile.OnFragmentInteractionListener,SingSong.OnFragmentInteractionListener,Moods.OnFragmentInteractionListener,GenericMood.OnFragmentInteractionListener,NotificationFragment.OnFragmentInteractionListener,KaraokeFragment.OnFragmentInteractionListener,ContactsFragment.OnFragmentInteractionListener{

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public static ViewPager mViewPager;
    public static ArrayList<String> tabNames = new ArrayList<>();
    private  TabLayout tabLayout;

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
        //Request all the dangerous permissions over here

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
    private int[] tabIcons = {
            R.drawable.changemood,
            R.drawable.btn_dedicate,
            R.drawable.tab_profile
    };
    private void setUpTabIcons(){
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    @Override
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
    }

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

            /*FragmentTransaction transaction = getFragmentManager().beginTransaction();
            Fragment newFragment = Moods.newInstance("replacedA","b");
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack if needed
            //transaction.replace(R.id.allmoods, newFragment);
            transaction.replace(R.id.fragment, newFragment);
            transaction.addToBackStack("mainA");*/
            //transaction.commitAllowingStateLoss();

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
        try {
            Log.e("AllTabs_onBackPressed", "onBackPressed:"+AppData.noOfTimesBackPressed);
            if ((AppData.noOfTimesBackPressed == 1&&mViewPager.getCurrentItem()!=1) || (AppData.noOfTimesBackPressed==0 && mViewPager.getCurrentItem() == 1)) {
                Messenger.print(getApplicationContext(), "Press Back again to Exit");
                if(mViewPager.getCurrentItem()==1)AppData.noOfTimesBackPressed=2;
            }
            else{
                super.onBackPressed();
            }
        }catch (Exception ee){return;}
    }

    User userData = User.getInstance();

    @Override
    protected void onDestroy() {
        Log.e("AllTabs_onDestroy","onDestroy");
        super.onDestroy();
        /*if(AppData.noOfTimesBackPressed==1 && mViewPager.getCurrentItem()==1){
            new ServerManager().exitLiveMood(User.getPhoneNumber());
            GenericMood.releaseMediaPlayerObject();
            NotificationFragment.releaseMediaPlayerObject(NotificationFragment.mp);
            Profile.releaseMediaPlayerObject(Profile.mediaPlayer);
        }
        else*/
        if(AppData.noOfTimesBackPressed==2){
            new ServerManager().exitLiveMood(userData.getUserMobileNumber());
            GenericMood.releaseMediaPlayerObject();
            NotificationFragment.releaseMediaPlayerObject(NotificationFragment.mp);
            Profile.releaseMediaPlayerObject(Profile.mediaPlayer);
            /*AppData.noOfTimesBackPressed=0;
            String currentPlayingSong = GenericMood.currentSong;
            if(currentPlayingSong != null) {
                manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                remoteViewBig = new RemoteViews(getPackageName(), R.layout.statusbar_expanded);
                remoteViewSmall = new RemoteViews(getPackageName(), R.layout.statusbar_minimized);
                remoteViewBig.setImageViewResource(R.id.albumimage,R.drawable.man);
                remoteViewSmall.setImageViewResource(R.id.albumimage,R.drawable.man);
                currentPlayingSong = currentPlayingSong.replaceAll("_"," ").replace(".mp3","");
                remoteViewBig.setTextViewText(R.id.songname,"Mood: "+Character.toUpperCase(GenericMood.currentMood.charAt(0))+GenericMood.currentMood.substring(1));
                remoteViewBig.setTextViewText(R.id.songdetails,currentPlayingSong+" - Arijit Singh");
                remoteViewSmall.setTextViewText(R.id.songname,currentPlayingSong);
                remoteViewSmall.setTextViewText(R.id.songdetails,currentPlayingSong+" - Arijit Singh");
                builder = new NotificationCompat.Builder(this);
                builder
                        .setSmallIcon(R.drawable.btn_dedicate)
                        .setAutoCancel(true)
                        .setContentTitle(currentPlayingSong)
                        .setContentText(currentPlayingSong+" - Arijit Singh")
                        .setContent(remoteViewSmall)
                        .setCustomBigContentView(remoteViewBig)
                        .setColor(Color.rgb(255, 0, 0));
                manager.notify(0, builder.build());
            }*/
        }

    }
    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private int notification_Id;
    private RemoteViews remoteViewBig,remoteViewSmall;

}
