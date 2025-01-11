package com.learning.billbuddy.views.home;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.learning.billbuddy.R;

public class AboutUsActivity extends AppCompatActivity {

    private ImageButton returnButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        returnButton = findViewById(R.id.button_return);

        returnButton.setOnClickListener(v -> finish());
    }
}