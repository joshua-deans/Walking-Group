package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginScreen";
    public static final String LOG_IN_KEY = "ca.cmpt276.walkinggroupindigo.walkinggroup - LoginActivity";
    public static final String LOG_IN_SAVE_KEY = "ca.cmpt276.walkinggroupindigo.walkinggroup - LoginActivity Save Key";
    private WGServerProxy proxy;

    private User user = User.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getApiKey();
        checkIfUserIsLoggedIn();
        setUpLoginButton();
    }

    private void checkIfUserIsLoggedIn() {
        Context context = LoginActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        String userString = sharedPref.getString(LOG_IN_SAVE_KEY, "");
        if (!userString.equals("")) {
            String id = extractFromStrings(userString, "id=", ",");
            String email = extractFromStrings(userString, ", email='", "'");
            String password = extractFromStrings(userString, ", password='", "'");
            String name = extractFromStrings(userString, ", name='", "'");
            user.setId(Long.valueOf(id));
            user.setEmail(email);
            user.setPassword(password);
            user.setName(name);
            ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));
            Call<Void> caller = proxy.login(user);
            ProxyBuilder.callProxy(LoginActivity.this, caller, returnedNothing -> logInAlreadySaved(returnedNothing));
        }
    }

    private String extractFromStrings(String userString, String startString, String endString) {
        // Gets values from String from indexStart to indexEnd
        int indexStart = userString.indexOf(startString) + startString.length();
        int indexEnd = userString.indexOf(endString, indexStart + 1);
        return userString.substring(indexStart, indexEnd);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signUpButton:
                Intent intent = SignUpActivity.makeIntent(LoginActivity.this);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
    
    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        proxy = ProxyBuilder.getProxy(apiKey,null);

    }

    private void setUpLoginButton() {
        Button button = findViewById(R.id.login_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });
    }
    
    private void LoginUser() {
        String userEmail = getInputText(R.id.emailEdit);
        String userPass = getInputText(R.id.passEdit);
        if (userEmail.matches("")) {
            Toast.makeText(LoginActivity.this, R.string.email_empty_login, Toast.LENGTH_SHORT).show();
        } else if (userPass.matches("")) {
            Toast.makeText(LoginActivity.this, R.string.password_empty_login, Toast.LENGTH_SHORT).show();
        } else {
            user.setEmail(userEmail);
            user.setPassword(userPass);
            String userString = user.toString();
            ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));
            Call<Void> caller = proxy.login(user);
            ProxyBuilder.callProxy(LoginActivity.this, caller, returnedNothing -> logIn(returnedNothing, userEmail));
        }
    }

    private void logIn(Void returnedNothing, String userEmail) {
        Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        Call<User> caller = proxy.getUserByEmail(userEmail);
        ProxyBuilder.callProxy(LoginActivity.this, caller, returnedUser -> getUserInfo(returnedUser));
    }

    private void logInAlreadySaved(Void returnedNothing) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveLogIn(String userString) {
        Context context = LoginActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LOG_IN_SAVE_KEY, userString);
        editor.apply();
    }

    private String getInputText(int id){
        EditText text = findViewById(id);
        return text.getText().toString();
    }

    private void onReceiveToken(String token) {
        proxy = ProxyBuilder.getProxy(getString(R.string.apikey), token);
    }


    public void getUserInfo(User returnedUser) {
        user.setId(returnedUser.getId());
        user.setName(returnedUser.getName());
        saveLogIn(user.toString());
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}