package ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends IdItemBase{
    private Boolean hasMessages;
    private String groupDescription;
    private String locationName;
    // The latitude of the starting location
    private double[] routeLatArray;
    // The latitude of the starting location
    private double[] routeLngArray;
    private User leader;
    private Set<User> memberUsers = new HashSet<>();
    private String customJson;

    public Group() {
        routeLatArray = new double[2];
        routeLngArray = new double[2];
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getStartLatitude() {
        return routeLatArray[0];
    }

    public void setStartLatitude(double startLat) {
        routeLatArray[0] = startLat;
    }

    public double getDestLatitude() {
        return routeLatArray[1];
    }

    public void setDestLatitude(double destLat) {
        routeLatArray[1] = destLat;
    }

    public double getStartLongitude() {
        return routeLngArray[0];
    }

    public void setStartLongitude(double startLong) {
        routeLngArray[0] = startLong;
    }

    public double getDestLongitude() {
        return routeLngArray[1];
    }

    public void setDestLongitude(double destLong) {
        routeLngArray[1] = destLong;
    }


    public double[] getRouteLatArray() {
        return routeLatArray;
    }

    public void setRouteLatArray(double[] routeLatArray) {
        this.routeLatArray = routeLatArray;
    }

    public double[] getRouteLngArray() {
        return routeLngArray;
    }

    public void setRouteLngArray(double[] routeLngArray) {
        this.routeLngArray = routeLngArray;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public String getCustomJson() {
        return customJson;
    }

    public void setCustomJson(String customJson) {
        this.customJson = customJson;
    }

    public Boolean itHasMessages() {
        return hasMessages;
    }

    public void setHasMessages(Boolean hasMessages) {
        this.hasMessages = hasMessages;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupDescription='" + groupDescription + '\'' +
                ", routeLatArray=" + Arrays.toString(routeLatArray) +
                ", routeLngArray=" + Arrays.toString(routeLngArray) +
                ", leader=" + leader +
                ", memberUsers=" + memberUsers +
                ", customJson='" + customJson + '\'' +
                ", id=" + id +
                ", hasFullData=" + hasFullData +
                ", href='" + href + '\'' +
                '}';
    }
}