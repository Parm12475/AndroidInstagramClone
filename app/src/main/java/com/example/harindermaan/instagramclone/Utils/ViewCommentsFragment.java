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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.harindermaan.instagramclone.Login.LoginActivity;
import com.example.harindermaan.instagramclone.Models.Comment;
import com.example.harindermaan.instagramclone.Models.Like;
import com.example.harindermaan.instagramclone.Models.Photo;
import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.Models.UserAccountSettings;
import com.example.harindermaan.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import java.util.zip.CheckedOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewCommentsFragment extends Fragment
{

    private static final String TAG = "ViewCommentsFragment";


    //vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;
    private Context mContext ;


    //firebase auth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;


    //widgets
    private ImageView mCheckMark,mBackArrow;
    private EditText mComment;
    private ListView mListView;



    public ViewCommentsFragment()
    {
        super();
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_view_comments,container,false);

        mBackArrow = view.findViewById(R.id.backArrow);
        mCheckMark = view.findViewById(R.id.ivPostComment);
        mComment = view.findViewById(R.id.comment);
        mListView = view.findViewById(R.id.listView);
        mComments = new ArrayList<>();
        mContext = getActivity();


        try
        {
            mPhoto = getPhotoFromBundle();

        }//try
        catch(NullPointerException ex)
        {
            Log.d(TAG, "onCreateView: NullPointerException : Photo was null from bundle : "+ex.getMessage());
        }//catch
        setupFirebaseAuth();
        return view;
    }//onCreateView


    private void setupWidgets()
    {

        CommentListAdapter adapter = new CommentListAdapter(mContext,R.layout.layout_comment,mComments);
        mListView.setAdapter(adapter);


        mCheckMark.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (!mComment.getText().toString().equals("")) {
                    Log.d(TAG, "onClick: Attempting to Submit New Comment !!");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                }//if
                else
                {
                    Toast.makeText(getActivity(), "You cant post a blank comment", Toast.LENGTH_SHORT).show();
                }//else
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Navigating Back!! ");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });


    }//setupWidgets

    public String getTimestamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));

        return sdf.format(new Date());
    }//getTimeStamp


    private void closeKeyboard()
    {
        View view = getActivity().getCurrentFocus();

        if(view != null)
        {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);

        }//if
    }//closeKeyBoard


    private void addNewComment(String newComment)
    {
        Log.d(TAG, "addNewComment: Adding New Comment to DB: "+newComment);

        String commentID = databaseReference.push().getKey(); //unique id
        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());


        //insert into photos node
        databaseReference.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        //insert into user_photos node
        databaseReference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);


    }//addNewComment



    /*
   *
    * Retrieve the photo from incoming bundle from profileActivity Interface
    *
    * */
    private Photo getPhotoFromBundle()
    {

        Log.d(TAG, "getPhotoFromBundle: Arguments: "+getArguments());
        Bundle bundle = this.getArguments();
        Log.d(TAG, "getPhotoFromBundle: Bundle: "+bundle);
        Log.d(TAG, "getPhotoFromBundle: Arguments: "+this.getArguments());

        if(bundle != null)
        {
            return bundle.getParcelable(getString(R.string.photo));
        }//if
        else
        {
            return null;
        }//else
    }



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



        if(mPhoto.getComments().size() == 0 )
        {
            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(mPhoto.getCaption());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getDate_created());
            mComments.add(firstComment);
            mPhoto.setComments(mComments);
            setupWidgets();


        }//if


        databaseReference.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Query query = databaseReference
                                .child(mContext.getString(R.string.dbname_photos))
                                .orderByChild(mContext.getString(R.string.field_photo_id))
                                .equalTo(mPhoto.getPhoto_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                                {

                                    Photo photo = new Photo();

                                    Map<String,Object> objectMap = (HashMap<String,Object>)singleSnapshot.getValue();
                                    //Log.d(TAG, "onDataChange: Map Keys: "+objectMap.keySet());
                                    //Log.d(TAG, "onDataChange: Map Values : "+objectMap.values());
                                    //Log.d(TAG, "onDataChange: Photo_ID "+objectMap.get(getString(R.string.field_photo_id)));

                                    photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                    photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                                    photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                                    photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                    photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                                    photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());


                                    mComments.clear();
                                    Comment firstComment = new Comment();
                                    firstComment.setComment(mPhoto.getCaption());
                                    firstComment.setUser_id(mPhoto.getUser_id());
                                    firstComment.setDate_created(mPhoto.getDate_created());

                                    mComments.add(firstComment);




                                    for(DataSnapshot dSnapshot : singleSnapshot
                                            .child(mContext.getString(R.string.field_comments))
                                        .getChildren())

                                    {
                                        Log.d(TAG, "onDataChange: In Loop Comments");
                                        Comment comment = new Comment();
                                        Log.d(TAG, "onDataChange: Comment User ID: "+dSnapshot.getValue(Comment.class).getUser_id());
                                        comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                                        Log.d(TAG, "onDataChange: Comment : "+dSnapshot.getValue(Comment.class).getComment());
                                        comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                                        comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());

                                        mComments.add(comment);
                                    }//for

                                    photo.setComments(mComments);
                                    mPhoto = photo;

                                    setupWidgets();






                //                    List<Like> likesList = new ArrayList<Like>();
                //                    for(DataSnapshot dSnapshot : dataSnapshot
                //                            .child(getString(R.string.field_likes)).getChildren())
                //                    {
                //                        Log.d(TAG, "onDataChange: In Loop Likes");
                //                        Like like = new Like();
                //                        like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                //                        likesList.add(like);
                //                    }//for




                                }//for

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {
                                Log.d(TAG, "onCancelled: Query Cancelled");

                            }
                        });


                    }//onChildAdded

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

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





}//ViewCommentsFragment
