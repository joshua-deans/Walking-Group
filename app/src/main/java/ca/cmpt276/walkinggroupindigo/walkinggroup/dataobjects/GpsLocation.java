package ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Store information about a GPS location of a user.
 *
 * WARNING: INCOMPLETE! Server returns more information than this.
 * This is just to be a placeholder and inspire you how to do it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GpsLocation {
    private Double lng;
    private Double lat;
    private String timestamp;

    public GpsLocation() {
        lng = null;
        lat = null;
        timestamp = null;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setCurrentTimestamp() {
        Date currDate = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("YYYY-MM-dd'T'hh:mm:ss");
        ft.setTimeZone(TimeZone.getTimeZone("PST"));
        this.timestamp = ft.format(currDate);
    }
}
