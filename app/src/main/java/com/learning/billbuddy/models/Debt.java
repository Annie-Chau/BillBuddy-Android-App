package com.learning.billbuddy.models;

import java.math.BigDecimal;

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
}
