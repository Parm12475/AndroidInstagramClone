package com.example.harindermaan.instagramclone.Profile;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.harindermaan.instagramclone.Login.LoginActivity;
import com.example.harindermaan.instagramclone.Models.Comment;
import com.example.harindermaan.instagramclone.Models.Photo;
import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.Models.UserAccountSettings;
import com.example.harindermaan.instagramclone.Models.UserSettings;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.harindermaan.instagramclone.Utils.FirebaseMethods;
import com.example.harindermaan.instagramclone.Utils.GridImageAdapter;
import com.example.harindermaan.instagramclone.Models.Like;
import com.example.harindermaan.instagramclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface  OnGridImageSelectedListener
    {
        void onGridImageSelected(Photo photo,int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    //constants
    private static final int ACTIVITY_NUM=4;
    private static final int NUM_GRID_COLUMNS = 3;

    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseMethods mFirebaseMethods;


    private TextView mPosts,mFollowers,mFollowing,mDisplayName,mUsername,mWebsite,mDesription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private android.support.v7.widget.Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;

    public ProfileFragment()
    {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: Started..");

        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDesription = view.findViewById(R.id.description);
        mProfilePhoto = view.findViewById(R.id.profile_image);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvFollowers);
        mFollowing = view.findViewById(R.id.tvFollowing);
        mProgressBar = view.findViewById(R.id.profileProgressbBar);
        gridView = view.findViewById(R.id.gridView);
        toolbar =view.findViewById(R.id.profileToolBar);
        profileMenu = view.findViewById(R.id.profileMenu);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();
        mFirebaseMethods = new FirebaseMethods(getActivity());


        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();
        setupGridView();

        TextView editProfile = view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Navigating to : "+mContext.getString(R.string.edit_profile_fragment ));

                Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context)
    {
        try
        {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener)getActivity();
        }//try
        catch(ClassCastException ex)
        {
            Log.d(TAG, "onAttach: ClassCastException "+ex.getMessage());
        }//catch


        super.onAttach(context);
    }

    private void setupGridView()
    {
        Log.d(TAG, "setupGridView: Setting Up Image Grid");

        final ArrayList<Photo> photos = new ArrayList();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                      .child(getString(R.string.dbname_user_photos))
                      .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {

                    Photo photo = new Photo();

                    Map<String,Object> objectMap = (HashMap<String,Object>)singleSnapshot.getValue();
                    Log.d(TAG, "onDataChange: Map Keys: "+objectMap.keySet());
                    Log.d(TAG, "onDataChange: Map Values : "+objectMap.values());
                    Log.d(TAG, "onDataChange: Photo_ID "+objectMap.get(getString(R.string.field_photo_id)));

                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());


                    ArrayList<Comment> comments = new ArrayList<>();

                    for(DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_comments))
                            .getChildren())

                    {
                        Log.d(TAG, "onDataChange: In Loop Comments");
                        Comment comment = new Comment();
                        Log.d(TAG, "onDataChange: Comment User ID: "+dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        Log.d(TAG, "onDataChange: Comment : "+dSnapshot.getValue(Comment.class).getComment());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());

                        comments.add(comment);
                    }//for

                    photo.setComments(comments);


                    List<Like> likesList = new ArrayList<Like>();
                    for(DataSnapshot dSnapshot : dataSnapshot
                            .child(getString(R.string.field_likes)).getChildren())
                    {
                        Log.d(TAG, "onDataChange: In Loop Likes");
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }//for

                    photo.setLikes(likesList);
                    photos.add(photo);


                }//for

                //set up our image grid view
                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<>();
                for(int i=0;i<photos.size();i++)
                {
                    imgUrls.add(photos.get(i).getImage_path());
                }//for

                GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,"",
                        imgUrls);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(position),ACTIVITY_NUM);

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled: Query Cancelled");

            }
        });

    }//setupGridView


    public void setupToolbar()
    {
        Log.d(TAG, "setupToolbar() ");

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating to account settings !");
                Intent i = new Intent(mContext,AccountSettingsActivity.class);
                startActivity(i);

            }
        });

    }//setupToolbar

    private void setProfileWidgets(UserSettings userSettings)
    {
        //Log.d(TAG, "setProfileWidgets: Setting Profile widgets with data retrieved from firebase database "+userSettings.toString());
        //Log.d(TAG, "setProfileWidgets: Username "+userSettings.getSettings().getUsername());

        User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(user.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDesription.setText(settings.getDescription());
        mPosts.setText(String .valueOf(settings.getPosts()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));

        mProgressBar.setVisibility(View.GONE);


    }//setProfileWidgets


    //Bottom Navigation View Setup
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: setting Up Bottom Navigation View !");

        BottomNavigationViewHelper.setupNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext,getActivity(),bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }//setupBottomNavigationView

     /*----------------------------------------------Firebase------------------------------------------------------------ */

    //set up the firebase auth object
    private void setupFirebaseAuth()
    {
        Log.d(TAG, "setupFirebaseAuth()-> Setting Up Firebase Authentication");
        mAuth = FirebaseAuth.getInstance();
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


}
