package com.mdh.ivanmuniz.copilotapp.collection;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.mdh.ivanmuniz.copilotapp.object.Machine;
import com.mdh.ivanmuniz.copilotapp.object.Path;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MachineCollection implements EventListener<QuerySnapshot> {

    private static final String DB_DOCUMENT = "machines";

    // Properties
    private IMachineCollectionUpdate listener;
    private List<Machine> machineList;
    private FirebaseFirestore database;

    private static MachineCollection _instance;

    private MachineCollection(){
        machineList = new ArrayList<>();
        // Get data from Firebase.
        database = FirebaseFirestore.getInstance();
        fetchMachines();
        // Listen for path changes
        database.collection(DB_DOCUMENT).addSnapshotListener( this );
    }

    public static MachineCollection getInstance(){
        if( _instance == null ) {
            _instance = new MachineCollection();
        }
        return _instance;
    }

    private void fetchMachines() {
        database.collection(DB_DOCUMENT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Machine machine = document.toObject(Machine.class);
                                machine.setId(document.getId());
                                machineList.add(machine);
                            }
                        } else {
                            Log.d("COLLECTION", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    @Override
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
        if( e != null ){
            Log.w("PathCollection", "listen:error", e );
            return;
        }

        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    onFirebaseMachineAdd( dc.getDocument() );
                    break;
                case MODIFIED:
                    onFirebaseMachineChange( dc.getDocument() );
                    break;
                case REMOVED:
                    onFirebaseMachineRemove( dc.getDocument() );
                    break;
            }
        }
    }

    private void onFirebaseMachineAdd(QueryDocumentSnapshot document) {
        // TODO: What if a machine is added? This should add new machines to the list
    }

    private void onFirebaseMachineChange(QueryDocumentSnapshot document) {
        String documentID = document.getId();
        for(int i = 0 ; i < machineList.size() ; i++){
            Machine machine = machineList.get(i);

            if( machine == null )
                continue;

            if( machine.getId().isEmpty() || !machine.getId().equals(documentID)){
                continue;
            }

            machine.setLocked(document.getBoolean("isLocked"));
            machine.setReservedBy(document.getString("reservedBy"));

            if( listener != null )
                listener.onDataEvent(machine);
        }
    }

    private void onFirebaseMachineRemove(QueryDocumentSnapshot document) {
        // TODO: What if a machine is removed, this should remove the machine from active devices
    }

    /*
     ** Get a list of all paths available
     */
    public List<Machine> getMachineList(){
        // Return a copy, you're not supposed to modify this internal list
        return new ArrayList<>( machineList );
    }

    public void addListener(IMachineCollectionUpdate listener){
        this.listener = listener;
    }

    public interface IMachineCollectionUpdate {
        void onDataEvent(Machine machine);
    }

}
