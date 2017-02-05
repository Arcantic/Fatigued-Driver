package com.fatigue.driver.app;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Eric on 2/4/2017.
 */

//ALSO COULD BE RENAMED TO "PROFILE" INSTEAD.
public class User {

    public User(){
    }

    public static String user_name;



    public static void changeUser(String user_name){
        loadUser(user_name);
    }

    public static void loadUser(String user_name){
        //TODO: Load save file information...
        String loaded_user_name = user_name;

        User.user_name = loaded_user_name;
    }

    //Save user_name and current timestamp to internal file.
    public static void newUser(String user_name, Context context){
        //TODO: Add user to UI selection...
        String filename = user_name;
        String directory_name = "profiles";

        //TODO: CANT MAKE DIRECTORY ON INTERNAL - MUST REWRITE FOR EXTERNAL STORAGE
        //File directory = new File(Environment.getExternalStorageDirectory() + directory_name);
        //directory.mkdirs();

        String data1 = "user_name:"+user_name;
        String data2 = "created:"+System.currentTimeMillis();

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename+".txt", Context.MODE_PRIVATE));

            outputStreamWriter.write(data1);
            outputStreamWriter.append("\n\r");
            outputStreamWriter.write(data2);

            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }


        User.user_name = user_name;
    }


    //If user_id is zero, then no user/profile has been selected.
    public static boolean isSelected(){
        if(user_name != null)
            return true;
        else return false;
    }

}
