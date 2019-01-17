package com.mdh.ivanmuniz.copilotapp.map;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.content.ContentValues;

import android.location.Location;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.darwindeveloper.horizontalscrollmenulibrary.custom_views.HorizontalScrollMenuView;
import com.darwindeveloper.horizontalscrollmenulibrary.extras.MenuItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;

import com.google.maps.android.SphericalUtil;
import com.mdh.ivanmuniz.copilotapp.R;
import com.mdh.ivanmuniz.copilotapp.collection.PathCollection;
import com.mdh.ivanmuniz.copilotapp.list.IPathListItemListener;
import com.mdh.ivanmuniz.copilotapp.list.PathListItem;
import com.mdh.ivanmuniz.copilotapp.object.Path;
import com.mdh.ivanmuniz.copilotapp.object.Point;
import com.mdh.ivanmuniz.copilotapp.object.PointType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, IPathListItemListener {
    private HorizontalScrollMenuView horizontalScrollMenuView;
    private ImageButton toggleButton;
    private ImageButton setToCurrentPositionButton;

    private TextView markerLocationTextView;
    private Marker nextPointMarker;
    private ArrayList<Marker> pointMarkerList;

    private Path path;
    private HashMap<String, ArrayList<Polyline>> drawnPathPolyLines = new HashMap<>();
    private HashMap<String, ArrayList<Marker>> drawnPathMarkers = new HashMap<>();
    private ArrayList<Path> drawnPaths = new ArrayList<>();
    private Boolean editing = false;
    private Boolean nextPointIsSet = false;
    private Boolean startPointIsSet = false;
    private int currentSelectedIndex = -1;

    private MapView mapView;
    private GoogleMap gMap;

    private MapPathRecyclerViewAdapter mapPathRecyclerViewAdapter;

    private LatLng currentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    public final int PERMISSIONS_REQUEST = 100;

    private final int ACTION_DELETE = 8;

    /*
     ** VIEW SPECIFIC METHODS
     */

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        MapFragment mapFragment = new MapFragment();
        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle data = getArguments();

        if(data != null){
            editing = !data.getBoolean("createNew");

            if(!editing) {
                /*
                 ** CREATE PATH ENTRY POINT
                 */
                path = new Path(data.getString("pathname"), data.getString("pathdescription"));

            } else {
                /*
                ** EDIT PATH ENTRY POINT
                 */
                // There is a difference between creating a new path object and getting an old one.
                path = PathCollection.getInstance().getPath(data.getString("pathid"));

            }
        }

        pointMarkerList = new ArrayList<>();

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Attach toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar( toolbar );
        setHasOptionsMenu(true);

        // Find the search bar in the slide out list
        SearchView searchView = view.findViewById(R.id.map_path_list_search);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Bind the path list view
        View list = view.findViewById( R.id.map_path_list );

        if (list instanceof RecyclerView) {
            Context context = list.getContext();
            RecyclerView recyclerView = (RecyclerView) list;
            LinearLayoutManager layoutManager = new LinearLayoutManager( context );
            recyclerView.setLayoutManager( layoutManager );
            mapPathRecyclerViewAdapter = new MapPathRecyclerViewAdapter( this, layoutManager, context, path );
            recyclerView.setAdapter( mapPathRecyclerViewAdapter );

            // Attach search listener
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mapPathRecyclerViewAdapter.filter(newText);
                    return false;
                }
            });
        }

        /* Location */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        /* Bottom menu view */
        horizontalScrollMenuView = view.findViewById(R.id.horizontal_menu);

        horizontalScrollMenuView.addItem(getString(R.string.button_start_point), R.drawable.ic_start_active);
        horizontalScrollMenuView.addItem(getString(R.string.button_line), R.drawable.ic_line_inactive);
        horizontalScrollMenuView.addItem(getString(R.string.button_curve), R.drawable.ic_curve_inactive);
        horizontalScrollMenuView.addItem(getString(R.string.button_load), R.drawable.ic_load_inactive);
        horizontalScrollMenuView.addItem(getString(R.string.button_unload), R.drawable.ic_unload_inactive);
        horizontalScrollMenuView.addItem(getString(R.string.button_wait), R.drawable.ic_wait_inactive);
        horizontalScrollMenuView.addItem(getString(R.string.button_interest_point), R.drawable.ic_interest_point_inactive);
        horizontalScrollMenuView.addItem(getString(R.string.button_finish_point), R.drawable.ic_finish_inactive);
        horizontalScrollMenuView.addItem(getString(R.string.button_delete_point), R.drawable.ic_delete_inactive);

        horizontalScrollMenuView.setOnHSMenuClickListener(new HorizontalScrollMenuView.OnHSMenuClickListener() {
            @Override
            public void onHSMClick(MenuItem menuItem, int position) {
                // Handle point type selection
                handlePointTypeSelection(position);
            }
        });

        /* Map view */
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        try { MapsInitializer.initialize(getActivity().getApplicationContext()); }
        catch (Exception e) { e.printStackTrace(); }

        mapView.getMapAsync(this);

        /* Buttons */
        toggleButton = view.findViewById(R.id.imageButton_toggle);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMapType();
            }
        });

        setToCurrentPositionButton = view.findViewById(R.id.imageButton_set_to_current_location);

        /* Text view */
        markerLocationTextView = view.findViewById(R.id.textView_marker_location);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        menu.removeItem(R.id.action_search);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
     ** SLIDER SPECIFIC METHODS
     */

    // Handles check-event from selecting a path item in the slide in view
    public void onItemEvent(PathListItem item, int type){
        switch( type ){
            case MapPathRecyclerViewAdapter.CHECK_EVENT:
                // Path in list was checked, draw it on map
                drawnPaths.add( item.getPath() );
                drawPathOnMap(item.getPath(), Color.BLUE, true );
                break;

            case MapPathRecyclerViewAdapter.UNCHECK_EVENT:
                // Path in list unchecked, remove markers and polylines from map
                drawnPaths.remove( item.getPath() );
                removeDrawnPath(item.getPath().getId());
                break;
        }
    }

    /*
    ** MAP SPECIFIC METHODS
     */

    // Switch point type or set a flag
    private void switchPointType(int newType){
        // Replace the current point
        Point currentPoint = path.getPoint(currentSelectedIndex);

        // Don't try to change the start point
        if(currentPoint.getType() == PointType.START)
            return;

        switch(newType){
            case PointType.LINE:
                currentPoint.setType(newType);
                redrawAllPaths();
                break;

            case PointType.CURVE:
                // Launch popup to enter curve values
                launchCurveEditDialog( currentPoint, currentSelectedIndex );
                return;

            /* If load, unload or wait, just flip the flag */
            case PointType.LOAD:
                currentPoint.setLoad(!currentPoint.isLoad());
                break;

            case PointType.UNLOAD:
                currentPoint.setUnload(!currentPoint.isUnload());
                break;

            case PointType.INTEREST:
                currentPoint.setInterest(!currentPoint.isInterest());
                break;

            case PointType.WAIT:
                currentPoint.setWait(!currentPoint.isWait());
                break;

            case ACTION_DELETE:
                // Make sure to modify all the tags after the current index
                for( int i = currentSelectedIndex; i < path.getPathListSize(); i++ ){
                    pointMarkerList.get( i ).setTag( i - 1 );
                }

                // Remove the point
                path.removePoint( currentSelectedIndex );

                // Hide the info window
                pointMarkerList.get( currentSelectedIndex ).hideInfoWindow();

                // Remove the map marker
                pointMarkerList.remove( currentSelectedIndex );

                // Set the last point to the current index
                currentSelectedIndex = path.getPathListSize() - 1;

                // In case its the last point being removed, make sure to set the new finish point
                path.getPoint( currentSelectedIndex ).setFinish( true );

                // Show last marker info
                pointMarkerList.get( currentSelectedIndex ).showInfoWindow();

                // Redraw paths after a point has been deleted
                redrawAllPaths();
                return;
        }

        // Show the currents point type
        Marker current = pointMarkerList.get(currentSelectedIndex);
        String text = Integer.toString( currentSelectedIndex + 1 ) + " - " + currentPoint.toString();
        current.setTitle(text);
        current.showInfoWindow();

        // Hide the nextPointMarkers type
        nextPointMarker.hideInfoWindow();
    }

    // Handle for what happens when a point type is selected
    private void handlePointTypeSelection(int position){
        if( position == PointType.FINISH ){
            // Launch WARNING that user is about to finish the path
            // TODO: Launch popup asking if we really want to finish
            //....

            // What if a new marker is placed, but line/curve hasn't been selected?
            // Quick fix: Set last proper point to finish again, and hide the new marker
            path.getPoint( path.getPathListSize() - 1 ).setFinish( true );
            nextPointMarker.setPosition(new LatLng(90.0, 0.0));

            // Update path in PathCollection
            // Create a preview of the map and add to path
            // We're now adding/updating the path in this method since we have to
            // wait for the async preview image
            createPathPreview();

            return;
        }

        // If the path is empty, the point must be of type START
        if(path.getPathListSize() == 0 && position != PointType.START){
            Toast.makeText(getContext(),  "Add a start point first", Toast.LENGTH_LONG).show();
            return;
        }

        LatLng pos = nextPointMarker.getPosition();
        Point point = new Point(pos.latitude, pos.longitude, new PointType(position));

        // If a new marker has been placed, add point to path if type was start, line or curve
        if(nextPointIsSet){
            switch(position) {
                case PointType.START:
                    // Add only if path is empty
                    if(path.getPathListSize() == 0){
                        addPointToPath(point);
                        startPointIsSet = true;
                    }
                    //else{
                    // TODO: if path is not empty, this point should be added before the current starting point
                    //}
                    break;

                case PointType.LINE:
                    addPointToPath(point);
                    break;

                case PointType.CURVE:
                    // Launch popup to enter curve values
                    launchCurveEditDialog( point, -1 );
                    break;

                default:
                    // You cannot not add a new point that is not start, line or curve
                    Toast.makeText(getContext(), "Create a start, line, or curve point first", Toast.LENGTH_SHORT).show();
            }
        }
        // If an already existing marker was selected, change type/state
        else {
            if(path.getPathListSize() > 0)
                switchPointType(position);
        }
    }

    private void launchCurveEditDialog( final Point point, final int index ){
        View mView = getLayoutInflater().inflate(R.layout.layout_edit_curve_dialog, null);
        final EditText editAngle = mView.findViewById(R.id.editAngleText);
        final SeekBar seekBar = mView.findViewById(R.id.seekBarAngle);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int level = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                level = progresValue - 90;
                if( level == 0 )
                    seekBar.setProgress( ( progresValue < 0 )? 89 : 91 );
                else
                    editAngle.setText( String.valueOf( level ) );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editAngle.setText( String.valueOf( level ) );
            }
        });

        double mag = point.getCurveMagnitude();
        double startVal = (mag == 0 || mag == 1)? 1 : mag * 90 * point.getCurveDirection();

        seekBar.setProgress( (int)startVal + 90 );
        seekBar.requestFocus();

        final AlertDialog mDialog = new AlertDialog.Builder(this.getContext())
                .setTitle("Create a curve")
                .setMessage("Select curvature" )
                .setIcon(R.drawable.ic_add_circle_black_24dp)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .setView(mView)
                .show();

        // This button will not automatically dismiss the dialog
        Button positiveButton = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = editAngle.getText().toString();

                if(!str.isEmpty()){
                    mDialog.dismiss();

                    try
                    {
                        int input = Integer.valueOf( str );

                        int dir = ( input < 0 )? -1 : 1;
                        double mag = (double)Math.abs( input ) / 90;

                        point.setCurveMagnitude( mag );
                        point.setCurveDirection( dir );

                        // Add the curve
                        if( !nextPointIsSet ){
                            point.setType( PointType.CURVE );
                            redrawAllPaths();

                            // Show the currents point type
                            Marker current = pointMarkerList.get( index );
                            String text = Integer.toString( index + 1 ) + " - " + point.toString();
                            current.setTitle(text);
                            current.showInfoWindow();
                            currentSelectedIndex = index;

                            // Hide the nextPointMarkers type
                            nextPointMarker.hideInfoWindow();
                        }
                        else
                            addPointToPath(point);
                    }
                    catch( Exception e )
                    {
                        Toast.makeText(getContext(), "Error: parsing curve value", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), "Error: missing curve value", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (gMap == null)
            gMap = googleMap;

        if(nextPointMarker == null){
            nextPointMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(90.0, 0.0)).draggable(true));
            // Set identifier to avoid errors.
            nextPointMarker.setTag( -1 );
        }

        // Handle marker drag
        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                setMarkerLocationText(marker.getPosition());
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                setMarkerLocationText(marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                setMarkerLocationText(marker.getPosition());
            }
        });

        // Handle marker clicks
        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int index = (int)marker.getTag();

                if( index == -1 )
                    return false;

                currentSelectedIndex = index;
                nextPointIsSet = false;

                // Hide marker
                nextPointMarker.setPosition(new LatLng(90.0, 0.0));

                // Set edit actions
                setEditMode( true );

                return false;
            }
        });

        // Handle clicks on the map that isnt a marker
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                nextPointMarker.setPosition(latLng);
                setMarkerLocationText(latLng);

                // A new point is placed
                nextPointIsSet = true;

                // Set new point actions
                setEditMode( false );
            }
        });

        // Handle current position button click
        setToCurrentPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurrentLocation();

                if(currentLocation != null) {
                    nextPointMarker.setPosition(currentLocation);

                    // A new point is placed
                    nextPointIsSet = true;

                    // Set new point actions
                    setEditMode( false );

                    setMarkerLocationText(currentLocation);
                    showLocation(currentLocation);
                }
            }
        });

        gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        gMap.getUiSettings().setCompassEnabled(true);
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setZoomGesturesEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        gMap.getUiSettings().setMapToolbarEnabled(false);

        // Draw path and show location of first point
        drawPathOnMap(path, Color.RED, false);

        // Show current marker
        if( pointMarkerList.size() > 0 )
            pointMarkerList.get( pointMarkerList.size() - 1 ).showInfoWindow();

        // Show path or current location
        if(path.getPathListSize() > 0)
            showPathOverview();
        else
            showCurrentLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /*
     * Sets the icons on the specified positions to active type and the other positions to the
     * inactive type. If no positions are specified, it will set all icons to the active type.
     */
    private void setActiveMenuItems(int ... positions){
        boolean[] flags = new boolean[9]; // default value is false

        // If no positions are specified, set all flags to setActive
        if(positions.length == 0)
            Arrays.fill(flags, true);
        else{
            for(int p : positions){
                if (p < 0 | p > 8)
                    throw new IllegalArgumentException("Position " + p + " does not exist in horizontalScrollMenuView");

                flags[p] = true;
            }
        }

        // If true, set icon on the corresponding position to active version, else set it to inactive version
        horizontalScrollMenuView.editItem( 0, horizontalScrollMenuView.getItem(0).getText(), flags[0] ? R.drawable.ic_start_active : R.drawable.ic_start_inactive, false, 0);
        horizontalScrollMenuView.editItem( 1, horizontalScrollMenuView.getItem(1).getText(), flags[1] ? R.drawable.ic_line_active : R.drawable.ic_line_inactive, false, 0);
        horizontalScrollMenuView.editItem( 2, horizontalScrollMenuView.getItem(2).getText(), flags[2] ? R.drawable.ic_curve_active : R.drawable.ic_curve_inactive, false, 0);
        horizontalScrollMenuView.editItem( 3, horizontalScrollMenuView.getItem(3).getText(), flags[3] ? R.drawable.ic_load_active : R.drawable.ic_load_inactive, false, 0);
        horizontalScrollMenuView.editItem( 4, horizontalScrollMenuView.getItem(4).getText(), flags[4] ? R.drawable.ic_unload_active : R.drawable.ic_unload_inactive, false, 0);
        horizontalScrollMenuView.editItem( 5, horizontalScrollMenuView.getItem(5).getText(), flags[5] ? R.drawable.ic_wait_active : R.drawable.ic_wait_inactive, false, 0);
        horizontalScrollMenuView.editItem( 6, horizontalScrollMenuView.getItem(6).getText(), flags[6] ? R.drawable.ic_interest_point_active : R.drawable.ic_interest_point_inactive, false, 0);
        horizontalScrollMenuView.editItem( 7, horizontalScrollMenuView.getItem(7).getText(), flags[7] ? R.drawable.ic_finish_active : R.drawable.ic_finish_inactive, false, 0);
        horizontalScrollMenuView.editItem( 8, horizontalScrollMenuView.getItem(8).getText(), flags[8] ? R.drawable.ic_delete_active : R.drawable.ic_delete_inactive, false, 0);
    }

    private void setEditMode( Boolean edit ){
        if( edit ) {
            setActiveMenuItems( 1, 2, 3, 4, 5, 6, 7, 8 );
            return;
        }

        if( startPointIsSet )
            setActiveMenuItems( 1, 2, 7 );
        else
            setActiveMenuItems( 0 );
    }

    private void setMarkerLocationText(LatLng newLocation){
        markerLocationTextView.setText( String.format( getString( R.string.textView_marker_location_set ), String.format("%.3f", newLocation.latitude), String.format("%.3f", newLocation.longitude) ) );
    }

    private void toggleMapType() {
        if(gMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN)
            gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else
            gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    private void updateCurrentLocation(){
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST);
        } else {
            Log.d(ContentValues.TAG, "updateCurrentLocation: permission granted");
        }

        // TODO: Implement onRequestPermissionsResult?
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!= null){
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            currentLocation = new LatLng(latitude, longitude);
                        }else{

                                double tempLatitude = 59.4005705;
                                double tempLongitude = 16.440150399999993;

                                currentLocation = new LatLng(tempLatitude, tempLongitude);


                        }

                    }
                });

    }

    /* Moves the camera to specified location */
    private void showLocation(LatLng loc) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(15).build();
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void showCurrentLocation() {
        updateCurrentLocation();

        if(currentLocation != null)
            showLocation(currentLocation);
    }

    private void showPathOverview() {
        if(path.getPathListSize() == 0)
            return;

        gMap.setPadding(150, 150, 150, 150);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Point p : path.getPathList()) {
            builder.include(p.getLatLng());
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 10);
        gMap.moveCamera(cameraUpdate);
        gMap.setPadding(0, 0, 0, 0);
    }

    private void redrawAllPaths(){
        gMap.clear();
        drawnPathMarkers.clear();
        drawnPathPolyLines.clear();
        pointMarkerList.clear();

        nextPointMarker = gMap.addMarker(new MarkerOptions().position(new LatLng(90.0, 0.0)).draggable(true));
        // Set identifier to avoid errors..
        nextPointMarker.setTag( -1 );

        drawPathOnMap( path, Color.RED, false );

        for( int i = 0; i < drawnPaths.size(); i++ ){
            drawPathOnMap( drawnPaths.get( i ), Color.BLUE, true );
        }
    }

    /* Draws a path on the map consisting of several Point(s) */
    private void drawPathOnMap( Path path, int color, Boolean overlay ) {
        ArrayList<Marker> drawnMarkers = new ArrayList<>();
        ArrayList<Polyline> drawnPolylines = new ArrayList<>();

        if(path.getPathListSize() == 0)
            return;

        startPointIsSet = true;

        // Add a marker for starting point
        Marker marker = gMap.addMarker(new MarkerOptions()
                .position(path.getPoint(0).getLatLng())
                .title("1 - " + path.getPoint(0).toString() ) );
        marker.setAlpha( 0.8f );
        marker.setVisible( !overlay );

        // Apparently comparing markers is hard... so set an identifier.
        marker.setTag( 0 );
        pointMarkerList.add( marker );
        drawnMarkers.add(marker);

        // Add a marker and a polyline for every point
        for (int i = 1; i < path.getPathListSize(); i++) {
            if(path.getPoint(i).getType() == PointType.LINE) {
                Polyline polyline = gMap.addPolyline(new PolylineOptions()
                                    .add(path.getPoint(i - 1).getLatLng(), path.getPoint(i).getLatLng())
                                    .width(5)
                                    .color(color));

                drawnPolylines.add(polyline);
            }
            else if(path.getPoint(i).getType() == PointType.CURVE) {
                Point point = path.getPoint( i );

                LatLng p1 = path.getPoint(i - 1).getLatLng();
                LatLng p2 = point.getLatLng();

                drawnPolylines.add( drawCurve( p1, p2, point.getCurveMagnitude(), point.getCurveDirection(), color ));
            }

            // Set the marker, and make it the currentMarker as well
            marker = gMap.addMarker(new MarkerOptions()
                    .position(path.getPoint(i).getLatLng())
                    .title(Integer.toString(i + 1) + " - " + path.getPoint(i).toString()));
            marker.setAlpha(0.8f);
            marker.setVisible( !overlay );

            // Set identifier
            marker.setTag( i );

            // Add the marker
            pointMarkerList.add( marker );
            drawnMarkers.add(marker);
        }

        if( !overlay ){
            currentSelectedIndex = pointMarkerList.size() - 1;

            // Set active actions
            setEditMode( true );
        }

        drawnPathMarkers.put(path.getId(), drawnMarkers);
        drawnPathPolyLines.put(path.getId(), drawnPolylines);
    }

    // TODO. Could be made more general and used by drawPathOnMap (and split into drawLine, drawPoint and take index)
    // Draw line and add marker
    private void drawPoint(Point point){
        int size = path.getPathListSize();

        // Add marker
        Marker marker = gMap.addMarker(new MarkerOptions()
                .position(path.getPoint(size - 1).getLatLng())
                .title(Integer.toString(size) + " - " + path.getPoint(size - 1).toString() ));
        marker.setAlpha(0.8f);

        // Set identifier
        marker.setTag( size - 1 );

        // Draw line if path contains more than 1 point
        if(path.getPathListSize() > 1){
            if(path.getPoint(size - 1).getType() == PointType.LINE) {
                gMap.addPolyline(new PolylineOptions()
                        .add(path.getPoint(size - 2).getLatLng(), path.getPoint(size - 1).getLatLng())
                        .width(5)
                        .color(Color.RED));
            }
            else if(path.getPoint(size - 1).getType() == PointType.CURVE) {
                point = path.getPoint(size - 1);

                LatLng p1 = path.getPoint(size - 2).getLatLng();
                LatLng p2 = point.getLatLng();

                drawCurve( p1, p2, point.getCurveMagnitude(), point.getCurveDirection(), Color.RED );
            }
        }

        // Keep track on the current marker, so that it can be changed.
        pointMarkerList.add( marker );
        currentSelectedIndex = size - 1;

        // Set active actions
        setEditMode( true );

        // Show what the currently placed point type is, for user clarity.
        marker.showInfoWindow();

        // Reset new pointer position
        nextPointMarker.setPosition(new LatLng(90.0, 0.0));
    }

    /**
     * Draws a customizable curve on the google map...
     * @param p1 origin point
     * @param p2 destination point
     * @param k curvature (0..1)
     * @param dir curve bend diraction (-1 or 1)
     * @param color curve color
     */
    private Polyline drawCurve( LatLng p1, LatLng p2, double k, int dir, int color ){
        //Calculate distance and heading between two points
        double distance = SphericalUtil.computeDistanceBetween(p1, p2);
        double halfDistance = distance * 0.5;
        double heading = SphericalUtil.computeHeading(p1, p2);

        //Midpoint position
        LatLng mid = SphericalUtil.computeOffset(p1, halfDistance, heading);

        //Apply some mathematics to calculate position of the circle center
        double sqrCurvature = k * k;
        double extraParam = distance / (4 * k);
        double midPerpendicularLength = (1 - sqrCurvature) * extraParam;
        double r = (1 + sqrCurvature) * extraParam;

        // Correct offset
        LatLng circleCenterPoint = SphericalUtil.computeOffset( mid, midPerpendicularLength, heading + (90.0 * dir) );

        //Polyline options
        PolylineOptions options = new PolylineOptions();
        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dash(30));

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(circleCenterPoint, p1);

        //Calculate positions of points on circle border and add them to polyline options
        int numpoints = 100;
        double step = Math.toDegrees( Math.atan( halfDistance / midPerpendicularLength ) ) * 2 / numpoints;

        for (int j = 0; j < numpoints; j++) {
            LatLng pi = SphericalUtil.computeOffset(circleCenterPoint, r, h1 + (j * step * dir) );
            options.add(pi);
        }

        //Draw and return polyline
        return gMap.addPolyline( options.width(5).color( color ).pattern(pattern) );
    }

    /* Adds a point to the current path */
    private void addPointToPath (Point point) {
        int size = path.getPathListSize();

        // If path is not empty, delete the last point if it is a finish point
        if (size > 0){
            if( path.getPoint(size - 1).isFinish() ){
                path.getPoint( size - 1 ).setFinish( false );
                pointMarkerList.get( size - 1 ).setTitle( Integer.toString( size ) + " - " + path.getPoint( size - 1 ).toString() );
            }
        }

        // This new point is now the finish point as well
        point.setFinish( true );

        // Add new point
        path.addPoint( point );

        // Update Path drawing
        drawPoint(point);
        Toast.makeText(getContext(),  "Point added", Toast.LENGTH_LONG).show();

        // We now have no new point selected anymore
        nextPointIsSet = false;
    }

    private void createPathPreview() {
        // Set map type to default
        //gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Hide all markers
        for( Marker marker : pointMarkerList ){
            marker.hideInfoWindow();
        }

        nextPointMarker.setPosition(new LatLng(90.0, 0.0));

        // Change shown location to get a better overview of the map
        showPathOverview();

        SnapshotReadyCallback callback = new SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                Bitmap resizedBitmap = ThumbnailUtils.extractThumbnail(snapshot, 750, 500);
                snapshot.recycle();

                path.setPreview(resizedBitmap);

                if( editing )
                    PathCollection.getInstance().update( path );
                else
                    PathCollection.getInstance().add( path );

                // Return to list view
                getFragmentManager().popBackStackImmediate();
            }
        };
        gMap.snapshot(callback);
    }

    private void removeDrawnPath(String pathId){
        if(drawnPathMarkers.containsKey(pathId)){
            for(Marker m : drawnPathMarkers.get(pathId))
                m.remove();

            drawnPathMarkers.remove(pathId);
        }

        if(drawnPathPolyLines.containsKey(pathId)){
            for(Polyline p : drawnPathPolyLines.get(pathId))
                p.remove();

            drawnPathPolyLines.remove(pathId);
        }
    }
}