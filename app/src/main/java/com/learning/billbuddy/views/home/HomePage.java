package com.learning.billbuddy.views.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

        Button addParticipantButton = view.findViewById(R.id.Navigate_to_add_group);
        addParticipantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userID = currentUser.getUid();
                    Log.d("HomePage", "Current User ID: " + userID);
                    db.collection("users").document(userID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                User user = document.toObject(User.class);
                                if (user != null) {
                                    Intent intent = new Intent(getActivity(), AddGroupActivity.class);
                                    intent.putExtra("OWNER_ID", user.getUserID());
                                    intent.putExtra("OWNER_NAME", user.getName());
                                    startActivity(intent);
                                }
                            } else {
                                Log.d("HomePage", "No such document");
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
}