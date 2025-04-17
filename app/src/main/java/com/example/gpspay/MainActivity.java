package com.example.gpspay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView gpsPayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gpsPayText = findViewById(R.id.gpsPayText);

        // Fade-in animation for GPS Pay text
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);

        gpsPayText.setVisibility(TextView.VISIBLE);
        gpsPayText.startAnimation(fadeIn);

        // Delay for 3 seconds and move to HomeActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
