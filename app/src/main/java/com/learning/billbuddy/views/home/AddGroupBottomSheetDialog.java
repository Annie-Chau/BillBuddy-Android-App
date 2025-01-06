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
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.Group;

import java.util.ArrayList;
import java.util.List;

public class AddGroupBottomSheetDialog extends BottomSheetDialogFragment {

    private FirebaseFirestore db;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button createButton;
    private Button cancelButton;
    private ImageButton emojiButton;
    private Button addMemberButton;
    private EditText memberEmailEditText;
    private TextView membersListTextView;
    private List<String> memberIDs = new ArrayList<>(); // List to store group members

    // Owner ID (current logged-in user)
    private String ownerID;


    public AddGroupBottomSheetDialog() {
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
        if (getContext() != null) {
            ownerID = getArguments().getString("OWNER_ID");
        }

        if (ownerID == null || ownerID.isEmpty()) {
            Log.e("AddGroupFragment", "Owner ID is null");
            Toast.makeText(requireContext(), "Error: User not logged in!", Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed(); // Navigate back if the owner ID is null
            return view;
        }

        // Add the owner to the group members list
        memberIDs.add(ownerID);
        // Update the members list in the UI
        updateMembersList();

        // Set click listeners
        createButton.setOnClickListener(v -> createGroup());
        addMemberButton.setOnClickListener(v -> addMember());
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    // Method to add a new member
    private void addMember() {
        String email = memberEmailEditText.getText().toString().trim();
        // Validate email
        if (TextUtils.isEmpty(email)) {
            memberEmailEditText.setError("Email is required");
            memberEmailEditText.requestFocus();
            return;
        }

        // Check if Firestore has the email
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Get the user's ID from the document
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        String userID = document.getString("userID");

                        if (memberIDs.contains(userID)) {
                            Toast.makeText(requireContext(), "User already added", Toast.LENGTH_LONG).show();
                        } else {
                            memberIDs.add(userID);
                            updateMembersList();
                            Toast.makeText(requireContext(), "User added successfully", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "No user found with this email", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to add member", Toast.LENGTH_SHORT).show());
    }

    // Update the list of members
    private void updateMembersList() {
        StringBuilder membersText = new StringBuilder("Current members:");
        for (int i = 0; i < memberIDs.size(); i++) {
            if (i == 0) {
                membersText.append("\n- Owner");
            } else {
                membersText.append("\n- Member ").append(i);
            }
        }
        membersListTextView.setText(membersText.toString());
    }

    // Method to create a new group
    private void createGroup() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(title)) {
            titleEditText.setError("Group Title is required");
            titleEditText.requestFocus();
            return;
        }

        if (memberIDs.size() < 2) { // A group must have at least two members
            Toast.makeText(requireContext(), "A group must have at least two members", Toast.LENGTH_LONG).show();
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

        Toast.makeText(requireContext(), "Group created successfully!", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
