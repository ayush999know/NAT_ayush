package com.example.gpspay;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cafe2 extends AppCompatActivity {

    private LinearLayout itemsContainer;
    private TextView cartSummary, totalPrice, statusText;
    private EditText searchBar;
    private View statusCircle;
    private Button checkoutButton;

    private Map<String, Integer> cart = new HashMap<>();
    private double total = 0.0;
    private final double GST_RATE = 0.18; // 18% GST

    private static final double CAFE_LAT = 28.79669;
    private static final double CAFE_LON = 77.53850;
    private static final float LOCATION_THRESHOLD = 500; // 500 meters

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private final String[] items = {"Tea", "Coffee", "Cappuccino", "Latte", "Espresso", "Hot Chocolate", "Sandwich", "Burger", "Pizza", "Pasta"};
    private final double[] prices = {20.0, 30.0, 50.0, 60.0, 40.0, 70.0, 80.0, 100.0, 120.0, 90.0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe2);

        // Initialize UI Components
        itemsContainer = findViewById(R.id.itemsContainer);
        cartSummary = findViewById(R.id.cartSummary);
        totalPrice = findViewById(R.id.totalPrice);
        searchBar = findViewById(R.id.searchBar);
        statusCircle = findViewById(R.id.statusCircle);
        statusText = findViewById(R.id.statusText);
        checkoutButton = findViewById(R.id.checkoutButton);

        // Disable checkout button initially
        checkoutButton.setEnabled(false);
        checkoutButton.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create Location Request
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        // Location Callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    updateLocationUI(locationResult.getLastLocation());
                }
            }
        };

        checkLocationPermission();

        // Add Items to Layout
        for (int i = 0; i < items.length; i++) {
            addItemToLayout(items[i], prices[i]);
        }

        updateCartSummary();

        // Search Bar Functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Checkout Button Click
        checkoutButton.setOnClickListener(v -> {
            if (checkoutButton.isEnabled()) {
                Intent intent = new Intent(Cafe2.this, Cafe3.class);
                intent.putExtra("cart", (Serializable) cart);
                intent.putExtra("total", total + (total * GST_RATE));
                startActivity(intent);
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateLocationUI(Location location) {
        if (location == null) return;

        double userLat = location.getLatitude();
        double userLon = location.getLongitude();

        float[] results = new float[1];
        Location.distanceBetween(userLat, userLon, CAFE_LAT, CAFE_LON, results);
        float distance = results[0];

        if (distance <= LOCATION_THRESHOLD) {
            statusCircle.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_green_dark));
            statusText.setText("You are in the cafe");

            checkoutButton.setEnabled(true);
            checkoutButton.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark));
        } else {
            statusCircle.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
            statusText.setText("You are not in the cafe");

            checkoutButton.setEnabled(false);
            checkoutButton.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
        }
    }

    private void addItemToLayout(String itemName, double price) {
        View itemView = getLayoutInflater().inflate(R.layout.item_layout, null);

        TextView itemNameView = itemView.findViewById(R.id.itemName);
        TextView itemPriceView = itemView.findViewById(R.id.itemPrice);
        ImageView addButton = itemView.findViewById(R.id.addButton); // Changed to ImageView
        ImageView subtractButton = itemView.findViewById(R.id.subtractButton); // Changed to ImageView

        itemNameView.setText(itemName);
        itemPriceView.setText(String.format("₹%.2f", price));

        addButton.setOnClickListener(v -> {
            addToCart(itemName, price);
            updateCartSummary();
        });

        subtractButton.setOnClickListener(v -> {
            subtractFromCart(itemName, price);
            updateCartSummary();
        });

        itemsContainer.addView(itemView);
    }

    private void addToCart(String itemName, double price) {
        cart.put(itemName, cart.getOrDefault(itemName, 0) + 1);
        total += price;
        updateCartSummary();
    }

    private void subtractFromCart(String itemName, double price) {
        if (cart.containsKey(itemName)) { // Fixed: Added missing parenthesis
            cart.put(itemName, cart.get(itemName) - 1);
            total -= price;
            if (cart.get(itemName) == 0) {
                cart.remove(itemName);
            }
        }
        updateCartSummary();
    }

    private void updateCartSummary() {
        StringBuilder summary = new StringBuilder();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            summary.append(entry.getKey()).append(" x ").append(entry.getValue()).append("\n");
        }

        if (cart.isEmpty()) {
            // Hide cart summary and total price when cart is empty
            cartSummary.setVisibility(View.GONE);
            totalPrice.setVisibility(View.GONE);
        } else {
            // Show cart summary and total price when items are added
            cartSummary.setVisibility(View.VISIBLE);
            totalPrice.setVisibility(View.VISIBLE);
            cartSummary.setText(summary.toString());
            totalPrice.setText(String.format("Total: ₹%.2f (incl. GST)", total + (total * GST_RATE)));
        }
    }

    private void filterItems(String query) {
        itemsContainer.removeAllViews();
        for (int i = 0; i < items.length; i++) {
            if (items[i].toLowerCase().contains(query.toLowerCase())) {
                addItemToLayout(items[i], prices[i]);
            }
        }
    }
}