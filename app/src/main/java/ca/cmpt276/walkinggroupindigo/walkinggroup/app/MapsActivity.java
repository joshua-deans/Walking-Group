package ca.cmpt276.walkinggroupindigo.walkinggroup.app;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Objects;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_TOKEN;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int DEFAULT_ZOOM = 15;
    private GoogleMap mMap;
    private User mUser;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    // Default location if permission is not granted
    private final LatLng mDefaultLocation = new LatLng(49.2827, -123.1207);
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private WGServerProxy proxy;

    public static Intent makeIntent (Context context){
        return new Intent (context, MapsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mUser = User.getInstance();
        proxy = ProxyFunctions.setUpProxy(MapsActivity.this, getString(R.string.apikey));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setActionBarText(getString(R.string.map));
        setUpToolBar();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findGroupMarkers();
    }

    private void setUpToolBar() {
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        mapLink.setClickable(false);
        mapLink.setAlpha(1f);
        monitoringLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ManageMonitoring.class);
                startActivity(intent);
            }
        });
        groupsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ManageGroups.class);
                startActivity(intent);
            }
        });
        messagesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Messages is not yet implemented", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creates action bar buttons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Click listener for action bar
        switch (item.getItemId()) {
            case R.id.logOutButton:
                Toast.makeText(MapsActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                logUserOut();
                return true;

            case R.id.accountInfoButton:
                Intent intent = new Intent(MapsActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void logUserOut() {
        Context context = MapsActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LOG_IN_SAVE_KEY, "");
        editor.putString(LOG_IN_SAVE_TOKEN, "");
        editor.apply();

        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void createAlertDialog(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setMessage("Would you like to join this group?")
                .setTitle(marker.getTitle());
        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                long groupId = Long.valueOf(Objects.requireNonNull(marker.getTag()).toString());
                Call<List<User>> caller = proxy.addGroupMember(groupId, mUser);
                ProxyBuilder.callProxy(MapsActivity.this, caller, user -> addUser(user));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addUser(List<User> user) {
        Toast.makeText(MapsActivity.this, "Successfully added to group", Toast.LENGTH_SHORT).show();
    }

    private void findGroupMarkers() {
        Call<List<Group>> caller = proxy.getGroups();
        ProxyBuilder.callProxy(MapsActivity.this, caller, groupList -> placeGroupMarkers(groupList));
    }

    private void placeGroupMarkers(List<Group> groupList) {
        for (Group group : groupList) {
            if (group.getRouteLatArray().length > 0 && group.getRouteLngArray().length > 0) {
                Marker currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng
                        (group.getDestLatitude(), group.getDestLongitude()))
                        .title(group.getGroupDescription()));
                currentMarker.setTag(group.getId());
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            mLocationPermissionGranted = true;
            updateMapLocation();
            setUpMarkerClickListener();
        }

    }

    private void setUpMarkerClickListener() {
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                createAlertDialog(marker);
                return false;
            }
        });
    }

    private void updateMapLocation() {
        if (mLocationPermissionGranted && hasPermission()) {
            @SuppressLint("MissingPermission") Task<Location> locationResult = mFusedLocationClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        try {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            mMap.setMyLocationEnabled(true);
                        } catch (NullPointerException e) {
                            Log.e("MapsActivity", "Exception: %s", e);
                            mapErrorHandler();
                        }
                    } else {
                        Log.e("MapsActivity", "Exception: %s", task.getException());
                        mapErrorHandler();
                    }
                }
            });
        }

    }

    @SuppressLint("MissingPermission")
    private void mapErrorHandler() {
        Log.d("MapsActivity", "Current location is null. Using defaults.");
        Toast.makeText(MapsActivity.this, "Location could not be found",
                Toast.LENGTH_SHORT).show();
        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(mDefaultLocation, 1));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setMyLocationEnabled(true);
    }

    private boolean hasPermission() {
        return !(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateMapLocation();
    }

    private void setActionBarText(String title) {
        try {
            getActionBar().setTitle(title);
            getSupportActionBar().setTitle(title);
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle(title);
        }
    }
}
