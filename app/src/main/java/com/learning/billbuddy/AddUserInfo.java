package com.learning.billbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.models.User;
import com.learning.billbuddy.views.authentication.Login;

import java.util.ArrayList;
import java.util.Objects;

public class AddUserInfo extends AppCompatActivity {

    private ImageButton changeAvatarButton;
    private EditText nameInput, phoneInput;
    private MaterialButton submitButton;
    String email, password, registrationMethod;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_info);

        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        registrationMethod = getIntent().getStringExtra("registrationMethod");

        // Get references to UI elements
        changeAvatarButton = findViewById(R.id.change_avatar_button);
        nameInput = findViewById(R.id.sign_up_name_text_input);
        phoneInput = findViewById(R.id.sign_up_phone_text_input);
        submitButton = findViewById(R.id.signup_confirm_button);

        // Set up click listener for the submit button
        submitButton.setOnClickListener(v -> submitUserInfo());

        // Set an onClickListener for the changeAvatarButton (You'll need to implement image selection/capture)
        changeAvatarButton.setOnClickListener(v -> {
            // TODO: Implement image selection/capture logic here
        });

        // Apply window insets to the main layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void submitUserInfo() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Basic input validation (You might want to add more robust validation)
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User.createUser(name, email, phone, "XXX", registrationMethod, new ArrayList<>());
        Toast.makeText(AddUserInfo.this, "User created successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AddUserInfo.this, Objects.equals(registrationMethod, "Google Account") ? MainActivity.class : Login.class);
        startActivity(intent);
        finish();
    }
}