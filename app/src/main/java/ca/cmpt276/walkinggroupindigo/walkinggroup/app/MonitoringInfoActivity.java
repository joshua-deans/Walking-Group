package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.MonitoringUsersActivity.MONITORED_ID;

public class MonitoringInfoActivity extends AppCompatActivity {

    User mUser;
    private WGServerProxy proxy;

    public static Intent makeIntent(Context context) {
        return new Intent(context, AccountInfoActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(MonitoringInfoActivity.this, mUser);
        setContentView(R.layout.activity_monitoring_info);
        setActionBarText("Account Information");
        proxy = ProxyFunctions.setUpProxy(MonitoringInfoActivity.this, getString(R.string.apikey));
        findUserInfo();
    }

    private void findUserInfo() {
        Long userID = getIntent().getLongExtra(MONITORED_ID, 0);
        Call<User> userCaller = proxy.getUserById(userID);
        ProxyBuilder.callProxy(MonitoringInfoActivity.this, userCaller, returnedUser -> populateForm(returnedUser));
    }

    private void setUpUpdateButton() {
        Button updateInfo = findViewById(R.id.update_info_btn);
        updateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCurrentUser();
                updateServerInformation();
            }
        });
    }

    private void updateServerInformation() {
        Call<User> updateCaller = proxy.editUser(mUser.getId(), mUser);
        ProxyBuilder.callProxy(MonitoringInfoActivity.this, updateCaller, returnedUser -> successFunction(returnedUser));
    }

    private void successFunction(User returnedUser) {
        Toast.makeText(MonitoringInfoActivity.this, "Account information updated",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateCurrentUser() {
        EditText fullName = findViewById(R.id.monitorInfoUserName);
        EditText email = findViewById(R.id.monitorInfoEmail);
        EditText birthMonth = findViewById(R.id.monitorInfoBirthMonth);
        EditText birthYear = findViewById(R.id.monitorInfoBirthYear);
        EditText address = findViewById(R.id.monitorInfoAddress);
        EditText homePhone = findViewById(R.id.monitorInfoHomePhone);
        EditText cellPhone = findViewById(R.id.monitorInfoCellPhone);
        EditText grade = findViewById(R.id.monitorInfoGrade);
        EditText teacher = findViewById(R.id.monitorInfoTeacher);
        EditText emergencyContact = findViewById(R.id.monitorInfoEmergencyContact);
        updateUserParams(fullName, email, birthMonth, birthYear, address, homePhone, cellPhone,
                grade, teacher, emergencyContact);
    }

    private void updateUserParams(EditText fullName, EditText email, EditText birthMonth,
                                  EditText birthYear, EditText address, EditText homePhone,
                                  EditText cellPhone, EditText grade, EditText teacher, EditText emergencyContact) {

        mUser.setName(fullName.getText().toString());
        mUser.setEmail(email.getText().toString());
        mUser.setBirthMonth(birthMonth.getText().toString());
        mUser.setBirthYear(birthYear.getText().toString());
        mUser.setAddress(address.getText().toString());
        mUser.setHomePhone(homePhone.getText().toString());
        mUser.setCellPhone(cellPhone.getText().toString());
        mUser.setGrade(grade.getText().toString());
        mUser.setTeacherName(teacher.getText().toString());
        mUser.setEmergencyContactInfo(emergencyContact.getText().toString());
    }

    private void populateForm(User returnedUser) {
        mUser = returnedUser;
        EditText fullName = findViewById(R.id.monitorInfoUserName);
        EditText email = findViewById(R.id.monitorInfoEmail);
        EditText birthMonth = findViewById(R.id.monitorInfoBirthMonth);
        EditText birthYear = findViewById(R.id.monitorInfoBirthYear);
        EditText address = findViewById(R.id.monitorInfoAddress);
        EditText homePhone = findViewById(R.id.monitorInfoHomePhone);
        EditText cellPhone = findViewById(R.id.monitorInfoCellPhone);
        EditText grade = findViewById(R.id.monitorInfoGrade);
        EditText teacher = findViewById(R.id.monitorInfoTeacher);
        EditText emergencyContact = findViewById(R.id.monitorInfoEmergencyContact);
        setTextFields(fullName, email, birthMonth, birthYear, address, homePhone, cellPhone,
                grade, teacher, emergencyContact);
        setUpUpdateButton();
    }

    private void setTextFields(EditText fullName, EditText email, EditText birthMonth, EditText birthYear, EditText address, EditText homePhone, EditText cellPhone, EditText grade, EditText teacher, EditText emergencyContact) {
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

    private void setActionBarText(String title) {
        try {
            getActionBar().setTitle(title);
            getSupportActionBar().setTitle(title);
        } catch (NullPointerException e) {
            getSupportActionBar().setTitle(title);
        }
    }
}
