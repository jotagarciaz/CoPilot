package com.mdh.ivanmuniz.copilotapp.object;

import android.graphics.Bitmap;

import com.mdh.ivanmuniz.copilotapp.collection.PathCollection;
import com.mdh.ivanmuniz.copilotapp.user.User;

import java.util.ArrayList;
import java.util.Date;

/*The Path class represents a path which autonomous vehicle can follow
* The class contains four attributes id, name, description and pathList
* id is the unique identification for the path,
* name is the name of the path
* description is a short description of the path
* pathList is an ArrayList that contains all the points in the path instance */
public class Path {
    private String id;
    private String name;
    private String description;
    private ArrayList<Point> pathList;
    private Date creationDate;
    private Date editDate;
    private String createdBy;
    private String editedBy;
    private Bitmap preview;

    public Path() {
        // Public no-arg constructor need for dbm
    }

    public Path( String name, String description ){
        this.id = ""; // Id gets assigned by the database
        this.name = name;
        this.description = description;
        this.pathList = new ArrayList<>();
        this.creationDate = this.editDate = new Date();
        this.createdBy = this.editedBy = User.getInstance().getID();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Point getPoint(int index){
        return pathList.get(index);
    }

    public int getPathListSize(){
        return pathList.size();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getEditDate() {
        return editDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getEditedBy() {
        return editedBy;
    }

    public ArrayList<Point> getPathList() { return pathList; }

    public Bitmap getPreview(){
        return preview;
    }

    // Database sets this ID when it's been added
    public void setId( String id ){
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEditDate(Date editDate) {
        this.editDate = editDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.creationDate = createdDate;
    }

    public void setEditedBy(String editedBy) {
        this.editedBy = editedBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setPreview( Bitmap bitmap ){
        this.preview = bitmap;
    }

    public Boolean equals( String id ){
        return id.equals( this.id );
    }

    /*Add new point to path if valid,
    * returns true if the insert was accepted and return false if it was rejected */
    public Boolean addPoint(Double lat, Double lng, int type){
        Point point = new Point(lat, lng, type);
        Boolean isValid = true;

        // TODO: validate that path is still valid after insert
        if (isValid)
            this.pathList.add(point);

        return isValid;
    }

    public Boolean addPoint( Point point ){
        Boolean isValid = true;

        // TODO: validate that path is still valid after insert
        if (isValid)
            this.pathList.add(point);

        return isValid;
    }

    /*Edit existing point in path if valid,
    * returns true if the edit was accepted and return false if it was rejected */
    public Boolean editPoint(int index, Double lat, Double lng, int type){
        Point point = new Point(lat, lng, type);
        Boolean isValid = true;

        // TODO: validate that path is still valid after edit
        if (isValid)
            this.pathList.set(index, point);

        return isValid;
    }

    /*Remove existing point in path if valid,
    * returns true if the removal was accepted and return false if it was rejected */
    public Boolean removePoint(int index){
        Boolean isValid = true;

        // TODO: validate that path is still valid after removal
        if (isValid)
            this.pathList.remove(index);

        return isValid;
    }

    public void clearPoints(){
        this.pathList = new ArrayList<>();
    }

    public void pointEdit(int index, Point point) {
        this.pathList.set(index, point);
    }
}
