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
import android.widget.EditText;
import android.widget.ListView;
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

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups.GROUP_ID_EXTRA;

public class GroupDetailsActivity extends AppCompatActivity {

    private Long mGroupId;
    private Long mMessageId;
    private WGServerProxy proxy;
    private User mUser;
    private User leaderUser;
    private Group messagedGroup;
    private Message mMessage;
    private Long leaderId;
    private boolean leader = false;

    private EditText inputMessage;

    public static Intent makeIntent(Context context) {
        return new Intent(context, GroupDetailsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(GroupDetailsActivity.this, mUser);
        setContentView(R.layout.activity_group_details);
        setActionBarText("");
        mMessage = new Message();
        leaderUser = new User();
        messagedGroup = new Group();
        proxy = ProxyFunctions.setUpProxy(GroupDetailsActivity.this, getString(R.string.apikey));
        getGroupId();
        if (mGroupId == -1) {
            errorMessage();
        } else {
            getGroupDetails(mGroupId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creates action bar buttons
        MenuInflater inflater = getMenuInflater();
        Call<Group> groupCall = proxy.getGroupById(mGroupId);
        ProxyBuilder.callProxy(GroupDetailsActivity.this,
                groupCall,
                returnedGroups ->
                    getUserInformation(returnedGroups, inflater, menu));
        return true;
    }

    private void getUserInformation(Group returnedGroups, MenuInflater inflater, Menu menu) {
        if (mUser.getId().equals(returnedGroups.getLeader().getId())) {
            inflater.inflate(R.menu.action_bar_messages, menu);
        } else {
            inflater.inflate(R.menu.action_bar_message_child, menu);
        }
    }

    private void getGroupLeader(Group returnedGroup) {
        leaderId = returnedGroup.getLeader().getId();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Call<Group> groupCaller = proxy.getGroupById(mGroupId);
        ProxyBuilder.callProxy(GroupDetailsActivity.this,
                groupCaller,
                returnedGroup -> getGroupLeader(returnedGroup));
        if (mUser.getId().equals(leaderId)) {
            leader = true;
            switch (item.getItemId()) {
                case R.id.broadcast_message:

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(GroupDetailsActivity.this);
                    builder1.setMessage("Send broadcast message:");
                    inputMessage = new EditText(this);
                    builder1.setView(inputMessage);
                    builder1.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mMessage.setText(inputMessage.getText().toString());
                            Call<List<Message>> groupMessageCaller = proxy.newMessageToGroup(mGroupId, mMessage);
                            ProxyBuilder.callProxy(GroupDetailsActivity.this, groupMessageCaller, message -> markAsUnread(message));
                            Call<List<User>> userCaller = proxy.getGroupMembers(mGroupId);
                            ProxyBuilder.callProxy(GroupDetailsActivity.this, userCaller, returnedUsers -> getParents(returnedUsers));
                        }
                    });
                    builder1.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialogBroadCast = builder1.create();
                    dialogBroadCast.show();
                    return true;

                case R.id.parents_message:

                    AlertDialog.Builder builder2 = new AlertDialog.Builder(GroupDetailsActivity.this);
                    builder2.setMessage("Send message to parents:");
                    inputMessage = new EditText(this);
                    builder2.setView(inputMessage);
                    builder2.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mMessage.setText(inputMessage.getText().toString());
                            Call<List<User>> userCaller = proxy.getGroupMembers(mGroupId);
                            ProxyBuilder.callProxy(GroupDetailsActivity.this, userCaller, returnedUsers -> getParents(returnedUsers));
                        }
                    });
                    builder2.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialogParents = builder2.create();
                    dialogParents.show();
                    return true;

                default:

