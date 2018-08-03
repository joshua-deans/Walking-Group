package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import java.util.List;
import java.util.Set;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
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

    public static Intent makeIntent(Context context) {
        return new Intent(context, PermissionActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        Helper.setCorrectTheme(PermissionActivity.this, user);
        setContentView(R.layout.activity_permission);
        initializeServer();
        updateUI();
    }

    private void initializeServer() {
        setActionBarText(getString(R.string.permissions));
        user = User.getInstance();
        proxy = ProxyFunctions.setUpProxy(PermissionActivity.this, getString(R.string.apikey));
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
        addGroupPermissions(adapter, permissions);

    }

    private void addGroupPermissions(ArrayAdapter<PermissionRequest> adapter, List<PermissionRequest> permissions) {
        List<Group> leadsGroups = user.getLeadsGroups();
        for (Group group : leadsGroups) {
            Call<List<PermissionRequest>> permissionGroupCaller = proxy.getPermissionsForGroup(group.getId());
            ProxyBuilder.callProxy(PermissionActivity.this, permissionGroupCaller,
                    returnedGroupPermissions -> {
                        addToAdapter(adapter, permissions, returnedGroupPermissions);
                    });
        }
    }

    private void addToAdapter(ArrayAdapter<PermissionRequest> adapter, List<PermissionRequest> permissions, List<PermissionRequest> returnedGroupPermissions) {
        for (PermissionRequest permission : permissions) {
            if (!permissions.contains(permission)) {
                adapter.addAll(returnedGroupPermissions);
            }
        }
    }

    private void setInvisible(PermissionRequest returnedRequest) {
        boolean show = false;
        returnedRequest.setVisibility(show);
        Toast.makeText(PermissionActivity.this, "Request deleted", Toast.LENGTH_SHORT).show();
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
        updateUI();
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private class MyPermissionList extends ArrayAdapter<PermissionRequest> {
        List<PermissionRequest> permissionList;

        public MyPermissionList(List<PermissionRequest> permList) {
            super(PermissionActivity.this,
                    R.layout.permission_layout_details
                    , permList);
            permissionList = permList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (convertView == null) {
                itemView = getLayoutInflater().inflate(
                        R.layout.permission_layout_details,
                        parent,
                        false);
            }

            PermissionRequest currentRequest;

            // Find the current PermissionRequest
            if (permissionList.isEmpty()) {
                currentRequest = new PermissionRequest();
                currentRequest.setUserA(new User());
                currentRequest.setMessage("No Message");
            } else {
                currentRequest = permissionList.get(position);
                itemView.setTag(currentRequest.getId());
            }
            if (currentRequest.getAction() != null && currentRequest.getMessage() != null) {
                if (currentRequest.isVisible()) {
                    try {
                        TextView nameText = itemView.findViewById(R.id.txtPermissionFrom);
                        Call<User> userCall = proxy.getUserById(currentRequest.getRequestingUser().getId());
                        ProxyBuilder.callProxy(PermissionActivity.this,
                                userCall,
                                returnedUser -> {
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

                        if (currentRequest.getStatus() != WGServerProxy.PermissionStatus.PENDING) {
                            acceptRequest.setVisibility(View.INVISIBLE);
                            declineRequest.setVisibility(View.INVISIBLE);
                        } else {
                            acceptRequest.setVisibility(View.VISIBLE);
                            declineRequest.setVisibility(View.VISIBLE);
                            acceptRequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Call<PermissionRequest> acceptCall = proxy.approveOrDenyPermissionRequest(
                                            currentRequest.getId(), WGServerProxy.PermissionStatus.APPROVED);
                                    ProxyBuilder.callProxy(PermissionActivity.this,
                                            acceptCall,
                                            returnedStatus -> {
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
                                            returnedStatus -> {
                                                updateUI();
                                            });
                                }
                            });
                        }

                        Set<PermissionRequest.Authorizor> authorizors = currentRequest.getAuthorizors();
                        User WhoApprovedOrDenied;
                        WhoApprovedOrDenied = new User();
                        for (PermissionRequest.Authorizor authorizor : authorizors) {
                            WhoApprovedOrDenied = authorizor.getWhoApprovedOrDenied();
                        }
                        Call<User> userCaller = proxy.getUserById(WhoApprovedOrDenied.getId());
                        ProxyBuilder.callProxy(PermissionActivity.this, userCaller, returnedUser -> statusText.setText(statusText.getText() + " by " + returnedUser.getName()));

                    } catch (NullPointerException e) {
                        Log.e("Error", e + ":" + permissionList.toString());
                    }
                }
            }
            return itemView;
        }
    }
}
