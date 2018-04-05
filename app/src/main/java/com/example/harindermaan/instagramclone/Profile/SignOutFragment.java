package com.example.harindermaan.instagramclone.Profile;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.harindermaan.instagramclone.Login.LoginActivity;
import com.example.harindermaan.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignOutFragment extends android.support.v4.app.Fragment
{
    private static final String TAG = "SignOutFragment";


    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressBar mProgressBar;
    private TextView tvSignOut,tvSigningout;
    private Button btnConfirmSignout;

    public SignOutFragment()
    {
        Log.d(TAG, "SignOutFragment() ");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: Inflating Sign Out Layout");
        View view = inflater.inflate(R.layout.fragment_signout,container,false);

        tvSignOut = view.findViewById(R.id.tvConfirmSignOut);
        mProgressBar = view.findViewById(R.id.progressBar);
        tvSigningout = view.findViewById(R.id.tvSigningout);
        btnConfirmSignout = view.findViewById(R.id.btnConfirmSignout);

        mProgressBar.setVisibility(View.GONE);
        tvSigningout.setVisibility(View.GONE);

        setupFirebaseAuth();

        btnConfirmSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Attempting to sign out ");
                mProgressBar.setVisibility(View.VISIBLE);
                tvSigningout.setVisibility(View.VISIBLE);
                mAuth.signOut();
                getActivity().finish();
            }
        });
        return view;
    }//onCreateView


    //----------------------------------------------Firebase------------------------------------------------------------
    //set up the firebase auth object
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth() -> Setting Up Firebase Auth ");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null)
                {//user is signed in

                    Log.d(TAG, "onAuthStateChanged: Signed In Used ID : "+user.getUid());

                }//if
                else
                {//User is signed out
                    Log.d(TAG, "onAuthStateChanged: Signed Out");
                    Log.d(TAG, "onAuthStateChanged: Navigating back to Login Activity");
                    Intent intent = new Intent(getActivity(),LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }//else

            }//onAuthStateChanged
        };

    }//setupFirebaseAuth


    @Override
    public void onStart()
    {
        Log.d(TAG, "onStart() ");
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);


    }//onStart

    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop()");
        super.onStop();
        if(mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);

    }//onStart


}//SignOutFragment
