package com.fatigue.driver.app;

/**
 * Created by Eric on 2/16/2017.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ModelListAdapter extends RecyclerView.Adapter<ModelListAdapter.MyViewHolder> {

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public Button button;
        public String fileName = "";

        public MyViewHolder(final View view) {
            super(view);
            button = (Button) view.findViewById(R.id.button_username);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    GlobalSettings.svmModelFileName = fileName;
                    Toast.makeText(view.getContext(), "Loaded Model: " + fileName.substring(0, fileName.lastIndexOf('.')), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public File[] model_list;
    public ModelListAdapter(File[] model_list) {
        this.model_list = model_list;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sub_user_button, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String fileName = model_list[position].getName();
        holder.fileName = fileName;

        holder.button.setText(fileName.substring(0, fileName.lastIndexOf('.')));
    }

    @Override
    public int getItemCount() {
        return model_list.length;
    }
}
