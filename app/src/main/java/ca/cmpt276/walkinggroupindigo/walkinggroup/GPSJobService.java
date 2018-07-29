package ca.cmpt276.walkinggroupindigo.walkinggroup;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.GpsLocation;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups.GPS_DEST_LAT;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups.GPS_DEST_LONG;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups.GPS_JOB_ID;

// Used stack overflow help to create this service
// https://stackoverflow.com/questions/8828639/get-gps-location-via-a-service-in-android

public class GPSJobService extends Service {
    private static final String TAG = "GPSJobService";
    // Interval is approximately every 30 seconds
    private static final int LOCATION_INTERVAL = 30000;
    private static final float LOCATION_DISTANCE = 10f;
    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };
    private WGServerProxy proxy;
    private LocationManager mLocationManager = null;
    private Long currUserId;
    private double destLat;
    private double destLng;
    private boolean atDestination = false;
    private Intent requestIntent;
    private User mUser;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        currUserId = intent.getLongExtra(GPS_JOB_ID, 0);
        destLat = intent.getDoubleExtra(GPS_DEST_LAT, 0);
        destLng = intent.getDoubleExtra(GPS_DEST_LONG, 0);
        requestIntent = intent;
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        proxy = ProxyFunctions.setUpProxy(GPSJobService.this, getString(R.string.apikey));
        mUser = User.getInstance();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    private void getUserInfo() {
        Call<User> userCaller = proxy.getUserById(currUserId);
        ProxyBuilder.callProxy(userCaller, returnedUser -> updateUserScores(returnedUser));
    }

    private void updateUserScores(User returnedUser) {
        // So far, we are just adding one point for each successful walk
        returnedUser.setCurrentPoints(returnedUser.getCurrentPoints() + 1);
        returnedUser.setTotalPointsEarned(returnedUser.getTotalPointsEarned() + 1);
        Call<User> userCaller = proxy.editUser(currUserId, returnedUser);
        ProxyBuilder.callProxy(userCaller, updatedUser -> {
            Log.i(TAG, "Users score is updated!");
        });
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listeners, ignore", ex);
                }
            }
        }
        mUser.setCurrentWalkingGroup(null);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void successfulLocationUpdate(GpsLocation gpsLocation) {
    }

    private void lastTenMinutes(Intent requestIntent) {
        // Schedules the current service to stop after ten minutes
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                stopService(requestIntent);
            }
        };
        // Scheduled for 10 minutes from now
        timer.schedule(timerTask, 600000);
    }

    private void updateUserScore(Long currUserId) {

    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Location destLoc = new Location("");
            destLoc.setLatitude(destLat);
            destLoc.setLongitude(destLng);
            mLastLocation.set(location);
            // Shows distance within 75 meters
            if (!atDestination && location.distanceTo(destLoc) <= 75f) {
                atDestination = true;
                getUserInfo();
                lastTenMinutes(requestIntent);
            }
            GpsLocation currGPS = new GpsLocation();
            currGPS.setCurrentTimestamp();
            currGPS.setLat(location.getLatitude());
            currGPS.setLng(location.getLongitude());
            Call<GpsLocation> gpsLocationCall = proxy.setLastGpsLocation(currUserId, currGPS);
            ProxyBuilder.callProxy(gpsLocationCall, gpsLocation -> successfulLocationUpdate(gpsLocation));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
}