package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;


public class RewardShopActivity extends AppCompatActivity {
    private static final String TAG = "RewardShopActivity";
    private WGServerProxy proxy;
    private User user;
    private EarnedRewards userRewards = new EarnedRewards();
    List<String> rewards = new ArrayList<>();
    List<Integer> prices = new ArrayList<>();

    public static Intent makeIntent(Context context) {
        return new Intent(context, RewardShopActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards_shop);
        user = User.getInstance();
        proxy = ProxyFunctions.setUpProxy(RewardShopActivity.this, getString(R.string.apikey));
        populateRewards();
        generateRewards();
    }


    private void populateRewards() {
        ArrayAdapter<String> adapter = new RewardShopActivity.getRewardAdapter(R.id.reward_list);
        ListView list = findViewById(R.id.reward_list);
        list.setAdapter(adapter);
        registerOnClickCallback();
    }

    private void registerOnClickCallback() {
        ListView userList = findViewById(R.id.reward_list);
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

    private void generateRewards() {
        rewards.addAll(Arrays.asList(getResources().getStringArray(R.array.glorious_titles)));
        int rewardArray[] = getResources().getIntArray(R.array.glorious_title_prices);
        for (int showPrice : rewardArray) {
            prices.add(showPrice);
        }
    }


    private class getRewardAdapter extends ArrayAdapter<String> {

        private getRewardAdapter(int item) {
            super(RewardShopActivity.this, item, rewards);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.activity_reward, parent, false);
            }

            TextView makeText = convertView.findViewById(R.id.reward_name);
            makeText.setText(getString(R.string.reward_types, "Title", rewards.get(position)));

            TextView showPrice = convertView.findViewById(R.id.reward_price);
            showPrice.setText(getString(R.string.points, prices.get(position)));

            Button buyItem = convertView.findViewById(R.id.buy);
            buyItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //not implemented yet
                }
            });

            return convertView;
        }
    }
}






