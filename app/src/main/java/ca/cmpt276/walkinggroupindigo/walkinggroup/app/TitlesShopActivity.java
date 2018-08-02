package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.Helper;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class TitlesShopActivity extends AppCompatActivity {
    private static final String TAG = "TitlesShopActivity";
    private WGServerProxy proxy;
    private User user;
    private EarnedRewards userRewards = new EarnedRewards();
    private EarnedRewards themeRewards = new EarnedRewards();
    List<String> rewards = new ArrayList<>();
    List<Integer> prices = new ArrayList<>();

    public static Intent makeIntent(Context context) {
        return new Intent(context, TitlesShopActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        Helper.setCorrectTheme(TitlesShopActivity.this, user);
        setContentView(R.layout.activity_titles_shop);
        proxy = ProxyFunctions.setUpProxy(TitlesShopActivity.this, getString(R.string.apikey));
        setActionBarText("Rewards Shop");
        setUpToolbar();
        displayCurrentPoints();
        populateRewards();
        generateRewards();
    }

    private void setUpToolbar() {
        Button themesButton = findViewById(R.id.themesButton);
        Button titlesButton = findViewById(R.id.titlesButton);
        themesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TitlesShopActivity.this, ThemesShopActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        titlesButton.setClickable(false);
        titlesButton.setAlpha(1f);
    }

    private void displayCurrentPoints() {
        TextView currPoints = findViewById(R.id.current_points);
        currPoints.setText(String.format(getString(R.string.current_points), String.valueOf(user.getCurrentPoints())));
    }

    private void populateRewards() {
        ArrayAdapter<String> adapter = new TitlesShopActivity.getRewardAdapter(R.id.reward_list);
        ListView list = findViewById(R.id.reward_list);
        list.setAdapter(adapter);
//        registerOnClickCallback();
    }

//    private void registerOnClickCallback() {
//        ListView rewardList = findViewById(R.id.reward_list);
//        rewardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            }
//        });
//    }

    private void generateRewards() {
        rewards.addAll(Arrays.asList(getResources().getStringArray(R.array.glorious_titles)));
        int rewardArray[] = getResources().getIntArray(R.array.glorious_title_prices);
        for (int showPrice : rewardArray) {
            prices.add(showPrice);
        }
    }

    private class getRewardAdapter extends ArrayAdapter<String> {

        private getRewardAdapter(int item) {
            super(TitlesShopActivity.this, item, rewards);
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.activity_reward, parent, false);
            }

            EarnedRewards currentUserRewards = user.getRewards();

            TextView rewardName = convertView.findViewById(R.id.reward_name);
            String currentReward = rewards.get(position);
            rewardName.setText(getString(R.string.reward_types, "Title", currentReward));

            if (currentReward.equals(currentUserRewards.getTitle())) {
                TextView showPrice = convertView.findViewById(R.id.reward_price);
                showPrice.setVisibility(View.INVISIBLE);
                Button applyItem = convertView.findViewById(R.id.buy);
                applyItem.setClickable(false);
                applyItem.setText(R.string.applied);
            } else if (currentUserRewards.getListOfTitlesOwned().contains(currentReward)) {
                TextView showPrice = convertView.findViewById(R.id.reward_price);
                showPrice.setVisibility(View.INVISIBLE);
                Button applyItem = convertView.findViewById(R.id.buy);
                applyItem.setText(R.string.apply);
                applyItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        applyReward(currentReward, currentUserRewards);
                    }
                });
            } else {
                TextView showPrice = convertView.findViewById(R.id.reward_price);
                final Integer itemPrice = prices.get(position);
                showPrice.setText(getString(R.string.points, itemPrice));

                Button buyItem = convertView.findViewById(R.id.buy);
                buyItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        purchaseItem(currentReward, itemPrice, currentUserRewards);
                    }
                });
            }
            return convertView;
        }

        private void applyReward(String currentReward, EarnedRewards usersRewards) {
            usersRewards.setTitle(currentReward);
            user.setRewards(usersRewards);
            Call<User> userCall = proxy.editUser(user.getId(), user);
            ProxyBuilder.callProxy(TitlesShopActivity.this, userCall,
                    returnedUser -> {
                        populateRewards();
                    });
        }

        private void purchaseItem(String currentReward, Integer itemPrice, EarnedRewards usersRewards) {
            if (user.getCurrentPoints() < itemPrice) {
                Toast.makeText(TitlesShopActivity.this, R.string.not_enough_points, Toast.LENGTH_SHORT).show();
            } else {
                user.setCurrentPoints(user.getCurrentPoints() - itemPrice);
                usersRewards.addListOfTitlesOwned(currentReward);
                user.setRewards(usersRewards);
                Call<User> userCall = proxy.editUser(user.getId(), user);
                ProxyBuilder.callProxy(TitlesShopActivity.this, userCall,
                        returnedUser -> {
                            displayCurrentPoints();
                            populateRewards();
                        });
            }
        }
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






