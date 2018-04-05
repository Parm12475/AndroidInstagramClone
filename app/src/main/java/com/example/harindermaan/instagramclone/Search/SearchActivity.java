package com.example.harindermaan.instagramclone.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;


import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.Profile.ProfileActivity;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.harindermaan.instagramclone.Utils.UserListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SearchActivity extends AppCompatActivity
{
    private static final String TAG = "SearchActivity";
    private static final int ACTIVITY_NUM=1;
    private Context mContext=SearchActivity.this;



    //widgets
    private EditText mSearchParam;
    private ListView mListView;


    //vars
    private List<User> mUserList;
    private UserListAdapter mAdapter;


    private void hideSoftKeyboard()
    {


        if(getCurrentFocus() != null)
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

        }//if
    }//closeKeyBoard




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search );
        Log.d(TAG, "onCreate: ");

        mSearchParam =  findViewById(R.id.search);
        mListView = findViewById(R.id.listView);

        Log.d(TAG, "onCreate: Started !");

        hideSoftKeyboard();
        setupBottomNavigationView();
        initTextListener();
    }//onCreate



    private void searchForMatch(String keyword)
    {
        Log.d(TAG, "searchForMatch: Searching for a match : "+keyword);
        mUserList.clear();

        //update users list

        if(keyword.length() == 0)
        {

        }//if
        else
        {
            Log.d(TAG, "searchForMatch: Else");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

            Query query = reference
                                .child(getString(R.string.dbname_users))
                                .orderByChild(getString(R.string.field_username))
                                .equalTo(keyword);

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    Log.d(TAG, "onDataChange: ");
                    for(DataSnapshot singleSnapshot: dataSnapshot.getChildren())
                    {
                        Log.d(TAG, "onDataChange: Found User: "+singleSnapshot.getValue(User.class).toString());

                        mUserList.add(singleSnapshot.getValue(User.class));

                        //update user listview
                        updateUsersList();
                    }//for
                }//onDataChange

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });//addListenerForSingleValueEvent
        }//else
    }//searchForMatch

    private void updateUsersList()
    {
        Log.d(TAG, "updateUsersList: Updating Users List");

        mAdapter = new UserListAdapter(SearchActivity.this,R.layout.layout_user_list_item,mUserList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                Log.d(TAG, "onItemClick: Selected User: "+mUserList.get(position).toString());

                //navigate to profile activity
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                //intent.putExtra(getString(R.string.calling_activity),getString(R.string.search_activity));
                //intent.putExtra(getString(R.string.intent_user),mUserList.get(position));
                startActivity(intent);

            }//onItemClick
        });//setOnItemClickListener


    }//updateUsersList

    private void initTextListener()
    {
        Log.d(TAG, "initTextListener: initializing");

        mUserList = new ArrayList<>();

        mSearchParam.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }//beforeTextChanged

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }//onTextChanged

            @Override
            public void afterTextChanged(Editable s)
            {
                String text  = mSearchParam.getText().toString();
                searchForMatch(text);
            }//afterTextChanged
        });//addTextChangedListener
    }//initTextListener

    //Bottom Navigation View Setup
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "setupBottomNavigationView: setting Up Bottom Navigation View !");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }//setupBottomNavigationView
}//SearchActivity
