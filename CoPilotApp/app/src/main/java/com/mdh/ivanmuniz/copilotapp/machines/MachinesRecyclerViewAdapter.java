package com.mdh.ivanmuniz.copilotapp.machines;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mdh.ivanmuniz.copilotapp.R;
import com.mdh.ivanmuniz.copilotapp.collection.MachineCollection;
import com.mdh.ivanmuniz.copilotapp.object.Machine;
import com.mdh.ivanmuniz.copilotapp.user.Operator;
import com.mdh.ivanmuniz.copilotapp.user.Planner;
import com.mdh.ivanmuniz.copilotapp.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachinesRecyclerViewAdapter extends RecyclerView.Adapter<MachinesRecyclerViewAdapter.
        MachinesRecyclerViewHolder> implements MachineCollection.IMachineCollectionUpdate{

    private List<Machine> mList;

    // Loads the data that will be displayed in the recycler view (list)
    public MachinesRecyclerViewAdapter() {
        MachineCollection machineCollection = MachineCollection.getInstance();
        machineCollection.addListener(this);
        mList = machineCollection.getMachineList();
    }


    public void onDataEvent(Machine machine) {
        for(int i = 0 ; i < mList.size() ; i++) {
            Machine machineItem = mList.get(i);

            if(!machineItem.getId().equals(machine.getId())){
                continue;
            }

            notifyItemChanged(i);
            break;
        }
    }

    @Override
    public MachinesRecyclerViewAdapter.MachinesRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_machines_item, parent,
                false);
        return new MachinesRecyclerViewAdapter.MachinesRecyclerViewHolder(view);
    }

    // Passes each value (machine) of the machines list to the setData function to load its values
    // into the list
    @Override
    public void onBindViewHolder(@NonNull MachinesRecyclerViewHolder machinesRecyclerViewHolder, int i) {
        machinesRecyclerViewHolder.setData(mList.get(i));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //    sets the ui for list items
    public static class MachinesRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        Machine mMachine;
        private View view;
        private TextView tvMachineName;
        private TextView tvMachineModel;
        private TextView tvMachineId;
        private TextView  tvReserved;
        private ToggleButton btnStatus;

        User user = User.getInstance();

        // Database
        private FirebaseFirestore db = FirebaseFirestore.getInstance();
        private String uid = User.getInstance().getID();

        // Drawable icons
        private Drawable locked;
        private Drawable unlocked;

        public MachinesRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvMachineName = (TextView) view.findViewById(R.id.textView_machineName);
            tvMachineModel =(TextView) view.findViewById(R.id.textView_machineModel);
            tvReserved = (TextView) view.findViewById(R.id.textView_reserved);
            tvMachineId = (TextView) view.findViewById(R.id.textView_machineId);
            locked = ContextCompat.getDrawable( view.getContext(), R.drawable.baseline_lock_24 );
            unlocked = ContextCompat.getDrawable( view.getContext(), R.drawable.baseline_lock_open_24 );

            btnStatus = (ToggleButton) view.findViewById(R.id.button_machineStatus2);
            locked = ContextCompat.getDrawable( view.getContext(), R.drawable.baseline_lock_24 );
            unlocked = ContextCompat.getDrawable( view.getContext(), R.drawable.baseline_lock_open_24 );
        }

        // Receives the values of each machine and loads it into the list
        public void setData(Machine machine) {
            mMachine = machine;
            tvMachineName.setText(machine.getMachineName());
            tvMachineModel.setText(machine.getMachineModel());
            tvMachineId.setText(String.valueOf(machine.getMachineID()));

            if (machine.getReservedBy().equals("") || machine.getReservedBy().equals(uid)) {
                if (machine.getIsLocked()) {
                    btnStatus.setChecked(true);
                    btnStatus.setBackgroundDrawable(locked);
                    btnStatus.setEnabled(true);
                    tvReserved.setText("Locked");
                    tvReserved.setTextColor(Color.BLACK);
                } else {
                    btnStatus.setChecked(false);
                    btnStatus.setBackgroundDrawable(unlocked);
                    btnStatus.setEnabled(true);
                    tvReserved.setText("Unlocked");
                    tvReserved.setTextColor(Color.BLACK);
                }
            } else {
                btnStatus.setChecked(true);
                btnStatus.setBackgroundDrawable(locked);
                btnStatus.setEnabled(false);
                tvReserved.setText("Unavailable");
                tvReserved.setTextColor(Color.GRAY);
            }
            btnStatus.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v == btnStatus) {
                // Release machine
                if(mMachine.getIsLocked()) {
                    if (user instanceof  Operator) {
                        mMachine.setLocked(false);
                        mMachine.setReservedBy("");
                        db.collection("machines").document(mMachine.getId()).set(mMachine);
                        btnStatus.setBackgroundDrawable(locked);
                        tvReserved.setText("Locked");
                    } else {
                        Toast.makeText(v.getContext(), "Only operators can unlock machines", Toast.LENGTH_SHORT).show();
                    }
                }
                // Reserve machine
                else {
                    if (user instanceof Operator) {
                        mMachine.setLocked(true);
                        mMachine.setReservedBy(uid);
                        db.collection("machines").document(mMachine.getId()).set(mMachine);
                        btnStatus.setBackgroundDrawable(unlocked);
                        tvReserved.setText("Unlocked");
                    } else {
                        Toast.makeText(v.getContext(), "Only operators can lock machines", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}