package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups.GROUP_ID_EXTRA;

public class UserInfoActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    private Long mUserId;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(UserInfoActivity.this, mUser);
        setContentView(R.layout.activity_user_info);
        setActionBarText("");
        proxy = ProxyFunctions.setUpProxy(UserInfoActivity.this, getString(R.string.apikey));
        getUserId();
        if (mUserId == -1) {
            errorMessage();
        } else {
            getUserDetails(mUserId);
        }
    }

    private void getUserDetails(Long userId) {
        Call<User> userCaller = proxy.getUserById(userId);
        ProxyBuilder.callProxy(UserInfoActivity.this, userCaller, returnedUser -> extractUserData(returnedUser));
    }

    private void extractUserData(User returnedUser) {
        setActionBarText(returnedUser.getName());
        EditText fullName = findViewById(R.id.userInfoUserName);
        EditText email = findViewById(R.id.userInfoEmail);
        EditText birthMonth = findViewById(R.id.userInfoBirthMonth);
        EditText birthYear = findViewById(R.id.userInfoBirthYear);
        EditText address = findViewById(R.id.userInfoAddress);
        EditText homePhone = findViewById(R.id.userInfoHomePhone);
        EditText cellPhone = findViewById(R.id.userInfoCellPhone);
        EditText grade = findViewById(R.id.userInfoGrade);
        EditText teacher = findViewById(R.id.userInfoTeacher);
        EditText emergencyContact = findViewById(R.id.userInfoEmergencyContact);
        setTextFields(returnedUser, fullName, email, birthMonth, birthYear, address, homePhone, cellPhone,
                grade, teacher, emergencyContact);
    }

    private void setTextFields(User mUser, EditText fullName, EditText email, EditText birthMonth, EditText birthYear, EditText address, EditText homePhone, EditText cellPhone, EditText grade, EditText teacher, EditText emergencyContact) {
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

    private void getUserId() {
        mUserId = getIntent().getLongExtra(GROUP_ID_EXTRA, -1);
    }

    private void errorMessage() {
        Toast.makeText(UserInfoActivity.this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setActionBarText(String title) {
        try {
            getActionBar().setTitle(title);
            getSupportActionBar().setTitle(title);
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle(title);
        }
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, UserInfoActivity.class);
    }
}