                    return super.onOptionsItemSelected(item);
            }
        } else{
            switch (item.getItemId()) {
                case R.id.group_message:

                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupDetailsActivity.this);
                    builder.setMessage("Send message to group:");
                    inputMessage = new EditText(this);
                    builder.setView(inputMessage);
                    builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mMessage.setText(inputMessage.getText().toString());
                            Call<List<Message>> groupMessageCaller = proxy.newMessageToGroup(mGroupId, mMessage);
                            ProxyBuilder.callProxy(GroupDetailsActivity.this, groupMessageCaller, message -> markAsUnread(message));
                            Call<List<User>> userCaller = proxy.getGroupMembers(mGroupId);
                            ProxyBuilder.callProxy(GroupDetailsActivity.this, userCaller, returnedUsers -> getParents(returnedUsers));
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialogGroupMessage = builder.create();
                    dialogGroupMessage.show();
                    return true;

                default:

                    return super.onOptionsItemSelected(item);
            }
        }
    }

    private void getParents(List<User> returnedUsers) {
        for (User aUser : returnedUsers) {
            Call<List<Message>> messageCaller = proxy.newMessageToParentsOf(aUser.getId(), mMessage);
            ProxyBuilder.callProxy(GroupDetailsActivity.this,
                    messageCaller,
                    message -> markAsUnread(message));
        }
    }

    private void markAsUnread(List<Message> message) {
        for(Message aMessage : message) {
            aMessage.setIsRead(false);
            Call<Message> messageCaller = proxy.markMessageAsRead(aMessage.getId(), false);
            ProxyBuilder.callProxy(GroupDetailsActivity.this, messageCaller, returnNothing -> onSendSuccess(returnNothing));
        }
    }

    private void onSendSuccess(Message returnNothing) {
        Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show();
    }

    private void getGroupUsers(long groupId) {
        Call<List<User>> groupCaller = proxy.getGroupMembers(groupId);
        ProxyBuilder.callProxy(GroupDetailsActivity.this,
                groupCaller,
                returnedListOfUsers -> populateUserListView(returnedListOfUsers));
    }

    private void populateUserListView(List<User> returnedListOfUsers) {
        ArrayAdapter<User> adapter = new MyUsersList(returnedListOfUsers);
        ListView userListView = (ListView) findViewById(R.id.groupUserList);
        userListView.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.group_detail_list_view);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Long groupId = (Long) view.getTag();
                generateParentList(groupId);
            }
        });
        if (leader) {
            userListLongClickFunction(userListView);
        }
    }

    private void generateParentList(Long userID) {
        Call<List<User>> parents = proxy.getMonitoredByUsers(userID);
        ProxyBuilder.callProxy(GroupDetailsActivity.this,
                parents,
                returnedUsers->{
                    Log.i("Returned Users: ", returnedUsers.toString());
                    ArrayAdapter<User> parentAdapter = new MyParentsList(returnedUsers);
                    TextView parentView = findViewById(R.id.parentView);
                    parentView.setText(getString(R.string.parent_information));
                    ListView userListView = (ListView) findViewById(R.id.parentUserList);
                    userListView.setAdapter(parentAdapter);
                    new ArrayAdapter<>(this,
                            R.layout.group_detail_list_view);
                    userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Long groupId = (Long) view.getTag();
                            Intent intent = UserInfoActivity.makeIntent(GroupDetailsActivity.this);
                            intent.putExtra(GROUP_ID_EXTRA, groupId);
                            startActivity(intent);
                        }
                    });

                });
    }

    private void userListLongClickFunction(ListView userListView) {
        userListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
                return true;
            }
        });
    }

    private void successDelete(Void returnNothing) {
        Toast.makeText(GroupDetailsActivity.this, R.string.success_deletion, Toast.LENGTH_SHORT).show();
        getGroupDetails(mGroupId);
    }

    private void getGroupDetails(long groupId) {
        Call<Group> groupCaller = proxy.getGroupById(groupId);
        ProxyBuilder.callProxy(GroupDetailsActivity.this,
                groupCaller,
                returnedGroup -> extractGroupData(returnedGroup));
    }

    private void extractGroupData(Group returnedGroup) {
        TextView groupName = (TextView) findViewById(R.id.groupNameDetail);
        groupName.setText(returnedGroup.getGroupDescription());
        setActionBarText(returnedGroup.getGroupDescription());
        leaderId = returnedGroup.getLeader().getId();
        Call<User> leaderCaller = proxy.getUserById(leaderId);
        ProxyBuilder.callProxy(GroupDetailsActivity.this, leaderCaller, returnedLeader -> getLeaderData(returnedLeader));
    }

    private void getLeaderData(User returnedLeader) {
        leaderUser = returnedLeader;
        if (mUser.getId().equals(leaderId)) {
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
                    TextView titleText = itemView.findViewById(R.id.title);
                    nameText.setText(currentUser.getName());
                    emailText.setText(currentUser.getEmail());
                    if (currentUser.getRewards().getTitle() != null) {
                        titleText.setText(currentUser.getRewards().getTitle());
                    } else {
                        titleText.setVisibility(View.GONE);
                    }
                    if (leaderId != null && currentUser.getId().equals(leaderId)) {
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

    private void setActionBarText(String title) {
        try {
            getActionBar().setTitle(title);
            getSupportActionBar().setTitle(title);
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle(title);
        }
    }


    private class MyParentsList extends ArrayAdapter<User> {
        List<User> mUserList;

        public MyParentsList(List<User> userList) {
            super(GroupDetailsActivity.this, R.layout.parent_details, userList);
            mUserList = userList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (convertView == null) {
                itemView = getLayoutInflater().inflate(R.layout.parent_details,
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
                    TextView nameText = itemView.findViewById(R.id.parentDetailsName);
                    TextView emailText = itemView.findViewById(R.id.parentDetailsEmail);
                    nameText.setText(currentUser.getName());
                    emailText.setText(currentUser.getEmail());
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mUserList.toString());
                }
            }
            return itemView;
        }
    }
}