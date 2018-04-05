package com.example.harindermaan.instagramclone.Profile;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.harindermaan.instagramclone.Models.Photo;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.harindermaan.instagramclone.Utils.GridImageAdapter;
import com.example.harindermaan.instagramclone.Utils.UniversalImageLoader;
import com.example.harindermaan.instagramclone.Utils.ViewCommentsFragment;
import com.example.harindermaan.instagramclone.Utils.ViewPostFragment;
import com.example.harindermaan.instagramclone.Utils.ViewProfileFragment;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;


public class ProfileActivity extends AppCompatActivity implements
        ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener
{
    private static final String TAG = "ProfileActivity";
    private Context mContext=ProfileActivity.this;

    @Override
    public void OnCommentThreadSelectedListener(Photo photo)
    {
        Log.d(TAG, "OnCommentThreadSelectedListener: Selected a Comment Thread");
        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        //Log.d(TAG, "OnCommentThreadSelectedListener: Photo: "+photo);
        args.putParcelable(getString(R.string.photo),photo);
        fragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }//OnCommentThreadSelectedListener

    @Override
    public void onGridImageSelected(Photo photo, int activityNumber)
    {
        Log.d(TAG, "onGridImageSelected: Selected an image from gridview "+photo.toString());

        ViewPostFragment viewPostFragment = new ViewPostFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo),photo);
        args.putInt(getString(R.string.activity_number),activityNumber);
        viewPostFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container,viewPostFragment);
        transaction.addToBackStack(getString(R.string.view_post_fragment));
        transaction.commit();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate()");

        init();

    }//onCreate

    private void init()
    {
        Log.d(TAG, "init: Inflating : "+getString(R.string.profile_fragment));


        Intent intent = getIntent();

        if(intent.hasExtra(getString(R.string.calling_activity)))
        {
            Log.d(TAG, "init: Searching for user object attached as intent extra");

           if(intent.hasExtra(getString(R.string.intent_user)))
           {
               Log.d(TAG, "init: Inflating View Profile");
               ViewProfileFragment fragment = new ViewProfileFragment();
               Bundle args = new Bundle();

               args.putParcelable(getString(R.string.intent_user),
                       intent.getParcelableExtra(getString(R.string.intent_user)));
               fragment.setArguments(args);

               android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
               transaction.replace(R.id.container,fragment);
               transaction.addToBackStack(getString(R.string.view_profile_fragment));
               transaction.commit();


           }//if
            else
           {
               Toast.makeText(mContext, "Something Went Wrong", Toast.LENGTH_SHORT).show();
           }//else

        }//if
        else
        {
            Log.d(TAG, "init: Inflating Profile");
            ProfileFragment fragment = new ProfileFragment();
            android.support.v4.app.FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container,fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();
        }//else




    }//init



}//SearchActivity
