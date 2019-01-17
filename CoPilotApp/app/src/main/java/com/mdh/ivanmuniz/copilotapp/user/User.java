package com.mdh.ivanmuniz.copilotapp.user;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mdh.ivanmuniz.copilotapp.object.Path;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*The User class represents the app user,
* The Operator and Planner classes inherit attributes and methods from this class
* The class contains five attributes userID, name, lastName, email, and password
* userID is the unique identification for the user,
* name and lastName are the user´s first name and last name respectively
* email is the work email of the user and serves as an identifier when logging in to the system
* password is the password the user use when logging in to the system */
public abstract class User {
    private String userID;
    private String name;
    private String lastName;
    private String email;
    private boolean admin;
    private List<String> favorites = null;

    protected static User _instance = null;

    public static User getInstance(){
        return _instance;
    }

    public static User getInstance( String id, String name, String lastName, String email, Boolean admin ){
        return null;
    }

    public User( String id, String name, String lastName, String email,  Boolean admin ){
        this.userID = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.admin = admin;
    }

    public String getID() { return userID; }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getAdmin() { return admin; }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAdmin(Boolean isAdmin) { this.admin = isAdmin; }

    // Initially set the favorites that are gathered from the databse
    public void setFavorites(List<String> list){
        this.favorites = list;
    }

    // Method used to add a new favorite path
    public void addFavoritePath( String pathId ){
        if( favorites == null || favorites.contains( pathId ) )
            return;

        favorites.add( pathId );
        updateFavoritesDatabase();
    }

    // Method used to remove a already existing favorite
    public void removeFavoritePath( String pathId ){
        if( favorites == null || !favorites.contains( pathId ) )
            return;

        favorites.remove( pathId );
        updateFavoritesDatabase();
    }

    private void updateFavoritesDatabase(){
        Map<String, Object> update = new HashMap<>();
        update.put( "favorite", favorites );

        // Update database too.
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference ref = database.collection( "user" ).document( this.userID );
        ref.set( update, SetOptions.merge() );
    }
}

