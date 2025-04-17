package com.example.gpspay;



import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity {

    private TextView currentBalance;
    private EditText addMoneyInput;
    private Button addMoneyButton;
    private ListView transactionHistory;
    private Button payButton;

    private double balance = 0.0; // Current wallet balance
    private ArrayList<String> transactions; // List of transactions
    private ArrayAdapter<String> transactionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        // Initialize views
        currentBalance = findViewById(R.id.currentBalance);
        addMoneyInput = findViewById(R.id.addMoneyInput);
        addMoneyButton = findViewById(R.id.addMoneyButton);
        transactionHistory = findViewById(R.id.transactionHistory);
        payButton = findViewById(R.id.payButton);

        // Initialize transaction list
        transactions = new ArrayList<>();
        transactionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, transactions);
        transactionHistory.setAdapter(transactionAdapter);

        // Set click listener for the "Add Money" button
        addMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMoney();
            }
        });

        // Set click listener for the "Pay Now" button
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePayment();
            }
        });
    }

    private void addMoney() {
        String amountStr = addMoneyInput.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update balance
        balance += amount;
        currentBalance.setText(String.format(Locale.getDefault(), "₹%.2f", balance));

        // Add transaction to history
        String transaction = "Added ₹" + amount + " on " + getCurrentDateTime();
        transactions.add(transaction);
        transactionAdapter.notifyDataSetChanged();

        // Clear input
        addMoneyInput.setText("");
        Toast.makeText(this, "Money added successfully", Toast.LENGTH_SHORT).show();
    }

    private void makePayment() {
        if (balance <= 0) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deduct payment amount (e.g., ₹300 for toll payment)
        double paymentAmount = 300.0;
        if (balance >= paymentAmount) {
            balance -= paymentAmount;
            currentBalance.setText(String.format(Locale.getDefault(), "₹%.2f", balance));

            // Add transaction to history
            String transaction = "Paid ₹" + paymentAmount + " on " + getCurrentDateTime();
            transactions.add(transaction);
            transactionAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Insufficient balance for payment", Toast.LENGTH_SHORT).show();
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}