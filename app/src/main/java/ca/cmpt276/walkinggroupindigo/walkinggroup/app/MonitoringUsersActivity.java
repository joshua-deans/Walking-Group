package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.fragments.RemoveMonitoringUserGroupMessageFragment;
import ca.cmpt276.walkinggroupindigo.walkinggroup.fragments.StopMonitoringUserMessageFragment;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_TOKEN;

public class MonitoringUsersActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private User user;
    private Group group;
    private List<User> monitorsUserGroupList = new ArrayList<>();

    public static Intent makeIntent (Context context) {
        return new Intent (context, MonitoringUsersActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_users);
        user = User.getInstance();
        getApiKey();
        setUpAddGroupButton();
//        populateMonitorsUserGroups();                                                              //WILL IMPLEMENT SOON
        populateMonitorsUserGroupsListView();
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    private void setUpAddGroupButton() {
        Button addToGroupButton = findViewById(R.id.add_to_group_btn);
        addToGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText findGroupEditText = findViewById(R.id.group_id_edittxt);
                String address = findGroupEditText.getText().toString();
                if (address == null) {
                    Toast.makeText(MonitoringUsersActivity.this,
                            "" + R.string.group_id_empty,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    Long numAddress = Long.parseLong(address);
                    if(groupExists(numAddress)){
                        addMonitorsUserGroup(numAddress);
                        return;
                    }
                    else {
                        Toast.makeText(MonitoringUsersActivity.this,
                                "" + R.string.group_not_found,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
    }

    private boolean groupExists(Long address) {
        Call<List<Group>> groupCaller = proxy.getGroups();
        List<Group> existingGroups = new ArrayList<>();

        ProxyBuilder.callProxy(MonitoringUsersActivity.this, groupCaller, returnedGroups -> {
            existingGroups.addAll(returnedGroups);
        });
        return isFound(existingGroups, address);
    }

    private boolean isFound(List<Group> groups, Long address) {
        for (Group aGroup : groups) {
            if (aGroup.getId().equals(address)) {
                return true;
            }
        }
        return false;
    }

    private void addMonitorsUserGroup (Long groupsId) {
        Call<Group> groupCall = proxy.getGroupById(groupsId);
        List<Group> monitorsUserGroup = new ArrayList<>();
        ProxyBuilder.callProxy(MonitoringUsersActivity.this,
                groupCall, returnedGroup -> {
                    monitorsUserGroup.add(returnedGroup);
                });
        Group monitorUserGroup = monitorsUserGroup.get(0);
        Call<List<User>> monitorsUserGroupCaller = proxy.addGroupMember(groupsId, user);
        ProxyBuilder.callProxy(MonitoringUsersActivity.this,
                monitorsUserGroupCaller, returnMonitorsUserGroup -> {});
    }


    //POPULATE GROUP LIST VIEW CODE STARTING HERE:
    //TODO: uncomment the chunk of code bellow and solve the issue.
    //error: no suitable method found for addAll(List<Group>)
    //method Collection.addAll(Collection<? extends User>) is not applicable
    //(argument mismatch; List<Group> cannot be converted to Collection<? extends User>)


//    private void populateMonitorsUserGroups() {
//        Call<List<Group>> groupCaller = proxy.getGroups();
//        ProxyBuilder.callProxy(MonitoringUsersActivity.this, groupCaller,
//                returnedGroups -> monitorsUserGroupList.addAll(returnedGroups));
//    }
//
    private void populateMonitorsUserGroupsListView() {
        ArrayAdapter<User> adapter = new MonitoringUsersActivity.MonitoringUsersGroupList();
        ListView groupsList = findViewById(R.id.monitor_user_groups_list);
        groupsList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.group_layout);
        groupsList.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
                RemoveMonitoringUserGroupMessageFragment removeDialog = new RemoveMonitoringUserGroupMessageFragment();
                removeDialog.show(manager, "RemoveMonitoredUserFromGroup");
                return true;
            }
        });
    }

    private class MonitoringUsersGroupList extends ArrayAdapter<User>{
        public MonitoringUsersGroupList() {
            super (MonitoringUsersActivity.this, R.layout.group_layout
            ,monitorsUserGroupList );
        }
    }

    public String getToken() {
        Context context = MonitoringUsersActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(LOG_IN_SAVE_TOKEN, null);
        return token;
    }
}
