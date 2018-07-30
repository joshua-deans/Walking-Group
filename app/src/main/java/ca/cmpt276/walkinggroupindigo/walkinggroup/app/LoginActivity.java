package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.UpdateMessages;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginScreen";
    public static final String LOG_IN_KEY = "ca.cmpt276.walkinggroupindigo.walkinggroup - LoginActivity";
    public static final String LOG_IN_SAVE_KEY = "ca.cmpt276.walkinggroupindigo.walkinggroup - LoginActivity Save Key";
    public static final String LOG_IN_SAVE_TOKEN = "ca.cmpt276.walkinggroupindigo.walkinggroup - LoginActivity Save Token";
    public static final String MESSAGE_JOB_ID = "ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity - Message Job ID";
    private WGServerProxy proxy;
    private User user = User.getInstance();
    AlarmManager alarmMgr;
    PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        proxy = ProxyFunctions.setUpProxy(LoginActivity.this, getString(R.string.apikey));
        setActionBarText(getString(R.string.login));
        cancelAlarm(LoginActivity.this);
        checkIfUserIsLoggedIn();
        setUpLoginButton();
    }

    private void cancelAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent updateServiceIntent = new Intent(context, UpdateMessages.class);
        PendingIntent alarmIntent = PendingIntent.getService(context, 0, updateServiceIntent, 0);

        if (alarmMgr != null) {
            try {
                alarmMgr.cancel(alarmIntent);
            } catch (Exception e) {
                Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelAlarm(LoginActivity.this);
    }

    private void checkIfUserIsLoggedIn() {
        Context context = LoginActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        String userString = sharedPref.getString(LOG_IN_SAVE_KEY, "");
        if (!userString.equals("")) {
            String email = extractFromStrings(userString, ", email='", "'");
            String password = extractFromStrings(userString, ", password='", "'");
            user.setEmail(email);
            user.setPassword(password);
            ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));
            Call<Void> caller = proxy.login(user);
            ProxyBuilder.callProxy(LoginActivity.this, caller, returnedNothing -> logIn(returnedNothing, email, false));
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
            ProxyBuilder.setOnTokenReceiveCallback(token -> onReceiveToken(token));
            Call<Void> caller = proxy.login(user);
            ProxyBuilder.callProxy(LoginActivity.this, caller, returnedNothing -> logIn(returnedNothing, userEmail, true));
        }
    }

    private void logIn(Void returnedNothing, String userEmail, boolean saveInfo) {
        Toast.makeText(LoginActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
        Call<User> caller = proxy.getUserByEmail(userEmail);
        ProxyBuilder.callProxy(LoginActivity.this, caller, returnedUser -> getUserInfo(returnedUser, saveInfo));
    }

    private void logInAlreadySaved(Void returnedNothing) {
        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
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
        saveToken(token);
    }

    private void saveToken(String token) {
        Context context = LoginActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LOG_IN_SAVE_TOKEN, token);
        editor.apply();
    }

    public void getUserInfo(User returnedUser, boolean saveInfo) {
        setUserParams(returnedUser);
        if (saveInfo) {
            saveLogIn(user.toString());
        }
        startMessageService(user.getId());
        Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startMessageService(Long id) {
        Calendar calendar = Calendar.getInstance();
        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(LoginActivity.this, UpdateMessages.class);
        alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        intent.putExtra(MESSAGE_JOB_ID, id);
        // Start tracking GPS
        assert alarmMgr != null;
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60, alarmIntent);
    }

    private void setUserParams(User returnedUser) {
        user.setId(returnedUser.getId());
        user.setHref("/users/" + returnedUser.getId());
        user.setName(returnedUser.getName());
        user.setLeadsGroups(returnedUser.getLeadsGroups());
        user.setMemberOfGroups(returnedUser.getMemberOfGroups());
        user.setMonitorsUsers(returnedUser.getMonitorsUsers());
        user.setMonitoredByUsers(returnedUser.getMonitoredByUsers());
        user.setBirthMonth(returnedUser.getBirthMonth());
        user.setBirthYear(returnedUser.getBirthYear());
        user.setEmergencyContactInfo(returnedUser.getEmergencyContactInfo());
        user.setTeacherName(returnedUser.getTeacherName());
        user.setCellPhone(returnedUser.getCellPhone());
        user.setHomePhone(returnedUser.getHomePhone());
        user.setAddress(returnedUser.getAddress());
        user.setGrade(returnedUser.getGrade());
        user.setCurrentPoints(returnedUser.getCurrentPoints());
        user.setTotalPointsEarned(returnedUser.getTotalPointsEarned());
        user.setRewards(returnedUser.getRewards());
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