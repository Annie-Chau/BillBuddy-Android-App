package com.learning.billbuddy.views.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.GroupAdapter;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomePage extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView groupRecyclerView;
    private GroupAdapter groupAdapter;
    private List<Group> groupList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_page, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        groupRecyclerView = view.findViewById(R.id.group_list);

        // Setup RecyclerView
        groupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(requireContext(), groupList);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        groupRecyclerView.setAdapter(groupAdapter);

        setupRealTimeGroupUpdates();

        ImageButton addParticipantButton = view.findViewById(R.id.to_add_group_btn);
        addParticipantButton.setOnClickListener(v -> openAddGroupDialog());

        return view;
    }

    private void openAddGroupDialog() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d("HomePage", "No authenticated user found");
            return;
        }

        String firebaseAuthID = currentUser.getUid();
        db.collection("users").whereEqualTo("userID", firebaseAuthID).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d("HomePage", "get failed with ", task.getException());
                return;
            }

            if (task.getResult() != null && !task.getResult().isEmpty()) {
                for (DocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    if (user != null) {
                        AddGroupBottomSheetDialog bottomSheet = new AddGroupBottomSheetDialog();
                        Bundle args = new Bundle();
                        args.putString("OWNER_ID", user.getUserID());
                        args.putString("OWNER_NAME", user.getName());
                        bottomSheet.setArguments(args);
                        bottomSheet.show(requireActivity().getSupportFragmentManager(), "AddGroupBottomSheetDialog");
                    }
                }
            } else {
                Log.d("HomePage", "No matching user documents found");
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupRealTimeGroupUpdates() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("HomePage", "No authenticated user found for real-time updates");
            return;
        }

        String currentUserId = currentUser.getUid();

        Group.fetchAllGroups(groups -> {
            groupList = groups.stream()
                    .filter(group -> group.getMemberIDs() != null && group.getMemberIDs().contains(currentUserId))
                    .collect(Collectors.toList());
            groupAdapter.groupList = groupList;
            groupAdapter.notifyDataSetChanged();
            Log.d("HomePage", "Groups updated in real-time. Total groups: " + groupList.size());
        });
    }
}