package com.example.harindermaan.instagramclone.Utils;


public class StringManipulation
{
    public static String expandUsername(String username)
    {
       return username.replace("."," ");
    }//expandUsername

    public static String condenseUsername(String username)
    {
        return username.replace(" ",".");
    }//expandUsername


    public static String getTags(String string)
    {
        if(string.indexOf("#") > 0)
        {
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWord = false;

            for(char c : charArray)
            {
                if(c == '#')
                {
                    foundWord = true;
                    sb.append(c);
                }//if
                else
                {
                    if(foundWord)
                    {
                        sb.append(c);
                    }//if
                }//else

                if(c == ' ')
                {
                    foundWord = false;
                }//if
            }//for

            String s = sb.toString().replace(" ","").replace("#",",#");
            return s.substring(1,s.length());
        }//if

        return string;
    }//getTags

}//StringManipulation
