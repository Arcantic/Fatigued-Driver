package com.fatigue.driver.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by Eric on 2/4/2017.
 */

//ALSO COULD BE RENAMED TO "PROFILE" INSTEAD.
public class User {

    public User(){
    }

    public static String user_name;


    public static void selectUser(String user_name){
        User.user_name = user_name;
    }

    public static ArrayList loadUserList(Context context) {
        ArrayList<String> user_list = new ArrayList<>();

        //Load list of users from file, then return that list
        try {
            FileInputStream fis = context.openFileInput(manifest_file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                user_list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return user_list;
    }


    public static String manifest_file = "user_list.txt";
    public static void newUser(String user_name, Context context){
        File _list = new File(context.getFilesDir().getPath().toString() + "/" + manifest_file);
        try{
            if(!_list.exists()){
               //Make a new file if it doesn't exist
                _list.createNewFile();
            }
            //Append user list
            PrintWriter out = new PrintWriter(new FileOutputStream(_list, true));
            out.append(user_name+"\n");
            out.close();
        }catch(IOException e) {
            e.printStackTrace();
        }

        User.user_name = user_name;
    }


    //If user_id is zero, then no user/profile has been selected.
    public static boolean isSelected(){
        if(user_name != null)
            return true;
        else return false;
    }

    //Log the user out - return to login screen.
    public static void logout(Activity mActivity){
        Toast.makeText(mActivity.getApplicationContext(), user_name+" Logged Out", Toast.LENGTH_SHORT).show();
        user_name = null;
        Intent intent = new Intent(mActivity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(intent);
        mActivity.finish();
    }

}
