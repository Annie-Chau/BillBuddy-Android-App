package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.utils.ExpenseCallback;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Expense implements Serializable {

    private String expenseID; // Firestore Document ID
    private String title;
    private String avatarURL; // URL from Firebase storage
    private Double amount;
    private String notes;
    private String billPictureURL; // URL from Firebase storage
    private String payerID; // Reference to User.userID
    private List<String> participantIDs; // List of User.userIDs
    private List<Map<String, Double>> splits;
    private Date timestamp;
    private Date createdTime; // Timestamp when the expense is created
    private String currency;

    private Boolean isReimbursed = false;

    // Constructor
    public Expense(String expenseID, String title, String avatarURL, Double amount, String notes,
                   String billPictureURL, String payerID, List<String> participantIDs,
                   List<Map<String, Double>> splits, Date timestamp, String currency,
                   Boolean isReimbursed, Date createdTime) {
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
        this.currency = currency;
        this.isReimbursed = isReimbursed;
        this.createdTime = createdTime;
    }

    public Expense(String expenseID, String title, String avatarURL, Double amount, String notes,
                   String billPictureURL, String payerID, List<String> participantIDs,
                   List<Map<String, Double>> splits, Date timestamp, String currency) {
        this(expenseID, title, avatarURL, amount, notes, billPictureURL, payerID, participantIDs,
                splits, timestamp, currency, false, new Date()); // Default value for isReimbursed
    }

    public Boolean getIsReimbursed() {
        if (isReimbursed == null) return false;
        return isReimbursed;
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

    public List<Map<String, Double>> getSplits() {
        return splits;
    }

    public void setSplits(List<Map<String, Double>> splits) {
        this.splits = splits;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public String getTimeString() {
        // return Today if it's created today
        if (new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp).equals(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()))) {
            return "Today";
        } else if (new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp).equals(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(System.currentTimeMillis() - 86400000)))) {
            return "Yesterday";
        }
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp);
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

    public String getPaidByName(List<User> userList) {
        for (User user : userList) {
            if (user.getUserID().equals(payerID)) {
                return user.getName();
            }
        }
        return "Unknown"; // Or handle the case where the payer is not found
    }


    private void updateParticipantsInFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("expenses").document(this.expenseID)
                .update("participantIDs", this.participantIDs)
                .addOnSuccessListener(aVoid -> Log.d("Expense Update", "Participant list successfully updated!"))
                .addOnFailureListener(e -> Log.e("Expense Update", "Error updating participant list", e));
    }

    public String getFormattedAmount() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormat.setCurrency(java.util.Currency.getInstance(currency)); // Set the currency
        return currencyFormat.format(amount);
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
                                    (List<Map<String, Double>>) document.get("splits"), // Update to get List<Map>
                                    document.getTimestamp("timestamp") != null ?
                                            Objects.requireNonNull(document.getTimestamp("timestamp")).toDate() : null,
                                    document.getString("currency"), // Add currency when fetching
                                    document.getBoolean("isReimbursed"), // Add isReimbursed when fetching
                                    document.getTimestamp("createdTime") != null ?
                                            Objects.requireNonNull(document.getTimestamp("createdTime")).toDate() : null
                            ));
                        }
                    }

                    // Sort the expenses by timestamp in descending order
                    result.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));
                    callback.onCallback(result);
                });
    }


    // Method to create a new expense
    public static void createExpense(String groupID, String title, String avatarURL, Double amount, String notes,
                                     String billPictureURL, String payerID, List<String> participantIDs,
                                     List<Map<String, Double>> splits, Date timestamp, String currency, Boolean isReimbursed) { // Updated parameter
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String expenseID = db.collection("expenses").document().getId();

        // Create a new Expense object
        Map<String, Object> expenseData = new HashMap<>();
        expenseData.put("expenseID", expenseID);
        expenseData.put("title", title);
        expenseData.put("avatarURL", avatarURL);
        expenseData.put("amount", amount);
        expenseData.put("notes", notes);
        expenseData.put("billPictureURL", billPictureURL);
        expenseData.put("payerID", payerID);
        expenseData.put("participantIDs", participantIDs);
        expenseData.put("splits", splits); // Update to put List<Map>


        //create new expense that copy the old expense except hour and minute and second
        //set timeStamp with hour minute second to current
        timestamp.setHours(new Date().getHours());
        timestamp.setMinutes(new Date().getMinutes());
        timestamp.setSeconds(new Date().getSeconds());

        expenseData.put("timestamp", timestamp);
        expenseData.put("currency", currency); // Add currency to the expense data
        expenseData.put("isReimbursed", isReimbursed); // Add isReimbursed to the expense data
        expenseData.put("createdTime", timestamp);

        // Add the new expense to the "expenses" collection in Firestore
        db.collection("expenses")
                .document(expenseID)
                .set(expenseData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Expense Creation", "Expense created with ID: " + expenseID);

                    db.collection("groups").document(groupID)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    List<String> expenseIDs = (List<String>) documentSnapshot.get("expenseIDs");
                                    if (expenseIDs == null) {
                                        expenseIDs = new ArrayList<>();
                                    }
                                    expenseIDs.add(expenseID);

                                    db.collection("groups").document(groupID)
                                            .update("expenseIDs", expenseIDs)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("Group Update", "Expense ID added to group successfully");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("Group Update", "Error adding expense ID to group", e);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Group Fetch", "Error fetching group", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Expense Creation", "Error creating expense: ", e);
                });
    }

    public List<String> getParticipantNameList(List<User> userList) {
        return participantIDs.stream()
                .map(participantID -> userList.stream()
                        .filter(user -> user.getUserID().equals(participantID))
                        .findFirst()
                        .map(User::getName)
                        .orElse("Unknown")) // Or handle the case where the user is not found
                .collect(Collectors.toList());
    }

    public Boolean isQualifyForSearch(String searchQuery) {
        return title.toLowerCase().contains(searchQuery.toLowerCase());
    }

    @Override
    public String toString() {
        return "Expense{" +
                "expenseID='" + expenseID + '\'' +
                ", title='" + title + '\'' +
                ", avatarURL='" + avatarURL + '\'' +
                ", amount=" + amount +
                ", notes='" + notes + '\'' +
                ", billPictureURL='" + billPictureURL + '\'' +
                ", payerID='" + payerID + '\'' +
                ", participantIDs=" + participantIDs +
                ", splits=" + splits +
                ", timestamp=" + timestamp +
                ", currency='" + currency + '\'' +
                ", isReimbursed=" + isReimbursed +
                '}';
    }
}