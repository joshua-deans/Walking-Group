package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class EmergencyMessagesActivity extends AppCompatActivity {

    long mGroupId;
    public static final String GROUP_ID_EXTRA = "ca.cmpt276.walkinggroupindigo.walkinggroup - ManageGroups groupID";
    private WGServerProxy proxy;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(EmergencyMessagesActivity.this, mUser);
        setContentView(R.layout.activity_emergency_messages);
        setActionBarText(getString(R.string.manage_messages));
        setUpToolBar();
        proxy = ProxyFunctions.setUpProxy(EmergencyMessagesActivity.this, getString(R.string.apikey));
        populateEmergency();
        updateUI();
        if (mGroupId == -1) {
            errorMessage();
        } else {
            getEmergencyMessages(mGroupId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {

    }

    private void setUpToolBar() {
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        messagesLink.setClickable(false);
        mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        monitoringLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmergencyMessagesActivity.this, ManageMonitoring.class);
                startActivity(intent);
                finish();
            }
        });
        groupsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmergencyMessagesActivity.this, ManageGroups.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void getGroupId() {
        mGroupId = getIntent().getLongExtra(GROUP_ID_EXTRA, -1);
    }

    private void errorMessage() {
        Toast.makeText(EmergencyMessagesActivity.this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void getEmergencyMessages(long groupId) {
    }

    private void populateEmergency() {
        Call<List<Group>> groupCaller = proxy.getGroups();
        ProxyBuilder.callProxy(EmergencyMessagesActivity.this, groupCaller,
                returnedGroups -> populateEmergencyListView(returnedGroups));
    }

    private void populateEmergencyListView(List<Group> returnedGroups) {
        List<Group> userInGroups = getAllGroups(returnedGroups);
        ArrayAdapter<Group> adapter = new MyGroupsList(userInGroups);
        ListView groupsList = findViewById(R.id.emer_messages_listview);
        groupsList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.emergency_messages_layout);
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Long groupId = (Long) view.getTag();
                Intent intent = new Intent(EmergencyMessagesActivity.this, ManageMessagesActivity.class);
                intent.putExtra(GROUP_ID_EXTRA, groupId);
                startActivity(intent);
            }
        });

        groupsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });
    }

    private List<Group> getAllGroups(List<Group> returnedGroups) {
        List<Group> userIn = getGroupsUserIn (returnedGroups);
        userIn.addAll(getGroupsUserLead(returnedGroups));
        return userIn;
    }

    private List<Group> getGroupsUserLead(List<Group> returnedGroups) {
        List<Group> groupInformation = new ArrayList<>();
        for(Group aGroup : returnedGroups) {
            if (aGroup.getLeader().getId().equals(mUser.getId())) {
                groupInformation.add(aGroup);
            }
        }
        return groupInformation;
    }

    private List<Group> getGroupsUserIn(List<Group> returnedGroups) {
        List<Group> groupInformation = new ArrayList<>();
        List<Group> userGroups = mUser.getMemberOfGroups();
        for(Group aGroup : returnedGroups) {
            for (Group u: userGroups) {
                if (u.getId().equals(aGroup.getId()))
                    groupInformation.add(aGroup);
            }
        }
        return groupInformation;
    }

    private class MyGroupsList extends ArrayAdapter<Group> {
        List<Group> mGroupList;

        MyGroupsList(List<Group> groupList) {
            super(EmergencyMessagesActivity.this, R.layout.emergency_messages_layout
                    , groupList);
            mGroupList = groupList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView= convertView;
            if (convertView == null) {
                itemView = getLayoutInflater().inflate(R.layout.emergency_messages_layout,
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
                    TextView nameText = itemView.findViewById(R.id.emergency_message_name);
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
