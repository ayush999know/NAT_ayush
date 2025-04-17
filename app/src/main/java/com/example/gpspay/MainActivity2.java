package com.example.gpspay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity2 extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final double TARGET_LAT = 28.79669;
    private static final double TARGET_LON = 77.53850;
    private static final float PROXIMITY_RADIUS_METERS = 500;
    private static final String UPI_ID = "ayush99know@okaxis";
    private static final long LOCATION_UPDATE_INTERVAL = 5000;
    private static final long FASTEST_LOCATION_INTERVAL = 3000;

    private TextView gpsCoordinatesTextView, locationTextView;
    private TextView businessNameText, floorText, distanceText;
    private View proximityIndicator, proximityContainer;
    private MaterialButton payNowButton;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigationView;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private ExecutorService executorService;
    private boolean isInProximity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initializeViews();
        setupServices();
        setupNavigation();
        setupPayNowButton();
        setupProximityClick();
        createLocationRequest();
        createLocationCallback();
        checkLocationPermission();
    }

    private void initializeViews() {
        gpsCoordinatesTextView = findViewById(R.id.gpsCoordinates);
        locationTextView = findViewById(R.id.location);
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        proximityContainer = findViewById(R.id.proximity_container);
        proximityIndicator = findViewById(R.id.proximity_indicator);
        businessNameText = findViewById(R.id.proximity_business_name);
        floorText = findViewById(R.id.proximity_floor);
        distanceText = findViewById(R.id.proximity_distance);
        payNowButton = findViewById(R.id.payNowButton);

        proximityIndicator.setVisibility(View.GONE);
        floorText.setVisibility(View.GONE);
        payNowButton.setVisibility(View.GONE);
    }

    private void setupServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    handleLocationUnavailable();
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationUI(location);
                        checkProximity(location);
                        reverseGeocodeLocation(location);
                    } else {
                        handleLocationUnavailable();
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            hideProximityUI();
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void setupNavigation() {
        topAppBar.setNavigationOnClickListener(v -> {
            if (!this.getClass().getSimpleName().equals("HomeActivity")) {
                navigateToActivity(HomeActivity.class);
            }
        });

        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                navigateToActivity(SettingsActivity.class);
                return true;
            }
            return false;
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home && !this.getClass().getSimpleName().equals("MainActivity2")) {
                navigateToActivity(HomeActivity.class);
            } else if (itemId == R.id.nav_explore && !this.getClass().getSimpleName().equals("TollTax")) {
                navigateToActivity(TollTax.class);  // Changed to navigate to TollTax
            } else if (itemId == R.id.nav_favorite && !this.getClass().getSimpleName().equals("FavoritesActivity")) {
                navigateToActivity(FavoritesActivity.class);
            } else if (itemId == R.id.nav_settings && !this.getClass().getSimpleName().equals("SettingsActivity")) {
                navigateToActivity(SettingsActivity.class);
            }
            return true;
        });
    }

    private void setupPayNowButton() {
        payNowButton.setOnClickListener(v -> initiateUpiPayment());
    }

    private void setupProximityClick() {
        proximityIndicator.setOnClickListener(v -> {
            if (proximityIndicator.getVisibility() == View.VISIBLE) {
                startActivity(new Intent(this, NatCafeActivity.class));
            }
        });
    }

    private void initiateUpiPayment() {
        try {
            Uri uri = Uri.parse("upi://pay").buildUpon()
                    .appendQueryParameter("pa", UPI_ID)
                    .appendQueryParameter("pn", "Green Cafe")
                    .appendQueryParameter("mc", "5411")
                    .appendQueryParameter("tr", "TXN" + System.currentTimeMillis())
                    .appendQueryParameter("tn", "Payment for meal at Nat's Cafe")
                    .appendQueryParameter("cu", "INR")
                    .build();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);

            intent.setPackage("com.google.android.apps.nbu.paisa.user");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                intent.setPackage(null);
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error initiating payment", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void updateLocationUI(Location location) {
        runOnUiThread(() -> {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            gpsCoordinatesTextView.setText(String.format(Locale.getDefault(), "%.4f° N, %.4f° E", lat, lon));
        });
    }

    private void checkProximity(Location location) {
        float[] results = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                TARGET_LAT, TARGET_LON, results);
        float distanceInMeters = results[0];

        runOnUiThread(() -> {
            if (distanceInMeters <= PROXIMITY_RADIUS_METERS) {
                if (!isInProximity) {
                    showProximityUI(distanceInMeters);
                    isInProximity = true;
                }
            } else {
                if (isInProximity) {
                    hideProximityUI();
                    isInProximity = false;
                }
            }
        });
    }

    private void showProximityUI(float distance) {
        proximityIndicator.setVisibility(View.VISIBLE);
        floorText.setVisibility(View.VISIBLE);
        payNowButton.setVisibility(View.VISIBLE);
        distanceText.setText(String.format(Locale.getDefault(), "%.0fm away", distance));
    }

    private void hideProximityUI() {
        proximityIndicator.setVisibility(View.GONE);
        floorText.setVisibility(View.GONE);
        payNowButton.setVisibility(View.GONE);
    }

    private void reverseGeocodeLocation(Location location) {
        executorService.execute(() -> {
            Geocoder geocoder = new Geocoder(MainActivity2.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        String city = addresses.get(0).getLocality();
                        String country = addresses.get(0).getCountryName();
                        locationTextView.setText(city != null ? city + ", " + country : country);
                    } else {
                        locationTextView.setText("Location not found");
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> locationTextView.setText("Error getting address"));
            }
        });
    }

    private void handleLocationUnavailable() {
        runOnUiThread(() -> {
            gpsCoordinatesTextView.setText("Location unavailable");
            locationTextView.setText("Enable GPS and try again");
            hideProximityUI();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
                hideProximityUI();
            }
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}