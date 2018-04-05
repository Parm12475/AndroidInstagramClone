package com.example.harindermaan.instagramclone.Login;


import android.content.Context;
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

import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.FirebaseMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity
{
    private static final String TAG = "RegisterActivity";
    private Context mContext = RegisterActivity.this;
    private String email,username,password;
    private EditText mEmail,mUsername,mPassword;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressbar;
    private FirebaseMethods firebaseMethods;
    private String append="";

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //firebase database
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate()");

        firebaseMethods = new FirebaseMethods(mContext);
        initWidgets();
        setupFirebaseAuth();
        init();

    }//onCreate

    private void initWidgets()
    {
        Log.d(TAG, "initWidgets: Initializing Widgets");
        mEmail = findViewById(R.id.input_email);
        mUsername = findViewById(R.id.input_username);
        mPassword = findViewById(R.id.input_password);
        btnRegister = findViewById(R.id.btn_register);

        loadingPleaseWait = findViewById(R.id.loadingPleaseWait);
        mProgressbar = findViewById(R.id.progressbar);

        mProgressbar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);

    }//initWidgets



    private void init()
    {
        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                Log.d(TAG, "onClick() -> Email entered by user : "+email);
                Log.d(TAG, "onClick() -> Username entered by user : "+username);
                Log.d(TAG, "onClick() -> Password entered by user : "+password);

                if(checkInputs(email,username,password))
                {
                    mProgressbar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);

                    firebaseMethods.registerNewEmail(email,password,username);

                }//if


            }
        });
    }//init

    private boolean checkInputs(String email,String username,String password)
    {
        Log.d(TAG, "checkInputs() -> Checking Inputs for Registering user");
        if( isStringNull(email) || isStringNull(username) || isStringNull(password) )
        {
            Toast.makeText(mContext, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return true;
        }//if
        return true;
    }//checkInputs

    private boolean isStringNull(String s)
    {
        if(s.equals(""))
            return true;
        else
            return false;
    }//isStringNull


    private void checkIfUsernameExists(final String username)
    {
        Log.d(TAG, "checkIfUsernameExists: Checking if "+username+" already exists ");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    if(singleSnapshot.exists())
                    {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH "+singleSnapshot.getValue(User.class).getUsername());
                        append = myRef.push().getKey().substring(3,10);
                        Log.d(TAG, "onDataChange: Username Already Exists Appending Random name to it "+append);

                    }//if
                }//for

                String mUsername = "";
                mUsername = username + append;

                //add new user to the database
                firebaseMethods.addNewUser(email,mUsername,"","","");

                Toast.makeText(mContext,"Sign up Success , Sending Verification Email",Toast.LENGTH_SHORT).show();
                mAuth.signOut();



            }//onDataChange

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }//onCancelled
        });//addListenerForSingleValueEvent


    }//checkIfUsernameExists


    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth() -> Setting Up Firebase Auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = mAuth.getCurrentUser();

                if(user != null)
                {//user is signed in
                    Log.d(TAG, "onAuthStateChanged() -> Signed In UserID : "+user.getUid());
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            checkIfUsernameExists(username);
                        }//onDataChange

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }


                    });
                    finish();
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
