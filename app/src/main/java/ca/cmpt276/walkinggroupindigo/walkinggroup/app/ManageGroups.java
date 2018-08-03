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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ca.cmpt276.walkinggroupindigo.walkinggroup.GPSJobService;
import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ManageGroups extends AppCompatActivity {

    public static final String GROUP_ID_EXTRA = "ca.cmpt276.walkinggroupindigo.walkinggroup - ManageGroups groupID";
    public static final int PICK_REQUEST = 9;
    public static final String GPS_JOB_ID = "ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups - GPS Job ID";
    public static final String GPS_DEST_LAT = "ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups - GPS dest lat";
    public static final String GPS_DEST_LONG = "ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups - GPS dest long";
    private WGServerProxy proxy;
    private User user;
    //private Group group;

    public static Intent makeIntent(Context context) {
        return new Intent (context, ManageGroups.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        Helper.setCorrectTheme(ManageGroups.this, user);
        setContentView(R.layout.activity_manage_group);
        proxy = ProxyFunctions.setUpProxy(ManageGroups.this, getString(R.string.apikey));
        setUpToolBar();
        setActionBarText(getString(R.string.manage_groups));
        updateUI();
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

    private void updateUI() {
        Call<List<Group>> groupsCaller = proxy.getGroups();
        ProxyBuilder.callProxy(
                ManageGroups.this, groupsCaller,
                returnedGroups -> {
                    populateGroupsListView(returnedGroups);
                });
    }

    @Override
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
                intent = new Intent(ManageGroups.this, CreateGroup.class);
                startActivity(intent);
                return true;
            case R.id.accountInfoButton:
                intent = new Intent(ManageGroups.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;
            case R.id.logOutButton:
                Toast.makeText(ManageGroups.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Helper.logUserOut(ManageGroups.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void setUpToolBar() {
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        Button parentsLink = findViewById(R.id.parentsLink);
        groupsLink.setClickable(false);
        groupsLink.setAlpha(1f);
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        monitoringLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageGroups.this, ManageMonitoring.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
        messagesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageGroups.this, ManageMessagesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
        parentsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageGroups.this, ParentDashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
    }

    private void populateGroupsListView(List<Group> returnedGroups) {
        List<Group> userInGroups = getAllGroups(returnedGroups);
        ArrayAdapter<Group> adapter = new MyGroupsList(userInGroups);
        ListView groupsList = findViewById(R.id.group_listview);
        groupsList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.group_layout);
        setGroupListItemClicker(groupsList);

        setGroupListItemLongClicker(groupsList);
    }

    private void setGroupListItemClicker(ListView groupsList) {
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Long groupId = (Long) view.getTag();
                Intent intent = GroupDetailsActivity.makeIntent(ManageGroups.this);
                intent.putExtra(GROUP_ID_EXTRA, groupId);
                startActivity(intent);
            }
        });
    }

    private void setGroupListItemLongClicker(ListView groupsList) {
        groupsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageGroups.this);
                builder.setMessage("Would you like to exit this group?");
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Long groupId = (Long) view.getTag();
                        exitGroup(groupId);
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
                return true;
            }
        });
    }

    private List<Group> getAllGroups(List<Group> returnedGroups) {
        List<Group> userIn = getGroupsUserIn(returnedGroups);
        userIn.addAll(getGroupsUserLead(returnedGroups));
        return userIn;
    }


    private List<Group> getGroupsUserLead(List<Group> returnedGroups) {
        List<Group> groupInformation = new ArrayList<>();
        for(Group aGroup : returnedGroups) {
            if (aGroup.getLeader() != null && aGroup.getLeader().getId().equals(user.getId())  ) {
                groupInformation.add(aGroup);
            }
        }
        return groupInformation;
    }


    private List<Group> getGroupsUserIn(List<Group> returnedGroups) {
        List<Group> groupInformation = new ArrayList<>();
        List<Group> userGroups = user.getMemberOfGroups();
        for(Group aGroup : returnedGroups) {
            for (Group u: userGroups) {
                if (u.getId().equals(aGroup.getId()))
                    groupInformation.add(aGroup);
            }
        }
        return groupInformation;
    }


    private void exitGroup(Long groupId) {
        Long currentUserId = user.getId();
        Call<Group> getCurrentGroup = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(ManageGroups.this,
                getCurrentGroup,
                returnedGroups ->
                       removeMember(returnedGroups, currentUserId));
        }

    private void removeMember(Group group, Long currentUserId) {
        if(group.getLeader().getId().equals(currentUserId)){
            Call<Void> deleteCaller = proxy.deleteGroup(group.getId());
            ProxyBuilder.callProxy(ManageGroups.this, deleteCaller, returnNothing -> exitFromGroupSuccess(returnNothing));
        }
        else {
            Call<Void> exitCaller = proxy.removeGroupMember(group.getId(), currentUserId);
            ProxyBuilder.callProxy(ManageGroups.this, exitCaller, returnNothing -> exitFromGroupSuccess(returnNothing));
        }
    }


    private void exitFromGroupSuccess(Void returnNothing) {
        Call<User> callerUser = proxy.getUserById(user.getId());
        ProxyBuilder.callProxy(ManageGroups.this, callerUser, returnedUser -> onSuccess(returnedUser));

    }

    private void onSuccess(User returnedUser) {
        user.setMemberOfGroups(returnedUser.getMemberOfGroups());
        user.setLeadsGroups(returnedUser.getLeadsGroups());
        Toast.makeText(ManageGroups.this,
                "You are removed from the group",
                Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private void toggleSwitchListener(Group currentGroup, Switch toggleWalkSwitch) {
        toggleWalkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(ManageGroups.this, GPSJobService.class);
                // Prevents user from walking with multiple groups
                if (isChecked) {
                    if (user.getCurrentWalkingGroup() == null) {
                        user.setCurrentWalkingGroup(currentGroup);
                        intent.putExtra(GPS_JOB_ID, user.getId());
                        intent.putExtra(GPS_DEST_LAT, currentGroup.getDestLatitude());
                        intent.putExtra(GPS_DEST_LONG, currentGroup.getDestLongitude());
                        // Start tracking GPS
                        startService(intent);
                        Toast.makeText(ManageGroups.this, "Started walking with " + currentGroup.getGroupDescription(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (!Objects.equals(user.getCurrentWalkingGroup().getId(), currentGroup.getId())) {
                            toggleWalkSwitch.setChecked(false);
                        }
                    }
                } else {
                    if (Objects.equals(user.getCurrentWalkingGroup().getId(), currentGroup.getId())) {
                        user.setCurrentWalkingGroup(null);
                        // Stop tracking GPS
                        stopService(intent);
                        Toast.makeText(ManageGroups.this, "Stopped walking with " + currentGroup.getGroupDescription(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private class MyGroupsList extends ArrayAdapter<Group> {
        List<Group> mGroupsList;

        public MyGroupsList(List<Group> groupList) {
            super(ManageGroups.this, R.layout.group_layout
                    , groupList);
            mGroupsList = groupList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;

            if (convertView == null) {
                itemView = getLayoutInflater().inflate(R.layout.group_layout,
                        parent,
                        false);
            }

            Group groupWalkingWith = user.getCurrentWalkingGroup();
            Switch toggleWalkSwitch = itemView.findViewById(R.id.toggleWalk);

            manageGroupView(position, itemView, groupWalkingWith, toggleWalkSwitch);

            return itemView;
        }

        private void manageGroupView(int position, View itemView, Group groupWalkingWith, Switch toggleWalkSwitch) {
            Group currentGroup;
            if (mGroupsList.isEmpty()) {
                currentGroup = new Group();
            } else {
                currentGroup = mGroupsList.get(position);
                itemView.setTag(currentGroup.getId());
            }
            if (currentGroup.getGroupDescription() != null) {
                try {
                    TextView nameText = itemView.findViewById(R.id.group_name);
                    nameText.setText(currentGroup.getGroupDescription());
                    TextView leaderText = itemView.findViewById(R.id.group_id);
                    Call<User> current = proxy.getUserById(currentGroup.getLeader().getId());
                    ProxyBuilder.callProxy(ManageGroups.this,
                            current,
                            returnedUser->{
                                leaderText.setText(String.format("%s %s", getString(R.string.leader_of_group), returnedUser.getName()));
                            });

                    //TODO: DISPLAY GROUP LEADER AS WELL, or some new and surprising idea!!
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mGroupsList.toString());
                }
                if (groupWalkingWith != null && currentGroup.getId().equals(groupWalkingWith.getId())) {
                    toggleWalkSwitch.setChecked(true);
                }
                toggleSwitchListener(currentGroup, toggleWalkSwitch);
            }
        }
    }

    private void getNumUnreadMessages(TextView unreadMessagesText) {
        Call<List<Message>> messageCall = proxy.getUnreadMessages(user.getId(), null);
        ProxyBuilder.callProxy(ManageGroups.this, messageCall, returnedMessages -> getInNumber(returnedMessages, unreadMessagesText));
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

