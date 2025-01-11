package com.learning.billbuddy.views.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.learning.billbuddy.R;

import java.util.Objects;

public class Register extends AppCompatActivity {

    private EditText emailInput, passwordInput, confirmPasswordInput;
    private Button signupButton;
    private TextView navigateToSignIn;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        emailInput = findViewById(R.id.sign_up_email_text_input);
        passwordInput = findViewById(R.id.signup_password_text_input);
        confirmPasswordInput = findViewById(R.id.signup_confirm_password_text_input);
        signupButton = findViewById(R.id.signup_confirm_button);
        navigateToSignIn = findViewById(R.id.navigate_to_sign_in);

        // Set up Sign Up button click listener
        signupButton.setOnClickListener(v -> handleSignup());

        // Navigate to Sign In screen
        navigateToSignIn.setOnClickListener(v -> navigateToSignIn());

    }

    private void handleSignup() {
        // Get input values
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Confirm your password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        // Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(Register.this, AddUserInfo.class);
                        intent.putExtra("userId", Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid());
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        intent.putExtra("registrationMethod", "Email");
                        firebaseAuth.signOut();

                        // Navigate to Add Info Activity
                        startActivity(intent);
                        finish();
                    } else {
                        // Sign up failed
                        Toast.makeText(Register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }
}
