package com.longynuss.guessgame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_display);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(SplashDisplay.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }
}
