package com.example.harindermaan.instagramclone.Utils;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.harindermaan.instagramclone.Main.MainActivity;
import com.example.harindermaan.instagramclone.Models.Photo;
import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.Models.UserAccountSettings;
import com.example.harindermaan.instagramclone.Models.UserSettings;
import com.example.harindermaan.instagramclone.Profile.AccountSettingsActivity;
import com.example.harindermaan.instagramclone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods
{
    private static final String TAG = "FirebaseMethods";
    private Context mContext ;
    private String userID;


    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //firebase database
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;
    StorageReference mStorageReference;

    //vars
    private double mPhotoUploadProgress = 0;

    public FirebaseMethods(Context mContext)
    {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef =  mFirebaseDatabase.getReference();
        this.mContext = mContext;
        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if(mAuth.getCurrentUser() != null)
        {
            userID = mAuth.getCurrentUser().getUid();
        }//if
    }//FirebaseMethods


    public void updateEmail(String email)
    {
        Log.d(TAG, "updateEmail: Updating email to "+email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);


    }//updateUsername

    public void updateUsername(String username)
    {
        Log.d(TAG, "updateUsername: Updating username to "+username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

    }//updateUsername


    public void registerNewEmail(String email,String password,final String username)
    {
        Log.d(TAG, "registerNewEmail() -> Registering New User ");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful()) {
                            // Registeration is successfull
                            Log.d(TAG, "createUserWithEmail() -> Registeration is Successfull");
                            sendVerificationEmail();
                            FirebaseUser user = mAuth.getCurrentUser();
                            userID = user.getUid();
                            Log.d(TAG, "onComplete() -> Registered User's UserID is : "+userID);

                        }//if
                        else
                        {
                            //Registeration failed
                            Log.w(TAG, "createUserWithEmail() -> Registeration failed ", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }//else


                    }//onComplete
                });

    }//registerNewEmail

    public void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null)
        {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {

                            }//if
                            else
                            {
                                Toast.makeText(mContext,"Couldn't send Emal Verification",Toast.LENGTH_SHORT).show();
                            }//else

                        }//onComplete
                    });//addOnCompleteListener
        }//if
    }//sendVerificationEmail



