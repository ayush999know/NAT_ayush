package com.example.gpspay;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TaxReceiptsActivity extends AppCompatActivity {

    private TextView paymentDetailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tax_receipts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        paymentDetailsTextView = findViewById(R.id.paymentDetailsTextView);

        // Retrieve payment details from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("TollTaxPrefs", MODE_PRIVATE);
        String paymentTime = sharedPreferences.getString("payment_time", "N/A");
        String paymentDate = sharedPreferences.getString("payment_date", "N/A");
        String paymentAmount = sharedPreferences.getString("payment_amount", "N/A");
        String tollTaxName = sharedPreferences.getString("toll_tax_name", "N/A");
        String transactionId = sharedPreferences.getString("transaction_id", "N/A");

        // Display payment details
        String paymentDetails = "Payment Time: " + paymentTime + "\n" +
                "Payment Date: " + paymentDate + "\n" +
                "Payment Amount: " + paymentAmount + "\n" +
                "Toll Tax Name: " + tollTaxName + "\n" +
                "Transaction ID: " + transactionId;

        paymentDetailsTextView.setText(paymentDetails);
    }
}