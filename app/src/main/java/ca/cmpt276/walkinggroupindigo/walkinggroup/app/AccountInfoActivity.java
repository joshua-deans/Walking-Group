package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;

public class AccountInfoActivity extends AppCompatActivity {

    User mUser;
    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        mUser = User.getInstance();
        getApiKey();
        populateForm();
    }

    private void populateForm() {
        // Add data to the forms so that the user can see.
        EditText fullName = findViewById(R.id.accountInfoUserName);
        EditText email = findViewById(R.id.accountInfoEmail);
        EditText birthMonth = findViewById(R.id.accountInfoBirthMonth);
        EditText birthYear = findViewById(R.id.accountInfoBirthYear);
        EditText address = findViewById(R.id.accountInfoAddress);
        EditText homePhone = findViewById(R.id.accountInfoHomePhone);
        EditText cellPhone = findViewById(R.id.accountInfoCellPhone);
        EditText grade = findViewById(R.id.accountInfoGrade);
        EditText teacher = findViewById(R.id.accountInfoTeacher);
        EditText emergencyContact = findViewById(R.id.accountInfoEmergencyContact);
        fullName.setText(mUser.getName());
        email.setText(mUser.getEmail());
        birthMonth.setText(mUser.getBirthMonth());
        birthYear.setText(mUser.getBirthYear());
        address.setText(mUser.getAddress());
        homePhone.setText(mUser.getHomePhone());
        cellPhone.setText(mUser.getCellPhone());
        grade.setText(mUser.getGrade());
        teacher.setText(mUser.getTeacherName());
        emergencyContact.setText(mUser.getEmergencyContactInfo());
    }

    private void getApiKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    public String getToken() {
        Context context = AccountInfoActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LoginActivity.LOG_IN_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(LoginActivity.LOG_IN_SAVE_TOKEN, "");
        return token;
    }
}
