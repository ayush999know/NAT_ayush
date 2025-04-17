package com.example.gpspay;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class TollTax extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView tollTaxMessage;
    private TextView carNumber;
    private Switch autoPaySwitch;
    private static final LatLng TOLL_LOCATION = new LatLng(28.79669, 77.53850);
    private static final float RADIUS = 500;
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private static final String CAR_NUMBER_KEY = "car_number";
    private boolean isPaymentDone = false;
    private boolean isPaymentInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toll_tax);

        // Initialize views
        tollTaxMessage = findViewById(R.id.tollTaxMessage);
        carNumber = findViewById(R.id.carNumber);
        autoPaySwitch = findViewById(R.id.autoPaySwitch);

        // Set up click listeners for icons
        ImageView walletIcon = findViewById(R.id.walletIcon);
        walletIcon.setOnClickListener(v -> {
            Intent intent = new Intent(TollTax.this, WalletActivity.class);
            startActivity(intent);
        });

        ImageView taxReceiptsIcon = findViewById(R.id.taxReceiptsIcon);
        taxReceiptsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(TollTax.this, TaxReceiptsActivity.class);
            startActivity(intent);
        });

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("TollTaxPrefs", Context.MODE_PRIVATE);

        // Set car number (load saved or generate new)
        String savedCarNumber = sharedPreferences.getString(CAR_NUMBER_KEY, generateRandomCarNumber());
        carNumber.setText(savedCarNumber);

        // Set up car number change dialog
        carNumber.setOnClickListener(v -> showChangeCarNumberDialog());

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check and request location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            startLocationUpdates();
        }
    }

    private String generateRandomCarNumber() {
        String[] series = {"MH 01", "MH 02", "MH 03", "MH 04"};
        String[] letters = {"AB", "CD", "EF", "GH"};
        int randomNumber = (int) (Math.random() * 10000);
        return series[(int) (Math.random() * series.length)] + " " +
                letters[(int) (Math.random() * letters.length)] + " " +
                String.format("%04d", randomNumber);
    }

    private void showChangeCarNumberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Car Number");

        final EditText input = new EditText(this);
        input.setText(carNumber.getText());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newCarNumber = input.getText().toString().trim();
            if (!newCarNumber.isEmpty()) {
                carNumber.setText(newCarNumber);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(CAR_NUMBER_KEY, newCarNumber);
                editor.apply();
            } else {
                Toast.makeText(TollTax.this, "Car number cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null || isPaymentDone || isPaymentInProgress) return;

            for (Location location : locationResult.getLocations()) {
                if (isWithinRadius(location.getLatitude(), location.getLongitude(),
                        TOLL_LOCATION.latitude, TOLL_LOCATION.longitude, RADIUS)) {
                    tollTaxMessage.setText("Toll tax detected");
                    if (autoPaySwitch.isChecked()) {
                        startPaymentProcess();
                    } else {
                        tollTaxMessage.setText("Autopay is off. Payment not initiated.");
                    }
                }
            }
        }
    };

    private boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, float radius) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0] <= radius;
    }

    private void startPaymentProcess() {
        if (isPaymentDone || isPaymentInProgress) return;

        isPaymentInProgress = true;

        handler.postDelayed(() -> {
            tollTaxMessage.setText("Receiving data");
            handler.postDelayed(() -> {
                tollTaxMessage.setText("Paying 300");
                handler.postDelayed(() -> {
                    tollTaxMessage.setText("Payment done");
                    isPaymentDone = true;
                    isPaymentInProgress = false;

                    // Save payment details
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("payment_time", System.currentTimeMillis() + "");
                    editor.putString("payment_date", new java.util.Date().toString());
                    editor.putString("payment_amount", "300");
                    editor.putString("toll_tax_name", "Toll Tax Name");
                    editor.putString("transaction_id", "TX123456789");
                    editor.apply();

                }, 3000);
            }, 3000);
        }, 3000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        handler.removeCallbacksAndMessages(null);
    }
}