package com.example.harindermaan.instagramclone.Utils;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.harindermaan.instagramclone.Models.Comment;
import com.example.harindermaan.instagramclone.Models.UserAccountSettings;
import com.example.harindermaan.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment>
{

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;

    public CommentListAdapter(@NonNull Context context, int resource,
                              @NonNull List<Comment> objects)
    {
        super(context, resource, objects);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
    }


    private static class ViewHolder
    {
        TextView comment,username,likes,timestamp,reply;
        CircleImageView profileImage;
        ImageView like;
    }//viewholder

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        final ViewHolder holder;

        if(convertView == null)
        {
            convertView = mInflater.inflate(layoutResource,parent,false);

            holder = new ViewHolder();

            holder.comment = convertView.findViewById(R.id.comment);
            holder.username = convertView.findViewById(R.id.comment_username);
            holder.timestamp = convertView.findViewById(R.id.comment_time_posted);
            holder.reply = convertView.findViewById(R.id.comment_reply);
            holder.like = convertView.findViewById(R.id.comment_like);
            holder.likes = convertView.findViewById(R.id.comment_likes);
            holder.profileImage = convertView.findViewById(R.id.comment_profile_image);

            convertView.setTag(holder);

        }//if
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }//else


        //set the comment
        holder.comment.setText(getItem(position).getComment());

        //set the timeStampDiff
        String timeStampDifference = getTimestampDifference(getItem(position));
        if(!timeStampDifference.equals("0"))
        {
            holder.timestamp.setText(timeStampDifference + " d");
        }//if
        else
        {
            holder.timestamp.setText("today");

        }//else

        //set the username and profile image
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Log.d(TAG, "getView: "+FirebaseAuth.getInstance().getCurrentUser().getUid()+" == "+
                getItem(position).getUser_id());

        Query query = reference
                .child(mContext.getString(R.string.dbname_user_account_settings))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(getItem(position).getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onDataChange: Datasnapshot: "+dataSnapshot.getChildren());


                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    Log.d(TAG, "onDataChange: Setting Comments Widgets in for loop");
                    holder.username.setText(singleSnapshot.getValue(UserAccountSettings.class).getUsername());

                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(singleSnapshot.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.profileImage);

                }//for

                Log.d(TAG, "onDataChange: ENDS");


            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "onCancelled: Query Cancelled");

            }
        });
       try
       {
           if(position == 0)
           {
               holder.like.setVisibility(View.GONE);
               holder.likes.setVisibility(View.GONE);
               holder.reply.setVisibility(View.GONE);

           }//if
       }//try
        catch(NullPointerException ex)
        {
            Log.d(TAG, "getView: NullPointerException "+ex.getMessage());
        }//catch

        return convertView;
    }

    private String getTimestampDifference(Comment comment)
    {
        Log.d(TAG, "getTimestampDifference: Getting Time Stamp Difference");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();

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
}
