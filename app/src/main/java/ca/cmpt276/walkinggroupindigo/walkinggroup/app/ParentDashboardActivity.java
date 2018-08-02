package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.GpsLocation;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ParentDashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    // Default location if permission is not granted
    private final LatLng mDefaultLocation = new LatLng(49.2827, -123.1207);
    private GoogleMap mMap;
    private User mUser;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private WGServerProxy proxy;
    private List<Long> markersPlaced;

    public static Intent makeIntent(Context context) {
        return new Intent(context, ParentDashboardActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(ParentDashboardActivity.this, mUser);
        setContentView(R.layout.activity_parent_maps);
        markersPlaced = new ArrayList<>();
        initServerUser();
        setUpToolBar();
        setActionBarText(getString(R.string.parent_dashboard));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getMonitoredUsers();
    }

    private void initServerUser() {
        mUser = User.getInstance();
        proxy = ProxyFunctions.setUpProxy(ParentDashboardActivity.this, getString(R.string.apikey));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_manage_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.create_group:
                intent = new Intent(ParentDashboardActivity.this, CreateGroup.class);
                startActivity(intent);
                return true;
            case R.id.accountInfoButton:
                intent = new Intent(ParentDashboardActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;
            case R.id.logOutButton:
                Toast.makeText(ParentDashboardActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Helper.logUserOut(ParentDashboardActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void setUpToolBar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.linkToolbar);
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        Button parentsLink = findViewById(R.id.parentsLink);
        parentsLink.setClickable(false);
        parentsLink.setAlpha(1f);
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        groupsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentDashboardActivity.this, ManageGroups.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
        messagesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentDashboardActivity.this, ManageMessagesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
        monitoringLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentDashboardActivity.this, ManageMonitoring.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
    }

    private void getMonitoredUsers() {
        Call<List<User>> caller = proxy.getMonitorsUsers(mUser.getId());
        ProxyBuilder.callProxy(ParentDashboardActivity.this,
                caller,
                returnedUsers -> findMonitorMarkers(returnedUsers, false));
        List<Group> groupsList = mUser.getMemberOfGroups();
        getGroupLeaders(groupsList);
    }

    private void getGroupLeaders(List<Group> groupsList) {
        Call<List<Group>> groups = proxy.getGroups();
        ProxyBuilder.callProxy(ParentDashboardActivity.this,
                groups,
                returnedGroups -> {
                    List<User> allLeader = new ArrayList<>();
                    for (Group g : returnedGroups) {
                        User u = g.getLeader();
                        if (!allLeader.contains(u) && u != null && (!u.getId().equals(mUser.getId()))
                                && groupsList.contains(g)) {
                            allLeader.add(u);
                        }
                    }
                    findMonitorMarkers(allLeader, true);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMonitoredUsers();
    }

    private void findMonitorMarkers(List<User> monitorsUsers, boolean leader) {
        for (User users : monitorsUsers) {
            Call<User> caller = proxy.getUserById(users.getId());
            ProxyBuilder.callProxy(ParentDashboardActivity.this,
                    caller, returnedUser ->
                            getUserGPSInfo(returnedUser, leader));
        }
    }

    private void getUserGPSInfo(User returnedUser, boolean leader) {
        Call<GpsLocation> caller = proxy.getLastGpsLocation(returnedUser.getId());
        ProxyBuilder.callProxy(ParentDashboardActivity.this,
                caller, gpsLocation ->
                        placeUserMarker(gpsLocation, returnedUser, leader));
    }


    private void placeUserMarker(GpsLocation lastGpsLocation, User returnedUser, boolean leader) {
        if (lastGpsLocation.getLng() != null && lastGpsLocation.getLat() != null
                && !markersPlaced.contains(returnedUser.getId())) {
            Marker currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng
                    (lastGpsLocation.getLat(), lastGpsLocation.getLng()))
                    .title(returnedUser.getName())
                    .snippet(editTimeStamp(lastGpsLocation.getTimestamp())));
            if (leader) {
                currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                currentMarker.setTitle("Leader: " + currentMarker.getTitle());
            }
            markersPlaced.add(returnedUser.getId());
        }
    }

    private String editTimeStamp(String timestamp) {
        String[] ts = timestamp.split("T");
        return ("Last update: " + ts[0] + " " + ts[1]);
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
        }
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
        Toast.makeText(ParentDashboardActivity.this, "Location could not be found",
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

    private void getNumUnreadMessages(TextView unreadMessagesText) {
        Call<List<Message>> messageCall = proxy.getUnreadMessages(mUser.getId(), null);
        ProxyBuilder.callProxy(ParentDashboardActivity.this, messageCall, returnedMessages -> getInNumber(returnedMessages, unreadMessagesText));
    }

    private void getInNumber(List<Message> returnedMessages, TextView unreadMessagesText) {
        unreadMessagesText.setText(String.valueOf(returnedMessages.size()));
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