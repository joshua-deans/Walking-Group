package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.MapsActivity.EMERGENCY_ID;

public class GroupedMessagesActivity extends AppCompatActivity {

    Long mGroupId;
    Long mEmergencyMessageId;
    public static final String GROUP_ID_EXTRA = "ca.cmpt276.walkinggroupindigo.walkinggroup - ManageGroups groupID";
    private WGServerProxy proxy;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(GroupedMessagesActivity.this, mUser);
        setContentView(R.layout.activity_grouped_messages);
        setActionBarText(getString(R.string.manage_messages));
        proxy = ProxyFunctions.setUpProxy(GroupedMessagesActivity.this, getString(R.string.apikey));
//        setUpToolBar();
        getEmergencyMessageId();
//        populateGroups();
//        setUpUnreadMessagesTextView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
//        populateGroups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_manage_monitoring, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.accountInfoButton:
                intent = new Intent(GroupedMessagesActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;
            case R.id.logOutButton:
                Toast.makeText(GroupedMessagesActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Helper.logUserOut(GroupedMessagesActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

//    private void setUpToolBar() {
//        Button mapLink = findViewById(R.id.mapLink);
//        Button groupsLink = findViewById(R.id.groupsLink);
//        Button monitoringLink = findViewById(R.id.monitoringLink);
//        Button messagesLink = findViewById(R.id.messagesLink);
//        Button parentsLink = findViewById(R.id.parentsLink);
//        Button permissionLink = findViewById(R.id.permissionLink);
//        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
//        getNumUnreadMessages(unreadMessages);
//        messagesLink.setClickable(false);
//        messagesLink.setAlpha(1f);
//        unreadMessages.setAlpha(1f);
//        mapLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//                overridePendingTransition(0, 0); //0 for no animation
//            }
//        });
//        monitoringLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(GroupedMessagesActivity.this, ManageMonitoring.class);
//                startActivity(intent);
//                overridePendingTransition(0, 0); //0 for no animation
//                finish();
//            }
//        });
//        groupsLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(GroupedMessagesActivity.this, ManageGroups.class);
//                startActivity(intent);
//                overridePendingTransition(0, 0); //0 for no animation
//                finish();
//            }
//        });
//        parentsLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(GroupedMessagesActivity.this, ParentDashboardActivity.class);
//                startActivity(intent);
//                overridePendingTransition(0, 0); //0 for no animation
//                finish();
//            }
//        });
//        permissionLink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = PermissionActivity.makeIntent(GroupedMessagesActivity.this);
//                startActivity(intent);
//                overridePendingTransition(0, 0); //0 for no animation
//                finish();
//            }
//        });
//    }

//    private void populateGroups() {
//        Call<List<Group>> groupCaller = proxy.getGroups();
//        ProxyBuilder.callProxy(GroupedMessagesActivity.this, groupCaller,
//                returnedGroups -> populateGroupsListView(returnedGroups));
//    }
//
//    private void populateGroupsListView(List<Group> returnedGroups) {
//        List<Group> userInGroups = getAllGroups(returnedGroups);
//        ArrayAdapter<Group> adapter = new MyGroupsList(userInGroups);
//        ListView groupsList = findViewById(R.id.grouped_messages_listview);
//        groupsList.setAdapter(adapter);
//        new ArrayAdapter<>(this,
//                R.layout.grouped_messages_layout);
//        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Long groupId = (Long) view.getTag();
//                Intent intent = new Intent(GroupedMessagesActivity.this, ManageMessagesActivity.class);
//                intent.putExtra(GROUP_ID_EXTRA, groupId);
//                startActivity(intent);
//            }
//        });
//
//        groupsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                return true;
//            }
//        });
//    }
//
//    private List<Group> getAllGroups(List<Group> returnedGroups) {
//        List<Group> userIn = getGroupsUserIn (returnedGroups);
//        userIn.addAll(getGroupsUserLead(returnedGroups));
//        return userIn;
//    }
//
//    private List<Group> getGroupsUserLead(List<Group> returnedGroups) {
//        List<Group> groupInformation = new ArrayList<>();
//        for(Group aGroup : returnedGroups) {
//            if (aGroup.getLeader() != null && aGroup.getLeader().getId().equals(mUser.getId())) {
//                groupInformation.add(aGroup);
//            }
//        }
//        return groupInformation;
//    }
//
//    private List<Group> getGroupsUserIn(List<Group> returnedGroups) {
//        List<Group> groupInformation = new ArrayList<>();
//        List<Group> userGroups = mUser.getMemberOfGroups();
//        for(Group aGroup : returnedGroups) {
//            for (Group u: userGroups) {
//                if (u.getId().equals(aGroup.getId())) {
//                    groupInformation.add(aGroup);
//                }
//            }
//        }
//        return groupInformation;
//    }

    private void getNumUnreadMessages(TextView unreadMessagesText) {
        Call<List<Message>> messageCall = proxy.getUnreadMessages(mUser.getId(), null);
        ProxyBuilder.callProxy(GroupedMessagesActivity.this, messageCall, returnedMessages -> getInNumber(returnedMessages, unreadMessagesText));
    }

    private void getInNumber(List<Message> returnedMessages, TextView unreadMessagesText) {
        unreadMessagesText.setText(String.valueOf(returnedMessages.size()));
    }

    public void getEmergencyMessageId() {
        mEmergencyMessageId = getIntent().getLongExtra(EMERGENCY_ID, -1);
    }

    private class MyGroupsList extends ArrayAdapter<Group> {
        List<Group> mGroupList;

        MyGroupsList(List<Group> groupList) {
            super(GroupedMessagesActivity.this, R.layout.grouped_messages_layout
                    , groupList);
            mGroupList = groupList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView= convertView;
            if (convertView == null) {
                itemView = getLayoutInflater().inflate(R.layout.grouped_messages_layout,
                        parent,
                        false);
            }

            Group currentGroup;

            if (mGroupList.isEmpty()) {
                currentGroup = new Group();
            } else {
                currentGroup = mGroupList.get(position);
                itemView.setTag(currentGroup.getId());
            }
            if (currentGroup.getGroupDescription() != null) {
                try {
                    TextView nameText = itemView.findViewById(R.id.group_message_name);
                    nameText.setText(currentGroup.getGroupDescription());
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mGroupList.toString());
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
