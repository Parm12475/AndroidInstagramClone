package com.example.harindermaan.instagramclone.Profile;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harindermaan.instagramclone.Dialogs.ConfirmPasswordDialog;
import com.example.harindermaan.instagramclone.Login.LoginActivity;
import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.Models.UserAccountSettings;
import com.example.harindermaan.instagramclone.Models.UserSettings;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Share.ShareActivity;
import com.example.harindermaan.instagramclone.Utils.FirebaseMethods;
import com.example.harindermaan.instagramclone.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends android.support.v4.app.Fragment implements ConfirmPasswordDialog.OnConfirmPasswordListener
{

    private static final String TAG = "EditProfileFragment";
    private ImageView back_arrow;
    private  Context mContext = getActivity();

    //Edit Profile Fragments Widgets
    private EditText mDisplayName,mUsername,mWebsite,mDescription,mEmail,mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;
    private UserSettings mUserSettings;

    private String userID;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseMethods mFirebaseMethods;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView() -> Inflating Edit Profile Layout");

        View view = inflater.inflate(R.layout.fragment_edit_profile,container,false);



        back_arrow =  view.findViewById(R.id.backArrow);

        mProfilePhoto = view.findViewById(R.id.profile_photo);
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);
        mUsername = view.findViewById(R.id.username);
        mDisplayName = view.findViewById(R.id.display_name);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);

        mEmail = view.findViewById(R.id.email);
        mPhoneNumber = view.findViewById(R.id.phone_number);

        mFirebaseMethods = new FirebaseMethods(getActivity());

        //setProfileImage();
        setupFirebaseAuth();


       back_arrow.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.d(TAG, "onClick: ");
               getActivity().finish();

           }
       });

       ImageView checkMark = view.findViewById(R.id.saveChanges);
       checkMark.setOnClickListener(new View.OnClickListener()
       {
           @Override
           public void onClick(View v)
           {
               Log.d(TAG, "onClick: Attempting to save changes !!");
               saveProfileSettings();
           }//onClick
       });//setOnClickListener


        return view;
    }//onCreateView

    @Override
    public void onConfirmPassword(String password)
    {
        Log.d(TAG, "onConfirmPassword: Got a password : "+password);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(),password);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {


                            /* Check to see if email is not already present in the firebase database  */
                            mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<ProviderQueryResult> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        try
                                        {
                                            if(task.getResult().getProviders().size() == 1)
                                            {
                                                Log.d(TAG, "onComplete: That Email is Already in use !!");
                                                Toast.makeText(getContext(), "That Email is already in use", Toast.LENGTH_SHORT).show();
                                            }//if
                                            else
                                            {
                                                Log.d(TAG, "onComplete: That Email is available");

                                            /* Email is available so update it */
                                                mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                Log.d(TAG, "onComplete: Email Address is Updated ");
                                                                Toast.makeText(getContext(), "Email Updated", Toast.LENGTH_SHORT).show();
                                                                mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                            }//onComplete
                                                        });


                                            }//else
                                        }//try
                                        catch(NullPointerException ex)
                                        {
                                            Log.d(TAG, "onComplete: NullPointerException: "+ex.getMessage());
                                        }//catch
                                    }//if
                                    else
                                    {

                                    }//else
                                }
                            });
                        }//if
                        else
                        {
                            Log.d(TAG, "onComplete: Re-Authentication Failed");
                        }//else
                    }
                });


    }//onConfirmPassword


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
                //No match is found from database
                if(!dataSnapshot.exists())
                {//username doesn't exist in database , add the username
                    Log.d(TAG, "onDataChange: No Match Found,Hence Updating Username");
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getContext(), "Username Updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onDataChange: Toasted");
                }//if


                //if match is found in database
                Log.d(TAG, "onDataChange: dataSnapshot.getChildren() "+dataSnapshot.getChildren());
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {

                    if(singleSnapshot.exists())
                    {
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH "+singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getContext(), "Username Already Exists", Toast.LENGTH_SHORT).show();
                    }//if
                }//for


            }//onDataChange

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }//onCancelled
        });//addListenerForSingleValueEvent


    }//checkIfUsernameExists
    


    private void saveProfileSettings()
    {
        Log.d(TAG, "saveProfileSettings: Saving Profile Settings ");
        
        final String username = mUsername.getText().toString();
        Log.d(TAG, "saveProfileSettings: Username : "+username);

        final String displayName = mDisplayName.getText().toString();
        Log.d(TAG, "saveProfileSettings: Display Name : "+displayName);

        final String website = mWebsite.getText().toString();
        Log.d(TAG, "saveProfileSettings: Website : "+website);

        final String description = mDescription.getText().toString();
        Log.d(TAG, "saveProfileSettings: Description : "+description);

        final String email = mEmail.getText().toString();
        Log.d(TAG, "saveProfileSettings: Email : "+email);

        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());
        Log.d(TAG, "saveProfileSettings: Phone Number : "+phoneNumber);


        //case 1: user has changed his username
        if(!mUserSettings.getUser().getUsername().equals(username))
        {
            Log.d(TAG, "onDataChange: IF");
            checkIfUsernameExists(username);
        }//if
        //Case 2: User has changed his email
        if(!mUserSettings.getUser().getEmail().equals(email))
        {
            //step1 : Reauthenticate
            //         - confirm password and email
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
            dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this,1);

            //step2 : check if email already is registered
            //       fetchProvidersForEmail(String email)
            //step3 : change the email
            //       submit new email to the database and authentication

        }//else

        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName))
        {
            //update displayname
            mFirebaseMethods.updateUserAccountSettings(displayName,null,null);
        }//if

        if(!mUserSettings.getSettings().getWebsite().equals(website))
        {
            //update website
            mFirebaseMethods.updateUserAccountSettings(null,website,null );
        }//if

        if(!mUserSettings.getSettings().getDescription().equals(description))
        {
            //update description
            mFirebaseMethods.updateUserAccountSettings(null,null,description);
        }//if

       

    }//saveProfileSettings




    private void setProfileWidgets(UserSettings userSettings)
    {
        //Log.d(TAG, "setProfileWidgets: Setting Edit Profile widgets with data retrieved from firebase database "+userSettings.toString());
        //Log.d(TAG, "setProfileWidgets: Username "+userSettings.getSettings().getUsername());

        mUserSettings = userSettings;

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");
        mUsername.setText(user.getUsername());
        mDisplayName.setText(settings.getDisplay_name());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Changing profile photo");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();

            }
        });


        //mProgressBar.setVisibility(View.GONE);


    }//setProfileWidgets



         /*----------------------------------------------Firebase------------------------------------------------------------ */

    //set up the firebase auth object
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth()-> Setting Up Firebase Authentication");
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
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
                //retrieve user information from database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));


                //retrieve images for the user in question
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



}//EditProfileFragment
