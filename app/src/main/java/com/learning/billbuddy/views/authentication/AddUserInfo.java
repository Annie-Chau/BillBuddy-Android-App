package com.learning.billbuddy.views.authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.MainActivity;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class AddUserInfo extends AppCompatActivity {


    private TextView userInfoFormHeadingTop;

    private TextView cancelTextView;
    private TextView userInfoFormHeadingBottom;
    private ImageButton changeAvatarButton;
    private EditText nameInput, phoneInput;
    private Button submitButton;
    private ImageView userAvatar;
    private TextView userAvatarText;
    String userId, email, password, registrationMethod;

    private User currentUser;
    private FirebaseUser userAuth;

    private static final int PICK_IMAGE_REQUEST = 1;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_info);

        cancelTextView = findViewById(R.id.user_form_cancel_btn);

        FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();


        userId = getIntent().getStringExtra("userId");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");
        registrationMethod = getIntent().getStringExtra("registrationMethod");
        currentUser = (User) getIntent().getSerializableExtra("currentUser");

        // Handle case update
        Boolean isEdit = getIntent().getBooleanExtra("isEdit", false);
        String currentName = getIntent().getStringExtra("currentName");
        String currentPhoneNumber = getIntent().getStringExtra("currentPhoneNumber");
        String currentPhotoUrl = getIntent().getStringExtra("currentPhotoUrl");

        if (!isEdit) {
            cancelTextView.setVisibility(View.INVISIBLE);
        }
        this.userAuth = FirebaseAuth.getInstance().getCurrentUser();

        //heading
        userInfoFormHeadingTop = findViewById(R.id.user_info_form_heading_top);
        userInfoFormHeadingBottom = findViewById(R.id.user_info_form_heading_bottom);

        // Get references to UI elements
//        changeAvatarButton = findViewById(R.id.change_avatar_button);
        nameInput = findViewById(R.id.sign_up_name_text_input);
        phoneInput = findViewById(R.id.sign_up_phone_text_input);
        submitButton = findViewById(R.id.signup_confirm_button);
        userAvatarText = findViewById(R.id.user_form_avatar_text_view);
        userAvatar = findViewById(R.id.user_form_avatar);

        if (isEdit) {
            userInfoFormHeadingTop.setText("Edit your profile");
            userInfoFormHeadingBottom.setText("Update your information");
            submitButton.setText("Update");
        }

        if (currentName != null && !currentName.isEmpty()) {
            nameInput.setText(currentName);
        }
        if (currentPhoneNumber != null && !currentPhoneNumber.isEmpty()) {
            phoneInput.setText(currentPhoneNumber);
        }

        // Set up click listener for the submit button
        submitButton.setOnClickListener(v -> submitUserInfo());

        // Apply window insets to the main layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (currentUser == null) {
            if (userAuth == null || userAuth.getPhotoUrl() == null) {
                userAvatarText.setVisibility(View.GONE);
                userAvatar.setVisibility(View.VISIBLE);
                userAvatar.setImageDrawable(getDrawable(R.drawable.person_icon));
            } else {
                userAvatarText.setVisibility(View.GONE);
                userAvatar.setVisibility(View.VISIBLE);
                Glide.with(this).load(userAuth.getPhotoUrl()).circleCrop().into(userAvatar);
            }

        } else {
            if (currentUser.getProfilePictureURL() == null || currentUser.getProfilePictureURL().equals("XXX") || currentUser.getProfilePictureURL().isEmpty()) {
                Log.d("Profile", currentUser.getProfilePictureURL());
                userAvatarText.setText(currentUser.getName().substring(0, 1));
                userAvatarText.setVisibility(View.VISIBLE);
            } else {
                userAvatarText.setVisibility(View.GONE);
                userAvatar.setVisibility(View.VISIBLE);
                Glide.with(this).load(userAuth.getPhotoUrl()).circleCrop().into(userAvatar);
            }
        }


        // Change the avatar of the user
//        changeAvatarButton.setOnClickListener(v -> openGalerry());
        cancelTextView.setOnClickListener(v -> {
            finish();
        });
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

        Boolean isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (isEdit) {
            User.updateUser(
                    userId,
                    nameInput.getText().toString(),
                    phoneInput.getText().toString());
            Toast.makeText(AddUserInfo.this, "User updated successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddUserInfo.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            String photoUrl = (userAuth != null && userAuth.getPhotoUrl() != null? Objects.requireNonNull(userAuth.getPhotoUrl()).toString() : "");
            User.createUser(userId, name, email, phone, photoUrl, registrationMethod, new ArrayList<>(), false);
            Toast.makeText(AddUserInfo.this, "User created successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddUserInfo.this, Objects.equals(registrationMethod, "Google Account") ? MainActivity.class : Login.class);
            startActivity(intent);
            finish();
        }
    }
}