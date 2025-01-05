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

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        TextView navigateSignUpButton =findViewById(R.id.navigate_to_sign_up);

        navigateSignUpButton.setOnClickListener(v -> {
            // Navigate to Sign Up activity
            Intent intent = new Intent(this, Signup.class);
            startActivity(intent);
        });
    }
}
