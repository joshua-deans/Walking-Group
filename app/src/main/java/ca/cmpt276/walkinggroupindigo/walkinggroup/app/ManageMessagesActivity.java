package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.TextView;
import java.util.List;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_TOKEN;

public class ManageMessagesActivity extends AppCompatActivity {
    public static final String GROUP_ID_EXTRA = "ca.cmpt276.walkinggroupindigo.walkinggroup - ManageMassages groupID";

    long mGroupId;
    private WGServerProxy proxy;
    private User user;

    EditText inputMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_messages);
        getAPIKey();
        getGroupId();
        setUpNewMessageButton();
        populateMessages();
    }

    private void getAPIKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    private void getGroupId() {
        mGroupId = getIntent().getLongExtra(GROUP_ID_EXTRA, -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        long groupId;
        groupId = mGroupId;
        switch (item.getItemId()) {
            case R.id.group_details_btn:
                Intent intent = new Intent(ManageMessagesActivity.this, GroupDetailsActivity.class);
                intent.putExtra(GROUP_ID_EXTRA, groupId);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpNewMessageButton() {
        Button newMessageButton = findViewById(R.id.new_message_btn);
        newMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageMessagesActivity.this);
                inputMessage = new EditText(ManageMessagesActivity.this);
                builder.setView(inputMessage);
                builder.setMessage("Write a massage");
                builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String textMessage = inputMessage.getText().toString();
                        //TODO: Send the message;
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    private void populateMessages() {
        Call<List<Message>> messageCaller = proxy.getMessages(user.getId());
       ProxyBuilder.callProxy(ManageMessagesActivity.this, messageCaller, returnedMessages -> {
            populateMessagesListView(returnedMessages);
        });
    }

    private void populateMessagesListView(List<Message> returnedMessages) {
        ArrayAdapter<Message> adapter = new MessageList(returnedMessages);
        ListView messagesListView = findViewById(R.id.messages_listvIew);
        messagesListView.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.messages_layout);
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });
    }

    private class MessageList extends ArrayAdapter<Message>{
        List<Message> mMessageList;

        public MessageList(List<Message> messageList) {
            super(ManageMessagesActivity.this, R.layout.messages_layout
                    , messageList);
            mMessageList = messageList;
        }

        /*
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (convertView == null) {
                itemView = getLayoutInflater().inflate(R.layout.messages_layout,
                        parent, false);
            }

            Message currentMessage;

            if (mMessageList.isEmpty()) {
                currentMessage = new Message();
            } else {
                currentMessage = mMessageList.get(position);
                itemView.setTag(currentMessage.getId());
            }
            if (currentMessage.getText() != null) {
                try {
                    TextView messageHeader = itemView.findViewById(R.id.message_header);
                    messageHeader.setText(currentMessage.getFromUser().getName());

                    TextView messageContext = itemView.findViewById(R.id.message_context);
                    messageContext.setText(currentMessage.getText());
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mMessageList.toString());
                }
            }
            return itemView;
        }*/
    }

    private String getToken() {
        Context context = ManageMessagesActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(LOG_IN_SAVE_TOKEN, null);
        return token;
    }
}
