package com.learning.billbuddy.views.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.learning.billbuddy.R;
import com.learning.billbuddy.adapters.GroupAdapter;
import com.learning.billbuddy.models.Group;
import com.learning.billbuddy.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HomePage extends Fragment {

    private enum FilterOption {
        FILTER("Filter"),
        DATE_ASCENDING("Date Ascending"),
        DATE_DESCENDING("Date Descending"),
        REIMBURSEMENT_AVAILABLE("Reimbursement Available");

        private final String filterOption;

        FilterOption(String filterOption) {
            this.filterOption = filterOption;
        }

        public String getFilterOption() {
            return filterOption;
        }
    }

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView groupRecyclerView;
    private GroupAdapter groupAdapter;

    private EditText searchGroupEditText;

    private Spinner filterSelection;

    private ArrayList<Group> prevGroupList;
    private ArrayList<Group> currentGroupList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_page, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        groupRecyclerView = view.findViewById(R.id.group_list);
        searchGroupEditText = view.findViewById(R.id.home_view_search_text_field);
        filterSelection = view.findViewById(R.id.home_spinner_filter_selection);

        // Setup RecyclerView
        prevGroupList = new ArrayList<>();
        currentGroupList = new ArrayList<>();
        groupAdapter = new GroupAdapter(requireContext(), currentGroupList);
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        groupRecyclerView.setAdapter(groupAdapter);

        setupRealTimeGroupUpdates();

        ImageButton addParticipantButton = view.findViewById(R.id.to_add_group_btn);
        addParticipantButton.setOnClickListener(v -> openAddGroupDialog());
        this.handleSearchGroupEditText();

        filterSelection.setDropDownVerticalOffset(150);
        filterSelection.setAdapter(new FilterSelectionAdapter(requireContext()));
        filterSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Not needed
            }
        });

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
                        AddGroupBottomSheet bottomSheet = new AddGroupBottomSheet();
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

    private void setupRealTimeGroupUpdates() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e("HomePage", "No authenticated user found for real-time updates");
            return;
        }

        String currentUserId = currentUser.getUid();

        Group.fetchAllGroups(groups -> {
            currentGroupList = groups.stream()
                    .filter(group -> group.getMemberIDs() != null && group.getMemberIDs().contains(currentUserId))
                    .sorted((group1, group2) -> group2.getCreatedDateLongFormat().compareTo(group1.getCreatedDateLongFormat()))
                    .collect(Collectors.toCollection(ArrayList::new));

            if (prevGroupList.isEmpty()) {
                groupAdapter.groupList = currentGroupList;
                groupAdapter.notifyDataSetChanged();
            } else {
                if (prevGroupList.size() < currentGroupList.size()) {
                    //perform added
                    groupAdapter.groupList.add(0, currentGroupList.get(0));
                    groupAdapter.notifyItemInserted(0);
                    if (currentGroupList.size() > 1) {
                        groupAdapter.notifyItemChanged(1);
                    }
                    groupRecyclerView.scrollToPosition(0);

                } else if (prevGroupList.size() > currentGroupList.size()) {
                    //perform delete
                    //find the group exist in groupList but not in currentGroupList

                    List<Group> groupsToRemove = new ArrayList<>();

                    groupAdapter.groupList.forEach(group -> {
                        boolean existsInCurrentGroupList = currentGroupList.stream()
                                .anyMatch(item -> item.getGroupID().equals(group.getGroupID()));

                        if (!existsInCurrentGroupList) {
                            Log.d("HomePage", "Found item being removed " + group.getName());
                            groupsToRemove.add(group); // Mark group for removal
                        }
                    });

                    groupsToRemove.forEach(group -> {
                        int index = groupAdapter.groupList.indexOf(group);
                        if (index != -1) {
                            groupAdapter.groupList.remove(index);
                            groupAdapter.notifyItemRemoved(index);
                        }
                        if (index < groupAdapter.groupList.size() - 2) { //case delete top and net want need to show date
                            groupAdapter.notifyItemChanged(index + 1);
                        }
                    });

                } else {
                    return;
                }
            }
            prevGroupList = currentGroupList;
            Log.d("HomePage", "Groups updated in real-time. Total groups: " + currentGroupList.size());
        });



    }

