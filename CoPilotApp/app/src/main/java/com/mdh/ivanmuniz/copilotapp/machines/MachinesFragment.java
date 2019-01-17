package com.mdh.ivanmuniz.copilotapp.machines;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;

import com.mdh.ivanmuniz.copilotapp.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MachinesFragment extends Fragment {



    public MachinesFragment() {
        // Required empty public constructor
    }

    public static MachinesFragment newInstance() {
        MachinesFragment machinesFragment = new MachinesFragment();
        return machinesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_machines, container, false);

        // Attach toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar( toolbar );
        setHasOptionsMenu(true);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_listMachines);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setAdapter(new MachinesRecyclerViewAdapter());
        recyclerView.setLayoutManager(linearLayoutManager);

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
