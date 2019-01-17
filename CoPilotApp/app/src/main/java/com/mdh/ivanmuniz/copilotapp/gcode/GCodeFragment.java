package com.mdh.ivanmuniz.copilotapp.gcode;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import com.mdh.ivanmuniz.copilotapp.R;
import com.mdh.ivanmuniz.copilotapp.collection.PathCollection;
import com.mdh.ivanmuniz.copilotapp.object.Path;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GCodeFragment extends Fragment {

    private Path path;

    public GCodeFragment() {
        // Required empty public constructor
    }

    public static GCodeFragment newInstance() {
        GCodeFragment gCodeFragment = new GCodeFragment();
        return gCodeFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_gcode, container, false);

        // Attach toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar( toolbar );
        setHasOptionsMenu(true);

        // Get path instance
        Bundle data = getArguments();
        if(data != null) {
            path = PathCollection.getInstance().getPath(data.getString("pathid"));
        } else {
            // Error handling
        }

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_gcode);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setAdapter(new GCodeRecyclerViewAdapter(getActivity(),path));
        recyclerView.setLayoutManager(linearLayoutManager);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        menu.removeItem(R.id.action_search);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