//    /**
//     * Replaces the custom "fetchAllGroups" logic with a Firestore real-time listener.
//     */
//    @SuppressLint("NotifyDataSetChanged")
//    private void setupRealTimeGroupUpdates() {
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser == null) {
//            Log.e("HomePage", "No authenticated user found for real-time updates");
//            return;
//        }
//
//        String currentUserId = currentUser.getUid();
//
//        // Real-time listener on groups that contain the current user in their memberIDs
//        db.collection("groups")
//                .whereArrayContains("memberIDs", currentUserId)
//                .addSnapshotListener((querySnapshot, e) -> {
//                    if (e != null) {
//                        Log.w("HomePage", "Listen failed.", e);
//                        return;
//                    }
//                    if (querySnapshot != null) {
//                        List<Group> updatedGroups = new ArrayList<>();
//                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
//                            Group group = doc.toObject(Group.class);
//                            if (group != null) {
//                                updatedGroups.add(group);
//                            }
//                        }
//                        // Update current list and re-apply any search/filter logic
//                        this.currentGroupList = (ArrayList<Group>) updatedGroups;
//                        onSearch();  // Will handle both search filtering and the user’s filter choice
//                        Log.d("HomePage", "Groups updated in real-time. Size: " + updatedGroups.size());
//                    }
//                });
//        Group.fetchAllGroups(groups -> {
//            currentGroupList = groups.stream()
//                    .filter(group -> group.getMemberIDs() != null && group.getMemberIDs().contains(currentUserId))
//                    .sorted((group1, group2) -> group2.getCreatedDateLongFormat().compareTo(group1.getCreatedDateLongFormat()))
//                    .collect(Collectors.toCollection(ArrayList::new));
//
//            if (prevGroupList.isEmpty()) {
//                groupAdapter.groupList = currentGroupList;
//                groupAdapter.notifyDataSetChanged();
//            } else {
//                if (prevGroupList.size() < currentGroupList.size()) {
//                    //perform added
//                    groupAdapter.groupList.add(0, currentGroupList.get(0));
//                    groupAdapter.notifyItemInserted(0);
//                    if (currentGroupList.size() > 1) {
//                        groupAdapter.notifyItemChanged(1);
//                    }
//                    groupRecyclerView.scrollToPosition(0);
//
//                } else if (prevGroupList.size() > currentGroupList.size()) {
//                    //perform delete
//                    //find the group exist in groupList but not in currentGroupList
//
//                    List<Group> groupsToRemove = new ArrayList<>();
//
//                    groupAdapter.groupList.forEach(group -> {
//                        boolean existsInCurrentGroupList = currentGroupList.stream()
//                                .anyMatch(item -> item.getGroupID().equals(group.getGroupID()));
//
//                        if (!existsInCurrentGroupList) {
//                            Log.d("HomePage", "Found item being removed " + group.getName());
//                            groupsToRemove.add(group); // Mark group for removal
//                        }
//                    });
//
//                    groupsToRemove.forEach(group -> {
//                        int index = groupAdapter.groupList.indexOf(group);
//                        if (index != -1) {
//                            groupAdapter.groupList.remove(index);
//                            groupAdapter.notifyItemRemoved(index);
//                        }
//                        if (index < groupAdapter.groupList.size() - 2) { //case delete top and net want need to show date
//                            groupAdapter.notifyItemChanged(index + 1);
//                        }
//                    });
//
//                } else {
//                    //perform update
//                    for (int i = 0; i < currentGroupList.size(); i++) {
//                        if (currentGroupList.get(i).isDifferentByContent(groupAdapter.groupList.get(i))) {
//                            groupAdapter.groupList.set(i, currentGroupList.get(i));
//                            groupAdapter.notifyItemChanged(i);
//                        }
//                    }
//                }
//            }
//            prevGroupList = currentGroupList;
//            Log.d("HomePage", "Groups updated in real-time. Total groups: " + currentGroupList.size());
//        });
//    }

    private void handleSearchGroupEditText() {
        searchGroupEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                onSearch();
            }
        });
    }

    private void onFilter() {
        String filterOption = filterSelection.getSelectedItem().toString();
        Log.d("HomePage", "Filter option selected: " + filterOption);
        // Filter the groupList based on the filter option
        switch (filterOption) {
            case "Date Ascending":
                groupAdapter.groupList.sort((group1, group2) -> group1.getCreatedDateLongFormat().compareTo(group2.getCreatedDateLongFormat()));
                break;
            case "Date Descending":
                groupAdapter.groupList.sort((group1, group2) -> group2.getCreatedDateLongFormat().compareTo(group1.getCreatedDateLongFormat()));
                break;
            case "Reimbursement Available":
                //TODO: Implement this
                groupAdapter.groupList = currentGroupList;
                break;
            default:
                break;
        }
        groupAdapter.notifyDataSetChanged();
    }

    private void onSearch() {
        String searchQuery = searchGroupEditText.getText().toString().toLowerCase();
        Log.d("HomePage", "before serach y: " + currentGroupList.size());

        if (searchQuery.trim().isEmpty()) {
            groupAdapter.groupList = currentGroupList;
        } else {
            groupAdapter.groupList = currentGroupList.stream()
                    .filter(group -> group.isQualifyForSearch(searchQuery))
                    .collect(Collectors.toList());
        }
        onFilter();
    }
}