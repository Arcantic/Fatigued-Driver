package com.fatigue.driver.app;

/**
 * Created by Eric on 2/16/2017.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyViewHolder> {

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public Button title;

        public MyViewHolder(final View view) {
            super(view);
            title = (Button) view.findViewById(R.id.button_username);

            title.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    User.selectUser((String)title.getText());//...
                    Toast.makeText(view.getContext(), "Welcome, " + title.getText(), Toast.LENGTH_SHORT).show();
                    ((LoginActivity)view.getContext()).loadMainActivity();
                }
            });
        }
    }

    public ArrayList<String> user_list;
    public UserListAdapter(ArrayList user_list) {
        this.user_list = user_list;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sub_user_button, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String username = user_list.get(position);
        holder.title.setText(username);
    }

    @Override
    public int getItemCount() {
        return user_list.size();
    }
}
