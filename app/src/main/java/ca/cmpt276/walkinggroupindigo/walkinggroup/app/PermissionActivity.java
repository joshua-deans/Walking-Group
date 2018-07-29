package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
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
                Intent intent = new Intent(PermissionActivity.this, ManageMessagesActivity.class);
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
        // get all the permissions from the child
        callAdapter(permissions);
    }

    private void callAdapter(List<PermissionRequest> permissions) {
        ArrayAdapter<PermissionRequest> adapter = new MyPermissionList(permissions);
        ListView permissionList = findViewById(R.id.permission_list_view);
        permissionList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.permission_layout_details);
        permissionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // TODO: Here we can give the full details about the Permission.

                Toast.makeText(PermissionActivity.this,
                        "I want to see more details!!", Toast.LENGTH_SHORT).show();
            }
        });

        permissionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //TODO: Delete request.

                Toast.makeText(PermissionActivity.this,
                        "I want to delete this permission", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private class MyPermissionList extends ArrayAdapter<PermissionRequest> {
        List<PermissionRequest> permissionList;

        public MyPermissionList(List<PermissionRequest> groupList) {
            super(PermissionActivity.this,
                    R.layout.permission_layout_details
                    , groupList);
            permissionList = groupList;
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

            // Find the current PermissionRequest
            if (permissionList.isEmpty()){
                currentRequest = new PermissionRequest();
                currentRequest.setUserA(new User());
                currentRequest.setMessage("No Message");
            }
            else {
                currentRequest = permissionList.get(position);
                itemView.setTag(currentRequest.getId());
            }
            if (currentRequest.getAction()!= null && currentRequest.getMessage() != null) {
                try {
                    TextView nameText = itemView.findViewById(R.id.txtPermissionFrom);
                    Call<User> userCall = proxy.getUserById(currentRequest.getRequestingUser().getId());
                    ProxyBuilder.callProxy(PermissionActivity.this,
                            userCall,
                            returnedUser->{
                                nameText.setText(getString(R.string.request_from) + returnedUser.getName());
                            });
                    TextView emailText = itemView.findViewById(R.id.txtPermissionAction);
                    emailText.setText(currentRequest.getMessage());
                    // should be getAction() for debugging I changed it to getMessage()

                    TextView statusText = itemView.findViewById(R.id.txtPermissionStatus);
                    statusText.setText(currentRequest.getStatus().toString());

                    // if user already accepted or declined the button
                    Button acceptRequest = itemView.findViewById(R.id.btnAccept);
                    Button declineRequest = itemView.findViewById(R.id.btnDecline);
//                    if(currentRequest.getStatus() == WGServerProxy.PermissionStatus.APPROVED ||
//                            currentRequest.getStatus() == WGServerProxy.PermissionStatus.DENIED){
                    if (currentRequest.getStatus() != WGServerProxy.PermissionStatus.PENDING) {
                        acceptRequest.setVisibility(View.INVISIBLE);
                        declineRequest.setVisibility(View.INVISIBLE);
                    }
                    else{
                        acceptRequest.setVisibility(View.VISIBLE);
                        declineRequest.setVisibility(View.VISIBLE);
                        acceptRequest.setOnClickListener(new View.OnClickListener() {
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

                        declineRequest.setOnClickListener(new View.OnClickListener() {
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
