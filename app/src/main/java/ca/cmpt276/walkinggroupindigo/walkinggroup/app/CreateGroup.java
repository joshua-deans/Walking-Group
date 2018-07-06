package ca.cmpt276.walkinggroupindigo.walkinggroup.app;

import android.content.Intent;
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

public class CreateGroup extends AppCompatActivity {

    int START_REQUEST = 1;
    int DEST_REQUEST = 2;
    Group currentGroup;
    LatLng startLatLng;
    LatLng destLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        currentGroup = new Group();
        setUpDestination();
        setUpStartingLocation();
        setUpOK();
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
                    // TODO: add information to server
                    finish();
                }
            }
        });
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
}

