package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_TOKEN;

public class DashboardActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private User mUser;
    private Message mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mUser = User.getInstance();

        getApiKey();
        createGreeting();
        setupMapButton();
        setupMonitorButton();
        setupGroupButton();
//        populateMessages();
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    private void createGreeting() {
        TextView greeting = (TextView) findViewById(R.id.welcome_user);
        greeting.setText(String.format("Welcome %s!", mUser.getName()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creates action bar buttons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Click listener for action bar
        switch (item.getItemId()) {
            case R.id.logOutButton:
                Toast.makeText(DashboardActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                logUserOut();
                return true;

            case R.id.accountInfoButton:
                Intent intent = new Intent(DashboardActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void logUserOut() {
        Context context = DashboardActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LOG_IN_SAVE_KEY, "");
        editor.putString(LOG_IN_SAVE_TOKEN, "");
        editor.apply();

        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupGroupButton() {
        Button groupButton = (Button) findViewById(R.id.manage_group);
        groupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ManageGroups.class);
                startActivity(intent);
            }
        });
    }

    private void setupMonitorButton() {
        Button monitorButton = (Button) findViewById(R.id.manage_monitoring);
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ManageMonitoring.class);
                startActivity(intent);
            }
        });
    }

    private void setupMapButton() {
        Button mapButton = (Button) findViewById(R.id.viewMapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

//    private void populateCoversations() {
//        Call<List<Message>> messagesCaller = proxy.getMessages();
//        ProxyBuilder.callProxy(
//                DashboardActivity.this, messagesCaller,
//                returnedMessages -> {
//                    populateConversationsListView(returnedMessages);
//                });
//    }
//
//    private void populateConversationsListView(List<Message> returnedMessages) {
//        Call<List<Message>> adapter = new MessagesList(returnedMessages);
//    }
//
//    private class MessagesList extends ArrayAdapter {
//        List<Message> mMessageList;
//
//        public MessagesList(List<Message> messageList) {
//            super(DashboardActivity.this, R.layout.messages_layout
//                    , messageList);
//            mMessageList = messageList;
//        }
//
//        @NonNull
//        @Override
//        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//            View itemView = convertView;
//            if (convertView == null) {
//                itemView = getLayoutInflater().inflate(R.layout.group_layout,
//                        parent,
//                        false);
//            }
//
//            Message message;
//
//            if (mMessageList.isEmpty()) {
//                message = new Message();
//            } else {
//                message = mMessageList.get(position);
//                itemView.setTag(message.getData());
//            }
//            if (message.getData() != null) {
//                try {
//                    TextView
//                }
//            }
//        }
//
//    }

    public String getToken() {
        Context context = DashboardActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LoginActivity.LOG_IN_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(LoginActivity.LOG_IN_SAVE_TOKEN, "");
        return token;
    }
}
