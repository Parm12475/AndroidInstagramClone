package com.example.harindermaan.instagramclone.Share;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.harindermaan.instagramclone.Profile.AccountSettingsActivity;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.Permissions;


public class PhotoFragment extends android.support.v4.app.Fragment
{
    private static final String TAG = "PhotoFragment";

    //constant
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: Started !!");
        View view = inflater.inflate(R.layout.fragment_photo,container,false);
        Button btnLaunchCamera = view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Launching Camera !!");

                if(((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM)
                {
                    if(((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0]))
                    {
                        Log.d(TAG, "onClick: Starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
                    }//if
                    else
                    {
                        Intent intent = new Intent(getActivity(),ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }//else
                }//if

            }//onClick
        });

        return view;
    }//onCreateView


    private boolean isRootTask()
    {
        if( ((ShareActivity)getActivity()).getTask() == 0 )
        {
            return true;
        }//if
        else
        {
            return false;
        }//else if
    }//isRootTask


    //to capture clicked image or handles data that is coming from camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE)
        {
            Log.d(TAG, "onActivityResult: Done Taking a photo");
            Log.d(TAG, "onActivityResult: Attempting to navigate to final share screen !!");

            Bitmap bitmap;
            bitmap = (Bitmap)data.getExtras().get("data");
            
            if(isRootTask())
            {
                try
                {
                    Log.d(TAG, "onActivityResult: Received New Bitmap from Camera ");

                    Intent intent = new Intent(getActivity(),NextActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap),bitmap);
                    startActivity(intent);


                }//try
                catch(NullPointerException ex)
                {
                    Log.d(TAG, "onActivityResult: NullPointerException "+ex);


                }//catch
            }//if
            else
            {
                try
                {
                    Log.d(TAG, "onActivityResult: Received New Bitmap from Camera ");

                    Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap),bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();


                }//try
                catch(NullPointerException ex)
                {
                    Log.d(TAG, "onActivityResult: NullPointerException "+ex);


                }//catch

            }//else

        }//if
    }//onActivityResult
}//PhotoFragment
