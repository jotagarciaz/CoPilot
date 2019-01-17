package com.mdh.ivanmuniz.copilotapp;

//import android.app.FragmentManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mdh.ivanmuniz.copilotapp.collection.MachineCollection;
import com.mdh.ivanmuniz.copilotapp.collection.PathCollection;
import com.mdh.ivanmuniz.copilotapp.list.PathListFragment;
import com.mdh.ivanmuniz.copilotapp.machines.MachinesFragment;
import com.mdh.ivanmuniz.copilotapp.object.Machine;
import com.mdh.ivanmuniz.copilotapp.user.Operator;
import com.mdh.ivanmuniz.copilotapp.user.Planner;
import com.mdh.ivanmuniz.copilotapp.user.User;
import com.mdh.ivanmuniz.copilotapp.user.UserType;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EditDialog.EditDialogListener {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialized local settings, needs a context reference
        super.onCreate(savedInstanceState);

       loginAction();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and updateDescription UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        //updateUI(currentUser);
    }



    @Override
    public void onStop()
    {
        super.onStop();
    }

    public void applyTexts( String pathId, String name, String description ) {
        PathCollection.getInstance().updateDescription(pathId, name, description);
    }

    public void logout_dialog(){
        AlertDialog mDialog = new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you wish to sign out?" )
                .setIcon(R.drawable.ic_person_black_24dp)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        mGoogleSignInClient.signOut();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout_dialog();
        }

        if(id == R.id.action_machines) {
            MachinesFragment machinesFragment = MachinesFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment, machinesFragment).addToBackStack(null).commit();
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {

        List fragmentList = getSupportFragmentManager().getFragments();

        boolean handled = false;
        for(Object f: fragmentList) {
            if (f instanceof PathListFragment) {
                handled = ((PathListFragment) f).onBackPressed();

                if (handled) {
                    break;
                }
            }
        }

        if(!handled) {
            super.onBackPressed();
        }
    }

    private  void loginAction(){
        
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser userTemp = mAuth.getCurrentUser();
        if(userTemp ==null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }else {
            // Store username and email in finals to allow later access
            final String uid = userTemp.getUid();
            final String username = userTemp.getDisplayName();
            final String email = userTemp.getEmail();

            // Create an half empty user with limited functionality anyway... In case database request fails
            Planner.getInstance( uid, username,"", email, false );

            // Get additional user data, such as favorites, admin and user type
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference ref = database.collection( "user" ).document( uid );
            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        try
                        {
                            boolean userAdmin = snapshot.getBoolean( "admin" );
                            long userType = snapshot.getLong("type");
                            ArrayList userFavorites = (ArrayList<String>)snapshot.get("favorite");

                            if( !UserType.userTypeExist( (int)userType ) ){
                                Log.d("USERDATA", "User type does not exist.");
                                return;
                            }

                            User user = null;

                            // Create the local user, based on userType
                            switch( (int)userType ){
                                case UserType.PLANNER:
                                    user = Planner.getInstance( uid, username,"", email, userAdmin );
                                    break;

                                case UserType.OPERATOR:
                                    user = Operator.getInstance( uid, username,"", email, userAdmin );
                                    break;
                            }

                            // Add favorites to user object
                            if( user != null )
                                user.setFavorites( userFavorites );

                            Log.d("USERDATA", "Success");
                        }
                        catch( Exception e ){
                            Log.d("USERDATA", "Error parsing user-data: ", e );
                        }
                    } else {
                        Log.d("USERDATA", "Error getting user-data: ", task.getException());
                    }

                    // Make sure this isn't launched until we get user data from database, or if it query fails
                    // Also make sure path list is initialized before launching the pathlist fragment
                    PathCollection.getInstance();
                    MachineCollection.getInstance();

                    setContentView(R.layout.activity_main);

                    PathListFragment pathListFragment = PathListFragment.newInstance();
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.add(R.id.fragment, pathListFragment).commit();

                    statusCheck();
                }
            });
        }
    }
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
       final Context t=this;
        final AlertDialog.Builder builder = new AlertDialog.Builder(t);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        Toast.makeText(t,"GPS must be turn on",Toast.LENGTH_SHORT).show();
                        statusCheck();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
