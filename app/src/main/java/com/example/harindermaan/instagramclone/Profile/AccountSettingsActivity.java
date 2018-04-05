package com.example.harindermaan.instagramclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;


import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.harindermaan.instagramclone.Utils.FirebaseMethods;
import com.example.harindermaan.instagramclone.Utils.SectionsStatePagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;


public class AccountSettingsActivity extends AppCompatActivity
{
    private static final int ACTIVITY_NUM=4;
    private static final String TAG = "AccountSettingsActivity";
    private Context  mContext =  AccountSettingsActivity.this;
    public SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, " onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);

        mViewPager = findViewById(R.id.container);
        mRelativeLayout = findViewById(R.id.relLayout_1);


        setupBottomNavigationView();
        setupSettingsList();
        setupFragments();
        getIncomingIntent();

        //set up the back arrow for navigating to ProfileActivity
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Navigating back to 'ProfileActivity' ");
                finish();
            }//onClick
        });//setOnClickListener

    }//onCreate



    public void setViewPager(int fragmentNumber)
    {
        Log.d(TAG, "setViewPager: Navigating to Fragment # "+fragmentNumber);
        mRelativeLayout.setVisibility(View.GONE);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);

    }//setViewPager

    private void setupSettingsList()
    {
        Log.d(TAG, " setupSettingsList() -> 'Initializing Account settings List' ");
        final ListView listView = findViewById(R.id.lvaccountsettings);
        ArrayList<String> options = new ArrayList();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_1,options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "onItemClick: Navigating to fragment  # "+position);
                setViewPager(position);



//                String title = (String)listView.getItemAtPosition(position);
//                if(title.equals("Edit Profile"))
//                {
//                    Log.d(TAG, "onItemClick: Navigating to EditProfile Activity");
//                    Intent intent = new Intent(AccountSettingsActivity.this,EditProfileActivity.class);
//                    startActivity(intent);
//                }//if
//                else if(title.equals("Sign Out"))
//                {
//                    Log.d(TAG, "onItemClick: Navigating to SignOut Activity");
//                    Intent intent = new Intent(AccountSettingsActivity.this,SignOutActivity.class);
//                    startActivity(intent);
//                }//else



            }//onItemClick
        });//setOnItemClickListener

    }//setupSettingsList



    //for EditProfileFragment
    private void getIncomingIntent()
    {
        Intent intent = getIntent();

        //if there is an imageURL attached in Extra, then it is chosen from Gallery/Photo Fragment


        if(intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap)))
        {
            Log.d(TAG, "getIncomingIntent: New Incoming Image URL");

            if(intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment)))
            {
                if(intent.hasExtra(getString(R.string.selected_image)))
                {
                    //set the new profile picture from gallery
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),null,0,
                            intent.getStringExtra(getString(R.string.selected_image)),null);
                }//if
                else if(intent.hasExtra(getString(R.string.selected_bitmap)))
                {
                    //set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),null,0,null,
                            (Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }//else if

            }//if

        }//if

        if(intent.hasExtra(getString(R.string.calling_activity)))
        {
            Log.d(TAG, "getIncomingIntent: Recieved Incoming Intent from "+getString(R.string.profile_activity));
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }//if
    }//getIncomingIntent

    private void setupFragments()
    {
        Log.d(TAG, "setupFragments: Adding Fragments to Adapter ");
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(),getString(R.string.edit_profile_fragment));
        pagerAdapter.addFragment(new SignOutFragment(),getString(R.string.sign_out_fragment));
    }//setupFragments



    //Responsible for Bottom Navigation View Setup
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: setting Up Bottom Navigation View !");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }//setupBottomNavigationView



}
