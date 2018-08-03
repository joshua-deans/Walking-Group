package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

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

import java.security.Permission;
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

public class ManageMessagesActivity extends AppCompatActivity {

    Long messageId;
    private WGServerProxy proxy;
    private User mUser;
    private Message mMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(ManageMessagesActivity.this, mUser);
        setContentView(R.layout.activity_manage_messages);
        setActionBarText(getString(R.string.manage_messages));
        mMessage = new Message();
        proxy = ProxyFunctions.setUpProxy(ManageMessagesActivity.this, getString(R.string.apikey));
        setUpToolBar();
        populateMessages();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        updateUI();
    }

    private void updateUI() {
        populateMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_manage_messages, menu);
        MenuItem permissionItem = menu.findItem(R.id.permissionsButton);
        getNumRequests(permissionItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        switch (item.getItemId()) {
            case R.id.accountInfoButton:
                intent = new Intent(ManageMessagesActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;
            case R.id.logOutButton:
                Toast.makeText(ManageMessagesActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Helper.logUserOut(ManageMessagesActivity.this);
                return true;
            case R.id.permissionsButton:
                intent = PermissionActivity.makeIntent(ManageMessagesActivity.this);
                startActivity(intent);
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
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        messagesLink.setClickable(false);
        messagesLink.setAlpha(1f);
        unreadMessages.setAlpha(1f);

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
                Intent intent = new Intent(ManageMessagesActivity.this, ManageMonitoring.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
        groupsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageMessagesActivity.this, ManageGroups.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
        parentsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageMessagesActivity.this, ParentDashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
                finish();
            }
        });
    }

    private void getNumUnreadMessages(TextView unreadMessagesText) {
        Call<List<Message>> messageCall = proxy.getUnreadMessages(mUser.getId(), null);
        ProxyBuilder.callProxy(ManageMessagesActivity.this, messageCall, returnedMessages -> getInNumber(returnedMessages, unreadMessagesText));
    }

    private void getInNumber(List<Message> returnedMessages, TextView unreadMessagesText) {
        unreadMessagesText.setText(String.valueOf(returnedMessages.size()));
    }



    private void populateMessages() {
        Call<List<Message>> messageCaller = proxy.getMessages(mUser.getId());
        ProxyBuilder.callProxy(ManageMessagesActivity.this, messageCaller,
                returnedMessages -> populateMessagesListView(returnedMessages));
    }

    private void populateMessagesListView(List<Message> returnedMessages) {
        markMessagesRead(returnedMessages);
        ArrayAdapter<Message> adapter = new MyListOfMessages(returnedMessages);
        ListView messagesListView = findViewById(R.id.messages_listview);
        messagesListView.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.messages_layout);
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Does nothing for now, no particular user stories.
            }
        });

        messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageMessagesActivity.this);
                builder.setMessage("Would you like to delete this message?");
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        Long monitoringUserID = (Long) view.getTag();
                        Long messageId = (Long) view.getTag();
                        deleteMessage(messageId);
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

    private void deleteMessage(Long messageId) {
        Call<Void> messageCall = proxy.deleteMessage(messageId);
        ProxyBuilder.callProxy(ManageMessagesActivity.this, messageCall, returnNothing -> onDeleteSuccess(returnNothing));
    }

    private void onDeleteSuccess(Void returnNothing) {
        Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show();
        updateUI();
    }


    private void markMessagesRead(List<Message> returnedMessages) {
        for (Message aMessage : returnedMessages) {
            if (!aMessage.isRead()) {
                messageId = aMessage.getId();
                Call<Message> messageCall = proxy.markMessageAsRead(messageId, true);
                ProxyBuilder.callProxy(ManageMessagesActivity.this, messageCall, returnNothing -> markedAsRead(returnNothing));
            }
        }
    }

    private void markedAsRead(Message returnNothing) {
        Toast.makeText(this, "Messages are read", Toast.LENGTH_SHORT).show();
    }

    public void getNumRequests(MenuItem pendingPermissions) {
        Call<List<PermissionRequest>> permCaller = proxy.getPermissions(mUser.getId(), WGServerProxy.PermissionStatus.PENDING);
        ProxyBuilder.callProxy(ManageMessagesActivity.this, permCaller, returnedPerms -> getNumPerms(returnedPerms, pendingPermissions));
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

    private class MyListOfMessages extends ArrayAdapter<Message> {
        List<Message> mMessageList;

        public MyListOfMessages(List<Message> messageList) {
            super(ManageMessagesActivity.this, R.layout.messages_layout
                , messageList);
            mMessageList = messageList;
        }

        @NonNull
        @Override
        public  View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if(convertView == null){
                itemView = getLayoutInflater().inflate(R.layout.messages_layout,
                        parent,
                        false);
            }

            Message newMessage;
            User newUser;

            if (mMessageList.isEmpty()) {
                newMessage = new Message();
            }
            else {
                newMessage = mMessageList.get(position);
                itemView.setTag(newMessage.getId());
            }
            if (newMessage.getText() != null) {
                try {
                    newUser = newMessage.getFromUser();
                    TextView context = itemView.findViewById(R.id.message_context);
                    context.setText(newMessage.getText());

                    TextView nameText = itemView.findViewById(R.id.message_header);
                    nameText.setText(newUser.getName());

                    TextView statusText = itemView.findViewById(R.id.message_status);
                    if (newMessage.isRead()) {
                        statusText.setText("read");
                    }else {
                        statusText.setText("unread");
                        if (newMessage.isEmergency()) {
                            statusText.setText("EMERGENCY");
                        }
                    }

                } catch (NullPointerException e) {
                    Log.e("Error", e + mMessageList.toString());
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
