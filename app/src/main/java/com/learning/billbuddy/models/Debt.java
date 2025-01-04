package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.utils.DebtCallback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Debt {

    private String debtID; // Firestore Document ID
    private String fromUserID; // Reference to User.userID
    private String toUserID; // Reference to User.userID
    private BigDecimal amount;
    private String status; // Enum{"Pending", "Resolved"}
    private String groupID; // Reference to Group.groupID

    // Constructor
    public Debt(String debtID, String fromUserID, String toUserID, BigDecimal amount, String status, String groupID) {
        this.debtID = debtID;
        this.fromUserID = fromUserID;
        this.toUserID = toUserID;
        this.amount = amount;
        this.status = status;
        this.groupID = groupID;
    }

    // Getters and setters
    public String getDebtID() {
        return debtID;
    }

    public void setDebtID(String debtID) {
        this.debtID = debtID;
    }

    public String getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(String fromUserID) {
        this.fromUserID = fromUserID;
    }

    public String getToUserID() {
        return toUserID;
    }

    public void setToUserID(String toUserID) {
        this.toUserID = toUserID;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    // Method
    public void resolveDebt() {
        this.status = "Resolved";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("debts").document(this.debtID)
                .update("status", "Resolved")
                .addOnSuccessListener(aVoid -> Log.d("Debt Resolve", "Debt status successfully updated!"))
                .addOnFailureListener(e -> Log.e("Debt Resolve", "Error updating debt status", e));
    }

    public static void fetchAllDebts(final DebtCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("debts")
                .addSnapshotListener((value, error) -> {
                    List<Debt> result = new ArrayList<>();
                    if (error != null) {
                        Log.e("Debt Fetching", "Error listening to debt updates: " + error);
                        callback.onCallback(result);
                        return;
                    }

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new Debt(
                                    document.getId(),
                                    document.getString("fromUserID"),
                                    document.getString("toUserID"),
                                    document.get("amount") != null ?
                                            new BigDecimal(Objects.requireNonNull(document.get("amount")).toString()) : BigDecimal.ZERO,
                                    document.getString("status"),
                                    document.getString("groupID")
                            ));
                        }
                    }

                    callback.onCallback(result);
                });
    }

    public static void createDebt(String fromUserID, String toUserID, BigDecimal amount, String status, String groupID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new Debt object
        Map<String, Object> debtData = new HashMap<>();
        debtData.put("fromUserID", fromUserID);
        debtData.put("toUserID", toUserID);
        debtData.put("amount", amount.toString());
        debtData.put("status", status);
        debtData.put("groupID", groupID);

        // Add the new debt to the "debts" collection in Firestore
        db.collection("debts")
                .add(debtData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Debt Creation", "Debt created with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Debt Creation", "Error creating debt: ", e);
                });
    }
}
