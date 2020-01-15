package com.example.tm18app;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Splash screen to show before main mContent.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 15.12.2019
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // No layout for this View to inflate. The splash screen is just a theme containing a
        // background and a drawable.
        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
        finish();
    }
}
