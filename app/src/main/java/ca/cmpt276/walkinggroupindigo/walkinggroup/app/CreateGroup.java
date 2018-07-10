package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_KEY;
import static ca.cmpt276.walkinggroupindigo.walkinggroup.app.LoginActivity.LOG_IN_SAVE_TOKEN;

public class CreateGroup extends AppCompatActivity {

    private int START_REQUEST = 1;
    private int DEST_REQUEST = 2;
    private Group currentGroup;
    private LatLng startLatLng;
    private LatLng destLatLng;
    private WGServerProxy proxy;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        proxy = ProxyFunctions.setUpProxy(CreateGroup.this, getString(R.string.apikey));
        currentGroup = new Group();
        mUser = User.getInstance();
        setUpDestination();
        setUpStartingLocation();
        setUpOK();
    }

    private void getAPIKey() {
        String apiKey = getString(R.string.apikey);
        String token = getToken();
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    private void setUpStartingLocation(){
        Button button = findViewById(R.id.starting_location);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    try {
                        startActivityForResult(builder.build(CreateGroup.this), START_REQUEST);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    private void setUpDestination(){
        Button button = findViewById(R.id.destination);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(CreateGroup.this), DEST_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void setUpOK(){
        Button button = findViewById(R.id.ok_create_group);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText groupDescriptionView = findViewById(R.id.editGroupName);
                String groupDescription = groupDescriptionView.getText().toString();
                if (groupDescription.matches("")) {
                    Toast.makeText(CreateGroup.this, "Group name required", Toast.LENGTH_SHORT).show();
                } else if (startLatLng == null) {
                    Toast.makeText(CreateGroup.this, "No starting location selected", Toast.LENGTH_SHORT).show();
                } else if (destLatLng == null) {
                    Toast.makeText(CreateGroup.this, "No destination selected", Toast.LENGTH_SHORT).show();
                } else {
                    currentGroup.setGroupDescription(groupDescription);
                    currentGroup.setLeader(mUser);
                    currentGroup.setStartLatitude(startLatLng.latitude);
                    currentGroup.setStartLongitude(startLatLng.longitude);
                    currentGroup.setDestLatitude(destLatLng.latitude);
                    currentGroup.setDestLongitude(destLatLng.longitude);
                    Call<Group> caller = proxy.createGroup(currentGroup);
                    ProxyBuilder.callProxy(CreateGroup.this, caller, group -> onSuccess(group));
                }
            }
        });
    }

    private void onSuccess(Group groupList) {
        Toast.makeText(CreateGroup.this, "Group successfully added", Toast.LENGTH_SHORT).show();
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                Button button = findViewById(R.id.starting_location);
                button.setText(place.getName());
                startLatLng = place.getLatLng();
            }
        } else if (requestCode == DEST_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                Button button = findViewById(R.id.destination);
                button.setText(place.getName());
                destLatLng = place.getLatLng();
            }
        }
    }

    private String getToken() {
        Context context = CreateGroup.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                LOG_IN_KEY, Context.MODE_PRIVATE);
        String token = sharedPref.getString(LOG_IN_SAVE_TOKEN, null);
        return token;
    }
}

