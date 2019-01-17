package com.mdh.ivanmuniz.copilotapp.collection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mdh.ivanmuniz.copilotapp.object.Path;
import com.mdh.ivanmuniz.copilotapp.object.Point;
import com.mdh.ivanmuniz.copilotapp.user.User;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathCollection implements EventListener<QuerySnapshot> {

    // Events
    public static final int INSERT_EVENT = 0;
    public static final int REMOVE_EVENT = 1;
    public static final int UPDATE_EVENT = 4;
    public static final int PREVIEW_EVENT = 8;

    private static final String DB_DOCUMENT = "path";

    // Properties
    private List<IPathCollectionEventHandler> eventListeners;
    private List<Path> pathList;
    private FirebaseFirestore database;
    private FirebaseStorage storage;


    private static PathCollection _instance;

    private PathCollection(){
        eventListeners = new ArrayList<>();
        pathList = new ArrayList<>();

        // Get data from Firebase.
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Listen for path changes
        database.collection(DB_DOCUMENT).addSnapshotListener( this );
    }

    /*
     ** Get singleton
     */
    public static PathCollection getInstance(){
        if( _instance == null )
            _instance = new PathCollection();

        return _instance;
    }

    /*
     ** Add a path
     */
    public void add( Path path ){

        // Add path locally
        pathList.add( path );

        Map<String, Object> add = new HashMap<>();
        add.put( "name", path.getName() );
        add.put( "description", path.getDescription() );
        add.put( "editedBy", User.getInstance().getID() );
        add.put( "createdBy", User.getInstance().getID() );
        add.put( "creationDate", path.getCreationDate().getTime() );
        add.put( "editDate", path.getEditDate().getTime() );
        add.put( "pathList", createViablePointList( path ) );

        final Bitmap preview = path.getPreview();

        // Add path to Firebase
        database.collection(DB_DOCUMENT)
            .add( add )
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d("PathCollection", "DocumentSnapshot written with ID: " + documentReference.getId() );

                    // Upload the preview image
                    uploadPreviewImage( documentReference.getId(), preview );
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w( "PathCollection", "Error adding document", e);
                }
            });
    }

    /*
     ** Remove a path
     */
    public void remove( Path path ){
        // Remove from Firebase
        database.collection(DB_DOCUMENT).document( path.getId() )
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d( "PathCollection", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w( "PathCollection", "Error deleting document", e);
                    }
                });


    }

    /*
     ** Change name and description for a path
     */
    public void updateDescription(String pathId, String name, String desc ){
        Map<String, Object> change = new HashMap<>();
        change.put( "name", name );
        change.put( "description", desc );
        change.put( "editedBy", User.getInstance().getID() );
        change.put( "editDate", (new Date()).getTime() );

        database.collection(DB_DOCUMENT).document( pathId )
                .update( change )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d( "PathCollection", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w( "PathCollection", "Error updating document", e);
                    }
                });
    }

    /*
     ** Update paths and preview image for a path
     */
    public void update( Path path ){
        if( path.getId().equals("") )
            return;

        final String id = path.getId();

        if( path.getPathList() == null || path.getPathList().size() == 0 )
            return;

        // Upload new pathList
        final Map<String, Object> change = new HashMap<>();
        change.put( "editedBy", User.getInstance().getID() );
        change.put( "editDate", path.getEditDate().getTime() );
        change.put( "pathList", createViablePointList( path ) );

        database.collection(DB_DOCUMENT).document( id )
            .update( change )
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d( "PathCollection", "DocumentSnapshot successfully updated!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w( "PathCollection", "Error updating document", e);
                }
            });

        // Upload new preview
        uploadPreviewImage( id, path.getPreview() );

        // Temporary fix for instant change of preview image, in case the upload/download is slow
        broadcastEvent( path, PREVIEW_EVENT );
    }

    /*
    ** Get a list of all paths available
     */
    public List<Path> getPathList(){
        // Return a copy, since this internal list is not supposed to be modified
        return new ArrayList<>( pathList );
    }

    // Get a single path if it exists
    public Path getPath( String id ){
        // Find the path object by id
        for( Path p : pathList )
        {
            if( !p.getId().equals( id ) )
                continue;

            // Return it
            return p;
        }

        // No path found
        return null;
    }

    /*
     ** Listen for Firebase events (add, updateDescription, remove)
     */
    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e ){
        if( e != null ){
            Log.w("PathCollection", "listen:error", e );
            return;
        }

        for (DocumentChange dc : snapshot.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    onFirebasePathAdd( dc.getDocument() );
                    break;
                case MODIFIED:
                    onFirebasePathChange( dc.getDocument() );
                    break;
                case REMOVED:
                    onFirebasePathRemove( dc.getDocument() );
                    break;
            }
        }
    }

    private void onFirebasePathAdd( QueryDocumentSnapshot snapshot ){

        // If there is a path with empty id, we only need to updateDescription the id value
        final String id = snapshot.getId();

        for( Path p : pathList ){
            if( !p.getId().equals("") )
                continue;

            // The new path was found, other tasks are not to be done
            p.setId( id );
            broadcastEvent( p, UPDATE_EVENT );
            return;
        }

        // Path was not found, so we need to updateDescription it
        Path path = new Path( snapshot.getString("name" ), snapshot.getString("description" ) );
        path.setId( id );
        path.setEditedBy( snapshot.getString("editedBy" ) );
        path.setCreatedBy( snapshot.getString("createdBy" ) );
        path.setCreatedDate( new Date( snapshot.getLong("creationDate" ) ) );
        path.setEditDate( new Date( snapshot.getLong( "editDate" ) ) );
        downloadPreviewImage( id );

        // Get path list
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)snapshot.get("pathList");

        if( data != null ){
            for( Map<String,Object> map : data ){
                GeoPoint point = (GeoPoint)map.get("point");
                long type = (long)map.get("type");
                long curveDirection = (long)map.get( "curveDirection" );

                path.addPoint( point.getLatitude(), point.getLongitude(), (int)type );
                Point p = path.getPoint( path.getPathListSize() - 1 );
                p.setLoad( tryGetState( map, "stateLoad" ) );
                p.setUnload( tryGetState( map, "stateUnload" ) );
                p.setWait( tryGetState( map, "stateWait" ) );
                p.setInterest( tryGetState( map, "stateInterest" ) );
                p.setFinish( tryGetState( map, "stateFinish" ) );
                p.setCurveDirection( (int)curveDirection );
                p.setCurveMagnitude( (double)map.get( "curveMagnitude" ) );
            }
        }

        pathList.add( path );
        broadcastEvent( path, INSERT_EVENT );
    }

    private void onFirebasePathChange( QueryDocumentSnapshot snapshot ){
        // Get the path that has changed
        String documentId = snapshot.getId();
        downloadPreviewImage( documentId );

        for( int i = 0; i < pathList.size(); i++ ){
            Path path = pathList.get( i );

            if( !path.equals( documentId ) )
                continue;

            path.setName( snapshot.getString("name" ) );
            path.setDescription( snapshot.getString("description" ) );
            path.setEditDate( new Date( snapshot.getLong( "editDate" ) ) );
            path.setEditedBy( snapshot.getString("editedBy" ) );

            // Get path list
            ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)snapshot.get("pathList");

            if( data != null ){
                // Clear points before getting new batch.
                path.clearPoints();

                for( Map<String,Object> map : data ){
                    GeoPoint point = (GeoPoint)map.get("point");
                    long type = (long)map.get("type");
                    long curveDirection = (long)map.get( "curveDirection" );

                    path.addPoint( point.getLatitude(), point.getLongitude(), (int)type );
                    Point p = path.getPoint( path.getPathListSize() - 1 );
                    p.setLoad( tryGetState( map, "stateLoad" ) );
                    p.setUnload( tryGetState( map, "stateUnload" ) );
                    p.setWait( tryGetState( map, "stateWait" ) );
                    p.setInterest( tryGetState( map, "stateInterest" ) );
                    p.setFinish( tryGetState( map, "stateFinish" ) );
                    p.setCurveDirection( (int)curveDirection );
                    p.setCurveMagnitude( (double)map.get( "curveMagnitude" ) );
                }
            }

            broadcastEvent( path, UPDATE_EVENT );
        }
    }

    private void onFirebasePathRemove( QueryDocumentSnapshot snapshot ){
        // Get id of path that was removed
        String documentId = snapshot.getId();

        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child( "preview/" + documentId + ".png" );
        imageRef.delete();

        for( int i = 0; i < pathList.size(); i++ ){
            Path path = pathList.get( i );

            if( !path.equals( documentId ) )
                continue;

            // Remove locally
            pathList.remove( path );

            // Send event
            broadcastEvent( path, REMOVE_EVENT );

            // If a path is removed, it can't be a favorite anymore, so we remove it
            User.getInstance().removeFavoritePath( path.getId() );
        }
    }

    private void downloadPreviewImage(final String id ){
        // When ID gets added, get the preview image for this path from the cloud storage
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("preview/" + id + ".png");

        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returned, use this as needed
                Bitmap bitmap = BitmapFactory.decodeByteArray( bytes,0, bytes.length );

                for( Path p : pathList ){
                    if( !p.getId().equals( id ) )
                        continue;

                    p.setPreview( bitmap );
                    broadcastEvent( p, PREVIEW_EVENT );
                    break;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Failed to get image.
            }
        });
    }

    private void uploadPreviewImage( final String id, final Bitmap bitmap ) {

        // In case the image trying to be uploaded is null
        if( bitmap == null )
            return;

        // Update Firebase cloud storage with new image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        // Create a storage reference
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child( "preview/" + id + ".png");

        // Upload the image
        UploadTask uploadTask = imageRef.putBytes( data );
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                downloadPreviewImage( id );
            }
        });
    }

    private ArrayList<Map<String, Object>> createViablePointList( Path path ){
        ArrayList<Map<String, Object>> pointList = new ArrayList<>();

        for( Point point : path.getPathList() ){
            HashMap<String, Object> data = new HashMap<>();
            data.put( "point", new GeoPoint( point.getLat(), point.getLng() ) );
            data.put( "type", point.getType() );
            data.put( "stateLoad", point.isLoad() );
            data.put( "stateUnload", point.isUnload() );
            data.put( "stateWait", point.isWait() );
            data.put( "stateInterest", point.isInterest() );
            data.put( "stateFinish", point.isFinish() );
            data.put( "curveDirection", point.getCurveDirection() );
            data.put( "curveMagnitude", point.getCurveMagnitude() );

            pointList.add( data );
        }

        return pointList;
    }

    private Boolean tryGetState( Map<String,Object> map, String stateName ){
        try {
            if( map.containsKey( stateName ) )
                return (Boolean)map.get( stateName );

            return false;
        } catch( Exception e ) {
            return false;
        }
    }

    /*
     ** Events handling
     */
    public void addListener( IPathCollectionEventHandler listener ){
        eventListeners.add( listener );
    }

    public void removeListener( IPathCollectionEventHandler listener ){
        eventListeners.remove( listener );
    }

    private void broadcastEvent( Path item, int type )
    {
        for( IPathCollectionEventHandler handler : eventListeners)
            handler.onDataEvent( item, type );
    }

    public interface IPathCollectionEventHandler {
        void onDataEvent( Path path, int type );
    }
}
