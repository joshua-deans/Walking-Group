package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.type.ArrayType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.ManagerFactoryParameters;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ManageMonitoring extends AppCompatActivity {

    public static final int MASSAGE_CODE = 100;

    private WGServerProxy proxy;
    User user = new User();
    long userId = user.getId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_monitoring);
        setUpAddMonitoringButton();
        getMonitoringUsers();
        getMonitoredUsers();

    }

    private void setUpAddMonitoringButton() {
        Button addMonitoring = findViewById(R.id.add_monitoring_btn);
        addMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AddMonitoringActivity.makeIntent(ManageMonitoring.this);
                startActivityForResult(intent, MASSAGE_CODE);
            }
        });
    }


    private void getMonitoringUsers() {
        Call<List<User>> caller = proxy.getMonitorsUsers(userId);
        // How to convert our call list to arrayList
        ArrayList<User> allUser = (ArrayList<User>) caller;
        ProxyBuilder.callProxy(ManageMonitoring.this, caller, returnedCaller -> generateMonitoringUsers(allUser));

        ListView monitoringView = findViewById(R.id.monitoring_listview);
        monitoringView.setAdapter();

    }

    private void generateMonitoringUsers(ArrayList<User> allUser) {
        if(allUser.size() == 0) {
            // handle the issue
            TextView emptyText1 = findViewById(R.id.empty_text1);
            emptyText1.setText("You are not monitoring anyone");
        }

        for(User u:allUser) {
            // u have to insert into ur list
            populateMonitoringListView();
        }
    }

    private void populateMonitoringListView() {
        ArrayAdapter<ArrayList<User>> adapter = new ArrayAdapter<>(this, R.layout.monitoring_layout);
        ListView monitoringList = findViewById(R.id.monitoring_listview);
        monitoringList.setAdapter(adapter);
    }


    private void getMonitoredUsers() {
        Call<List<User>> caller = proxy.getMonitoredByUsers(userId);
        ArrayList<User>allUser = (ArrayList<User>) caller;
        ProxyBuilder.callProxy(ManageMonitoring.this, caller, returnedCaller -> generateMonitoredUsers(allUser));
    }

    private void generateMonitoredUsers(ArrayList<User> allUser) {
        if(allUser.size() == 0) {
            TextView emptyText2 = findViewById(R.id.empty_text2);
            emptyText2.setText("No one is monitoring you");
        }

        for(User u:allUser) {
            PopulateMonitoredListView();
        }
    }

    private void PopulateMonitoredListView() {
        ArrayAdapter<ArrayList<User>> adapter = new ArrayAdapter<>(this, R.layout.monitored_layout);
        ListView monitoredList = findViewById(R.id.monitored_listview);
        monitoredList.setAdapter(adapter);
    }


}
