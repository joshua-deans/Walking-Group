package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class LeaderboardActivity extends AppCompatActivity {
    User mUser;
    private WGServerProxy proxy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        mUser = User.getInstance();
        proxy = ProxyFunctions.setUpProxy(LeaderboardActivity.this, getString(R.string.apikey));
        updateUserScore();
        getUsersList();
    }

    private void getUsersList() {
        Call<List<User>> usersListCaller = proxy.getUsers();
        ProxyBuilder.callProxy(LeaderboardActivity.this, usersListCaller, userList -> modFunc(userList));
    }

    private void modFunc(List<User> userList) {
        Collections.sort(userList, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                if (o1.getTotalPointsEarned() < o2.getTotalPointsEarned()) {
                    return -1;
                } else if (o1.getTotalPointsEarned() > o2.getTotalPointsEarned()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        populateListView(userList);
    }

    private void populateListView(List<User> userList) {
        ListView leaderboardList = findViewById(R.id.leaderboard_list);
    }

    private void updateUserScore() {
        TextView scoreString = findViewById(R.id.your_score);
        scoreString.setText(String.format(getString(R.string.total_score_display), mUser.getTotalPointsEarned()));
    }
}
