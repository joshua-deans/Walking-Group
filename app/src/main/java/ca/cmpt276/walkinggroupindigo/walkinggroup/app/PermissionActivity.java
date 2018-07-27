package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class PermissionActivity extends AppCompatActivity {

    public static final String EMERGENCY_ID = "ca.cmpt276.walkinggroupindigo.walkinggroup.app_mEmergencyMessageId";
    private WGServerProxy proxy;
    private User mUser;
    private Long mEmergencyMessageId;
    private Message mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        initializeServer();
        setUpToolBar();
    }

    private void initializeServer() {
        mUser = User.getInstance();
        mMessage = new Message();
        proxy = ProxyFunctions.setUpProxy(PermissionActivity.this, getString(R.string.apikey));
    }

    public static Intent makeIntent(Context context){
        return new Intent(context, PermissionActivity.class);
    }

    private void setUpToolBar() {
        Button mapLink = findViewById(R.id.mapLink);
        Button groupsLink = findViewById(R.id.groupsLink);
        Button monitoringLink = findViewById(R.id.monitoringLink);
        Button messagesLink = findViewById(R.id.messagesLink);
        Button parentsLink = findViewById(R.id.parentsLink);
        Button permissionLink = findViewById(R.id.permissionLink);
        permissionLink.setClickable(false);
        mapLink.setAlpha(1f);
        TextView unreadMessages = findViewById(R.id.unreadMessagesLink);
        getNumUnreadMessages(unreadMessages);
        monitoringLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, ManageMonitoring.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        groupsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, ManageGroups.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        messagesLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, GroupedMessagesActivity.class);
                intent.putExtra(EMERGENCY_ID, mEmergencyMessageId);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        parentsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PermissionActivity.this, ParentDashboardActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MapsActivity.makeIntent(PermissionActivity.this);
                startActivity(intent);
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
    }

    private void getNumUnreadMessages(TextView unreadMessagesText) {
        Call<List<Message>> messageCall = proxy.getUnreadMessages(mUser.getId(), null);
        ProxyBuilder.callProxy(PermissionActivity.this,
                messageCall,
                returnedMessages -> getInNumber(returnedMessages, unreadMessagesText));
    }

    private void getInNumber(List<Message> returnedMessages, TextView unreadMessagesText) {
        unreadMessagesText.setText(String.valueOf(returnedMessages.size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Creates action bar buttons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_dashboard, menu);
        MenuItem item = menu.findItem(R.id.emergency_message);
        if (mUser.getCurrentWalkingGroup() == null) {
            item.setVisible(false);
        } else {
            item.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Click listener for action bar
        Intent intent;
        switch (item.getItemId()) {
            case R.id.logOutButton:
                Toast.makeText(PermissionActivity.this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                Helper.logUserOut(PermissionActivity.this);
                return true;

            case R.id.accountInfoButton:
                intent = new Intent(PermissionActivity.this, AccountInfoActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}
