package com.fatigue.driver.app;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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


    /*
    public static void loadUser(String user_name, Context context) {
        //TODO: Load save file information...
        String loaded_user_name = user_name;

        try {
            FileInputStream fis = context.openFileInput(user_name+".txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    */


    public static void selectUser(String user_name){
        User.user_name = user_name;
    }

    public static ArrayList loadUserList(Context context) {
        ArrayList<String> user_list = new ArrayList<>();

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
        File _list = new File(manifest_file);
        try{
            if(_list.exists()==false){
               //Make a new file
                _list.createNewFile();
            }
            PrintWriter out = new PrintWriter(_list);
            out.append(user_name+"\n");
            out.close();
        }catch(IOException e) {
            System.out.println("COULD NOT LOG!!");
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
