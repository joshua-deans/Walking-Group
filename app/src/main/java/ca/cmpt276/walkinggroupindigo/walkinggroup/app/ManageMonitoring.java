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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ManageMonitoring extends AppCompatActivity {

    private WGServerProxy proxy;
    private User user;

    public static Intent makeIntent (Context context){
        return new Intent (context, ManageMonitoring.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_monitoring);
        setActionBarText(getString(R.string.manage_monitoring));
        setUpToolBar();
        user = User.getInstance();
        setUpAddMonitoringButton();
        setUpAddMonitoredButton();
        proxy = ProxyFunctions.setUpProxy(ManageMonitoring.this, getString(R.string.apikey));
        populateMonitorsUser();
        populateMonitoredByUsers();
    }

    private void setUpToolBar() {
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.linkToolbar);
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        monitoringLink.setClickable(false);
        mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        groupsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageMonitoring.this, ManageGroups.class);
                startActivity(intent);
                finish();
            }
        });
        messagesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageMonitoring.this, ManageMessagesActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        populateMonitorsUser();
        populateMonitoredByUsers();
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

    private void setActionBarText(String title) {
        try {
            getActionBar().setTitle(title);
            getSupportActionBar().setTitle(title);
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle(title);
        }
    }
}