//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot)
//    {
//        Log.d(TAG, "checkIfUsernameExists: checking if "+username+" already exists");
//
//       User user = new User();
//       for(DataSnapshot ds : dataSnapshot.child(userID).getChildren())
//       {
//           Log.d(TAG, "checkIfUsernameExists: Datasnapshot = "+ds);
//           user.setUsername(ds.getValue(User.class).getUsername());
//           Log.d(TAG, "checkIfUsernameExists: username = "+user.getUsername());
//           if(StringManipulation.expandUsername(user.getUsername()).equals(username))
//           {
//               Log.d(TAG, "checkIfUsernameExists: Found a match");
//               return true;
//           }//if
//
//       }//for
//
//        return false;
//
//    }//checkIfUsernameExists

    public void addNewUser(String email,String username,String description,String website,String profile_photo)
    {
            //add data to user table
            User user = new User(userID,1,email,StringManipulation.condenseUsername(username));
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(userID)
                    .setValue(user);

            //add data to user_account_setting table
            UserAccountSettings userAccountSettings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                    StringManipulation.condenseUsername(username),
                website,
                    userID);

            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(userAccountSettings);

    }//addNewUser


    /*
    * Retrieving user account settings details from Firebase database.
    * retrieving data from user_account_setting node.
    * */
    public UserSettings getUserSettings(DataSnapshot dataSnapshot)
    {
        Log.d(TAG, "getUserAccountSettings: Retrieving data from firebase database  user_account_setting !!");

        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        for(DataSnapshot ds : dataSnapshot.getChildren())
        {
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings)))
            {
                Log.d(TAG, "getUserAccountSettings: datasnapshot : "+ds);

                try
                {

                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );

                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );

                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );

                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );

                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );

                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );

                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );

                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );
                }//try
                catch(NullPointerException ex)
                {
                    Log.d(TAG, "getUserAccountSettings: NullPointerException "+ ex.getMessage());
                }//catch

                Log.d(TAG, "getUserAccountSettings: Retrieved UserAccountSettings information : "+settings.toString());

            }//if

            if(ds.getKey().equals(mContext.getString(R.string.dbname_users)))
            {
                try
                {
                    user.setUsername(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUsername()
                    );

                    user.setEmail(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getEmail()
                    );

                    user.setPhone_number(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getPhone_number()
                    );

                    user.setUser_id(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUser_id()
                    );


                }//try
                catch(NullPointerException ex)
                {
                    Log.d(TAG, "getUserAccountSettings: NullPointerException "+ex.getMessage());
                }//catch

                Log.d(TAG, "getUserAccountSettings: Retrieved Users information : "+user.toString());
            }//if

        }//for


        return new UserSettings(user,settings);
    }//getUserAccountSettings

    public void updateUserAccountSettings(String displayName,String website,String description)
    {
        Log.d(TAG, "updateUserAccountSettings: ");


        if(displayName != null)
        {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }//if


       if(website != null)
       {
           myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                   .child(userID)
                   .child(mContext.getString(R.string.field_website))
                   .setValue(website);
       }//if

        if(description != null)
        {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }//if
    }//updateUserAccountSettings


    public int getImageCount(DataSnapshot dataSnapshot)
    {
        int count = 0;

        for(DataSnapshot ds : dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()
                )
        {
            count++;
        }//for


        return count;
    }



    public String getTimestamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));

        return sdf.format(new Date());
    }//getTimeStamp


    private void addPhotoToDatabase(String caption, String url)
    {
        Log.d(TAG, "addPhotoToDatabase: Adding Photo to Firebase Database");

        String tags = StringManipulation.getTags(caption);
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimestamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);


        //insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photo);

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(newPhotoKey)
                .setValue(photo);




    }//addPhotoToDatabase
    
    

    public void uploadNewPhoto(String photoType, final String caption,final int count, final String imgUrl,Bitmap bm)
    {
        Log.d(TAG, "uploadNewPhoto: Attempting to upload new photo");

        FilePaths filePaths = new FilePaths();


        //case 1: New Photo
        if(photoType.equals(mContext.getString(R.string.new_photo)))
        {
            Log.d(TAG, "uploadNewPhoto: Uploading New Photo");
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference =  mStorageReference.
                                        child(filePaths.FIREBASE_IMAGE_STORAGE+"/"+user_id+"/photo"+(count+1));

            //convert url image to bitmap

            if(bm == null)
            {
                bm = ImageManager.getBitMap(imgUrl);
            }//if

            byte bytes[] = ImageManager.getBytesFromBitmap(bm,100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    String firebaseUrl = taskSnapshot.getDownloadUrl().toString();

                    Toast.makeText(mContext, "Photo Upload Success", Toast.LENGTH_SHORT).show();

                    //add photo to 'photos' node and 'user_photos' node

                    addPhotoToDatabase(caption,firebaseUrl);

                    //navigate to the main feed so that user can see their photo
                    Intent intent = new Intent(mContext, MainActivity.class);
                    mContext.startActivity(intent);


                }//onSuccess

             
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, "onFailure: Photo Upload Failed");
                    Toast.makeText(mContext, "Photo Upload Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();


                    if(progress - 15 > mPhotoUploadProgress)
                    {
                        Toast.makeText(mContext, "Photo Upload Progress : "+ String.format("%.0f",progress)+"%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;

                        Log.d(TAG, "onProgress: Upload Progress "+progress+" % done");

                    }//if
                }
            });

            
        }//if
        //case 2 : New Profile Photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo)))
        {
            Log.d(TAG, "uploadNewPhoto: Uploading New Profile Photo");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference storageReference =  mStorageReference.
                    child(filePaths.FIREBASE_IMAGE_STORAGE+"/"+user_id+"/profile_photo");

            //convert url image to bitmap
            if(bm == null)
            {
                bm = ImageManager.getBitMap(imgUrl);
            }//if
            byte bytes[] = ImageManager.getBytesFromBitmap(bm,100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    String firebaseUrl = taskSnapshot.getDownloadUrl().toString();

                    Toast.makeText(mContext, "Photo Upload Success", Toast.LENGTH_SHORT).show();

                    //insert photo into 'user_account_settings' node
                    setProfilePhoto(firebaseUrl);

                    ((AccountSettingsActivity)mContext).setViewPager(
                            ((AccountSettingsActivity)mContext)
                                    .pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );





                }//onSuccess


            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, "onFailure: Photo Upload Failed");
                    Toast.makeText(mContext, "Photo Upload Failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();


                    if(progress - 15 > mPhotoUploadProgress)
                    {
                        Toast.makeText(mContext, "Photo Upload Progress : "+ String.format("%.0f",progress)+"%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;

                        Log.d(TAG, "onProgress: Upload Progress "+progress+" % done");

                    }//if
                }
            });

        }//if
        
    }//uploadNewPhoto

    private void setProfilePhoto(String url)
    {
        Log.d(TAG, "setProfilePhoto: Setting New Profile Image : "+url);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }//setProfilePhoto


}//FirebaseMethods

