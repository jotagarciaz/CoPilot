package com.mdh.ivanmuniz.copilotapp.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mdh.ivanmuniz.copilotapp.user.User;

import java.util.ArrayList;

/*Machine class represents the autonomous vehicle
 * The class contains three attributes machineID, machineNickname and machineModel
 * machineID is the unique identification for the machine which was assigned by Volvo CE,
 * machineNickname is the name of the machine assigned the buyer
 * machineModel is the model of the Machine */

public class Machine {
    private String id;
    private int machineID;
    private String machineName;
    private String machineModel;
    private boolean isLocked;
    private String reservedBy;

    public Machine() {
        // Public no-arg constructor need for dbm
    }

    public Machine(int machineID, String name, String model, boolean isLocked) {
        this.id = "";
        this.machineID = machineID;
        this.machineName = name;
        this.machineModel = model;
        this.isLocked = isLocked;
        this.reservedBy = "";
    }

    // GETTERS
    public String getId() {
        return id;
    }

    public int getMachineID() {
        return machineID;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getMachineModel() {
        return machineModel;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    //SETTERS
    public void setId(String id) {
        this.id = id;
    }

    public void setMachineID(int machineID) {
        this.machineID = machineID;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public void setMachineModel(String machineModel) {
        this.machineModel = machineModel;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public void setReservedBy(String reservedBy) {
        this.reservedBy = reservedBy;
    }
}
