package com.example.harindermaan.instagramclone.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that stores fragments for tabs.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter
{
    private static final String TAG = "com.example.harindermaan.instagramclone.Utils.SectionsPagerAdapter";
    private final List<Fragment> mFragmentList = new ArrayList<>();
    public SectionsPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }//SectionsPagerAdapter

    @Override
    public Fragment getItem(int position)
    {
        return mFragmentList.get(position);
    }//getItem

    @Override
    public int getCount()
    {
        return mFragmentList.size();
    }//getCount

    public void addFragment(Fragment fragment)
    {
        mFragmentList.add(fragment);
    }//addFragment

}//SectionsPagerAdapter
