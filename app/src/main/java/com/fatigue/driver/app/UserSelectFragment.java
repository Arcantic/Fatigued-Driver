package com.fatigue.driver.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Eric on 11/14/2016.
 */

public class UserSelectFragment extends Fragment{
    Button button_start;
    Button button_new_user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_user_select, container, false);

        loadUserList();

        //TODO: Add buttons programatically using a button fragment. Right now, just using a temporary "Start"-like button.
        //button_start = (Button)view.findViewById(R.id.button_start_temp);
        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                User.selectUser((String)button_start.getText());//...
                Toast.makeText(getActivity().getApplicationContext(), "User Selected...", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).loadMainFragment();
            }
        });

        //TODO: Make this button actually open a new user screen
        //New User...
        button_new_user = (Button)view.findViewById(R.id.button_newuser_temp);
        button_new_user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                User.newUser("Shia LaBeouf", getActivity());
                Toast.makeText(getActivity().getApplicationContext(), "New User Created...", Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).loadMainFragment();
            }
        });

        return view;
    }

    public void loadUserList(){
        ArrayList user_list = User.loadUserList(getContext());

        //Get the user_list layout
        //Loop arraylist elements
        //for each arraylist element, add a sub_user_button to the user_list layout
        //Each button needs to be named after each user
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}