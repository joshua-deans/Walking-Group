package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private String apiKey = null;

//    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        getApiKey();
//        user = User.getInstance();

        setUpLoginButton();
        setUpSignUpButton();
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        proxy = ProxyBuilder.getProxy(apiKey,null);
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

    private void setUpSignUpButton() {
        Button signUpButton = findViewById(R.id.signup_btn);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SignUpActivity.makeIntent(LoginActivity.this);
                startActivity(intent);

            }
        });
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