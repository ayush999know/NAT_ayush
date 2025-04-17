package com.example.gpspay;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter; // Add this import
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Cafe3 extends AppCompatActivity {

    private EditText instructionsInput;
    private Button submitButton;
    private TextView orderNumberView, orderDateTimeView;
    private static final int UPI_PAYMENT_REQUEST_CODE = 123;
    private double totalAmount;
    private String upiId = "ayush99know@okaxis"; // UPI ID
    private String merchantName = "Green Cafe";
    private String paymentNote = "Payment for order";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cafe3);

        ListView selectedItemsList = findViewById(R.id.selectedItemsList);
        TextView totalPrice = findViewById(R.id.totalPrice);
        instructionsInput = findViewById(R.id.instructionsInput);
        submitButton = findViewById(R.id.submitButton);
        orderNumberView = findViewById(R.id.orderNumber);
        orderDateTimeView = findViewById(R.id.orderDateTime);

        // Get data from intent
        HashMap<String, Integer> cart = (HashMap<String, Integer>) getIntent().getSerializableExtra("cart");
        totalAmount = getIntent().getDoubleExtra("total", 0.0);

        // Display total price
        totalPrice.setText(String.format("Pay: ₹%.2f", totalAmount));

        // Prepare data for ListView
        ArrayList<String> selectedItems = new ArrayList<>();
        if (cart != null) {
            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                String itemName = entry.getKey();
                int quantity = entry.getValue();
                double itemPrice = getItemPrice(itemName);
                double totalItemPrice = itemPrice * quantity;
                selectedItems.add(String.format("%s x %d - ₹%.2f", itemName, quantity, totalItemPrice));
            }
        }

        // Set up ListView adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedItems);
        selectedItemsList.setAdapter(adapter);

        // Handle input focus
        setupInstructionsInput();

        // Handle UPI payment on total price click
        totalPrice.setOnClickListener(v -> {
            logPaymentDetails(totalAmount); // Log payment details for debugging
            if (isUPIAppInstalled()) {
                if (validatePaymentDetails(totalAmount)) {
                    payUsingUPI(totalAmount);
                }
            } else {
                Toast.makeText(this, "No UPI app found. Install Google Pay, PhonePe, etc.", Toast.LENGTH_SHORT).show();
            }
        });

        // Show/hide submit button based on text input
        instructionsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    submitButton.setVisibility(View.VISIBLE); // Show submit button
                } else {
                    submitButton.setVisibility(View.GONE); // Hide submit button
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle submit button click
        submitButton.setOnClickListener(v -> {
            String instructions = instructionsInput.getText().toString().trim();
            if (!instructions.isEmpty()) {
                // Handle the submitted instructions (e.g., save to database, display a toast, etc.)
                Toast.makeText(this, "Instructions submitted: " + instructions, Toast.LENGTH_SHORT).show();

                // Clear the input and hide the submit button
                instructionsInput.setText("");
                submitButton.setVisibility(View.GONE);
            }
        });
    }

    // Helper method to get item price
    private double getItemPrice(String itemName) {
        String[] items = {"Tea", "Coffee", "Cappuccino", "Latte", "Espresso", "Hot Chocolate", "Sandwich", "Burger", "Pizza", "Pasta"};
        double[] prices = {20.0, 30.0, 50.0, 60.0, 40.0, 70.0, 80.0, 100.0, 120.0, 90.0};

        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(itemName)) {
                return prices[i];
            }
        }
        return 0.0;
    }

    // UPI Payment function
    private void payUsingUPI(double amount) {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId) // Payee UPI ID
                .appendQueryParameter("pn", merchantName) // Payee Name
                .appendQueryParameter("tn", paymentNote) // Transaction Note
                .appendQueryParameter("am", String.valueOf(amount)) // Amount
                .appendQueryParameter("cu", "INR") // Currency
                .appendQueryParameter("tr", String.valueOf(System.currentTimeMillis())) // Unique Transaction ID
                .appendQueryParameter("mc", "1234") // Merchant Code (Optional)
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        try {
            startActivityForResult(Intent.createChooser(upiPayIntent, "Pay with"), UPI_PAYMENT_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No UPI app found. Install Google Pay, PhonePe, etc.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle UPI Payment Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT_REQUEST_CODE) {
            if (data != null) {
                String response = data.getStringExtra("response");
                if (response != null) {
                    if (response.toLowerCase().contains("success")) {
                        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
                        generateOrderDetails();
                    } else if (response.toLowerCase().contains("failed")) {
                        Toast.makeText(this, "Payment Failed. Please try again.", Toast.LENGTH_SHORT).show();
                    } else if (response.toLowerCase().contains("limit exceeded")) {
                        Toast.makeText(this, "Bank limit exceeded. Please contact your bank.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Transaction Cancelled.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No response received.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Transaction Cancelled.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateOrderDetails() {
        int orderNumber = 100000 + new Random().nextInt(900000);
        String currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

        orderNumberView.setText("Order Number: " + orderNumber);
        orderDateTimeView.setText("Order Date & Time: " + currentDateTime);
    }

    private void setupInstructionsInput() {
        instructionsInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                instructionsInput.setFocusable(false);
            }
        });

        instructionsInput.setOnClickListener(v -> {
            instructionsInput.setFocusableInTouchMode(true);
            instructionsInput.setFocusable(true);
            instructionsInput.requestFocus();
        });

        findViewById(R.id.orderSummaryLayout).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (instructionsInput.hasFocus()) {
                    instructionsInput.clearFocus();
                }
            }
            return false;
        });
    }

    // Validate payment details
    private boolean validatePaymentDetails(double amount) {
        if (amount <= 0) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (upiId == null || upiId.isEmpty()) {
            Toast.makeText(this, "Invalid UPI ID", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Check if a UPI app is installed
    private boolean isUPIAppInstalled() {
        Uri uri = Uri.parse("upi://pay");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        return intent.resolveActivity(getPackageManager()) != null;
    }

    // Log payment details for debugging
    private void logPaymentDetails(double amount) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        Log.d("PaymentDetails", "Amount: " + amount + ", UPI ID: " + upiId + ", Date: " + currentDateTime);
    }
}