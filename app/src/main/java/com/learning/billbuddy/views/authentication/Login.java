package com.learning.billbuddy.views.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.learning.billbuddy.MainActivity;
import com.learning.billbuddy.R;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private EditText emailInput, passwordInput;
    private Button signInButton;
    private ImageButton googleLoginButton;
    private TextView navigateToSignUp;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Replace with your web client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Bind UI components
        emailInput = findViewById(R.id.login_email_text_input);
        passwordInput = findViewById(R.id.login_password_text_input);
        signInButton = findViewById(R.id.login_submit_button);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        navigateToSignUp = findViewById(R.id.navigate_to_sign_up);

        // Set up Sign In button click listener
        signInButton.setOnClickListener(v -> handleLogin());

        // Set up Google Login button click listener
        googleLoginButton.setOnClickListener(v -> handleGoogleLogin());

        // Navigate to Sign Up activity
        navigateToSignUp.setOnClickListener(v -> navigateToSignUp());
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Toast.makeText(Login.this, "Welcome, " + (user != null ? user.getEmail() : "User") + "!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, task -> {
                    Toast.makeText(Login.this, "Failed!", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleGoogleLogin() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
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
                            Intent intent = new Intent(Login.this, AddUserInfo.class);
                            assert user != null;
                            intent.putExtra("userId", user.getUid());
                            intent.putExtra("email", user.getEmail());
                            intent.putExtra("password", "");
                            intent.putExtra("registrationMethod", "Google Account");
                            startActivity(intent);
                            finish();
                        } else {
                            // Existing user, go to MainActivity
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(Login.this, "Google sign-in failed: " +
                                Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
        finish();
    }
}
