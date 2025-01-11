package com.learning.billbuddy.models;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.interfaces.IResponseCallback;
import com.learning.billbuddy.utils.GroupCallback;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class Group implements Serializable {

    private String groupID; // Firestore Document ID
    private String name;
    private String description;
    private String avatarURL; // URL from Firebase storage
    private String ownerID; // Reference to User.userID
    private List<String> memberIDs; // List of User.userIDs, minimum 2 including the admin
    private List<String> expenseIDs;
    private List<String> debtIds;

    @Nullable
    private Long createdDate;

    public Group() {
        // Default constructor for Firestore deserialization
    }

    // Constructor
    public Group(String groupID, String name, String description, String avatarURL, String ownerID, List<String> memberIDs, List<String> expenseIDs, List<String> debtIds, @Nullable Long createdDate) {
        this.groupID = groupID;
        this.name = name;
        this.description = description;
        this.avatarURL = avatarURL;
        this.ownerID = ownerID;
        this.memberIDs = memberIDs;
        this.expenseIDs = expenseIDs;
        this.debtIds = debtIds;
        this.createdDate = createdDate;
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

    public Long getCreatedDateLongFormat() {
        return createdDate;
    }

    @SuppressLint("SimpleDateFormat")
    public String getCreatedDateStringFormat() {
        //to this format December 12 2024
        if (this.createdDate == null) {
            return "";
        }

        if (new SimpleDateFormat("MMMM dd, yyyy").format(new Date(this.createdDate)).equals(new SimpleDateFormat("MMMM dd, yyyy").format(new Date(System.currentTimeMillis())))) {
            return "Today";
        }
        if (new SimpleDateFormat("MMMM dd, yyyy").format(new Date(this.createdDate)).equals(new SimpleDateFormat("MMMM dd, yyyy").format(new Date(System.currentTimeMillis() - 86400000)))) {
            return "Yesterday";
        }

        return new SimpleDateFormat("MMMM dd, yyyy").format(new Date(this.createdDate));
    }

    // Methods
    public void addMember(String memberID) {
        this.memberIDs.add(memberID);
        updateMembersInFirestore();
    }

    public void removeMember(String memberID) {
        this.memberIDs.remove(memberID);
        updateMembersInFirestore();
    }

    private void updateMembersInFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("groups").document(this.groupID)
                .update("memberIDs", this.memberIDs)
                .addOnSuccessListener(aVoid -> Log.d("Group Update", "Member list successfully updated!"))
                .addOnFailureListener(e -> Log.e("Group Update", "Error updating member list", e));
    }

    public void sendNotification(Notification notification) {
        // Add logic here to send a notification to group members
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

    public List<Chat> getGroupChat(List<Chat> allChats) {
        return allChats.stream()
                .filter(chat -> Objects.equals(chat.getGroupID(), this.groupID))
                .collect(Collectors.toList());
    }

    public static void fetchAllGroups(final GroupCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("groups")
                .addSnapshotListener((value, error) -> {
                    List<Group> result = new ArrayList<>();
                    if (error != null) {
                        Log.e("Group Fetching", "Error listening to group updates: " + error);
                        callback.onCallback(result);
                        return;
                    }

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
                                    document.getLong("createdDate")
                            ));
                        }
                    }

                    callback.onCallback(result);
                });
    }

    // Method to create a new group in Firestore
    public static void createGroup(String name, String description, String avatarURL, String ownerID, List<String> memberIDs, List<String> expenseIDs, List<String> debtIds, Long createdDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String groupID = db.collection("groups").document().getId();

        // Create a new Group object
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("groupID", groupID);
        groupData.put("name", name);
        groupData.put("description", description);
        groupData.put("avatarURL", avatarURL);
        groupData.put("ownerID", ownerID);
        groupData.put("memberIDs", memberIDs);
        groupData.put("expenseIDs", expenseIDs);
        groupData.put("debtIds", debtIds);
        groupData.put("createdDate", createdDate);

        // Add the new group to the "groups" collection in Firestore
        db.collection("groups")
                .document(groupID)
                .set(groupData)
                .addOnSuccessListener(aVoid -> Log.d("Group Creation", "Group created with ID: " + groupID))
                .addOnFailureListener(e -> Log.e("Group Creation", "Error creating group: ", e));
    }

    public List<String> getMemberNameList(List<User> userList) {
        return memberIDs.stream()
                .map(memberID -> userList.stream()
                        .filter(user -> user.getUserID().equals(memberID))
                        .findFirst()
                        .map(User::getName)
                        .orElse("Unknown")) // Or handle the case where the user is not found
                .collect(Collectors.toList());
    }

    public List<User> getMemberList(List<User> userList) {
        return memberIDs.stream()
                .map(memberID -> userList.stream()
                        .filter(user -> user.getUserID().equals(memberID))
                        .findFirst()
                        .orElse(null)) // Or handle the case where the user is not found
                .filter(Objects::nonNull) // Remove null elements if any
                .collect(Collectors.toList());
    }

    public boolean isQualifyForSearch(String query) {
        return name.toLowerCase().contains(query.toLowerCase());
    }

    public static class Reimbursement {
        private String payerId;
        private String payeeId;
        private Double amount = 0.0;

        public Reimbursement(String payerId, String payeeId, Double amount) {
            this.payerId = payerId;
            this.payeeId = payeeId;
            this.amount = amount;
        }

        public String getPayerId() {
            return payerId;
        }

        public void setPayerId(String payerId) {
            this.payerId = payerId;
        }

        public String getPayeeId() {
            return payeeId;
        }

        public void setPayeeId(String payeeId) {
            this.payeeId = payeeId;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "Reimbursement{" +
                    "payerId='" + payerId + '\'' +
                    ", payeeId='" + payeeId + '\'' +
                    ", amount=" + amount +
                    "}\n";
        }
    }

    public interface ReimbursementsCallback<T> {
        void onResult(T result);
    }

    public void getReimbursements(ReimbursementsCallback<List<Reimbursement>> callback) {
        List<Reimbursement> reimbursements = memberIDs.stream()
                .flatMap(memberId1 -> memberIDs.stream()
                        .filter(memberId2 -> !Objects.equals(memberId1, memberId2))
                        .map(memberId2 -> new Reimbursement(memberId1, memberId2, 0.0)))
                .collect(Collectors.toList());

        Expense.fetchAllExpenses(expenses -> {
            List<Expense> groupExpense = getGroupExpenses(expenses);

            groupExpense.forEach(expense -> {
                List<Map<String, Double>> splits = expense.getSplits();
                splits.forEach(split -> reimbursements.forEach(reimbursement -> {
                    if (reimbursement.getPayerId().equals(expense.getPayerID()) &&
                            expense.getParticipantIDs().contains(reimbursement.getPayeeId()) &&
                            split.get(reimbursement.getPayeeId()) != null) {
                        reimbursement.amount += split.get(reimbursement.getPayeeId());
                    }
                }));
            });

            callback.onResult(processReimbursements(reimbursements)); // Return the result in the callback
        });
    }


    public static List<Reimbursement> processReimbursements(List<Reimbursement> reimbursements) {
        List<Reimbursement> result = new ArrayList<>();
        // Extract unique participant IDs
        Set<String> participants = new HashSet<>();
        for (Reimbursement r : reimbursements) {
            participants.add(r.getPayerId());
            participants.add(r.getPayeeId());
        }

        List<String> ids = new ArrayList<>(participants);

        // Map participant IDs to indices
        Map<String, Integer> idToIndex = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            idToIndex.put(ids.get(i), i);
        }

        // Initialize total debt and credit arrays
        int n = ids.size();
        double[] totalDebt = new double[n];
        double[] totalCredit = new double[n];

        for (Reimbursement r : reimbursements) {
            int payerIndex = idToIndex.get(r.getPayerId());
            int payeeIndex = idToIndex.get(r.getPayeeId());
            totalDebt[payerIndex] += r.getAmount();
            totalCredit[payeeIndex] += r.getAmount();
        }

        // Calculate net amounts
        double[] netAmounts = new double[n];
        for (int i = 0; i < n; i++) {
            netAmounts[i] = totalDebt[i] - totalCredit[i];
        }

        System.out.println("Participant IDs:");
        for (String id : ids) {
            System.out.println(id);
        }

        System.out.println("\nNet Amounts:");
        System.out.println(Arrays.toString(netAmounts));

        System.out.println("\nDebt Settlements:");
        Log.d("Test", Arrays.toString(netAmounts) + ids);
        settleDebts(result, netAmounts, ids);
        return result;
    }

    private static void settleDebts(List<Reimbursement> result, double[] amounts, List<String> ids) {
        int maxDebtorIndex = 0;
        int maxCreditorIndex = 0;

        for (int i = 0; i < amounts.length; i++) {
            if (amounts[i] > amounts[maxDebtorIndex]) maxDebtorIndex = i;
            if (amounts[i] < amounts[maxCreditorIndex]) maxCreditorIndex = i;
        }

        if (amounts[maxDebtorIndex] == 0 && amounts[maxCreditorIndex] == 0) return;

        double settlement = Math.min(amounts[maxDebtorIndex], -amounts[maxCreditorIndex]);

        if (settlement == 0) return;
        amounts[maxDebtorIndex] -= settlement;
        amounts[maxCreditorIndex] += settlement;

        Log.d("Test", ids.get(maxDebtorIndex) + " owes " + ids.get(maxCreditorIndex) + " " + settlement);
        result.add(new Reimbursement(ids.get(maxDebtorIndex), ids.get(maxCreditorIndex), settlement));

        settleDebts(result, amounts, ids);
    }

    public static void deleteGroup(Group group, IResponseCallback deleteGroupCallBack) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("groups").document(group.getGroupID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Group Deletion", "Group deleted successfully");
                    deleteGroupCallBack.onSuccess();
                })
                .addOnFailureListener(e -> {
                            Log.e("Group Deletion", "Error deleting group", e);
                            deleteGroupCallBack.onFailure(e);
                        }
                );
    }

}
