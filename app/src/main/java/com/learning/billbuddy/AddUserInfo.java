package com.learning.billbuddy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.models.User;
import com.learning.billbuddy.views.authentication.Login;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class AddUserInfo extends AppCompatActivity {

    private ImageButton changeAvatarButton;
    private EditText nameInput, phoneInput;
    private MaterialButton submitButton;
    private ImageView userAvatar;
    String userId, email, password, registrationMethod;

    private static final int PICK_IMAGE_REQUEST = 1;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_info);

        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        registrationMethod = getIntent().getStringExtra("registrationMethod");

        // Get references to UI elements
        changeAvatarButton = findViewById(R.id.change_avatar_button);
        nameInput = findViewById(R.id.sign_up_name_text_input);
        phoneInput = findViewById(R.id.sign_up_phone_text_input);
        submitButton = findViewById(R.id.signup_confirm_button);
        userAvatar = findViewById(R.id.user_avatar);

        // Set up click listener for the submit button
        submitButton.setOnClickListener(v -> submitUserInfo());

        // Apply window insets to the main layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Change the avatar of the user
        changeAvatarButton.setOnClickListener(v -> openGalerry());
    }

    private void openGalerry() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            // Use Glide to load the image into the ImageView with a circular crop
            Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .into(userAvatar);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                userAvatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void submitUserInfo() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Basic input validation (You might want to add more robust validation)
        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User.createUser(userId, name, email, phone, "XXX", registrationMethod, new ArrayList<>());
        Toast.makeText(AddUserInfo.this, "User created successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AddUserInfo.this, Objects.equals(registrationMethod, "Google Account") ? MainActivity.class : Login.class);
        startActivity(intent);
        finish();
    }
}