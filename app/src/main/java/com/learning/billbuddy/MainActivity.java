package com.learning.billbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.learning.billbuddy.models.User;
import com.learning.billbuddy.views.authentication.Login;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button = findViewById(R.id.to_add_expense);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        });
    }

    // Example for fetch user and use it
    private void fetchUsers() {
        User.fetchAllUsers(users -> {
            Log.d("User", users.toString());
        });
    }
}