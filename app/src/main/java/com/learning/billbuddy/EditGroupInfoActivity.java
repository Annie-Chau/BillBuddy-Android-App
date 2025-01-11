package com.learning.billbuddy;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.MemberAdapter;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.Notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditGroupInfoActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText;
    private RecyclerView membersRecyclerView;
    private Button saveChangesButton;
    private Group currentGroup;
    private List<String> memberNames = new ArrayList<>();
    private List<String> memberIDs;
    private MemberAdapter memberAdapter;
    private EditText editMemberEmail;
    private Button addMemberButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group_info);

        // Initialize UI elements
        titleEditText = findViewById(R.id.edit_group_enter_title);
        descriptionEditText = findViewById(R.id.edit_group_description);
        membersRecyclerView = findViewById(R.id.members_recycler_view);
        saveChangesButton = findViewById(R.id.save_group_changes_button);
        editMemberEmail = findViewById(R.id.edit_member_email);
        addMemberButton = findViewById(R.id.add_member_button);
        cancelButton = findViewById(R.id.edit_group_cancel_button);

        addMemberButton.setOnClickListener(v -> addMemberByEmail());
        cancelButton.setOnClickListener(v -> finish());


        // Retrieve the current group passed through intent
        currentGroup = (Group) getIntent().getSerializableExtra("group");
        if (currentGroup == null) {
            Toast.makeText(this, "Group data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize member IDs
        memberIDs = new ArrayList<>(currentGroup.getMemberIDs());

        // Prefill the group title and description
        titleEditText.setText(currentGroup.getName());
        descriptionEditText.setText(currentGroup.getDescription());

        // Fetch member names from Firestore
        fetchMemberNames();

        // Set up RecyclerView for members
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new MemberAdapter(memberNames, position -> {
            if (position != 0) { // Prevent owner from being removed (assuming index 0 is owner)
                memberNames.remove(position);
                memberIDs.remove(position);
                memberAdapter.updateList(memberNames);
            }
        });
        membersRecyclerView.setAdapter(memberAdapter);

        // Handle save changes button click
        saveChangesButton.setOnClickListener(v -> saveGroupChanges());
    }

    private void fetchMemberNames() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        memberNames.clear(); // Clear in case of reload

        for (String memberID : memberIDs) {
            db.collection("users").document(memberID).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String name = documentSnapshot.getString("name");
                        if (name != null) {
                            memberNames.add(name);
                            memberAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this,
                            "Failed to fetch member details.",
                            Toast.LENGTH_SHORT).show());
        }
    }

    private void saveGroupChanges() {
        String updatedTitle = titleEditText.getText().toString().trim();
        String updatedDescription = descriptionEditText.getText().toString().trim();

        if (updatedTitle.isEmpty()) {
            titleEditText.setError("Title cannot be empty");
            titleEditText.requestFocus();
            return;
        }

        // Update group data in Firestore
        FirebaseFirestore.getInstance().collection("groups")
                .document(currentGroup.getGroupID())
                .update(
                        "name", updatedTitle,
                        "description", updatedDescription,
                        "memberIDs", memberIDs
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Group updated successfully!", Toast.LENGTH_SHORT).show();

                    // Send notifications to added/removed members if required
                    sendNotifications();

                    // Optional: setResult(RESULT_OK); // if you want to do something specific on return
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update group.", Toast.LENGTH_SHORT).show()
                );
    }

    private void sendNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Identify added and removed members
        List<String> originalMembers = currentGroup.getMemberIDs();
        List<String> addedMembers = new ArrayList<>(memberIDs);
        List<String> removedMembers = new ArrayList<>(originalMembers);

        addedMembers.removeAll(originalMembers); // Newly added
        removedMembers.removeAll(memberIDs);     // Removed

        for (String addedMember : addedMembers) {
            sendNotification(addedMember,
                    "You have been added to the group: " + currentGroup.getName());
        }

        for (String removedMember : removedMembers) {
            sendNotification(removedMember,
                    "You have been removed from the group: " + currentGroup.getName());
        }
    }

    private void sendNotification(String memberId, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String notificationId = db.collection("notifications").document().getId();

        db.collection("notifications").document(notificationId).set(new Notification(
                notificationId,
                "",
                "Group",
                message,
                new Date(),
                false
        )).addOnSuccessListener(aVoid ->
                db.collection("users")
                        .document(memberId)
                        .update("notificationIds", FieldValue.arrayUnion(notificationId))
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to send notification.", Toast.LENGTH_SHORT).show()
        );
    }

    private void addMemberByEmail() {
        String email = editMemberEmail.getText().toString().trim();
        if (email.isEmpty()) {
            editMemberEmail.setError("Email is required");
            editMemberEmail.requestFocus();
            return;
        }

        // Optional: Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editMemberEmail.setError("Invalid email format");
            editMemberEmail.requestFocus();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Found a matching user
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String userID = document.getString("userID");
                        String userName = document.getString("name");

                        if (userID == null || userName == null) {
                            Toast.makeText(this, "Invalid user data in Firestore", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Check if user is already in the group
                        if (memberIDs.contains(userID)) {
                            Toast.makeText(this, "User already added", Toast.LENGTH_SHORT).show();
                        } else {
                            // Add to your local lists
                            memberIDs.add(userID);
                            memberNames.add(userName);
                            memberAdapter.updateList(memberNames);

                            // Clear the input
                            editMemberEmail.setText("");
                            Toast.makeText(this, "Member added locally! Tap Save to confirm changes.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No user found with this email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EditGroupInfo", "Error fetching user by email", e);
                    Toast.makeText(this, "Failed to add member", Toast.LENGTH_SHORT).show();
                });
    }

}
