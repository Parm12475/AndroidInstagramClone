package com.example.harindermaan.instagramclone.Utils;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.harindermaan.instagramclone.Models.User;
import com.example.harindermaan.instagramclone.Models.UserAccountSettings;
import com.example.harindermaan.instagramclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends ArrayAdapter<User>
{
    private static final String TAG = "UserListAdapter";

    private LayoutInflater mInflater;
    private List<User> mUsers = null;
    private int layoutResource;
    private Context mContext;



    public UserListAdapter(@NonNull Context context, int resource, @NonNull List<User> objects)
    {
        super(context, resource, objects);
        mContext = context;
        layoutResource = resource;
        mUsers = objects;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

       
    }//UserListAdapter

    private static class ViewHolder
    {
        TextView username,email;
        CircleImageView profileImage;
    }//ViewHolder

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Log.d(TAG, "getView: ");
        final ViewHolder holder;

        if(convertView == null)
        {
            convertView = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.username);
            holder.email = convertView.findViewById(R.id.email);
            holder.profileImage = convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);

        }//if
        else
        {
            holder = (ViewHolder) convertView.getTag();

            Log.d(TAG, "getView: "+getItem(position).getUsername());
            Log.d(TAG, "getView: "+getItem(position).getEmail());
            Log.d(TAG, "getView: "+getItem(position).getUser_id());
            Log.d(TAG, "getView: User ID: "+mContext.getString(R.string.field_user_id));

        }//else

        holder.username.setText(getItem(position).getUsername());
        holder.email.setText(getItem(position).getEmail());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: Found User: "+singleSnapshot.getValue(UserAccountSettings.class).toString());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),holder.profileImage);
                }//for
            }//onDataChange

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }//onCancelled
        });//addListenerForSingleValueEvent


        return convertView;
    }//getView
}//UserListAdapter
