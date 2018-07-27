package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class PermissionActivity extends AppCompatActivity {

    public static final String EMERGENCY_ID = "ca.cmpt276.walkinggroupindigo.walkinggroup.app_mEmergencyMessageId";
    private WGServerProxy proxy;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        initializeServer();
        setUpToolBar();
        updateUI();
    }

    private void initializeServer() {
        setActionBarText(getString(R.string.permissions));
        user = User.getInstance();
        proxy = ProxyFunctions.setUpProxy(PermissionActivity.this, getString(R.string.apikey));
    }

    public static Intent makeIntent(Context context){
        return new Intent(context, PermissionActivity.class);
    }

    private void setUpToolBar() {
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        Button parentsLink = findViewById(R.id.parentsLink);
        Button permissionLink = findViewById(R.id.permissionLink);
        permissionLink.setClickable(false);
        mapLink.setAlpha(1f);
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        monitoringLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, ManageMonitoring.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        groupsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, ManageGroups.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        messagesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long mEmergencyMessageId = 1L;
                Intent intent = new Intent(PermissionActivity.this, GroupedMessagesActivity.class);
                intent.putExtra(EMERGENCY_ID, mEmergencyMessageId);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        parentsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, ParentDashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MapsActivity.makeIntent(PermissionActivity.this);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
    }

    private void getNumUnreadMessages(TextView unreadMessagesText) {
        Call<List<Message>> messageCall = proxy.getUnreadMessages(user.getId(), null);
        ProxyBuilder.callProxy(PermissionActivity.this,
                messageCall,
                returnedMessages -> getInNumber(returnedMessages, unreadMessagesText));
    }

    private void getInNumber(List<Message> returnedMessages, TextView unreadMessagesText) {
        unreadMessagesText.setText(String.valueOf(returnedMessages.size()));
    }

    private void updateUI() {
        Call<List<PermissionRequest>> permissionCaller = proxy.getPermissionsForUser(user.getId());
        ProxyBuilder.callProxy(
                PermissionActivity.this,
                permissionCaller,
                returnedPermissions -> {
                    populateListPermission(returnedPermissions);
                });
    }

    private void populateListPermission(List<PermissionRequest> permissions) {
        getAllUsers(permissions);

   //     setGroupListItemLongClicker(groupsList);
    }

    private void getAllUsers(List<PermissionRequest> permissions) {
        Call<List<User>> usersCall = proxy.getUsers();
        ProxyBuilder.callProxy(PermissionActivity.this,
                usersCall,
                returnedUsers->{
                    List<User> results = new ArrayList<>();
                    for(User u: returnedUsers){
                        for(PermissionRequest r: permissions){
                            if(r.getRequestingUser().getId().equals(u.getId())){
                                results.add(u);
                            }
                        }
                    }
                    callAdapter(permissions, results);
                });
    }

    private void callAdapter(List<PermissionRequest> permissions, List<User> results) {
        ArrayAdapter<PermissionRequest> adapter = new MyPermissionList(permissions, results);
        ListView permissionList = findViewById(R.id.permission_list_view);
        permissionList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.permission_layout_details);
        permissionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(PermissionActivity.this,
                        "I did press details", Toast.LENGTH_SHORT).show();
            }
        });

        // accept button is clicked
    }

    private class MyPermissionList extends ArrayAdapter<PermissionRequest> {
        List<PermissionRequest> permissionList;
        List<User> userList;

        public MyPermissionList(List<PermissionRequest> groupList, List<User> uList) {
            super(PermissionActivity.this,
                    R.layout.permission_layout_details
                    , groupList);
            permissionList = groupList;
            userList = uList;
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if(convertView == null){
                itemView = getLayoutInflater().inflate(
                        R.layout.permission_layout_details,
                        parent,
                        false);
            }

            PermissionRequest currentRequest;
            User currentUser;

            // Find the current PermissionRequest
            if (permissionList.isEmpty() || userList.isEmpty()) {
                currentRequest = new PermissionRequest();
                currentRequest.setUserA(new User());
                currentUser = new User();
                currentRequest.setMessage("No Message");
            }
            else {
                currentRequest = permissionList.get(position);
                currentUser = userList.get(position);
                itemView.setTag(currentRequest.getId());
            }
            if (currentRequest.getAction()!= null && currentRequest.getMessage() != null) {
                try {
                    TextView nameText = itemView.findViewById(R.id.txtPermissionFrom);
                    nameText.setText(currentUser.getName());

                    TextView emailText = itemView.findViewById(R.id.txtPermissionAction);
                    emailText.setText(currentRequest.getMessage());
                    // should be getAction() for debugging I changed it to getMessage()
                    // if user already accepted or declined the button
                    Button accept = itemView.findViewById(R.id.btnAccept);
                    Button decline = itemView.findViewById(R.id.btnDecline);
                    if(currentRequest.getStatus() == WGServerProxy.PermissionStatus.APPROVED ||
                            currentRequest.getStatus() == WGServerProxy.PermissionStatus.DENIED){
                        accept.setVisibility(View.INVISIBLE);
                        decline.setVisibility(View.INVISIBLE);
                    }
                    else{
                        accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Call<PermissionRequest> acceptCall = proxy.approveOrDenyPermissionRequest(
                                        currentRequest.getId(), WGServerProxy.PermissionStatus.APPROVED);
                                ProxyBuilder.callProxy(PermissionActivity.this,
                                        acceptCall,
                                        returnedStatus->{
                                    updateUI();
                                        });
                            }
                        });

                        decline.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Call<PermissionRequest> deniedCall = proxy.approveOrDenyPermissionRequest(
                                        currentRequest.getId(), WGServerProxy.PermissionStatus.DENIED);
                                ProxyBuilder.callProxy(PermissionActivity.this,
                                        deniedCall,
                                        returnedStatus->{
                                            updateUI();
                                        });
                            }
                        });
                    }
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + permissionList.toString());
                }
            }

            return itemView;
        }
    }

    private void setActionBarText(String title) {
        try {
            getActionBar().setTitle(title);
            getSupportActionBar().setTitle(title);
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creates action bar buttons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_dashboard, menu);
        MenuItem item = menu.findItem(R.id.emergency_message);
        if (user.getCurrentWalkingGroup() == null) {
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
            case R.id.logOutButton:
                Toast.makeText(PermissionActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Helper.logUserOut(PermissionActivity.this);
                return true;

            case R.id.accountInfoButton:
                intent = new Intent(PermissionActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = User.getInstance();
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        updateUI();
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
