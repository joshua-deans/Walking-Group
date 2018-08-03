package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
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
        mUser = User.getInstance();
        Helper.setCorrectTheme(LeaderboardActivity.this, mUser);
        setContentView(R.layout.activity_leaderboard);
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
                    return 1;
                } else if (o1.getTotalPointsEarned() > o2.getTotalPointsEarned()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        populateListView(userList);
    }

    private void populateListView(List<User> userList) {
        ListView leaderboardList = findViewById(R.id.leaderboard_list);
        ArrayAdapter<User> adapter = new MyLeaderboardList(userList);
        leaderboardList.setAdapter(adapter);
        new ArrayAdapter<>(this,
                R.layout.leaderboard_layout);
    }

    private void updateUserScore() {
        TextView scoreString = findViewById(R.id.your_score);
        scoreString.setText(String.format(getString(R.string.total_score_display), mUser.getTotalPointsEarned()));
    }

    private class MyLeaderboardList extends ArrayAdapter<User> {
        List<User> mUserList;

        MyLeaderboardList(List<User> userList) {
            super(LeaderboardActivity.this, R.layout.leaderboard_layout
                    , userList);
            mUserList = userList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;

            if (convertView == null) {
                itemView = getLayoutInflater().inflate(R.layout.leaderboard_layout,
                        parent,
                        false);
            }

            manageUserView(position, itemView);

            return itemView;
        }

        private void manageUserView(int position, View itemView) {
            User currentUser;
            if (mUserList.isEmpty()) {
                currentUser = new User();
            } else {
                currentUser = mUserList.get(position);
                itemView.setTag(currentUser.getId());
            }
            if (currentUser.getTotalPointsEarned() != null) {
                try {
                    TextView nameText = itemView.findViewById(R.id.userNameLeaderboard);
                    String[] splitString = currentUser.getName().split(" ");
                    if (splitString.length > 1) {
                        nameText.setText(String.format("%s %s", splitString[0], splitString[splitString.length - 1].charAt(0)));
                    } else {
                        nameText.setText(splitString[0]);
                    }
                    TextView scoreText = itemView.findViewById(R.id.scoreLeaderboard);
                    scoreText.setText(String.format("Score: %s", String.valueOf(currentUser.getTotalPointsEarned())));
                } catch (NullPointerException e) {
                    Log.e("Error", e + ":" + mUserList.toString());
                }
            }
        }
    }
}
