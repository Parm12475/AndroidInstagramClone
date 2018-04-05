package com.example.harindermaan.instagramclone.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.harindermaan.instagramclone.Utils.Permissions;
import com.example.harindermaan.instagramclone.Utils.SectionsPagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;



public class ShareActivity extends AppCompatActivity
{
    private static final String TAG = "ShareActivity";
    private static final int ACTIVITY_NUM=2;
    private static  final  int VERIFY_PERMISSIONS_REQUEST = 1;

    private Context mContext=ShareActivity.this;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: Started !");

        if(checkPermissionsArray(Permissions.PERMISSIONS))
        {
            setupViewPager();
        }//if
        else
        {
            verifyPermissions(Permissions.PERMISSIONS);

        }//else



        //setupBottomNavigationView();
    }//onCreate

    private boolean checkPermissionsArray(String[] permissions)
    {
        Log.d(TAG, "checkPermissionsArray: Checking Permissions Array !!");

        for(int i=0;i < permissions.length;i++)
        {
            String check = permissions[i];
            if(!checkPermissions(check))
            {
                return false;
            }//if
        }//for

          return true;
    }//checkPermissionsArray





    /*
     * Returns Current tab Number
     * 0 = Gallery Fragment
     * 1 = Photo Fragment
      *
      * */
    public int getCurrentTabNumber()
    {
        return mViewPager.getCurrentItem();
    }//getCurrentTabNumber


    private void setupViewPager()
    {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(adapter);


        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));
    }//setupViewPager


    public int getTask()
    {
        //Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }//getTasks


    public boolean checkPermissions(String permission)
    {
        Log.d(TAG, "checkPermissions: Checking permission : "+permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this,permission);

        if(permissionRequest != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "checkPermissions: Permission was not granted for "+permission);
            return false;
        }//if
        else
        {
            Log.d(TAG, "checkPermissions: Permission was granted for "+permission);
            return true;
        }//else

    }//checkPermissions

    public void verifyPermissions(String[] permissions)
    {
        Log.d(TAG, "verifyPermissions: Verifying Permissions !!");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }//verifyPermissions

    //Bottom Navigation View Setup
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
}//SearchActivity
