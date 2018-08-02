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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups.GROUP_ID_EXTRA;

public class MonitoringUsersActivity extends AppCompatActivity {

    public static final String MONITORED_ID = "ca.cmpt276.walkinggroupindigo.walkinggroup.app.MonitoringUsersActivity - Monitored ID";
    private WGServerProxy proxy;
    private User user;
    private static User addedOne;

    public static Intent makeIntent (Context context, User user) {
        addedOne = user;
        return new Intent (context, MonitoringUsersActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        Helper.setCorrectTheme(MonitoringUsersActivity.this, user);
        setContentView(R.layout.activity_monitoring_users);
        setActionBarText(String.format(getString(R.string.monitoring_title), addedOne.getName()));
        proxy = ProxyFunctions.setUpProxy(MonitoringUsersActivity.this, getString(R.string.apikey));
        setUpAddGroupButton();
        populateMonitorsUserGroups();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateMonitorsUserGroups();
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_monitoring, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.edit_info:
                intent = new Intent(MonitoringUsersActivity.this, MonitoringInfoActivity.class);
                intent.putExtra(MONITORED_ID, addedOne.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void updateUI() {
        Call<List<Group>> groupsCaller = proxy.getGroups();
        ProxyBuilder.callProxy(
                MonitoringUsersActivity.this, groupsCaller,
                returnedGroups -> {
                    populateMonitorsUserGroupsListView(returnedGroups);
                });
    }

    private void setUpAddGroupButton() {
        Button addToGroupButton = findViewById(R.id.add_to_group_btn);
        addToGroupButton.setOnClickListener(view -> {
            EditText findGroupEditText = findViewById(R.id.group_id_edittxt);
            String address = findGroupEditText.getText().toString();
            if (address.matches("")) {
                Toast.makeText(MonitoringUsersActivity.this,
                        "" + R.string.group_id_empty,
                        Toast.LENGTH_SHORT).show();
            }
            else {
                groupExists(address);
                Toast.makeText(MonitoringUsersActivity.this,
                        "" + R.string.group_not_found,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void groupExists(String address) {
        Call<List<Group>> groupCaller = proxy.getGroups();
        ProxyBuilder.callProxy(MonitoringUsersActivity.this,
                groupCaller,
                returnedGroups->{
                    checkIfFound(returnedGroups, address);
                });
    }

    private void checkIfFound(List<Group> returnedGroups, String address) {
        for (Group aGroup : returnedGroups) {
            if (aGroup.getGroupDescription().equalsIgnoreCase(address)) {
                addMonitorsUserGroup(aGroup);
            }
        }
    }

    private void addMonitorsUserGroup (Group group) {
        Call<List<User>> monitorsUserGroupCaller = proxy.addGroupMember(group.getId(), addedOne);
        ProxyBuilder.callProxy(MonitoringUsersActivity.this,
                monitorsUserGroupCaller,
                returnMonitorsUserGroup -> {
                    Toast.makeText(MonitoringUsersActivity.this,
                            "User added to the group", Toast.LENGTH_SHORT).show();
                });
        updateUI();
        finish();
        return;
    }

    private void populateMonitorsUserGroups() {
        Call<List<Group>> groupsCaller = proxy.getGroups();
        ProxyBuilder.callProxy(MonitoringUsersActivity.this, groupsCaller,
                this::populateMonitorsUserGroupsListView);
    }

    private void populateMonitorsUserGroupsListView(List<Group> returnedGroups) {
        List<Group> userInGroups = getAllGroups(returnedGroups);
        ArrayAdapter<Group> adapter = new MonitoringUsersActivity.MonitoringUsersGroupList(userInGroups);
        ListView groupsList = findViewById(R.id.monitor_user_groups_list);
        groupsList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.group_layout);
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Long groupId = (Long) view.getTag();
                Intent intent = new Intent(MonitoringUsersActivity.this, GroupDetailsActivity.class);
                intent.putExtra(GROUP_ID_EXTRA, groupId);
                startActivity(intent);
            }
        });

        groupsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MonitoringUsersActivity.this);
                builder.setMessage("Would you like to remove this user from the group?");
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Long groupId = (Long) view.getTag();
                        removeFromGroup(groupId);
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                updateUI();
                return true;
            }
        });
    }

    private void removeFromGroup(Long groupId) {
        Call<Void> removeFromGroupCaller = proxy.removeGroupMember(groupId, addedOne.getId());
        ProxyBuilder.callProxy(MonitoringUsersActivity.this, removeFromGroupCaller,
                returnNothing -> removeFromGroupSuccess(returnNothing));
    }

    private void removeFromGroupSuccess(Void returnNothing) {
        Toast.makeText(MonitoringUsersActivity.this,
                "You removed the user from this group",
                Toast.LENGTH_SHORT).show();
        populateMonitorsUserGroups();
        updateUI();
        finish();
    }

    private List<Group> getGroupsUserLead(List<Group> returnedGroups) {
        List<Group> groupInformation = new ArrayList<>();
        for(Group aGroup : returnedGroups) {
            if (aGroup.getLeader() != null && aGroup.getLeader().getId().equals(addedOne.getId())) {
                groupInformation.add(aGroup);
            }
        }
        return groupInformation;
    }

    private List<Group> getGroupsUserIn(List<Group> returnedGroups) {
        List<Group> groupInformation = new ArrayList<>();
        List<Group> userGroups = addedOne.getMemberOfGroups();
        for (Group aGroup : returnedGroups) {
            for (Group u : userGroups) {
                if (u.getId().equals(aGroup.getId())) {
                    groupInformation.add(aGroup);
                }
            }
        }
        return groupInformation;
    }

    private List<Group> getAllGroups(List<Group> returnedGroups) {
        List<Group> userIn = getGroupsUserIn(returnedGroups);
        userIn.addAll(getGroupsUserLead(returnedGroups));
        return userIn;
    }

    private class MonitoringUsersGroupList extends ArrayAdapter<Group> {
        List<Group> mGroupList;

        public MonitoringUsersGroupList(List<Group> groupList) {
            super(MonitoringUsersActivity.this, R.layout.group_layout
                , groupList);
            mGroupList = groupList;
        }

        @NonNull
        @Override
        public  View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (convertView == null) {
                itemView = getLayoutInflater().inflate(R.layout.group_layout,
                        parent,
                        false);
            }

            Group currentGroup;
            Switch toggleWalkSwitch = itemView.findViewById(R.id.toggleWalk);

            if (mGroupList.isEmpty()) {
                currentGroup = new Group();
            } else {
                currentGroup = mGroupList.get(position);
                itemView.setTag(currentGroup.getId());
            }
            if (currentGroup.getGroupDescription() != null) {
                try {
                    TextView nameText = itemView.findViewById(R.id.group_name);
                    nameText.setText(currentGroup.getGroupDescription());
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mGroupList.toString());
                }
            }
            toggleWalkSwitch.setClickable(false);
            toggleWalkSwitch.setVisibility(View.INVISIBLE);
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
