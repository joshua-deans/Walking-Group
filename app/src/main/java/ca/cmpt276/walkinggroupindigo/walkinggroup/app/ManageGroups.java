package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ManageGroups extends AppCompatActivity {

    private WGServerProxy proxy;
    private User user;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_group);
        user = User.getInstance();
        getApiKey();
        populateGroups();
    }

    protected void onResume() {
        super.onResume();
        populateGroups();
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_manage_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_group:
                Intent intent = new Intent(ManageGroups.this, CreateGroup.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    private void populateGroups() {
        Call<List<Group>> groupsCaller = proxy.getGroups();
        ProxyBuilder.callProxy(ManageGroups.this, groupsCaller,
                returnedGroups -> {
                    populateGroupsListView(returnedGroups);
                });
    }
//
//    private boolean checkForUserGroups(List<User> returnedUsers) {
//        for (User returnedUser : returnedUsers) {
//            return  if(user.getId() != returnedUser.getId());
//        }
//    }

    private void populateGroupsListView(List<Group> returnedGroups) {
//        Call<List<User>> userCaller = proxy.getGroupMembers(user.getId());
//        ProxyBuilder.callProxy(ManageGroups.this, userCaller,
//                returnedUsers -> checkForUserGroups(returnedUsers));
        ArrayAdapter<Group> adapter = new MyGroupsList(returnedGroups);
        ListView groupsList = findViewById(R.id.group_listview);
        groupsList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.group_layout);
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //go to view group members
            }
        });

        groupsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageGroups.this);
                builder.setMessage("Would you like to exit this group?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Long groupId = (Long) view.getTag();
                        exitGroup(groupId);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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

    private void exitGroup(Long groupId) {
        Long currentUserId = user.getId();
        Call<Void> exitCaller = proxy.removeGroupMember(groupId, currentUserId);
        ProxyBuilder.callProxy(exitCaller, returnNothing -> exitFromGroupSuccess(returnNothing));
    }

    private void exitFromGroupSuccess(Void returnNothing) {
        Toast.makeText(ManageGroups.this,
                "You are removed from the group",
                Toast.LENGTH_SHORT).show();
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

                    //TODO: DISPLAY GROUP LEADER AS WELL, or some new and surprising idea!!
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mGroupsList.toString());
                }
            }
            return itemView;
        }
    }

    public String getToken() {
        Context context = ManageGroups.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LoginActivity.LOG_IN_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(LoginActivity.LOG_IN_SAVE_TOKEN, "");
        return token;
    }
}

