package com.fatigue.driver.app;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Eric on 11/14/2016.
 */

public class UserSelectFragment extends Fragment{
    FloatingActionButton button_new_user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_select, container, false);

        //Load users
        loadUserList(view, inflater);

        //New User...
        button_new_user = (FloatingActionButton)view.findViewById(R.id.button_newuser);
        button_new_user.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment dialog = new NewUserDialog();
                dialog.show(getActivity().getSupportFragmentManager(), "NewUserDialog");

                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }
        });

        return view;
    }

    public void loadUserList(View view, LayoutInflater inflater){
        ArrayList user_list = User.loadUserList(getContext());
        System.out.println("USER LIST SIZE: " + user_list.size());

        RelativeLayout parent_layout = (RelativeLayout)view.findViewById(R.id.layout_root_users);

        ArrayList<View> children = new ArrayList<>();

        //Loop through user list and add a child layout for each
        for(int i=0; i<user_list.size(); i++) {
            System.out.println("Adding User...");

            //Inflate the child layout
            View child = inflater.inflate(R.layout.sub_user_button, parent_layout, false);
            RelativeLayout child_layout = (RelativeLayout) child.findViewById(R.id.layout_user_name);

            //Set params on the child layout
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) child_layout.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            if(i > 0)
                layoutParams.addRule(RelativeLayout.BELOW, children.get(i-1).getId());
            layoutParams.setMargins(0, 0, 0, 8);
            child_layout.setLayoutParams(layoutParams);

            //Set the button info and click
            final Button button = (Button) child.findViewById(R.id.button_user_name);
            button.setText((String) user_list.get(i));
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    User.selectUser((String)button.getText());//...
                    Toast.makeText(getActivity().getApplicationContext(), "Welcome, " + button.getText(), Toast.LENGTH_SHORT).show();
                    ((MainActivity)getActivity()).loadMainFragment();
                }
            });

            //Add child to parent view, then add to list
            child.setId(i+1);
            parent_layout.addView(child);
            children.add(child);
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}