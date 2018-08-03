package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class AddMonitoredActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private User user;

    public static Intent makeIntent (Context context) {
        return new Intent (context, AddMonitoredActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        Helper.setCorrectTheme(AddMonitoredActivity.this, user);
        setContentView(R.layout.activity_add_monitored);
        setActionBarText(getString(R.string.add_user_monitor_you));
        proxy = ProxyFunctions.setUpProxy(AddMonitoredActivity.this, getString(R.string.apikey));
        setUpGetMonitoredButton();
    }

    private void setUpGetMonitoredButton() {
        Button getMonitoredButton = findViewById(R.id.get_monitored_btn);
        getMonitoredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText findUserEditText = findViewById(R.id.find_user_edit_txt2);
                String address = findUserEditText.getText().toString();
                if (address.matches("")) {
                    Toast.makeText(AddMonitoredActivity.this,
                            "" + R.string.email_empty_login,
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    findUser(address);
                }
            }
        });
    }

    private void findUser(String address) {
        Call<List<User>> usersCaller = proxy.getUsers();

        ProxyBuilder.callProxy(AddMonitoredActivity.this, usersCaller,
                returnedUsers -> checkIfFound(returnedUsers, address));
    }

    private void checkIfFound(List<User> returnedUsers, String address) {
        boolean userFound = false;
        for (User aUser : returnedUsers) {
            if (aUser.getEmail().equalsIgnoreCase(address)) {
                userFound = true;
                addMonitoredUser(address);
            }
        }
        if (!userFound) {
            Toast.makeText(AddMonitoredActivity.this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMonitoredUser(String address) {
        Call<User> userCall = proxy.getUserByEmail(address);
        ProxyBuilder.callProxy(AddMonitoredActivity.this,
                userCall, returnedUser -> findUserByEmail(returnedUser));
    }

    private void findUserByEmail(User returnedUser) {
        Call<List<User>> monitorsCaller = proxy.addToMonitorsUsers(returnedUser.getId(), user);
        ProxyBuilder.callProxy(AddMonitoredActivity.this,
                monitorsCaller, returnMonitors -> successMonitored(returnMonitors));
    }

    private void successMonitored(List<User> returnMonitors) {
        Toast.makeText(AddMonitoredActivity.this, "Monitoring successful", Toast.LENGTH_SHORT).show();
        finish();
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