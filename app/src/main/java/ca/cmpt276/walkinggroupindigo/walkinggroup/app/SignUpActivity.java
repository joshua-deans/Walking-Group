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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

/*
 * Create a user based on the provided information.
 * Name, Email, password, birth date, address, cellphone
 * home phone, grade, teacher name, and emergency contact information
 */

public class SignUpActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private User user;

    public static Intent makeIntent(Context context) {
        return new Intent(context, SignUpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getApiKey();
        setupSignUpButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logInButton:
                finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    // When clicked, a new user will create
    private void setupSignUpButton() {
        Button button = findViewById(R.id.signup_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getTextInformation(R.id.user_name);
                String email = getTextInformation(R.id.email_information);
                String password = getTextInformation(R.id.create_password);
                String confirm_password = getTextInformation(R.id.confirm_password);
                CreateUser(name, email, password, confirm_password);
            }
        });
    }

    // Based on the provided view_id, return the filled information
    private String getTextInformation(int view_id) {
        EditText text = findViewById(view_id);
        return text.getText().toString();
    }

    // Create a user based on the information provided
    private void CreateUser(String name, String email, String password, String confirm_password) {
        if (name == null || name.matches("")) {
            Toast.makeText(SignUpActivity.this, R.string.please_enter_name, Toast.LENGTH_SHORT).show();
        } else if (email == null || email.matches("")) {
            Toast.makeText(SignUpActivity.this, R.string.please_enter_email, Toast.LENGTH_SHORT).show();
        } else if (password.length() == 0) {
            Toast.makeText(SignUpActivity.this, R.string.password_empty, Toast.LENGTH_SHORT).show();
        } else if (!(password.equals(confirm_password))) {
            Toast.makeText(SignUpActivity.this, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show();
        } else {
            user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            insertIntoServer(user);
        }
    }

    // Insert the successfully created user into the server
    private void insertIntoServer(User user) {

        Call<List<User>> usersCaller = proxy.getUsers();
        List<User> existingUsers = new ArrayList<>();

        ProxyBuilder.callProxy(SignUpActivity.this, usersCaller, returnedUsers -> {
            existingUsers.addAll(returnedUsers);
        });

        if(isDuplicated(existingUsers, user)) {
            Toast.makeText(SignUpActivity.this,
                    R.string.duplicated_email,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Call<User> caller = proxy.createUser(user);
        ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedUser -> successfulSignUp(returnedUser));
            // Need to go to Account monitor activity
//        }

    }

    private boolean isDuplicated(List<User> users, User user) {
        for (User aUser : users) {
            if (aUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                return true;
            }
        }
        return false;
    }

    private void successfulSignUp(User returnedUser) {
        if (returnedUser != null) {
            Toast.makeText(SignUpActivity.this,
                    R.string.success_sign_up,
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(SignUpActivity.this,
                    R.string.no_success_sign_up,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public String getToken() {
        Context context = SignUpActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LoginActivity.LOG_IN_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(LoginActivity.LOG_IN_SAVE_TOKEN, "");
        return token;
    }
}