package com.example.harindermaan.instagramclone.Main;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.harindermaan.instagramclone.Login.LoginActivity;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.harindermaan.instagramclone.Utils.SectionsPagerAdapter;
import com.example.harindermaan.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 0;
    private Context mContext=MainActivity.this;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate()  Starting...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupFirebaseAuth();
        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();

    }//onCreate

    public void initImageLoader()
    {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }//initImageLoader

   // Responsible for adding 3 tabs : Camera , Home , Messages

    private void setupViewPager()
    {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment());
        adapter.addFragment(new MainFragment());
        adapter.addFragment(new MessagesFragment());
        ViewPager viewPager = findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_action_name);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow);

    }//setupViewPager


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



    /*----------------------------------------------Firebase------------------------------------------------------------ */

    //set up the firebase auth object
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth()-> Setting Up Firebase Authentication");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = mAuth.getCurrentUser();
                Log.d(TAG, "onAuthStateChanged: CurrentUser : "+user);
                Log.d(TAG, "onAuthStateChanged: ");
                //check if user is logged in
                checkCurrentUser(user);

                if(user != null)
                {//user is signed in
                    Log.d(TAG, "onAuthStateChanged() -> Signed In Used ID : "+user.getUid());
                }//if
                else
                {//User is signed out
                    Log.d(TAG, "onAuthStateChanged() -> Signed Out");
                }//else

            }//onAuthStateChanged
        };

    }//setupFirebaseAuth

    private void checkCurrentUser(FirebaseUser user)
    {
        Log.d(TAG, "checkCurrentUser() -> Checking if user is logged in ");
        if(user != null)
          Log.d(TAG, "checkCurrentUser() -> Current UserID : "+user.getUid());
        if(user == null)
        {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }//if
    }//checkCurrentUser


    @Override
    public void onStart()
    {
        Log.d(TAG, "onStart() ");
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());

    }//onStart

    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop()");
        super.onStop();
        if(mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);

    }//onStart



}//MainActivity
