package com.example.gpspay;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class NatCafeActivity extends AppCompatActivity {

    private TextView message1, message2, message3, timeTextView;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nat_cafe);

        // Initialize Views
        timeTextView = findViewById(R.id.timeTextView);
        message1 = findViewById(R.id.message1);
        message2 = findViewById(R.id.message2);
        message3 = findViewById(R.id.message3);

        // Set greeting based on time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 0 && hour < 12) {
            timeTextView.setText("Good Morning");
        } else if (hour >= 12 && hour < 16) {
            timeTextView.setText("Good Afternoon");
        } else {
            timeTextView.setText("Good Evening");
        }

        // Navigate to NextActivity when TextView is clicked
        timeTextView.setOnClickListener(v -> {
            Intent intent = new Intent(NatCafeActivity.this, Cafe2.class);
            startActivity(intent);
        });

        // Show animated messages
        showMessagesWithAnimation();
    }

    private void showMessagesWithAnimation() {
        handler.postDelayed(() -> fadeInView(message1), 500);  // Show first message after 0.5s
        handler.postDelayed(() -> fadeInView(message2), 2000); // Show second message after 2s
        handler.postDelayed(() -> fadeInView(message3), 3500); // Show third message after 3.5s
    }

    private void fadeInView(View view) {
        view.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        view.startAnimation(fadeIn);
    }
}
