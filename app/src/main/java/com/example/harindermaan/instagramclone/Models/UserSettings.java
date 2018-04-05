package com.example.harindermaan.instagramclone.Models;


import android.util.Log;

public class UserSettings
{
    private static final String TAG = "UserSettings";
    private User user;
    private UserAccountSettings settings;

    public UserSettings(User user, UserAccountSettings settings)
    {
        Log.d(TAG, "UserSettings: ");
        this.user = user;
        this.settings = settings;
    }//UserSettings

    public UserSettings()
    {
        Log.d(TAG, "UserSettings: ");
    }//UserSettings

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public UserAccountSettings getSettings()
    {
        return settings;
    }

    public void setSettings(UserAccountSettings settings)
    {
        this.settings = settings;
    }

    @Override
    public String toString()
    {
        return "UserSettings{" +
                "user=" + user +
                ", settings=" + settings +
                '}';
    }
}//UserSettings
