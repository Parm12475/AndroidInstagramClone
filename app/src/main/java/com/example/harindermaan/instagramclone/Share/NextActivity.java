package com.example.harindermaan.instagramclone.Share;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harindermaan.instagramclone.Login.LoginActivity;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.FirebaseMethods;
import com.example.harindermaan.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NextActivity extends AppCompatActivity
{
    private static final String TAG = "NextActivity";
    private Context mContext = NextActivity.this;




    //widgets
    private EditText mCaption;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseMethods mFirebaseMethods;


    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;

    private Intent intent;
    private Bitmap bitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        mFirebaseMethods = new FirebaseMethods(mContext);
        mCaption = findViewById(R.id.caption);

        setupFirebaseAuth();

        ImageView backArrow = findViewById(R.id.ivbackArrow);
        backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Closing the activity !!");
                finish();
            }
        });

        TextView share = findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Navigating to the final share screen");

                //upload image to the firebase
                Toast.makeText(mContext, "Attempting to upload new photo", Toast.LENGTH_SHORT).show();
                String caption = mCaption.getText().toString();

                if(intent.hasExtra(getString(R.string.selected_image)))
                {//image is taken from gallery
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,imgUrl,null);
                }//if
                else if(intent.hasExtra(getString(R.string.selected_bitmap)))
                {//image is taken from camera
                    bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirebaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,null,bitmap);
                }//else if




            }
        });

        setImage();
    }//onCreate


    //get the image url from incoming intent and displays chosen image
    private void setImage()
    {
        intent =  getIntent();
        ImageView image = findViewById(R.id.imageShare);

        if(intent.hasExtra(getString(R.string.selected_image)))
        {//image is taken from gallery
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: Got new image url "+imgUrl);
            UniversalImageLoader.setImage(imgUrl,image,null,mAppend);
        }//if
        else if(intent.hasExtra(getString(R.string.selected_bitmap)))
        {//image is taken from camera
            bitmap = (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: Got new bitmap");
            image.setImageBitmap(bitmap);
        }//else if


    }//setImage


    private void someMethod()
    {
        //Step 1
        //create data model for photos

        //Step 2
        //Add properties to the photo obeject (caption,date,imgUrl,photo_id,tags,user_id)


        //Step 3
        //Count the number of photos that user already has

        //Step 4
         // a)Upload photo to Firebase Storage and insert two nodes in the Firebase Database
        //  b) 'photos' node
        //  c) 'user_photos' node
    }//someMethod



     /*----------------------------------------------Firebase------------------------------------------------------------ */

    //set up the firebase auth object
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth()-> Setting Up Firebase Authentication");
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        Log.d(TAG, "onDataChange: ImageCount : "+imageCount);

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

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                imageCount = mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: ImageCount : "+imageCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

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




}//NextActivity
