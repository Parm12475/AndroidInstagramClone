package com.example.harindermaan.instagramclone.Utils;


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
import android.widget.Toast;

import com.example.harindermaan.instagramclone.Login.LoginActivity;
import com.example.harindermaan.instagramclone.Models.Comment;
import com.example.harindermaan.instagramclone.Models.Like;
import com.example.harindermaan.instagramclone.Models.Photo;
import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.Models.UserAccountSettings;
import com.example.harindermaan.instagramclone.Models.UserSettings;
import com.example.harindermaan.instagramclone.Profile.AccountSettingsActivity;
import com.example.harindermaan.instagramclone.Profile.ProfileActivity;
import com.example.harindermaan.instagramclone.R;
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

public class ViewProfileFragment extends Fragment
{

    private static final String TAG = "ViewProfileFragment";

    public interface  OnGridImageSelectedListener
    {
        void onGridImageSelected(Photo photo, int activityNumber);
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


    private TextView mPosts,mFollowers,mFollowing,mDisplayName,mUsername,mWebsite,mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private android.support.v7.widget.Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx bottomNavigationView;
    private Context mContext;

    //vars
    private User mUser;


    public ViewProfileFragment()
    {
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: Started..");

        View view = inflater.inflate(R.layout.fragment_view_profile,container,false);

        mDisplayName = view.findViewById(R.id.display_name);
        mUsername = view.findViewById(R.id.username);
        mWebsite = view.findViewById(R.id.website);
        mDescription = view.findViewById(R.id.description);
        mProfilePhoto =  view.findViewById(R.id.profile_photo);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvFollowers);
        mFollowing =  view.findViewById(R.id.tvFollowing);
        mProgressBar =  view.findViewById(R.id.profileProgressBar);
        gridView = view.findViewById(R.id.gridView);
        toolbar = view.findViewById(R.id.profileToolBar);
        profileMenu =  view.findViewById(R.id.profileMenu);
        bottomNavigationView =  view.findViewById(R.id.bottomNavViewBar);
        mContext = getActivity();


        Log.d(TAG, "onCreateView: ######");


        try
        {
            mUser = getUserFromBundle();
            init();
        }//try
        catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: "  + e.getMessage() );
            Toast.makeText(mContext, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
        }//catch

        setupBottomNavigationView();
        setupToolbar();
        setupFirebaseAuth();

        setupGridView();
        TextView editProfile = view.findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Navigating to : "+mContext.getString(R.string.profile_activity ));

                Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });

        return view;
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


    private void init() {

        //set the profile widgets
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference1.child(getString(R.string.dbname_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found user:" + singleSnapshot.getValue(UserAccountSettings.class).toString());

                    UserSettings settings = new UserSettings();
                    settings.setUser(mUser);
                    settings.setSettings(singleSnapshot.getValue(UserAccountSettings.class));
                    setProfileWidgets(settings);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //get the users profile photos

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference();
        Query query2 = reference2
                .child(getString(R.string.dbname_user_photos))
                .child(mUser.getUser_id());
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<Photo> photos = new ArrayList<Photo>();
                for ( DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                    photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                    ArrayList<Comment> comments = new ArrayList<Comment>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_comments)).getChildren()){
                        Comment comment = new Comment();
                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                        comments.add(comment);
                    }

                    photo.setComments(comments);

                    List<Like> likesList = new ArrayList<Like>();
                    for (DataSnapshot dSnapshot : singleSnapshot
                            .child(getString(R.string.field_likes)).getChildren()){
                        Like like = new Like();
                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                        likesList.add(like);
                    }
                    photo.setLikes(likesList);
                    photos.add(photo);
                }
                setupImageGrid(photos);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled.");
            }
        });

    }//init

    private void setupImageGrid(final ArrayList<Photo> photos){
        //setup our image grid
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        ArrayList<String> imgUrls = new ArrayList<String>();
        for(int i = 0; i < photos.size(); i++){
            imgUrls.add(photos.get(i).getImage_path());
        }
        GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,
                "", imgUrls);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnGridImageSelectedListener.onGridImageSelected(photos.get(position), ACTIVITY_NUM);
            }
        });
    }//setupImageGrid





    private User getUserFromBundle(){
        Log.d(TAG, "getUserFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.intent_user));
        }else{
            return null;
        }
    }//getUserFromBundle

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try
        {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener)getActivity();
        }//try
        catch(ClassCastException ex)
        {
            Log.d(TAG, "onAttach: ClassCastException "+ ex.getMessage());
            ex.printStackTrace();

        }//catch

    }//onAttach

    private void setProfileWidgets(UserSettings userSettings)
    {
        //Log.d(TAG, "setProfileWidgets: Setting Profile widgets with data retrieved from firebase database "+userSettings.toString());
        //Log.d(TAG, "setProfileWidgets: Username "+userSettings.getSettings().getUsername());

        //User user = userSettings.getUser();
        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mPosts.setText(String .valueOf(settings.getPosts()));
        mFollowers.setText(String.valueOf(settings.getFollowers()));
        mFollowing.setText(String.valueOf(settings.getFollowing()));
        mProgressBar.setVisibility(View.GONE);


    }//setProfileWidgets


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
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            }
        });

    }//setupToolbar

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
