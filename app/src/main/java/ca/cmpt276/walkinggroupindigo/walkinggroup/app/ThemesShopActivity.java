package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.annotation.SuppressLint;
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

public class ThemesShopActivity extends AppCompatActivity {
    private WGServerProxy proxy;
    private User user;
    List<String> themes = new ArrayList<>();
    List<Integer> prices = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = User.getInstance();
        Helper.setCorrectTheme(ThemesShopActivity.this, user);
        setContentView(R.layout.activity_themes_shop);
        proxy = ProxyFunctions.setUpProxy(ThemesShopActivity.this, getString(R.string.apikey));
        setActionBarText("Rewards Shop");
        setUpToolbar();
        displayCurrentPoints();
        populateThemes();
        generateThemes();
    }

    private void setUpToolbar() {
        Button themesButton = findViewById(R.id.themesButton);
        Button titlesButton = findViewById(R.id.titlesButton);
        titlesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThemesShopActivity.this, TitlesShopActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0); //0 for no animation
            }
        });
        themesButton.setClickable(false);
        themesButton.setAlpha(1f);
    }

    private void displayCurrentPoints() {
        TextView currPoints = findViewById(R.id.current_points);
        currPoints.setText(String.format(getString(R.string.current_points), String.valueOf(user.getCurrentPoints())));
    }

    private void populateThemes() {
        ArrayAdapter<String> adapter = new ThemesShopActivity.getThemeAdapter(R.id.themes_list);
        ListView list = findViewById(R.id.themes_list);
        list.setAdapter(adapter);
    }

    private void generateThemes() {
        themes.addAll(Arrays.asList(getResources().getStringArray(R.array.theme_names)));
        int rewardArray[] = getResources().getIntArray(R.array.theme_prices);
        for (int showPrice : rewardArray) {
            prices.add(showPrice);
        }
    }

    private class getThemeAdapter extends ArrayAdapter<String> {

        private getThemeAdapter(int item) {
            super(ThemesShopActivity.this, item, themes);
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.activity_reward, parent, false);
            }

            EarnedRewards currentUserRewards = user.getRewards();

            TextView themeName = convertView.findViewById(R.id.reward_name);
            String currentTheme = themes.get(position);
            themeName.setText(getString(R.string.reward_types, "Theme", currentTheme));

            if (currentTheme.equals(currentUserRewards.getSelectedTheme())) {
                TextView showPrice = convertView.findViewById(R.id.reward_price);
                showPrice.setVisibility(View.INVISIBLE);
                Button applyItem = convertView.findViewById(R.id.buy);
                applyItem.setClickable(false);
                applyItem.setText(R.string.applied);
            } else if (currentUserRewards.getListOfThemesOwned().contains(currentTheme)) {
                TextView showPrice = convertView.findViewById(R.id.reward_price);
                showPrice.setVisibility(View.INVISIBLE);
                Button applyItem = convertView.findViewById(R.id.buy);
                applyItem.setText(R.string.apply);
                applyItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        applyTheme(currentTheme, currentUserRewards);
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
                        purchaseItem(currentTheme, itemPrice, currentUserRewards);
                    }
                });
            }
            return convertView;
        }

        private void applyTheme(String currentTheme, EarnedRewards usersThemes) {
            usersThemes.setSelectedTheme(currentTheme);
            user.setRewards(usersThemes);
            Call<User> userCall = proxy.editUser(user.getId(), user);
            ProxyBuilder.callProxy(ThemesShopActivity.this, userCall,
                    returnedUser -> {
                        Intent intent = getIntent();
                        finish();
                        overridePendingTransition(0, 0); //0 for no animation
                        startActivity(intent);
                    });
        }

        private void purchaseItem(String currentTheme, Integer itemPrice, EarnedRewards usersThemes) {
            if (user.getCurrentPoints() < itemPrice) {
                Toast.makeText(ThemesShopActivity.this, R.string.not_enough_points, Toast.LENGTH_SHORT).show();
            } else {
                user.setCurrentPoints(user.getCurrentPoints() - itemPrice);
                usersThemes.addListOfThemesOwned(currentTheme);
                user.setRewards(usersThemes);
                Call<User> userCall = proxy.editUser(user.getId(), user);
                ProxyBuilder.callProxy(ThemesShopActivity.this, userCall,
                        returnedUser -> {
                            displayCurrentPoints();
                            populateThemes();
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
