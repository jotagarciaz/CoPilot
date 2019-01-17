package com.mdh.ivanmuniz.copilotapp.user;

import com.mdh.ivanmuniz.copilotapp.object.Machine;
import com.mdh.ivanmuniz.copilotapp.object.Path;

import java.util.ArrayList;

/*The Operator class represents the app user with some added abilities such as
* driving the autonomous vehicle along a predefined path, reserving and releasing autonomous vehicles
* The class an additional attribute reservedMachines
* reservedMachines is an ArrayList that contains all the machines the user reserved */
public class Operator extends User {
    private ArrayList<Machine> reservedMachines;

    private Operator( String id, String name, String lastName, String email, Boolean admin ){
        super( id, name, lastName, email, admin );
        this.reservedMachines = new ArrayList<Machine>();
    }

    public static User getInstance( String id, String name, String lastName, String email, Boolean admin ){
        _instance = new Operator( id, name, lastName, email, admin );

        return _instance;
    }

    /*executePath() allows Operator send machines on a path */
    public void executePath(Path path){
        // TODO: execute path on reserved machines
    }

    /*reserveMachine() allows Operator to reserve a machine from a pool of available machines */
    public void reserveMachine(Machine machine){
        // TODO: updateDescription machine pool
        reservedMachines.add(machine);
    }

    /*releaseMachine() allows Operator to release machines back to the pool of available machines */
    public void releaseMachine(Machine machine){
        // TODO: updateDescription machine pool
        reservedMachines.remove(machine);
    }
}
