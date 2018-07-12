package ca.cmpt276.walkinggroupindigo.walkinggroup;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.GpsLocation;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.ManageGroups.GPS_JOB_ID;

// Class made using various online tutorials on JobSchedulers
public class GPSJobService extends JobService {
    private static final String TAG = GPSJobService.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    boolean isWorking = false;
    boolean jobCancelled = false;
    private WGServerProxy proxy;

    // Called by the Android system when it's time to run the job
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Toast.makeText(getApplicationContext(),
                "JobService task running", Toast.LENGTH_LONG)
                .show();
        isWorking = true;
        proxy = ProxyFunctions.setUpProxy(GPSJobService.this, getString(R.string.apikey));
        startWorkOnNewThread(jobParameters);

        return isWorking;
    }

    private void startWorkOnNewThread(final JobParameters jobParameters) {
        new Thread(new Runnable() {
            public void run() {
                sendGPS(jobParameters);
            }
        }).start();
    }

    private void sendGPS(JobParameters jobParameters) {
        PersistableBundle b = jobParameters.getExtras();
        Long userID = b.getLong(GPS_JOB_ID);

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            jobCancelled = true;
        }
        if (!jobCancelled) {
            Task<Location> currentLocation = mFusedLocationClient.getLastLocation();

            currentLocation.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        Location mLastKnownLocation = task.getResult();
                        try {
                            GpsLocation currGPS = new GpsLocation();
                            currGPS.setLat(mLastKnownLocation.getLatitude());
                            currGPS.setLng(mLastKnownLocation.getLongitude());
                            currGPS.setCurrentTimestamp();

                            Call<GpsLocation> gpsCaller = proxy.setLastGpsLocation(userID, currGPS);
                            ProxyBuilder.callProxy(gpsCaller, gps -> successfulUpdate(gps, jobParameters));
                        } catch (NullPointerException e) {
                            Toast.makeText(getApplicationContext(), "Exception", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Task not successfull", Toast.LENGTH_LONG).show();
                        isWorking = false;
                        boolean needsReschedule = false;
                        jobFinished(jobParameters, needsReschedule);
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Task not successfull", Toast.LENGTH_LONG).show();
            isWorking = false;
            boolean needsReschedule = false;
            jobFinished(jobParameters, needsReschedule);
        }
    }

    private void successfulUpdate(GpsLocation gps, JobParameters jobParameters) {
        Toast.makeText(getApplicationContext(), "GPS location updated", Toast.LENGTH_LONG).show();
        isWorking = false;
        boolean needsReschedule = false;
        jobFinished(jobParameters, needsReschedule);
    }

    // Called if the job was cancelled before being finished
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job cancelled before being completed.");
        jobCancelled = true;
        boolean needsReschedule = isWorking;
        jobFinished(jobParameters, needsReschedule);
        return needsReschedule;
    }
}