package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.HttpURLConnection;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;

/*
 * Create a user based on the provided information.
 * Name, Email, password, birth date, address, cellphone
 * home phone, grade, teacher name, and emergency contact information
 */

public class SignUpActivity extends AppCompatActivity {
    private WGServerProxy builder;
    private User user;

    public static Intent makeIntent(Context context) {
        return new Intent(context, SignUpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        setupSignUpButton();
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
                insertIntoServer();
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
        if(name == null || email == null){
            // if possible we will Dialog(AlertDialog) to prevent user to go to next activity
        }
        if(password.equalsIgnoreCase(confirm_password)){
            // if possible we will Dialog(AlertDialog) to prevent user to go to next activity
        }
        user = new User(name, email, password);
    }

    // Insert the successfully created user into the server
    private void insertIntoServer() {
        HttpURLConnection connection;

        builder = ProxyBuilder.getProxy(getString(R.string.apikey), null);
    }
}