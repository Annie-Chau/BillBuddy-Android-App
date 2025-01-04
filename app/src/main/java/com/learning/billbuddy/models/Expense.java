package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.utils.ExpenseCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        updateParticipantsInFirestore();
    }

    public void removeParticipant(String participantID) {
        this.participantIDs.remove(participantID);
        updateParticipantsInFirestore();
    }

    private void updateParticipantsInFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("expenses").document(this.expenseID)
                .update("participantIDs", this.participantIDs)
                .addOnSuccessListener(aVoid -> Log.d("Expense Update", "Participant list successfully updated!"))
                .addOnFailureListener(e -> Log.e("Expense Update", "Error updating participant list", e));
    }

    public static void fetchAllExpenses(final ExpenseCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("expenses")
                .addSnapshotListener((value, error) -> {
                    List<Expense> result = new ArrayList<>();
                    if (error != null) {
                        Log.e("Expense Fetching", "Error listening to expense updates: " + error);
                        callback.onCallback(result);
                        return;
                    }

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new Expense(
                                    document.getId(),
                                    document.getString("title"),
                                    document.getString("avatarURL"),
                                    document.getDouble("amount"),
                                    document.getString("notes"),
                                    document.getString("billPictureURL"),
                                    document.getString("payerID"),
                                    (List<String>) document.get("participantIDs"),
                                    (Map<String, Double>) document.get("splits"),
                                    document.getTimestamp("timestamp") != null ?
                                            Objects.requireNonNull(document.getTimestamp("timestamp")).toDate() : null
                            ));
                        }
                    }

                    callback.onCallback(result);
                });
    }

    // Method to create a new expense
    public static void createExpense(String title, String avatarURL, Double amount, String notes,
                                     String billPictureURL, String payerID, List<String> participantIDs,
                                     Map<String, Double> splits, Date timestamp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new Expense object
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("title", title);
        expenseData.put("avatarURL", avatarURL);
        expenseData.put("amount", amount);
        expenseData.put("notes", notes);
        expenseData.put("billPictureURL", billPictureURL);
        expenseData.put("payerID", payerID);
        expenseData.put("participantIDs", participantIDs);
        expenseData.put("splits", splits);
        expenseData.put("timestamp", timestamp);

        // Add the new expense to the "expenses" collection in Firestore
        db.collection("expenses")
                .add(expenseData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Expense Creation", "Expense created with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Expense Creation", "Error creating expense: ", e);
                });
    }
}
