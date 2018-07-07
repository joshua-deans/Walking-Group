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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;
import retrofit2.Response;

public class AddMonitoringActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private User user;
    private String address;

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
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    private void setUpMonitorButton() {
        Button monitorButton = findViewById(R.id.add_user_button);
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText findUserEditText = findViewById(R.id.find_user_edit_txt);
                address = findUserEditText.getText().toString();
                if (address == null) {
                    Toast.makeText(AddMonitoringActivity.this,
                            "" + R.string.email_empty_login,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    addMonitorUser();
                }
            }
        });

    }

    // Check whether corresponding email address exists in the system
    private void addMonitorUser() {
        Call<List<User>> usersCaller = proxy.getUsers();
        ProxyBuilder.callProxy(AddMonitoringActivity.this, usersCaller, returnedUser -> successfulAdded(returnedUser),
                responseBody -> handleUserCreateError(responseBody));
    }

    private void handleUserCreateError(retrofit2.Response response) {
        try {
            String responseBody = response.errorBody().string();
            JSONObject json = new JSONObject(responseBody);
            Toast.makeText(AddMonitoringActivity.this,
                    json.get("message").toString(),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            Toast.makeText(AddMonitoringActivity.this,
                    "Unable to add a user",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void successfulAdded(List<User> returnedUser) {
        for(User aUser: returnedUser){
            if(aUser.getEmail().equalsIgnoreCase(address)){
                List<User> monitored = new ArrayList<>();
                monitored.addAll(user.getMonitorsUsers());
                monitored.add(aUser);
                user.setMonitorsUsers(monitored);
                finish();
            }
        }
        Toast.makeText(AddMonitoringActivity.this,
                "Unable to add a user: " + address
                + " does NOT exists in the system",
                Toast.LENGTH_SHORT).show();
    }

    public String getToken() {
        Context context = AddMonitoringActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LoginActivity.LOG_IN_KEY, context.MODE_PRIVATE);
        String token = sharedPref.getString(LoginActivity.LOG_IN_SAVE_TOKEN, "");
        return token;
    }
}
