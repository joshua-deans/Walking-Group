package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.List;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class ManageMonitoring extends AppCompatActivity {

    private WGServerProxy proxy;
    User user = new User();
    long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_monitoring);
        ListView monitringView = findViewById(R.id.monitoring_listview);

        populateMonitoringListView();
    }

    private void populateMonitoringListView() {
        Call<List<User>> users = proxy.getMonitorsUsers(userId);

//        ArrayAdapter<List<User>> adapter = new ArrayAdapter<>(this,R.layout.monitoring_layout,proxy.getMonitoredByUsers(userId));


        ListView monitoringList = findViewById(R.id.monitoring_listview);
        monitoringList.setAdapter(adapter);
    }


}
