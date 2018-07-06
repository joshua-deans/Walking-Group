package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
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
        setContentView(R.layout.activity_add_monitoring);
        user = User.getInstance();
        getApiKey();
        setUpMonitorButton();
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        // Token is not getting properly
        proxy = ProxyBuilder.getProxy(apiKey,  LoginActivity.getLogInToken(AddMonitoringActivity.this));
    }

    private void setUpMonitorButton() {
        Button monitorButton = findViewById(R.id.add_user_button);
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText findUserEditText = findViewById(R.id.find_user_edit_txt);
                String address = findUserEditText.getText().toString();
                if (address == null) {
                    Toast.makeText(AddMonitoringActivity.this,
                            "" + R.string.email_empty_login,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    if(userExists(address)){
                        addMonitorUser(address);
                    }
                    else{
                        Toast.makeText(AddMonitoringActivity.this,
                                "" + R.string.email_not_found,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

    }

    // Check whether corresponding email address exists
    private boolean userExists(String address) {
        Call<List<User>> usersCaller = proxy.getUsers();
        List<User> existingUsers = new ArrayList<>();

        ProxyBuilder.callProxy(AddMonitoringActivity.this, usersCaller, returnedUsers -> {
            existingUsers.addAll(returnedUsers);
        });
        return isFound(existingUsers, address);
    }

    // Return true if email address is found, otherwise false
    private boolean isFound(List<User> users, String address) {
        for (User aUser : users) {
            if (aUser.getEmail().equalsIgnoreCase(address)) {
                return true;
            }
        }
        return false;
    }

    // Adding the user into monitor sets
    private void addMonitorUser(String emailAddress) {
        Call<User> userCall = proxy.getUserByEmail(emailAddress);
        List<User> monitors = new ArrayList<>();
        ProxyBuilder.callProxy(AddMonitoringActivity.this,
                userCall, returnedUser->{
            monitors.add(returnedUser);
                });
        User monitor = monitors.get(0);
        Call<List<User>> monitorsCaller = proxy.addToMonitorsUsers(user.getId(), monitor);
        ProxyBuilder.callProxy(AddMonitoringActivity.this,
               monitorsCaller, returnMonitors->{} );
    }
}
