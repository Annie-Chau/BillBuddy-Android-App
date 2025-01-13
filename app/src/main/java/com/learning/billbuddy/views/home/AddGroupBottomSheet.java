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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.MemberAdapter;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.Notification;
import com.learning.billbuddy.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AddGroupBottomSheet extends BottomSheetDialogFragment implements MemberAdapter.OnRemoveClickListener {

    private FirebaseFirestore db;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button createButton;
    private Button cancelButton;
    private ImageButton emojiButton;
    private Button addMemberButton;
    private EditText memberEmailEditText;
    private RecyclerView membersRecyclerView;
    private MemberAdapter memberAdapter;
    private List<String> memberIDs = new ArrayList<>(); // List to store member IDs
    private List<String> memberNames = new ArrayList<>(); // List to store member names

    // Owner ID (current logged-in user)
    private String ownerID;

    public AddGroupBottomSheet() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.activity_add_group, container, false);

        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        titleEditText = view.findViewById(R.id.add_group_enter_title);
        descriptionEditText = view.findViewById(R.id.add_group_description); // Updated ID
        createButton = view.findViewById(R.id.add_group_btn_add);
        cancelButton = view.findViewById(R.id.add_group_cancel_button);
        emojiButton = view.findViewById(R.id.add_group_btn_emoji);
        addMemberButton = view.findViewById(R.id.add_member_button);
        memberEmailEditText = view.findViewById(R.id.add_member_email);
        membersRecyclerView = view.findViewById(R.id.members_recycler_view);

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

        // Initialize RecyclerView
        memberAdapter = new MemberAdapter(memberNames, this);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        membersRecyclerView.setAdapter(memberAdapter);
        membersRecyclerView.setHasFixedSize(true);

        // Set click listeners
        createButton.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

            User.fetchAllUsers(users -> {
                users.stream()
                        .filter(user -> Objects.equals(user.getUserID(), currentUserId))
                        .findFirst()
                        .ifPresent(user -> {
                            if (!user.isPremium()) {
                                user.getNumberOfGroupPossessed(count -> {
                                    if (count < 6) {
                                        createGroup();
                                    }
                                    else Toast.makeText(v.getContext(), "You can only create up to 6 groups as a free user. Upgrade to Premium for unlimited groups.", Toast.LENGTH_LONG).show();
                                });
                            }
                        });
            });
        });

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
                    memberAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    memberNames.add("Owner"); // Default fallback
                    memberAdapter.notifyDataSetChanged();
                });
    }

    private void addMember() {
        String email = memberEmailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            memberEmailEditText.setError("Email is required");
            memberEmailEditText.requestFocus();
            return;
        }

        // Optional: Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            memberEmailEditText.setError("Invalid email format");
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
                            memberAdapter.notifyItemInserted(memberNames.size() - 1);
                            memberEmailEditText.setText(""); // Clear the EditText
                            Toast.makeText(requireContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "No user found with this email", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AddGroupFragment", "Error fetching user by email", e);
                    Toast.makeText(requireContext(), "Failed to add member", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRemoveClick(int position) {
        if (position > 0) { // Ensure the owner cannot be removed
            String removedMemberID = memberIDs.remove(position);
            String removedMemberName = memberNames.remove(position);
            memberAdapter.notifyItemRemoved(position);
            Toast.makeText(requireContext(), "Removed: " + removedMemberName, Toast.LENGTH_SHORT).show();
            // Optional: Handle any additional cleanup, such as removing notifications
        }
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

        // Create the group (Assuming Group.createGroup handles Firestore operations)
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