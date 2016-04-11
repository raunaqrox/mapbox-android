package promignis.com.mapbox.Models;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import promignis.com.mapbox.Utils.Helper;

/**
 * Created by sahebjot on 4/11/16.
 */
public class Person {

    private String id;
    private LatLng location;

    public String getId() {
        return id;
    }

    public LatLng getLocation() {
        return location;
    }

    public Person(JSONObject personObj) throws JSONException {
        this.id = personObj.getString("id");
        this.location = Helper.decodeLocation(personObj.getString("location"));
    }
    public Person(String id, LatLng location) {
        this.id = id;
        this.location = location;
    }
}
