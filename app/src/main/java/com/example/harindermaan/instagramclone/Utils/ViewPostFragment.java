package com.example.harindermaan.instagramclone.Utils;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.harindermaan.instagramclone.Login.LoginActivity;
import com.example.harindermaan.instagramclone.Models.Comment;
import com.example.harindermaan.instagramclone.Models.Like;
import com.example.harindermaan.instagramclone.Models.Photo;
import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.Models.UserAccountSettings;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostFragment extends Fragment
{

    private static final String TAG = "ViewPostFragment";


    public interface OnCommentThreadSelectedListener
    {
        void OnCommentThreadSelectedListener(Photo photo);
    }

    OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;



    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationView;
    private TextView mBackLabel,mCaption,mUsername,mTimestamp,mLikes,mComments;
    private ImageView mBackArrow,mEllipses,mHeartRed,mHeartWhite,mSpeechBubble;
    private CircleImageView mProfileImage;


    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseMethods mFirebaseMethods;

    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private String photoUsername = "";
    private String profilePhotoUrl = "";
    private UserAccountSettings userAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart mHeart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";




    public ViewPostFragment()
    {
        super();
        setArguments(new Bundle());

    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        try
        {
            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener)getActivity();
        }//try
        catch(ClassCastException ex)
        {
            Log.d(TAG, "onAttach: ClassCastException "+ex.getMessage());
        }//catch
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_view_post,container,false);

        mBackArrow = view.findViewById(R.id.backArrow);
        mBackLabel = view.findViewById(R.id.tvBackLabel);
        mProfileImage = view.findViewById(R.id.profile_photo);
        mUsername = view.findViewById(R.id.username);
        mEllipses = view.findViewById(R.id.ivEllipses);
        mPostImage = view.findViewById(R.id.post_image);
        mHeartRed = view.findViewById(R.id.image_heart_red);
        mHeartWhite = view.findViewById(R.id.image_heart);
        mSpeechBubble = view.findViewById(R.id.speech_bubble);
        mCaption = view.findViewById(R.id.image_caption);
        mTimestamp = view.findViewById(R.id.image_time_posted);
        bottomNavigationView = view.findViewById(R.id.bottomNavViewBar);
        mLikes = view.findViewById(R.id.image_likes);
        mComments = view.findViewById(R.id.image_comments_link);


        mHeart = new Heart(mHeartWhite,mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(),new GestureListener());


        try
        {
            //mPhoto = getPhotoFromBundle();

            UniversalImageLoader.setImage(getPhotoFromBundle().getImage_path(),mPostImage, null , "");
            mActivityNumber = getActivityNumFromBundle();

            String photo_id = getPhotoFromBundle().getPhoto_id();

            Query query = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.dbname_photos))
                            .orderByChild(getString(R.string.field_photo_id))
                            .equalTo(photo_id);

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                    {
                        Photo newPhoto = new Photo();
                        Map<String,Object> objectMap = (HashMap<String,Object>)singleSnapshot.getValue();

                        newPhoto.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        newPhoto.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        newPhoto.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        newPhoto.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        newPhoto.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        newPhoto.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());


                        List<Comment> commentsList = new ArrayList<Comment>();

                        for(DataSnapshot dSnapshot : singleSnapshot
                                                    .child(getString(R.string.field_comments)).getChildren())
                        {
                            Comment comment = new Comment();

                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            commentsList.add(comment);

                        }//for

                        newPhoto.setComments(commentsList);
                        mPhoto = newPhoto;

                        getPhotoDetails();
                        getLikesString();

                    }//for
                }//onDataChange

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

            Log.d(TAG, "onCreateView: Photo: "+mPhoto);



        }//try
        catch(NullPointerException ex)
        {
            Log.d(TAG, "onCreateView: NullPointerException : Photo was null from bundle : "+ex.getMessage());
        }//catch

        setupFirebaseAuth();
        setupBottomNavigationView();


        return view;
    }//onCreateView


     /*
     *
      * Retrieve the activity number from incoming bundle from profileActivity Interface
      *
      * */
    private int getActivityNumFromBundle()
    {
        Log.d(TAG, "getActivityNumFromBundle: Arguments: "+getArguments());
        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            return bundle.getInt(getString(R.string.activity_number));
        }//if
        else
        {
            return 0;
        }//else
    }

    /*
     *
      * Retrieve the photo from incoming bundle from profileActivity Interface
      *
      * */
    private Photo getPhotoFromBundle()
    {
        Log.d(TAG, "getPhotoFromBundle: Arguments: "+getArguments());
        Bundle bundle = this.getArguments();

        if(bundle != null)
        {
            return bundle.getParcelable(getString(R.string.photo));
        }//if
        else
        {
            return null;
        }//else
    }

    //Bottom Navigation View Setup
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: setting Up Bottom Navigation View !");

        BottomNavigationViewHelper.setupNavigationView(bottomNavigationView);
        BottomNavigationViewHelper.enableNavigation(getActivity(),getActivity(),bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);

    }//setupBottomNavigationView

    private void getPhotoDetails()
    {
        Log.d(TAG, "getPhotoDetails: Fetching Photo Details");

       try
       {
           DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
           Query query = reference
                   .child(getString(R.string.dbname_user_account_settings))
                   .orderByChild(getString(R.string.field_user_id))
                   .equalTo(mPhoto.getUser_id());

           Log.d(TAG, "getPhotoDetails: QUERY "+query);
           Log.d(TAG, "getPhotoDetails: UserID: "+FirebaseAuth.getInstance().getCurrentUser().getUid());
           Log.d(TAG, "getPhotoDetails: User Id from Photo : "+mPhoto.getUser_id());

           query.addListenerForSingleValueEvent(new ValueEventListener()
           {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot)
               {
                   Log.d(TAG, "onDataChange: STARTS");
                   Log.d(TAG, "onDataChange: Setting Profile Image for view post ");
                   Log.d(TAG, "onDataChange: Datasnapshot: "+dataSnapshot.getChildren());


                   for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                   {

                       Log.d(TAG, "onDataChange: IN FOR LOOP");
                       Log.d(TAG, "onDataChange: ####33");
                       userAccountSettings = singleSnapshot.getValue(UserAccountSettings.class);
                       Log.d(TAG, "onDataChange: User ID "+userAccountSettings.getUser_id()+" == "+mPhoto.getUser_id());
                       Log.d(TAG, "onDataChange: #######44");
                   }//for

                  Log.d(TAG, "onDataChange: ENDS");


               }

               @Override
               public void onCancelled(DatabaseError databaseError)
               {
                   Log.d(TAG, "onCancelled: Query Cancelled");

               }
           });

       }//try
        catch(NullPointerException ex)
        {
            Log.d(TAG, "getPhotoDetails: NullPointerException: "+ex.getMessage());
            ex.printStackTrace();
        }//catch
        catch(ClassCastException ex)
        {
            Log.d(TAG, "getPhotoDetails: ClassCastException "+ex.getMessage());
        }//catch

    }//getPhotoDetails



    private void setupWidgets()
    {
        Log.d(TAG, "setupWidgets: ");
        String timestampDiff = getTimestampDifference();

        try
        {
            if(!timestampDiff.equals("0"))
            {
                mTimestamp.setText(timestampDiff + "DAYS AGO");
            }//if
            else
            {
                mTimestamp.setText("TODAY");

            }//else

            Log.d(TAG, "setupWidgets: UserAccountSetting: "+userAccountSettings);
            UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(),mProfileImage,null,"");
            mUsername.setText(userAccountSettings.getUsername());
            mLikes.setText(mLikesString);
            mCaption.setText(mPhoto.getCaption());

            if(mPhoto.getComments().size() > 0)
            {
                mComments.setText("View all "+mPhoto.getComments().size()+" comments");
            }//if
            else
            {
                mComments.setText("");
            }//else


            mComments.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.d(TAG, "onClick: Navigating to comments thread");
                    mOnCommentThreadSelectedListener.OnCommentThreadSelectedListener(mPhoto);

                }
            });




            mBackArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: Navigating Back");
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            mSpeechBubble.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.d(TAG, "onClick: Clicked On Comment Button");
                    mOnCommentThreadSelectedListener.OnCommentThreadSelectedListener(mPhoto);
                }
            });




            if(mLikedByCurrentUser)
            {
                mHeartWhite.setVisibility(View.GONE);
                mHeartRed.setVisibility(View.VISIBLE);
                mHeartRed.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        Log.d(TAG, "onTouch: Red Heart Touch Detected");
                        return mGestureDetector.onTouchEvent(event);
                    }
                });
            }//if
            else
            {
                mHeartWhite.setVisibility(View.VISIBLE);
                mHeartRed.setVisibility(View.GONE);

                mHeartWhite.setOnTouchListener(new View.OnTouchListener()
                {
                    @Override
                    public boolean onTouch(View v, MotionEvent event)
                    {
                        Log.d(TAG, "onTouch: White Heart Touch Detected");
                        return mGestureDetector.onTouchEvent(event);
                    }
                });
            }//else



        }
        catch(NullPointerException ex)
        {
            Log.d(TAG, "setupWidgets: NullPointerException: "+ex.getMessage());
            ex.printStackTrace();
        }//catch

    }





    public class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }//onDown

        @Override
        public boolean onDoubleTap(MotionEvent e)
        {
            Log.d(TAG, "onDoubleTap: Double Tap Detected");

            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            Query query = databaseReference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                    {
                        //case 1: user has liked the photo

                        String keyID = singleSnapshot.getKey();

                        if(mLikedByCurrentUser && singleSnapshot.getValue(Like.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        {

                            databaseReference.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            databaseReference.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mHeart.toggleLike();
                            getLikesString();
                        }//if

                        //case 2: user has not liked the photo
                        else if(!mLikedByCurrentUser)
                        {
                            //add new like
                            addNewLike();
                            break;
                        }//else if

                    }//for

                    if(!dataSnapshot.exists())
                    {
                        //add new like
                        addNewLike();

                    }//if
                }
                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
            return true;
        }//onDoubleTap
    }//GestureListener

    private void addNewLike()
    {
        Log.d(TAG, "addNewLike: Adding New Like");
        String newLikeID = databaseReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        databaseReference.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        databaseReference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mHeart.toggleLike();
        getLikesString();

    }//addNewLike

    private void getLikesString()
    {
        Log.d(TAG, "getLikesString: Getting Likes String");
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = databaseReference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mUsers = new StringBuilder();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Query query = databaseReference
                            .child(getString(R.string.dbname_users))
                            .orderByChild(getString(R.string.field_user_id))
                            .equalTo( singleSnapshot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {

                            for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                            {
                                Log.d(TAG, "onDataChange: Found Like "+singleSnapshot.getValue(User.class).getUsername());

                                mUsers.append(singleSnapshot.getValue(User.class).getUsername());
                                mUsers.append(",");

                            }//for

                            String[] splitUsers = mUsers.toString().split(",");

                            if(mUsers.toString().contains(userAccountSettings.getUsername() + ","))
                            {
                                mLikedByCurrentUser = true;

                            }//if
                            else
                            {
                                mLikedByCurrentUser = false;
                            }//else

                            int length = splitUsers.length;

                            if(length == 1)
                            {
                                mLikesString = "Liked by "+splitUsers[0];
                            }//if
                            else if(length == 2)
                            {
                                mLikesString = "Liked by "+splitUsers[0] + " and "+splitUsers[1];
                            }//if
                            else if(length == 3)
                            {
                                mLikesString = "Liked by "+splitUsers[0] + ", "+splitUsers[1]+" and "+splitUsers[2];
                            }//if
                            else if(length == 4)
                            {
                                mLikesString = "Liked by "+splitUsers[0] + ", "+splitUsers[1]+", "+splitUsers[2]+" and "+splitUsers[3];
                            }//if
                            else if(length > 4)
                            {
                                mLikesString = "Liked by "+splitUsers[0] + ", "+splitUsers[1]
                                        +", "+splitUsers[2]+" and "+(splitUsers.length - 3)+" others";

                            }//if

                            setupWidgets();

                        }//onDataChange

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }//for

                if(!dataSnapshot.exists())
                {
                    mLikesString = "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }//if
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }


        });

    }//getLikesString

    /*
    * Returns a String representing number of days ago post was made
    * */
    private String getTimestampDifference()
    {
        Log.d(TAG, "getTimestampDifference: Getting Time Stamp Difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = mPhoto.getDate_created();

        try
        {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(( ( today.getTime() - timestamp.getTime()) / 1000 / 60 /60 / 24 )));
        }//try
        catch(ParseException ex)
        {
            Log.d(TAG, "getTimestampDifference: ParseException: "+ ex.getMessage());
            difference = "0";
        }//catch

        return difference;

    }//getTimestampDifference


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






    }//setupFirebaseAuth




    private void checkCurrentUser(FirebaseUser user)
    {
        Log.d(TAG, "checkCurrentUser() -> Checking if user is logged in ");
        if(user != null)
            Log.d(TAG, "checkCurrentUser() -> Current UserID : "+user.getUid());
        if(user == null)
        {
            Intent intent = new Intent(getContext(), LoginActivity.class);
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



}//ViewPostFragment
