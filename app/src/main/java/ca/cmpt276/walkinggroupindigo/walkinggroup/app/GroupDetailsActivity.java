package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups.GROUP_ID_EXTRA;

public class GroupDetailsActivity extends AppCompatActivity {
    long mGroupId;
    private WGServerProxy proxy;
    private User mUser;
    private User leaderUser;
    private long leaderId;
    private boolean leader = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);
        mUser = User.getInstance();
        leaderUser = new User();
        getApiKey();
        getGroupId();
        if (mGroupId == -1) {
            errorMessage();
        } else {
            getGroupDetails(mGroupId);
        }
    }

    private void getGroupUsers(long groupId) {
        Call<List<User>> groupCaller = proxy.getGroupMembers(groupId);
        ProxyBuilder.callProxy(GroupDetailsActivity.this, groupCaller, returnedListOfUsers -> populateUserListView(returnedListOfUsers));
    }

    private void populateUserListView(List<User> returnedListOfUsers) {
        ArrayAdapter<User> adapter = new MyUsersList(returnedListOfUsers);
        ListView userListView = (ListView) findViewById(R.id.groupUserList);
        userListView.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.group_detail_list_view);
        if (leader) {
            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Long userID = (Long) view.getTag();
                    if (userID != leaderId) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
                        builder.setMessage(R.string.remove_user_from_group_prompt);
                        // Add the buttons
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Call<Void> deleteCaller = proxy.removeGroupMember(mGroupId, userID);
                                ProxyBuilder.callProxy(GroupDetailsActivity.this, deleteCaller, returnNothing -> successDelete(returnNothing));
                            }
                        });
                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        Toast.makeText(GroupDetailsActivity.this, R.string.cant_delete_leader, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void successDelete(Void returnNothing) {
        Toast.makeText(GroupDetailsActivity.this, R.string.success_deletion, Toast.LENGTH_SHORT).show();
        getGroupDetails(mGroupId);
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    private void getGroupDetails(long groupId) {
        Call<Group> groupCaller = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(GroupDetailsActivity.this, groupCaller, returnedGroup -> extractGroupData(returnedGroup));
    }

    private void extractGroupData(Group returnedGroup) {
        TextView groupName = (TextView) findViewById(R.id.groupNameDetail);
        groupName.setText(returnedGroup.getGroupDescription());
        leaderId = returnedGroup.getLeader().getId();
        Call<User> leaderCaller = proxy.getUserById(leaderId);
        ProxyBuilder.callProxy(GroupDetailsActivity.this, leaderCaller, returnedLeader -> getLeaderData(returnedLeader));
    }

    private void getLeaderData(User returnedLeader) {
        leaderUser = returnedLeader;
        if (mUser.getId() == leaderId) {
            leader = true;
            Toast.makeText(GroupDetailsActivity.this, R.string.you_are_leader, Toast.LENGTH_SHORT).show();
        }
        getGroupUsers(mGroupId);
    }

    private void errorMessage() {
        Toast.makeText(GroupDetailsActivity.this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void getGroupId() {
        mGroupId = getIntent().getLongExtra(GROUP_ID_EXTRA, -1);
    }

    public String getToken() {
        Context context = GroupDetailsActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LoginActivity.LOG_IN_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(LoginActivity.LOG_IN_SAVE_TOKEN, "");
        return token;
    }

    private class MyUsersList extends ArrayAdapter<User> {
        List<User> mUserList;

        public MyUsersList(List<User> userList) {
            super(GroupDetailsActivity.this, R.layout.group_detail_list_view, userList);
            mUserList = userList;
            mUserList.add(0, leaderUser);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (convertView == null) {
                itemView = getLayoutInflater().inflate(R.layout.group_detail_list_view,
                        parent,
                        false);
            }

            User currentUser;

            if (mUserList.isEmpty()) {
                currentUser = new User();
                currentUser.setName(getString(R.string.no_users_string));
            } else {
                currentUser = mUserList.get(position);
                itemView.setTag(currentUser.getId());
            }
            if (currentUser.getName() != null && currentUser.getEmail() != null) {
                try {
                    TextView nameText = itemView.findViewById(R.id.groupDetailUserName);
                    TextView emailText = itemView.findViewById(R.id.groupDetailUserEmail);
                    nameText.setText(currentUser.getName());
                    emailText.setText(currentUser.getEmail());
                    if (currentUser.getId() == leaderId) {
                        TextView leaderText = itemView.findViewById(R.id.groupLeaderTag);
                        leaderText.setText(R.string.leader_tag);
                    }

                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mUserList.toString());
                }
            }
            return itemView;
        }
    }
}