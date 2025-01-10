package com.learning.billbuddy;

import static com.learning.billbuddy.models.Group.createGroup;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.models.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddGroupActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button createButton;
    private Button cancelButton;
    private ImageButton emojiButton;
    private Button addMemberButton;
    private EditText memberEmailEditText;
    // Owner ID (current logged-in user)
    private String ownerID;
    private TextView membersListTextView;
    private List<String> memberIDs = new ArrayList<>(); // List to store group members

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        titleEditText = findViewById(R.id.add_group_enter_title);
        descriptionEditText = findViewById(R.id.add_expense_description);
        createButton = findViewById(R.id.add_group_btn_add);
        cancelButton = findViewById(R.id.add_group_cancel_button);
        emojiButton = findViewById(R.id.add_group_btn_emoji);
        addMemberButton = findViewById(R.id.add_member_button);
        memberEmailEditText = findViewById(R.id.add_member_email);
        membersListTextView = findViewById(R.id.members_list);

        // Retrieve the current logged-in user's ID
        ownerID = getIntent().getStringExtra("OWNER_ID"); // pass this from the previous activity
        if (ownerID == null || ownerID.isEmpty()) {
            Log.e("AddGroupActivity", "Owner ID is null");
            Toast.makeText(this, "Error: User not logged in!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Add the owner to the group members list
        memberIDs.add(ownerID);
        db.collection("users").document(ownerID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String ownerName = documentSnapshot.getString("name");
                    if (!TextUtils.isEmpty(ownerName)) {
                        memberNames.add(ownerName);
                    } else {
                        memberNames.add("Owner"); // Fallback if name is not available
                    }
                    updateMembersList();
                });


        createButton.setOnClickListener(v -> createGroup());
        cancelButton.setOnClickListener(v -> finish());
        addMemberButton.setOnClickListener(v -> addMember());

    }

    // Method to add a new member
    private List<String> memberNames = new ArrayList<>();
    private void addMember(){
        String email = memberEmailEditText.getText().toString().trim().toLowerCase();
        // Validate email
        if (TextUtils.isEmpty(email)){
            memberEmailEditText.setError("Email is required");
            memberEmailEditText.requestFocus();
            return;
        }

        // Check if Firestore has the email
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()){
                        // Get the user's ID from the doc
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String userID = document.getString("userID");
                        String userName = document.getString("name");

                        if (memberIDs.contains(userID)){
                            Toast.makeText(this, "User already added", Toast.LENGTH_LONG).show();
                        } else {
                            memberIDs.add(userID);
                            memberNames.add(userName);
                            updateMembersList();
                            Toast.makeText(this, "User added successfully", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("AddMember", "No user found with email: " + email); // for debugging
                        Toast.makeText(this, "No user found with this email", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to add member", Toast.LENGTH_SHORT).show());
    }

    // Update the list of members
    private void updateMembersList(){
        StringBuilder membersText = new StringBuilder("Current members:");
        for(int i = 0; i < memberIDs.size(); i++){
            if(i == 0){
                membersText.append("\n- Owner");
            }else{
                membersText.append("\n- ").append(memberNames.get(i));
            }
        }
        membersListTextView.setText(membersText.toString());
    }

    // Method to create a new group
    private void createGroup(){
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(title)){
            titleEditText.setError("Group Title is required");
            titleEditText.requestFocus();
            return;
        }

        if (memberIDs.size() < 2) { // A group must have at least two members
            Toast.makeText(this, "A group must have at least two members", Toast.LENGTH_LONG).show();
            return;
        }

        // Create the group in Firestore
        Group.createGroup(
                title,
                description.isEmpty() ? "No description provided" : description,
                "", // Avatar URL placeholder
                ownerID,
                memberIDs,
                new ArrayList<>(), // Expense IDs
                new ArrayList<>()  // Debt IDs
        );

        Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}