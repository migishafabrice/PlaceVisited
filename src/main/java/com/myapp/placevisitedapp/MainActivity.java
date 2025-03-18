package com.myapp.placevisitedapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper dbHelper;
    private double currentLatitude;
    private double currentLongitude;
    private LocationCallback locationCallback;
    private static final long UPDATE_INTERVAL = 10000;  // 10 seconds
    private static final long FASTEST_INTERVAL = 5000;  // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OpenStreetMap configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView = findViewById(R.id.mapView);
        mapView.setMultiTouchControls(true);

        FloatingActionButton fabAddPlace = findViewById(R.id.fabAddPlace);
        FloatingActionButton fabViewPlaces = findViewById(R.id.fabViewPlaces);

        // Create location callback
        createLocationCallback();

        // Check and request permissions
        checkLocationPermission();

        fabAddPlace.setOnClickListener(v -> showAddPlaceDialog());
        fabViewPlaces.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlacesListActivity.class);
            startActivity(intent);
        });
    }
@Override
public boolean onCreateOptionsMenu(Menu menu)
{
    MenuInflater inflater=getMenuInflater();
    inflater.inflate(R.menu.main_menu,menu);
    return true;
}
public boolean onOptionsItemSelected(MenuItem item)
{
    if(item.getItemId()==R.id.mn_add)
    {
        showAddPlaceDialog();
    }
    if(item.getItemId()==R.id.mn_history) {
        Intent intent = new Intent(MainActivity.this, PlacesListActivity.class);
        startActivity(intent);
    }
    return true;
}
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    updateMapLocation(currentLatitude, currentLongitude);
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            getCurrentLocation();
            startLocationUpdates();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            updateMapLocation(currentLatitude, currentLongitude);
                        }
                    });
        }
    }

    private void updateMapLocation(double latitude, double longitude) {
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        mapController.setCenter(startPoint);

        // Clear existing overlays
        mapView.getOverlays().clear();

        // Add marker for current location
        Marker marker = new Marker(mapView);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Current Location");
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showAddPlaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Place");

        final EditText input = new EditText(this);
        input.setHint("Enter place name");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = input.getText().toString();
            if (!title.isEmpty()) {
                dbHelper.addPlace(title, currentLatitude, currentLongitude);
                Toast.makeText(MainActivity.this, "Place saved!",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}