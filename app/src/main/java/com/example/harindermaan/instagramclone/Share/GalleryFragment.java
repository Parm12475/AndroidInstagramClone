package com.example.harindermaan.instagramclone.Share;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.harindermaan.instagramclone.Profile.AccountSettingsActivity;
import com.example.harindermaan.instagramclone.R;
import com.example.harindermaan.instagramclone.Utils.FilePaths;
import com.example.harindermaan.instagramclone.Utils.FileSearch;
import com.example.harindermaan.instagramclone.Utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;


public class GalleryFragment extends android.support.v4.app.Fragment
{
    private static final String TAG = "GalleryFragment";
    private static final int NUM_GRID_COLUMNS = 3;


    //widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar mProgressbar;
    private Spinner directorySpinner;



    //vars
    private ArrayList<String> directories;
    private String mAppend = "file:/";
    private String mSelectedImage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: Started !!");
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);

        galleryImage = view.findViewById(R.id.galleryImageView);
        gridView = view.findViewById(R.id.gridView);
        directorySpinner = view.findViewById(R.id.spinnerDirectory);
        mProgressbar = view.findViewById(R.id.progressBar);
        mProgressbar.setVisibility(View.GONE);

        directories = new ArrayList<>();

        ImageView shareClose = view.findViewById(R.id.ivCloseShare);
        shareClose.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Closing Gallery Fragment !!");
                getActivity().finish();
            }
        });

        TextView nextScreen = view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: Navigating to the final share screen");

                if(isRootTask())
                {
                    Intent intent = new Intent(getActivity(),NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    startActivity(intent);
                }//if
                else
                {
                    //coming from editProfile screen
                    Intent intent = new Intent(getActivity(),AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }//else
            }
        });


        init();
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


    private void init()
    {
        //check for other folders inside "/storage/emulated/0/pictures"
        Log.d(TAG, "init: ");

        FilePaths filePaths = new FilePaths();



        if( FileSearch.getDirectoryPaths(filePaths.PICTURES) != null)
        {
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURES);

        }//if

        directories.add(filePaths.CAMERA);

        ArrayList<String> directoryNames = new ArrayList<>();
        for(int i=0;i<directories.size();i++)
        {
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(index);
            directoryNames.add(string);
        }//for(i

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "onItemSelected: Selected : "+directories.get(position));

                //setup our image grid for the directory chosen
                setupGridView(directories.get(position));

            }//onItemSelected

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });//setOnItemSelectedListener


    }//init


    private void setupGridView(String selectedDirectory)
    {
        Log.d(TAG, "setupGridView: Selected Directory: "+selectedDirectory);
        final ArrayList<String> imgURLs = FileSearch.getFilePaths(selectedDirectory);

        //to ensure grid pics have same length and width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        //use grid adpater to adapt images to gridview
        GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,mAppend,imgURLs);
        gridView.setAdapter(adapter);


        //set the first imageview to be displayed when the activity fragment view is inflated
        try
        {
            setImage(imgURLs.get(0),galleryImage,mAppend);
            mSelectedImage = imgURLs.get(0);
        }//try
        catch(ArrayIndexOutOfBoundsException ex)
        {
            Log.d(TAG, "setupGridView: ArrayIndexOutOfBoundsException "+ex.getMessage());
        }//catch


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "onItemClick: Selected an image "+imgURLs.get(position));
                setImage(imgURLs.get(position),galleryImage,mAppend);
                mSelectedImage = imgURLs.get(position);
            }
        });

    }//setupGridView

    private void setImage(String img_URL,ImageView image,String append)
    {
        Log.d(TAG, "setImage: Setting Image ");

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(append + img_URL, image, new ImageLoadingListener()
        {
            @Override
            public void onLoadingStarted(String imageUri, View view)
            {
                mProgressbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressbar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressbar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressbar.setVisibility(View.INVISIBLE);
            }
        });
    }//set_Image


    
}//GalleryFragment
