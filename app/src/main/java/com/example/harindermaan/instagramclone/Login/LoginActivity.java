package com.example.harindermaan.instagramclone.Login;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harindermaan.instagramclone.Main.MainActivity;
import com.example.harindermaan.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity
{
    private static final String TAG = "LoginActivity";

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText mEmail,mPassword;
    private ProgressBar mProgressbar;
    private Context mContext = LoginActivity.this;
    private TextView mPleaseWait;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgressbar = findViewById(R.id.progressbar);
        mPleaseWait = findViewById(R.id.pleaseWait);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);

        mPleaseWait.setVisibility(View.GONE);
        mProgressbar.setVisibility(View.GONE);

        setupFirebaseAuth();
        init();
    }//onCreate


    private boolean isStringNull(String s)
    {
        if(s.equals(""))
            return true;
        else
            return false;
    }//isStringNull



    //----------------------------------------------Firebase------------------------------------------------------------


    private void signInWithEmailAndPassword(String email,String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (task.isSuccessful()) {
                            // Login  in success
                            Log.d(TAG, "signInWithEmail : Success");
                            Toast.makeText(mContext,R.string.auth_success,Toast.LENGTH_SHORT).show();
                            try
                            {
                                if(user.isEmailVerified())
                                {
                                    Log.d(TAG, "onComplete: Email Verified for user : "+user.getEmail());
                                    Intent intent = new Intent(mContext,MainActivity.class);
                                    startActivity(intent);
                                }//if
                                else
                                {
                                    Log.d(TAG, "onComplete: Email Not Verified,Check your email inbox");
                                    Toast.makeText(mContext,"Email not verified,Check your email inbox",Toast.LENGTH_SHORT).show();
                                    mProgressbar.setVisibility(View.GONE);
                                    mPleaseWait.setVisibility(View.GONE);
                                    mAuth.signOut();
                                }//else

                            }//try
                            catch(NullPointerException e)
                            {
                                Log.d(TAG, "onComplete: NullPointer Exception : "+e.getMessage());
                            }//catch
                            
                        }//if
                        else
                        {
                            // If Login in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail : Failure", task.getException());
                            Toast.makeText(mContext,R.string.auth_failed, Toast.LENGTH_SHORT).show();
                        }//else

                    }//onComplete
                });//.addOnCompleteListener
        
    }//signInWithEmailAndPassword

    private void init()
    {
        //initialize the button for logging in
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick() Attempting to log in ");

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                Log.d(TAG, "onClick: Login Email : "+email);
                Log.d(TAG, "onClick: Login password : "+password);

                if(isStringNull(email) && isStringNull(password))
                {
                    Toast.makeText(mContext,"You must fill out all fields",Toast.LENGTH_SHORT).show();
                }//if
                else
                {
                    mProgressbar.setVisibility(View.VISIBLE);
                    mPleaseWait.setVisibility(View.VISIBLE);

                    signInWithEmailAndPassword(email,password);

                }//else

            }//onClick
        });//setOnClickListener

        //link sign up
        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext,RegisterActivity.class);
                startActivity(intent);

            }
        });

        if(mAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
        }//if
    }//init

    //set up the firebase auth object
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth() -> Setting Up Firebase Auth");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = mAuth.getCurrentUser();

                if(user != null)
                {//user is signed in
                    Log.d(TAG, "onAuthStateChanged: Signed In UserID : "+user.getUid());
                }//if
                else
                {//User is signed out
                    Log.d(TAG, "onAuthStateChanged: Signed Out");
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


}//LoginActivity
