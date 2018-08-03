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

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.PermissionRequest;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static java.lang.Integer.valueOf;

public class ManageMonitoring extends AppCompatActivity {

    private WGServerProxy proxy;
    private User user;

    public static Intent makeIntent (Context context){
        return new Intent (context, ManageMonitoring.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        Helper.setCorrectTheme(ManageMonitoring.this, user);
        setContentView(R.layout.activity_manage_monitoring);
        setActionBarText(getString(R.string.manage_monitoring));
        user = User.getInstance();
        proxy = ProxyFunctions.setUpProxy(ManageMonitoring.this, getString(R.string.apikey));
        setUpToolBar();
        setUpAddMonitoringButton();
        setUpAddMonitoredButton();
        populateMonitorsUser();
        populateMonitoredByUsers();
    }

    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = User.getInstance();
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        populateMonitorsUser();
        populateMonitoredByUsers();
    }

    private void setUpToolBar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.linkToolbar);
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        Button parentsLink = findViewById(R.id.parentsLink);
        monitoringLink.setClickable(false);
        monitoringLink.setAlpha(1f);
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
                Intent intent = new Intent(ManageMonitoring.this, ManageGroups.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
        messagesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageMonitoring.this, ManageMessagesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
        parentsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageMonitoring.this, ParentDashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
    }

    private void setUpAddMonitoringButton() {
        Button addMonitoring = findViewById(R.id.add_monitoring_btn);
        addMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AddMonitoringActivity.makeIntent(ManageMonitoring.this);
                startActivity(intent);
            }
        });
    }


    private void setUpAddMonitoredButton() {
        Button addMonitored = findViewById(R.id.add_monitored_btn);
        addMonitored.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = AddMonitoredActivity.makeIntent(ManageMonitoring.this);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_manage_monitoring, menu);
        MenuItem permissionItem = menu.findItem(R.id.permissionsButtonMonitoring);
        getNumRequests(permissionItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.accountInfoButton:
                intent = new Intent(ManageMonitoring.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;
            case R.id.logOutButton:
                Toast.makeText(ManageMonitoring.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Helper.logUserOut(ManageMonitoring.this);
                return true;
            case R.id.permissionsButtonMonitoring:
                intent = PermissionActivity.makeIntent(ManageMonitoring.this);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void getNumRequests(MenuItem pendingPermissions) {
        Call<List<PermissionRequest>> permCaller = proxy.getPermissions(user.getId(), WGServerProxy.PermissionStatus.PENDING);
        ProxyBuilder.callProxy(ManageMonitoring.this, permCaller, returnedPerms -> getNumPerms(returnedPerms, pendingPermissions));
    }

    private void getNumPerms(List<PermissionRequest> returnedPerms, MenuItem pendingPermissions) {
        int number;
        number = valueOf(returnedPerms.size());
        if(number <= 0) {
            pendingPermissions.setTitle("requests");
        } else if(number == 1) {
            pendingPermissions.setTitle("" + String.valueOf(returnedPerms.size()) + " new request");
        } else {
            pendingPermissions.setTitle("" + String.valueOf(returnedPerms.size()) + " new requests");
        }
    }

    private void populateMonitorsUser() {
        Call<List<User>> userCaller = proxy.getMonitorsUsers(user.getId());
        ProxyBuilder.callProxy(ManageMonitoring.this, userCaller,
                returnedUsers -> {
                    populateMonitorsListView(returnedUsers);
                });
    }

    private void populateMonitoredByUsers() {
        Call<List<User>> userCaller = proxy.getMonitoredByUsers(user.getId());
        ProxyBuilder.callProxy(ManageMonitoring.this, userCaller,
                returnedUsers -> {
                    populateMonitoredByListView(returnedUsers);
                });
    }

    private void populateMonitorsListView(List<User> monitorsUser) {
        ArrayAdapter<User> adapter = new MyListMonitors(monitorsUser);
        ListView monitoringList = findViewById(R.id.monitoring_listview);
        monitoringList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.monitoring_layout);
        monitoringList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = MonitoringUsersActivity.makeIntent(ManageMonitoring.this,
                        monitorsUser.get(i));
                startActivity(intent);
            }
        });

        monitoringList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageMonitoring.this);
                builder.setMessage("Would you like to stop monitoring this user?");
                // Add the buttons
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Long monitoredUserID = (Long) view.getTag();
                        deleteMonitoringUser(monitoredUserID);
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
    }

    private void deleteMonitoringUser(Long monitoringUserID) {
        Long currentUserId = user.getId();
        Call<Void> deleteCaller = proxy.removeFromMonitoredByUsers(monitoringUserID, currentUserId);
        ProxyBuilder.callProxy(deleteCaller, returnNothing -> deleteMonitoringUserSuccess(returnNothing));
    }

    private void deleteMonitoringUserSuccess(Void returnNothing) {
        Toast.makeText(ManageMonitoring.this, "No longer monitoring user", Toast.LENGTH_SHORT).show();
        populateMonitorsUser();
    }

    private void populateMonitoredByListView(List<User> monitoredUser) {
        ArrayAdapter<User> adapter = new MyListMonitoredBy(monitoredUser);
        ListView monitoredByList = findViewById(R.id.monitored_listview);
        monitoredByList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.monitored_layout);

        monitoredByList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageMonitoring.this);
                builder.setMessage("Would you like to not be monitored by this user?");
                // Add the buttons
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Long monitoringUserID = (Long) view.getTag();
                        deleteMonitoredByUser(monitoringUserID);
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
    }

    private void deleteMonitoredByUser(Long monitoringUserID) {
        Long currentUserId = user.getId();
        Call<Void> deleteCaller = proxy.removeFromMonitoredByUsers(currentUserId, monitoringUserID);
        ProxyBuilder.callProxy(deleteCaller, returnNothing -> deleteMonitoredByUserSuccess(returnNothing));
    }

    private void deleteMonitoredByUserSuccess(Void returnNothing) {
        Toast.makeText(ManageMonitoring.this, "No longer being monitored by user", Toast.LENGTH_SHORT).show();
        populateMonitoredByUsers();
    }

    private class MyListMonitors extends ArrayAdapter<User>{
        List<User> mUserList;

        public MyListMonitors(List<User> userList) {
            super(ManageMonitoring.this, R.layout.monitoring_layout
                    , userList);
            mUserList = userList;
            Log.i("Test", mUserList.toString());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if(convertView == null){
                itemView = getLayoutInflater().inflate(R.layout.monitoring_layout,
                        parent,
                        false);
            }

            User currentUser;

            // Find the current User
            if (mUserList.isEmpty()) {
                currentUser = new User();
                currentUser.setName("No one is being monitored.");
                currentUser.setEmail(" ");
            }
            else {
                currentUser = mUserList.get(position);
                itemView.setTag(currentUser.getId());
            }
            if (currentUser.getName() != null && currentUser.getEmail() != null) {
                try {
                    TextView nameText = itemView.findViewById(R.id.txtMonitoringName);
                    nameText.setText(currentUser.getName());

                    TextView emailText = itemView.findViewById(R.id.txtMonitoringEmail);
                    emailText.setText(currentUser.getEmail());
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mUserList.toString());
                }
            }

            return itemView;
        }
    }

    private class MyListMonitoredBy extends ArrayAdapter<User> {
        List<User> mUserList;

        public MyListMonitoredBy(List<User> userList) {
            super(ManageMonitoring.this, R.layout.monitored_layout
                    , userList);
            mUserList = userList;
            Log.i("Test", mUserList.toString());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if(convertView == null){
                itemView = getLayoutInflater().inflate(R.layout.monitored_layout,
                        parent,
                        false);
            }

            User currentUser;

            // Find the current User
            if (mUserList.isEmpty()) {
                currentUser = new User();
                currentUser.setName("No one is monitoring you.");
                currentUser.setEmail(" ");
            }
            else {
                currentUser = mUserList.get(position);
                itemView.setTag(currentUser.getId());
            }
            if (currentUser.getName() != null && currentUser.getEmail() != null) {
                try {
                    TextView nameText = (TextView) itemView.findViewById(R.id.txtMonitedByName);
                    nameText.setText(currentUser.getName());

                    TextView emailText = (TextView) itemView.findViewById(R.id.txtMonitedByEmail);
                    emailText.setText(currentUser.getEmail());
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mUserList.toString());
                }
            }

            return itemView;
        }
    }

    private void getNumUnreadMessages(TextView unreadMessagesText) {
        Call<List<Message>> messageCall = proxy.getUnreadMessages(user.getId(), null);
        ProxyBuilder.callProxy(ManageMonitoring.this, messageCall, returnedMessages -> getInNumber(returnedMessages, unreadMessagesText));
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
