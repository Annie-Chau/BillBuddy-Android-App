package com.learning.billbuddy.views.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.learning.billbuddy.AddUserInfo;
import com.learning.billbuddy.MainActivity;
import com.learning.billbuddy.R;

import java.util.Objects;

public class Register extends AppCompatActivity {

    private EditText emailInput, usernameInput, passwordInput, confirmPasswordInput;
    private MaterialButton signupButton;
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

        // Configure Google Sign-In
        configureGoogleSignIn();

        // Initialize Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult();
                        if (account != null) {
                            firebaseAuthWithGoogle(account);
                        }
                    } catch (Exception e) {
                        Toast.makeText(Register.this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        // Add Google Sign-In button logic
        findViewById(R.id.googleLoginButton).setOnClickListener(v -> launchGoogleSignIn());
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
                        intent.putExtra("userId", firebaseAuth.getCurrentUser().getUid());
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

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void launchGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        // Check if the user is new
                        boolean isNewUser = Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser();

                        if (isNewUser) {
                            // New user, go to AddUserInfo activity
                            Intent intent = new Intent(Register.this, AddUserInfo.class);
                            assert user != null;
                            intent.putExtra("userId", user.getUid());
                            intent.putExtra("email", user.getEmail());
                            intent.putExtra("password", "");
                            intent.putExtra("registrationMethod", "Google Account");
                            startActivity(intent);
                            finish();
                        } else {
                            // Existing user, go to MainActivity
                            Intent intent = new Intent(Register.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(Register.this, "Google sign-in failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }
}
