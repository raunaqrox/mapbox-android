package promignis.com.mapbox;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import promignis.com.mapbox.Models.Person;
import promignis.com.mapbox.Utils.URLStore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MapView mapView;
    private Location currentBestLocation;
    private Socket mSocket;
    private String id;
    private MapboxMap map;

    {
        try {
            mSocket = IO.socket(URLStore.BASE_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        setupMap();
        setupSocketIO();
    }

    private void setupMap() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
//                mapboxMap.setStyleUrl(Style.DARK);
                currentBestLocation = getLastBestLocation();
                if (currentBestLocation != null) {
                    LatLng currLatLng = new LatLng(currentBestLocation.getLatitude(), currentBestLocation.getLongitude());
                    drawMarker(currLatLng, "Me!", "This is my location");

                    animateMeTo(currLatLng);
                } else {
                    Debug("currentBestLocation null");
                }
            }
        });
    }

    private void setupSocketIO() {
        mSocket.on("connection", Connected);
        currentBestLocation = getLastBestLocation();
        if(currentBestLocation != null) {
            mSocket.emit("storeLocation", getEncodedLocation());
        }
        mSocket.on("setId", setId);
        mSocket.on("newUser", showNewUser);
        mSocket.on("setMarker", setMarker);
        mSocket.connect();
    }

    private Emitter.Listener Connected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    private Emitter.Listener setId = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            id = (String) args[0];
        }
    };

    private Emitter.Listener showNewUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                Person newPerson = new Person((JSONObject)args[0]);
                drawMarker(newPerson.getLocation(), newPerson.getId(), "");
                Debug("Showing new user");
                animateMeTo(newPerson.getLocation());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener setMarker = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LatLng markerLatLng = getDecodedLocation((String)args[0]);
            drawMarker(markerLatLng, id, "");
        }
    };

    private String getEncodedLocation() {
        currentBestLocation = getLastBestLocation();
        return currentBestLocation.getLatitude() + ":" + currentBestLocation.getLongitude();
    }

    private LatLng getDecodedLocation(String location) {
        String[] markerLocation = location.split(":");
        return new LatLng(Double.parseDouble(markerLocation[0]), Double.parseDouble(markerLocation[1]));

    }

    private void drawMarker(LatLng markerLatLng, String title, String snippet) {
        map.addMarker(new MarkerOptions()
        .position(markerLatLng)
        .title(title)
        .snippet(snippet));
    }

    private void animateMeTo(LatLng animateToLocation) {
        CameraPosition position = new CameraPosition.Builder()
                .target(animateToLocation)
                .zoom(19)
                .bearing(180)
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000);
    }

    private Location getLastBestLocation() {
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Debug("Give gps permissions!");
            return null;
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if ( 0 < GPSLocationTime - NetLocationTime ) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    private void Debug(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public String getId() {
        return id;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }
}
