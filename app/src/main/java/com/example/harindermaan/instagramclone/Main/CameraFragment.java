package com.example.harindermaan.instagramclone.Main;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.harindermaan.instagramclone.R;


public class CameraFragment extends android.support.v4.app.Fragment
{
    private static final String TAG = "CameraFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_camera,container,false);
        return view;
    }//onCreateView
}//MainFragment
