package ca.cmpt276.walkinggroupindigo.walkinggroup.proxy;

import android.content.Context;
import android.content.SharedPreferences;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_TOKEN;

public class ProxyFunctions {
    public static WGServerProxy setUpProxy(Context currentContext, String apiString) {
        return getAPIKey(currentContext, apiString);
    }

    private static WGServerProxy getAPIKey(Context currContext, String apiKey) {
        String token = getToken(currContext);
        return ProxyBuilder.getProxy(apiKey, token);
    }

    private static String getToken(Context currContext) {
        SharedPreferences sharedPref = currContext.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        return sharedPref.getString(LOG_IN_SAVE_TOKEN, "");
    }
}
