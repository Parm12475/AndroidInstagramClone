package com.example.harindermaan.instagramclone.Utils;


import java.io.File;
import java.util.ArrayList;

public class FileSearch
{

    //search a directory and return list of all directories  contained inside
    public static ArrayList<String> getDirectoryPaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();

        for(int i=0;i<listFiles.length;i++)
        {
            if(listFiles[i].isDirectory())
            {
                pathArray.add(listFiles[i].getAbsolutePath());

            }//if
        }//for

        return pathArray;
    }//getDirectoryPaths


    //search a directory and return list of all files contained inside
    public static ArrayList<String> getFilePaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();

        for(int i=0;i<listFiles.length;i++)
        {
            if(listFiles[i].isFile())
            {
                pathArray.add(listFiles[i].getAbsolutePath());

            }//if
        }//for

        return pathArray;
    }//getFilePaths

}//FileSearch
