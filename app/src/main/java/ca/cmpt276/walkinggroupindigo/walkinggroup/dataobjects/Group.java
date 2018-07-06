package ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends IdItemBase{
    private String groupDescription;
    private String locationName;
    // The latitude of the starting location
    private double startLatitude;
    // The latitude of the starting location
    private double startLongitude;
    // The latitude of the final location
    private double destLatitude;
    // The latitude of the final location
    private double destLongitude;
    private User leader;

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public double getDestLatitude() {
        return destLatitude;
    }

    public void setDestLatitude(double latitude) {
        this.destLatitude = latitude;
    }

    public double getDestLongitude() {
        return destLongitude;
    }

    public void setDestLongitude(double longitude) {
        this.destLongitude = longitude;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(double startLongitude) {
        this.startLongitude = startLongitude;
    }
}