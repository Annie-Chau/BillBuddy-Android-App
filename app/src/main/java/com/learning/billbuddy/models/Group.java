package com.learning.billbuddy.models;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Group {

    private String groupID; // Firestore Document ID
    private String name;
    private String description;
    private String avatarURL; // URL from Firebase storage
    private String ownerID; // Reference to User.userID
    private List<String> memberIDs; // List of User.userIDs, minimum 2 including the admin
    private List<String> expenseIDs;
    private List<String> debtIds;
    private List<String> chatIds;


    // Constructor
    public Group(String groupID, String name, String description, String avatarURL, String ownerID, List<String> memberIDs, List<String> expenseIDs, List<String> debtIds, List<String> chatIds) {
        this.groupID = groupID;
        this.name = name;
        this.description = description;
        this.avatarURL = avatarURL;
        this.ownerID = ownerID;
        this.memberIDs = memberIDs;
        this.expenseIDs = expenseIDs;
        this.debtIds = debtIds;
        this.chatIds = chatIds;
    }

    // Getters and setters
    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public List<String> getMemberIDs() {
        return memberIDs;
    }

    public void setMemberIDs(List<String> memberIDs) {
        this.memberIDs = memberIDs;
    }

    public List<String> getExpenseIDs() {
        return expenseIDs;
    }

    public void setExpenseIDs(List<String> expenseIDs) {
        this.expenseIDs = expenseIDs;
    }

    public List<String> getDebtIds() {
        return debtIds;
    }

    public void setDebtIds(List<String> debtIds) {
        this.debtIds = debtIds;
    }

    public List<String> getChatIds() {
        return chatIds;
    }

    public void setChatIds(List<String> chatIds) {
        this.chatIds = chatIds;
    }

    // Methods
    public void addMember(String memberID) {
        this.memberIDs.add(memberID);
        // Add logic here to update the group in Firestore
    }

    public void removeMember(String memberID) {
        this.memberIDs.remove(memberID);
        // Add logic here to update the group in Firestore
    }

    public void createExpense(Expense expense) {
        // Add logic here to create an Expense and associate it with this group in Firestore
    }

    public void sendNotification(Notification notification) {
        // Add logic here to send a notification to group members
    }

    public static List<Group> fetchAllGroups() {
        List<Group> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("groups")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Group Fetching", "Error listening to group updates: " + error);
                        return;
                    }

                    result.clear();

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new Group(
                                    document.getId(),
                                    document.getString("name"),
                                    document.getString("description"),
                                    document.getString("avatarURL"),
                                    document.getString("ownerID"),
                                    (List<String>) document.get("memberIDs"),
                                    (List<String>) document.get("expenseIDs"),
                                    (List<String>) document.get("debtIds"),
                                    (List<String>) document.get("chatIds")
                            ));
                        }
                    }
                });

        return result;
    }

    public List<User> getGroupMembers(List<User> allUsers) {
        return allUsers.stream()
                .filter(user -> this.memberIDs.contains(user.getUserID()))
                .collect(Collectors.toList());
    }

    public List<Expense> getGroupExpenses(List<Expense> allExpenses) {
        return allExpenses.stream()
                .filter(expense -> this.expenseIDs.contains(expense.getExpenseID()))
                .collect(Collectors.toList());
    }

    public List<Debt> getGroupDebts(List<Debt> allDebts) {
        return allDebts.stream()
                .filter(debt -> this.debtIds.contains(debt.getDebtID()))
                .collect(Collectors.toList());
    }

    public List<Chat> getGroupChats(List<Chat> allChats) {
        return allChats.stream()
                .filter(chat -> this.chatIds.contains(chat.getChatID()))
                .collect(Collectors.toList());
    }
}
