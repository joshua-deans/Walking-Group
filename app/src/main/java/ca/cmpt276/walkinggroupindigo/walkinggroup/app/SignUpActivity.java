package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
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
        setActionBarText(getString(R.string.sign_up));
        proxy = ProxyFunctions.setUpProxy(SignUpActivity.this, getString(R.string.apikey));
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
                String birthMonth = getTextInformation(R.id.birthday_month_sign_up);
                String birthYear = getTextInformation(R.id.birthday_year_sign_up);
                String address = getTextInformation(R.id.address);
                String homePhone = getTextInformation(R.id.homePhone);
                String cellPhone = getTextInformation(R.id.cellPhone);
                String grade = getTextInformation(R.id.currentGrade);
                String teacher = getTextInformation(R.id.teacher);
                String emergencyContactInfo = getTextInformation(R.id.emergencyContactInfo);
                CreateUser(name, email, password, confirm_password, birthMonth, birthYear, address,
                        homePhone, cellPhone, grade, teacher, emergencyContactInfo);
            }
        });
    }

    // Based on the provided view_id, return the filled information
    private String getTextInformation(int view_id) {
        EditText text = findViewById(view_id);
        return text.getText().toString();
    }

    // Create a user based on the information provided
    private void CreateUser(String name, String email, String password, String confirm_password, String birthMonth, String birthYear,
                            String address, String homePhone, String cellPhone, String grade, String teacher, String emergencyContactInfo) {
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
            user.setBirthMonth(birthMonth);
            user.setBirthYear(birthYear);
            user.setAddress(address);
            user.setHomePhone(homePhone);
            user.setCellPhone(cellPhone);
            user.setGrade(grade);
            user.setTeacherName(teacher);
            user.setEmergencyContactInfo(emergencyContactInfo);
            user.setTotalPointsEarned(0);
            user.setCurrentPoints(0);
            user.setRewards(new EarnedRewards());
            insertIntoServer(user);
        }
    }

    // Insert the successfully created user into the server
    private void insertIntoServer(User user) {
        Call<User> caller = proxy.createUser(user);
        ProxyBuilder.callProxy(SignUpActivity.this, caller, returnedUser -> successfulSignUp(returnedUser),
                responseBody -> handleUserCreateError(responseBody));
    }

    private void handleUserCreateError(retrofit2.Response response) {
        try {
            String responseBody = response.errorBody().string();
            JSONObject json = new JSONObject(responseBody);
            Toast.makeText(SignUpActivity.this,
                    json.get("message").toString(),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException | JSONException e) {
            Toast.makeText(SignUpActivity.this,
                    "Unable to create a user",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void successfulSignUp(User returnedUser) {
        Toast.makeText(SignUpActivity.this, R.string.success_sign_up, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void setActionBarText(String title) {
        try {
            getActionBar().setTitle(title);
            getSupportActionBar().setTitle(title);
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle(title);
        }
    }
}