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

public class AddMonitoringActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private User user;

    public static Intent makeIntent (Context context){
        return new Intent (context, AddMonitoringActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        Helper.setCorrectTheme(AddMonitoringActivity.this, user);
        setContentView(R.layout.activity_add_monitoring);
        setActionBarText(getString(R.string.user_to_monitor));
        proxy = ProxyFunctions.setUpProxy(AddMonitoringActivity.this, getString(R.string.apikey));
        setUpMonitorButton();
    }

    private void setUpMonitorButton() {
        Button monitorButton = findViewById(R.id.add_monitoring_user_button);
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText findUserEditText = findViewById(R.id.find_user_edit_txt);
                String address = findUserEditText.getText().toString();
                if (address.matches("")) {
                    Toast.makeText(AddMonitoringActivity.this,
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
        ProxyBuilder.callProxy(AddMonitoringActivity.this, usersCaller,
                returnedUsers -> checkIfFound(returnedUsers, address));
    }

    private void checkIfFound(List<User> returnedUsers, String address) {
        boolean userFound = false;
        for (User aUser : returnedUsers) {
            if (aUser.getEmail().equalsIgnoreCase(address)) {
                userFound = true;
                addMonitorUser(address);
            }
        }
        if (!userFound) {
            Toast.makeText(AddMonitoringActivity.this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }

    // Adding the user into monitor sets
    private void addMonitorUser(String emailAddress) {
        Call<User> userCall = proxy.getUserByEmail(emailAddress);
        ProxyBuilder.callProxy(AddMonitoringActivity.this,
                userCall, returnedUser -> findUserByEmail(returnedUser));
    }

    private void findUserByEmail(User returnedUser) {
        if (returnedUser != null) {
            Call<List<User>> monitorsCaller = proxy.addToMonitorsUsers(user.getId(), returnedUser);
            ProxyBuilder.callProxy(AddMonitoringActivity.this,
                    monitorsCaller, returnMonitors -> successMonitor(returnMonitors));
        } else {
            Toast.makeText(AddMonitoringActivity.this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void successMonitor(List<User> returnMonitors) {
        Toast.makeText(AddMonitoringActivity.this, "Monitoring successful", Toast.LENGTH_SHORT).show();
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