package com.mdh.ivanmuniz.copilotapp.user;

/*The Planner class represents the app user,
* its the planners task to create paths for the autonomous vehicles
* The class does currently not have any additional attributes or method in comparison with the User class */
public class Planner extends User {

    private Planner(String id, String name, String lastName, String email,  Boolean admin ){
        super( id, name, lastName, email, admin );
    }

    public static User getInstance( String id, String name, String lastName, String email, Boolean admin ){
        _instance = new Planner( id, name, lastName, email, admin );

        return _instance;
    }
}
