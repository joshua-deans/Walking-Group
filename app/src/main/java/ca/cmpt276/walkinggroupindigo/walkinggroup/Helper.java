package ca.cmpt276.walkinggroupindigo.walkinggroup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.EarnedRewards;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_TOKEN;

public class Helper {
    public static void logUserOut(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        User user = User.getInstance();
        user = new User();
        editor.putString(LOG_IN_SAVE_KEY, "");
        editor.putString(LOG_IN_SAVE_TOKEN, "");
        editor.apply();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void setCorrectTheme(Context context, User user) {
        EarnedRewards rewards = user.getRewards();
        String selectedTheme = rewards.getSelectedTheme();
        switch (selectedTheme) {
            case "Default":
                return;
            case "Pink":
                context.setTheme(R.style.PinkTheme);
                break;
            case "Office Teal":
                context.setTheme(R.style.TealTheme);
                break;
            case "The Dark Knight":
                context.setTheme(R.style.DarkTheme);
                break;
            case "SFU Red":
                context.setTheme(R.style.RedTheme);
                break;
            case "Steel":
                context.setTheme(R.style.SteelTheme);
                break;
        }
    }
}
