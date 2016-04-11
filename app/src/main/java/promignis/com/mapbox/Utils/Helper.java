package promignis.com.mapbox.Utils;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by sahebjot on 4/11/16.
 */
public class Helper {
    public static LatLng decodeLocation(String location) {
        String[] markerLocation = location.split(":");
        return new LatLng(Double.parseDouble(markerLocation[0]), Double.parseDouble(markerLocation[1]));
    }
    public static String encodeLocation(LatLng location) {
        return location.getLatitude() + ":" + location.getLongitude();
    }
}
