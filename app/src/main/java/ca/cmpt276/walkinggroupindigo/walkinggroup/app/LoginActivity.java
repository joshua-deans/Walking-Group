package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginScreen";
    private WGServerProxy proxy;

//    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        String apikey = null;
        proxy = ProxyBuilder.getProxy(getString(R.string.apikey),null);
//        user = User.getInstance();

        setUpLoginButton();
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
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void setUpLoginButton() {
        User user = new User();
        EditText emailEditTxt = findViewById(R.id.emailEdit);
        String userEamil;
        userEamil = emailEditTxt.getText().toString();
        EditText passEditTxt = findViewById(R.id.passEdit);
        String userPass;
        userPass = passEditTxt.getText().toString();
        user.setEmail(userEamil);
        user.setPassword(userPass);

        ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));
        Call<Void> caller = proxy.login(user);
        ProxyBuilder.callProxy(LoginActivity.this, caller, returnedUser -> response(returnedUser));
    }

    private void notifyUserViaLogAndToast(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    private void response(Void returnedNothing) {
        notifyUserViaLogAndToast("Server replied to login request (no content was expected).");
    }

    private void response(List<User> returnedUsers) {
        notifyUserViaLogAndToast("Got list of " + returnedUsers.size() + " users! See logcat.");
        Log.w(TAG, "All Users:");
        for (User user : returnedUsers) {
            Log.w(TAG, "    User: " + user.toString());
        }
    }
    private void onReceiveToken(String token) {
        // Replace the current proxy with one that uses the token!
        Log.w(TAG, "   --> NOW HAVE TOKEN: " + token);
        proxy = ProxyBuilder.getProxy(getString(R.string.apikey), token);
    }
}