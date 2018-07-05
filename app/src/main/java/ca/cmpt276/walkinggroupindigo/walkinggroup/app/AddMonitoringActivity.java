package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class AddMonitoringActivity extends AppCompatActivity {

    private WGServerProxy proxy;
    User user = new User();
    Long userId = user.getId();

    public static Intent makeIntent (Context context){
        return new Intent (context, AddMonitoringActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_monitoring);
        setUpMonitorButton();
    }

    private void setUpMonitorButton() {
        EditText findUserEditText = findViewById(R.id.find_user_edit_txt);
        String address;
        address = findUserEditText.getText().toString();
        if (E-mail) {
            Call<List<User>> user = proxy.getUserByEmail(address);
        } else if (userId) {
            call<list<User>> user = proxy.getUserById(userId);
        }


        Button monitorButton = findViewById(R.id.add_user_button);
        monitorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<List<User>> caller = proxy.addToMonitorsUsers(userId, user);
            }
        });

    }
}
