package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class AddMonitoredActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private User user;

    public static Intent makeIntent (Context context) {
        return new Intent (context, AddMonitoredActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_monitored);
        user = User.getInstance();
        getApiKey();
        setUpGetMonitoredButton();
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    private void setUpGetMonitoredButton() {
        Button getMonitoredButton = findViewById(R.id.get_monitored_btn);
        getMonitoredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText findUserEditText = findViewById(R.id.find_user_edit_txt2);
                String address = findUserEditText.getText().toString();
                if (address == null) {
                    Toast.makeText(AddMonitoredActivity.this,
                            "" + R.string.email_empty_login,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    if (userExists(address)) {
                        addMonitoredUser(address);
                        finish();
                    }
                    else {
                        Toast.makeText(AddMonitoredActivity.this,
                                "" + R.string.email_not_found,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
    }

    private boolean userExists(String address) {
        Call<List<User>> usersCaller = proxy.getUsers();
        List<User> existingUsers = new ArrayList<>();

        ProxyBuilder.callProxy(AddMonitoredActivity.this, usersCaller,
                returnedUsers -> existingUsers.addAll(returnedUsers));
        return isFound(existingUsers, address);
    }

    private boolean isFound(List<User> users, String address) {
        for (User aUser : users) {
            if (aUser.getEmail().equalsIgnoreCase(address)) {
                return true;
            }
        }
        return false;
    }

    private void addMonitoredUser(String address) {
        Call<User> userCall = proxy.getUserByEmail(address);
        List<User> getMonitored = new ArrayList<>();
        ProxyBuilder.callProxy(AddMonitoredActivity.this,
                userCall, returnedUser -> getMonitored.add(returnedUser));
        User monitored = getMonitored.get(0);
        Call<List<User>> monitoredByUsers = proxy.addToMonitoredByUsers(user.getId(), monitored);
        ProxyBuilder.callProxy(AddMonitoredActivity.this,
                monitoredByUsers, returnMonitored -> {});
        finish();
    }

    public String getToken() {
        Context context = AddMonitoredActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LoginActivity.LOG_IN_KEY, context.MODE_PRIVATE);
        String token = sharedPref.getString(LoginActivity.LOG_IN_SAVE_TOKEN, "");
        return token;
    }
}