package com.learning.billbuddy.views.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.learning.billbuddy.AddGroupActivity;
import com.learning.billbuddy.R;
import com.learning.billbuddy.models.User;

public class HomePage extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_page, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        logAllUsers(); // Log all user documents

        ImageButton addParticipantButton = view.findViewById(R.id.to_add_group_btn);
        addParticipantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String firebaseAuthID = currentUser.getUid();
                    Log.d("HomePage", "Current Firebase Auth ID: " + firebaseAuthID);
                    db.collection("users").whereEqualTo("userID", firebaseAuthID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    User user = document.toObject(User.class);
                                    if (user != null) {
                                        Log.d("HomePage", "User found: " + user.getName());
                                        Intent intent = new Intent(requireActivity(), AddGroupActivity.class);
                                        intent.putExtra("OWNER_ID", user.getUserID());
                                        intent.putExtra("OWNER_NAME", user.getName());
                                        startActivity(intent);
                                        Log.d("HomePage", "Starting AddGroupActivity");
                                    }
                                }
                            } else {
                                Log.d("HomePage", "No matching documents found");
                            }
                        } else {
                            Log.d("HomePage", "get failed with ", task.getException());
                        }
                    });
                } else {
                    Log.d("HomePage", "No authenticated user found");
                }
            }
        });

        return view;
    }

    private void logAllUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("HomePage", "User Document: " + document.getData());
                }
            } else {
                Log.d("HomePage", "Error getting documents: ", task.getException());
            }
        });
    }
}