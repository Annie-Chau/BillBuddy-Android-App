package com.learning.billbuddy;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.models.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddGroupActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        db = FirebaseFirestore.getInstance();

        // Retrieve the data passed from HomePage
        String ownerID = getIntent().getStringExtra("OWNER_ID");
        String ownerName = getIntent().getStringExtra("OWNER_NAME");

        // Display the creator's name
        TextView creatorNameTextView = findViewById(R.id.creator_name);
        creatorNameTextView.setText(ownerName);

        // Get references to the input fields
        EditText titleEditText = findViewById(R.id.title_edit_text);
        EditText descriptionEditText = findViewById(R.id.description_edit_text);
        EditText participantEmailEditText = findViewById(R.id.participant_email);

        // Get reference to the create button
        Button createBillBuddyButton = findViewById(R.id.create_billbuddy_button);

        createBillBuddyButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String participantEmail = participantEmailEditText.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(AddGroupActivity.this, "Title and description are required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a list of participants
            List<String> memberIDs = new ArrayList<>();
            memberIDs.add(ownerID); // Add the owner to the memberIDs list

            // Create a new group
            String groupID = UUID.randomUUID().toString();
            Group group = new Group(groupID, title, description, "", ownerID, memberIDs, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

            // Save the group to Firestore
            db.collection("groups").document(groupID).set(group)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddGroupActivity.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    })
                    .addOnFailureListener(e -> {
                        Log.w("AddGroupActivity", "Error adding document", e);
                        Toast.makeText(AddGroupActivity.this, "Error creating group", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}