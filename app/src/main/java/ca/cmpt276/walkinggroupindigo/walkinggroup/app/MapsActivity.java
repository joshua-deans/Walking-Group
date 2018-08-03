package ca.cmpt276.walkinggroupindigo.walkinggroup.app;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.EditText;
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
import java.util.Objects;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int DEFAULT_ZOOM = 15;
    public static final String EMERGENCY_ID = "ca.cmpt276.walkinggroupindigo.walkinggroup.app_mEmergencyMessageId";
    public static final String EMERGENCY_GROUP_ID = "ca.cmpt276.walkinggroupindigo.walkinggroup.app_mEmergencyGroupId";

    Long mEmergencyMessageId;
    Long mEmergencyGroupId;
    private GoogleMap mMap;
    private User mUser;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    // Default location if permission is not granted
    private final LatLng mDefaultLocation = new LatLng(49.2827, -123.1207);
    private Message mMessage;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private WGServerProxy proxy;
    private List<Marker> inGroupMarkers;
    private String currentTheme;

    EditText inputMessage;

    public static Intent makeIntent (Context context){
        return new Intent (context, MapsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(MapsActivity.this, mUser);
        setContentView(R.layout.activity_maps);
        mMessage = new Message();
        currentTheme = mUser.getRewards().getSelectedTheme();
        proxy = ProxyFunctions.setUpProxy(MapsActivity.this, getString(R.string.apikey));
        inGroupMarkers = new ArrayList<>();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setActionBarText(getString(R.string.map));
        setUpToolBar();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findGroupMarkers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!currentTheme.equals(mUser.getRewards().getSelectedTheme())) {
            Helper.setCorrectTheme(MapsActivity.this, mUser);
            currentTheme = mUser.getRewards().getSelectedTheme();
            recreate();
        }
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        ActivityCompat.invalidateOptionsMenu(MapsActivity.this);
        findGroupMarkers();
    }

    private void setUpToolBar() {
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        Button parentsLink = findViewById(R.id.parentsLink);
        mapLink.setClickable(false);
        mapLink.setAlpha(1f);
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        monitoringLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ManageMonitoring.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        groupsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ManageGroups.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        messagesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ManageMessagesActivity.class);
                intent.putExtra(EMERGENCY_ID, mEmergencyMessageId);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        parentsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ParentDashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creates action bar buttons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_dashboard, menu);
        MenuItem item = menu.findItem(R.id.emergency_message);
        if (mUser.getCurrentWalkingGroup() == null) {
            item.setVisible(false);
        } else {
            item.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Click listener for action bar
        Intent intent;
        switch (item.getItemId()) {
            case R.id.emergency_message:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
                builder1.setMessage("Send emergency message:");
                inputMessage = new EditText(this);
                builder1.setView(inputMessage);
                builder1.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sentMessage = inputMessage.getText().toString();
                        if (sentMessage.matches("")) {
                            sentMessage = " ";
                        }
                        mMessage.setText(sentMessage);
                        mMessage.setEmergency(true);
                        Call<List<Message>> emergencyParentCaller = proxy.newMessageToParentsOf(mUser.getId(),mMessage);
                        ProxyBuilder.callProxy(MapsActivity.this,
                                emergencyParentCaller,
                                message -> markAsUnread(message));
                        sendMessageToGroup(sentMessage);
                    }
                });
                builder1.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialogBroadCast = builder1.create();
                dialogBroadCast.show();
                return true;
            case R.id.leaderboard_button:
                intent = new Intent(MapsActivity.this, LeaderboardActivity.class);
                startActivity(intent);
                return true;
            case R.id.rewards_shop:
                intent = new Intent(MapsActivity.this, TitlesShopActivity.class);
                startActivity(intent);
                return true;
            case R.id.logOutButton:
                Toast.makeText(MapsActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Helper.logUserOut(MapsActivity.this);
                return true;

            case R.id.accountInfoButton:
                intent = new Intent(MapsActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMessageToGroup(String sentMessage) {
        Group currentGroup = mUser.getCurrentWalkingGroup();
        // if current group is moving
        if (currentGroup != null) {
            Long myLeaderId = currentGroup.getLeader().getId();
            List<User> parents = mUser.getMonitoredByUsers();
            Call<User> myLeader = proxy.getUserById(myLeaderId);
            ProxyBuilder.callProxy(MapsActivity.this,
                    myLeader,
                    returnedUser -> createTemporaryGroups(returnedUser, sentMessage, parents));
        }
    }

    // create a temporary group
    private void createTemporaryGroups(User groupLeader, String newMessage, List<User> parents) {
        if (!parents.contains(groupLeader)) {
            Group currentGroup = new Group();
            int tempLocation = -1;
            int tempDestination = 0;
            currentGroup.setGroupDescription("temporary group");
            currentGroup.setLeader(mUser);
            currentGroup.setStartLatitude(tempLocation);
            currentGroup.setStartLongitude(tempLocation);
            currentGroup.setDestLatitude(tempDestination);
            currentGroup.setDestLongitude(tempDestination);
            Call<Group> caller = proxy.createGroup(currentGroup);
            ProxyBuilder.callProxy(MapsActivity.this,
                    caller, group -> addUsersToGroup(group, groupLeader, newMessage));
        }
    }

    // add all the leaders to the group
    private void addUsersToGroup(Group group, User groupLeader, String newMessage) {
        Call<List<User>> monitorsUserGroupCaller = proxy.addGroupMember(group.getId(), groupLeader);
        ProxyBuilder.callProxy(MapsActivity.this,
                monitorsUserGroupCaller,
                returnMonitorsUserGroup -> {
                    Message tempMessage = new Message();
                    tempMessage.setText(newMessage);
                    tempMessage.setEmergency(true);
                    Call<List<Message>> sendMessage = proxy.newMessageToGroup(group.getId(), tempMessage);
                    ProxyBuilder.callProxy(MapsActivity.this,
                            sendMessage,
                            returnedMessage -> deleteTheGroup(group), errorMessage -> deleteTheGroup(group));
                });
    }

    private void deleteTheGroup(Group group) {
        Call<Void> deleteGroup = proxy.deleteGroup(group.getId());
        ProxyBuilder.callProxy(MapsActivity.this,
                deleteGroup,
                returned->{}
                );
    }

    private void markAsUnread(List<Message> message) {
        for(Message aMessage : message) {
            aMessage.setIsRead(false);
            aMessage.setEmergency(true);
            mEmergencyMessageId = aMessage.getId();
            Call<Message> messageCaller = proxy.markMessageAsRead(aMessage.getId(), false);
            ProxyBuilder.callProxy(MapsActivity.this,
                    messageCaller,
                    returnNothing -> onSendSuccess(returnNothing));
        }
    }

    private void onSendSuccess(Message returnNothing) {
        Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
    }


    private List<User> getUserGroupLeaders(List<Group> returnedGroups) {
        List<User> userGroupLeaders = new ArrayList<>();
            for (Group u : returnedGroups) {
                userGroupLeaders.add(u.getLeader());
            }
        return userGroupLeaders;
    }

    private void createAddAlertDialog(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setMessage("Would you like to join this group?")
                .setTitle(marker.getTitle());
        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                long groupId = Long.valueOf(Objects.requireNonNull(marker.getTag()).toString());
                Call<Group> getGroupCaller = proxy.getGroupById(groupId);
                ProxyBuilder.callProxy(MapsActivity.this, getGroupCaller, group -> checkGroup(group));

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

    private void checkGroup(Group group) {
        if (group.getLeader() != null) {
            Call<List<User>> caller = proxy.addGroupMember(group.getId(), mUser);
            ProxyBuilder.callProxy(MapsActivity.this, caller, user -> addUser(user));
        } else if(group.getLeader() == null) {
            Toast.makeText(MapsActivity.this,
                    "You cannot be added to this group, since it has no leader", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUser(List<User> user) {
        Call<User> callerUser = proxy.getUserById(mUser.getId());
        ProxyBuilder.callProxy(MapsActivity.this, callerUser, returnedUser -> onSuccess(returnedUser));
    }

    private void onSuccess(User returnedUser) {
        mUser.setMemberOfGroups(returnedUser.getMemberOfGroups());
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
                if (mUser.getMemberOfGroups().contains(group) || mUser.getLeadsGroups().contains(group)) {
                    currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                } else {
                    inGroupMarkers.add(currentMarker);
                }
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
                if (inGroupMarkers.contains(marker)) {
                    createAddAlertDialog(marker);
                }
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

    private void getNumUnreadMessages(TextView unreadMessagesText) {
        Call<List<Message>> messageCall = proxy.getUnreadMessages(mUser.getId(), null);
        ProxyBuilder.callProxy(MapsActivity.this,
                messageCall,
                returnedMessages -> getInNumber(returnedMessages, unreadMessagesText));
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
