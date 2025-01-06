package com.learning.billbuddy.views.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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

        logAllUsers();
        ImageButton addGroupButton = view.findViewById(R.id.to_add_group_btn);
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    Log.d("HomePage", "No authenticated user found");
                    return;
                }
                String firebaseAuthID = currentUser.getUid();
                Log.d("HomePage", "Current Firebase Auth ID: " + firebaseAuthID);
                db.collection("users").whereEqualTo("userID", firebaseAuthID).get().addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.d("HomePage", "get failed with ", task.getException());
                        return;
                    }

                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Log.d("HomePage", "No matching documents found");
                        return;
                    }

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        User user = document.toObject(User.class);
                        if (user != null) {
                            Log.d("HomePage", "User found: " + user.getName());
                            AddGroupBottomSheetDialog bottomSheet = new AddGroupBottomSheetDialog();
                            Bundle args = new Bundle();
                            args.putString("OWNER_ID", user.getUserID());
                            args.putString("OWNER_NAME", user.getName());
                            bottomSheet.setArguments(args);
                            bottomSheet.show(requireActivity().getSupportFragmentManager(), "AddGroupBottomSheetDialog");
                        }
                    }
                });
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