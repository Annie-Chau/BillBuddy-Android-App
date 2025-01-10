package com.learning.billbuddy.views.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.Notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AddGroupBottomSheet extends BottomSheetDialogFragment {

    private FirebaseFirestore db;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button createButton;
    private Button cancelButton;
    private ImageButton emojiButton;
    private Button addMemberButton;
    private EditText memberEmailEditText;
    private TextView membersListTextView;
    private List<String> memberIDs = new ArrayList<>(); // List to store member IDs
    private List<String> memberNames = new ArrayList<>(); // List to store member names

    // Owner ID (current logged-in user)
    private String ownerID;

    public AddGroupBottomSheet() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.activity_add_group, container, false);

        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        titleEditText = view.findViewById(R.id.add_group_enter_title);
        descriptionEditText = view.findViewById(R.id.add_expense_description);
        createButton = view.findViewById(R.id.add_group_btn_add);
        cancelButton = view.findViewById(R.id.add_group_cancel_button);
        emojiButton = view.findViewById(R.id.add_group_btn_emoji);
        addMemberButton = view.findViewById(R.id.add_member_button);
        memberEmailEditText = view.findViewById(R.id.add_member_email);
        membersListTextView = view.findViewById(R.id.members_list);

        // Retrieve the current logged-in user's ID from arguments
        if (getArguments() != null) {
            ownerID = getArguments().getString("OWNER_ID");
        }

        if (ownerID == null || ownerID.isEmpty()) {
            Log.e("AddGroupFragment", "Owner ID is null");
            Toast.makeText(requireContext(), "Error: User not logged in!", Toast.LENGTH_LONG).show();
            dismiss();
            return view;
        }

        // Add the owner to the group members list
        memberIDs.add(ownerID);
        fetchOwnerName();

        // Set click listeners
        createButton.setOnClickListener(v -> createGroup());
        addMemberButton.setOnClickListener(v -> addMember());
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void fetchOwnerName() {
        db.collection("users").document(ownerID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String ownerName = documentSnapshot.getString("name");
                    if (!TextUtils.isEmpty(ownerName)) {
                        memberNames.add(ownerName);
                    } else {
                        memberNames.add("Owner"); // Fallback if name is not available
                    }
                    updateMembersList();
                })
                .addOnFailureListener(e -> {
                    memberNames.add("Owner"); // Default fallback
                    updateMembersList();
                });
    }

    private void addMember() {
        String email = memberEmailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            memberEmailEditText.setError("Email is required");
            memberEmailEditText.requestFocus();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String userID = document.getString("userID");
                        String userName = document.getString("name");

                        if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
                            Toast.makeText(requireContext(), "Invalid user data. Please check Firestore.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (memberIDs.contains(userID)) {
                            Toast.makeText(requireContext(), "User already added", Toast.LENGTH_SHORT).show();
                        } else {
                            memberIDs.add(userID);
                            memberNames.add(userName); // Add the user's name to the list
                            updateMembersList();
                            memberEmailEditText.setText(""); // Clear the EditText
                            Toast.makeText(requireContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "No user found with this email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to add member", Toast.LENGTH_SHORT).show());
    }

    private void updateMembersList() {
        StringBuilder membersText = new StringBuilder("Current members:");
        for (int i = 0; i < memberNames.size(); i++) {
            if (i == 0) {
                membersText.append("\n- Owner (").append(memberNames.get(i)).append(")");
            } else {
                membersText.append("\n- ").append(memberNames.get(i));
            }
        }
        membersListTextView.setText(membersText.toString());
    }

    private void createGroup() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("Group Title is required");
            titleEditText.requestFocus();
            return;
        }

        if (memberIDs.size() < 2) {
            Toast.makeText(requireContext(), "A group must have at least two members", Toast.LENGTH_LONG).show();
            return;
        }

        Group.createGroup(
                title,
                description.isEmpty() ? "No description provided" : description,
                "",
                ownerID,
                memberIDs,
                new ArrayList<>(),
                new ArrayList<>(),
                System.currentTimeMillis()
        );

        // Create notifications for all members except the owner
        Log.d("AddGroupFragment", "Member IDs: " + memberIDs.toString());
        for (String memberID : memberIDs) {
            if (!memberID.equals(ownerID)) {
                Log.d("AddGroupFragment", "Creating notification for member: " + memberID);
                createNotificationForMember(memberID, title);
            } else {
                Log.d("AddGroupFragment", "Skipping notification for owner: " + memberID);
            }
        }

        Toast.makeText(requireContext(), "Group created successfully!", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    // Method to create a notification for a member
    private void createNotificationForMember(String memberID, String groupName) {
        String notificationID = UUID.randomUUID().toString();
        String messageID = ""; // Assuming you have a message ID
        String type = "Group";
        String message = "You have been added to the group: " + groupName;
        Date timestamp = new Date();
        boolean isRead = false;

        Notification notification = new Notification(notificationID, messageID, type, message, timestamp, isRead);

        Log.d("AddGroupFragment", "Creating notification for member: " + memberID);
        db.collection("notifications").document(notificationID).set(notification)
                .addOnSuccessListener(aVoid -> {
                    Log.d("AddGroupFragment", "Notification created successfully for member: " + memberID);
                    // Add the notification ID to the user's document
                    db.collection("users").document(memberID)
                            .update("notificationIds", FieldValue.arrayUnion(notificationID))
                            .addOnSuccessListener(aVoid2 -> Log.d("AddGroupFragment", "Notification ID added to user: " + memberID))
                            .addOnFailureListener(e -> Log.e("AddGroupFragment", "Error adding notification ID to user", e));
                })
                .addOnFailureListener(e -> Log.e("AddGroupFragment", "Error creating notification", e));
    }
}