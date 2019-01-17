package com.mdh.ivanmuniz.copilotapp.list;

import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mdh.ivanmuniz.copilotapp.EditDialog;
import com.mdh.ivanmuniz.copilotapp.gcode.GCodeFragment;
import com.mdh.ivanmuniz.copilotapp.R;
import com.mdh.ivanmuniz.copilotapp.collection.PathCollection;
import com.mdh.ivanmuniz.copilotapp.map.MapFragment;
import com.mdh.ivanmuniz.copilotapp.user.User;

public class PathListFragment extends Fragment implements View.OnClickListener, IPathListItemListener {

    private LinearLayoutManager layoutManager;
    private FloatingActionButton addButton;
    private PathRecyclerViewAdapter recyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PathListFragment() {}

    public static PathListFragment newInstance() {
        PathListFragment pathListFragment = new PathListFragment();
        return pathListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public boolean onBackPressed() {

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Attach toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar( toolbar );
        setHasOptionsMenu(true);

        View list = view.findViewById( R.id.list );
        addButton = view.findViewById( R.id.item_add);

        // Set the adapter
        if (list instanceof RecyclerView) {
            Context context = list.getContext();
            RecyclerView recyclerView = (RecyclerView) list;
            layoutManager = new LinearLayoutManager( context );
            recyclerView.setLayoutManager( layoutManager );
            recyclerViewAdapter = new PathRecyclerViewAdapter( this, layoutManager, context );
            recyclerView.setAdapter( recyclerViewAdapter );
        }

        addButton.setOnClickListener( this );
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search).setVisible(true);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                recyclerViewAdapter.filter(newText);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        recyclerViewAdapter.Destroy();

        super.onDetach();
    }

    public void onClick(View view) {
        if( view == addButton ){
            // When add button is clicked

            View mView = getLayoutInflater().inflate(R.layout.layout_edit_dialog, null);
            final EditText nameEditText = mView.findViewById(R.id.nameEditText);
            final EditText descriptionEditText = mView.findViewById(R.id.descriptionEditText);

            final AlertDialog mDialog = new AlertDialog.Builder(this.getContext())
                    .setTitle("Create new path")
                    .setMessage("Enter name and description" )
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
                    if(!nameEditText.getText().toString().isEmpty()){

                        MapFragment mapFragment = MapFragment.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean( "createNew", true );
                        bundle.putString( "pathname", nameEditText.getText().toString() );
                        bundle.putString( "pathdescription", descriptionEditText.getText().toString() );
                        mapFragment.setArguments( bundle );

                        mDialog.dismiss();

                        // Switch to map
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment, mapFragment).addToBackStack(null).commit();
                    }
                    else {
                        Toast.makeText(getContext(), "Path name cant be empty", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void onItemEvent(PathListItem item, final int type ){
        switch( type )
        {
            case PathRecyclerViewAdapter.EDIT_NAME_EVENT:
                onEditName( item );
                break;

            case PathRecyclerViewAdapter.EDIT_PATH_EVENT:
                onEditPath( item );
                break;

            case PathRecyclerViewAdapter.EDIT_GCODE_EVENT:
                onEditGCode( item );
                break;

            // Update settings to reflect favorites
            case PathRecyclerViewAdapter.FAVORITE_EVENT:
                if( item.getFavorite() ){
                    User.getInstance().addFavoritePath( item.getPath().getId() );
                } else {
                    User.getInstance().removeFavoritePath( item.getPath().getId() );
                }
                break;

            // Remove item from all sources
            case PathRecyclerViewAdapter.REMOVE_EVENT:
                onRemove( item );
                break;
        }
    }

    private void onEditName( PathListItem item ){
        // When list item edit name is clicked
        EditDialog editDialog = new EditDialog();
        Bundle bundle = new Bundle();
        bundle.putString( "pathid", item.getPath().getId() );
        bundle.putString( "pathname", item.getPath().getName() );
        bundle.putString( "pathdescription", item.getPath().getDescription() );
        editDialog.setArguments( bundle );
        editDialog.show( getFragmentManager(), "edit dialog" );
    }

    private void onEditPath( PathListItem item ){
        // When list item edit path is clicked
        MapFragment mapFragment = MapFragment.newInstance();

        // Send path data too
        Bundle bundle = new Bundle();
        bundle.putBoolean( "createNew", false );
        bundle.putString( "pathid", item.getPath().getId() );
        mapFragment.setArguments( bundle );

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, mapFragment).addToBackStack(null).commit();
    }

    private void onEditGCode( PathListItem item ) {
        // When list item edit gcode is clicked
        GCodeFragment gCodeFragment = GCodeFragment.newInstance();
        // Send path data too GCodeFragment
        Bundle bundle = new Bundle();
        bundle.putString( "pathid", item.getPath().getId() );
        gCodeFragment.setArguments( bundle );

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, gCodeFragment).addToBackStack(null).commit();
    }

    private void onRemove(final PathListItem item){
        AlertDialog mDialog = new AlertDialog.Builder(this.getContext())
                .setTitle("Remove")
                .setMessage("Do you wish to remove the path?" )
                .setIcon(R.drawable.ic_delete_forever_black_24dp)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PathCollection.getInstance().remove( item.getPath() );
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}