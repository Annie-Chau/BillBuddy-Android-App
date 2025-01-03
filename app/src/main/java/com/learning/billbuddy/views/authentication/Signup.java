package com.learning.billbuddy.views.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.learning.billbuddy.R;

public class Signup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        TextView navigateToSigIn = findViewById(R.id.navigate_to_sign_in);

        navigateToSigIn.setOnClickListener(v -> {
            // Navigate to Sign Up activity
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        });


    }
}