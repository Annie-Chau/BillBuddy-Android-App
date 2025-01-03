package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
        // Add logic here to update the debt status in Firestore
    }

    public static List<Debt> fetchAllDebts() {
        List<Debt> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("debts")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Debt Fetching", "Error listening to debt updates: " + error);
                        return;
                    }

                    result.clear();

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
                });

        return result;
    }
}
