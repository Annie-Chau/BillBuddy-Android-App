package com.learning.billbuddy.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Expense {

    private String expenseID; // Firestore Document ID
    private String title;
    private String avatarURL; // URL from Firebase storage
    private Double amount;
    private String notes;
    private String billPictureURL; // URL from Firebase storage
    private String payerID; // Reference to User.userID
    private List<String> participantIDs; // List of User.userIDs
    private Map<String, Double> splits; // Key: User.userID, Value: Amount
    private Date timestamp;

    // Constructor
    public Expense(String expenseID, String title, String avatarURL, Double amount, String notes,
                   String billPictureURL, String payerID, List<String> participantIDs,
                   Map<String, Double> splits, Date timestamp) {
        this.expenseID = expenseID;
        this.title = title;
        this.avatarURL = avatarURL;
        this.amount = amount;
        this.notes = notes;
        this.billPictureURL = billPictureURL;
        this.payerID = payerID;
        this.participantIDs = participantIDs;
        this.splits = splits;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getExpenseID() {
        return expenseID;
    }

    public void setExpenseID(String expenseID) {
        this.expenseID = expenseID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getBillPictureURL() {
        return billPictureURL;
    }

    public void setBillPictureURL(String billPictureURL) {
        this.billPictureURL = billPictureURL;
    }

    public String getPayerID() {
        return payerID;
    }

    public void setPayerID(String payerID) {
        this.payerID = payerID;
    }

    public List<String> getParticipantIDs() {
        return participantIDs;
    }

    public void setParticipantIDs(List<String> participantIDs) {
        this.participantIDs = participantIDs;
    }

    public Map<String, Double> getSplits() {
        return splits;
    }

    public void setSplits(Map<String, Double> splits) {
        this.splits = splits;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Methods
    public void addParticipant(String participantID) {
        this.participantIDs.add(participantID);
        // Add logic here to update the expense in Firestore
    }

    public void removeParticipant(String participantID) {
        this.participantIDs.remove(participantID);
        // Add logic here to update the expense in Firestore
    }

    public void resolveDebt() {
        // Add logic here to create Debt objects for each participant
        // and update the expense status in Firestore
    }
}
