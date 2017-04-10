package com.fatigue.driver.app;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Created by Eric on 3/2/2017.
 */
public class ModelSelectFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_model_select, container, false);

        loadModels(view);

        return view;
    }

    public RecyclerView mRecyclerView;
    public RecyclerView.Adapter mAdapter;
    public RecyclerView.LayoutManager mLayoutManager;
    public void loadModels(View view){
        File[] model_list = new File[1];

        try {
            File appDir = Environment.getExternalStorageDirectory().getAbsoluteFile();
            appDir = new File(appDir + File.separator + GlobalSettings.getAppRootFolderName());
            File fl = new File(appDir + File.separator + "Users" + File.separator + GlobalSettings.userName + File.separator + "Evaluation");

            model_list = fl.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile();
                }
            });

        } catch (Exception ex){
            System.out.println("Could not find models");
            ex.printStackTrace();
        }


        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_users);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        //DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(title_text.getContext(), DividerItemDecoration.VERTICAL);
        //mRecyclerView.addItemDecoration(dividerItemDecoration);

        // specify an adapter (see also next example)
        mAdapter = new ModelListAdapter(model_list);
        mRecyclerView.setAdapter(mAdapter);
    }




}
