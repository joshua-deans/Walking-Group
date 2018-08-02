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

public class AccountInfoActivity extends AppCompatActivity {

    User mUser;
    private WGServerProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = User.getInstance();
        Helper.setCorrectTheme(AccountInfoActivity.this, mUser);
        setContentView(R.layout.activity_account_info);
        setActionBarText("Account Information");
        proxy = ProxyFunctions.setUpProxy(AccountInfoActivity.this, getString(R.string.apikey));
        populateForm();
        setUpUpdateButton();
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
        ProxyBuilder.callProxy(AccountInfoActivity.this, updateCaller, returnedUser -> successFunction(returnedUser));
    }

    private void successFunction(User returnedUser) {
        Toast.makeText(AccountInfoActivity.this, "Account information updated",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateCurrentUser() {
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
        setTextFields(fullName, email, birthMonth, birthYear, address, homePhone, cellPhone,
                grade, teacher, emergencyContact);
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

    public static Intent makeIntent(Context context) {
        return new Intent(context, AccountInfoActivity.class);
    }
}
