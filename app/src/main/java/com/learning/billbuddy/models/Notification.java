package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.utils.NotificationCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Notification {

    private String notificationID; // Firestore Document ID
    private String type; // Enum{"JoinGroup", "AddExpense", "DeptResolved", "NewMessage"}
    private String message;
    private Date timestamp;
    private boolean isRead;

    // Constructor
    public Notification(String notificationID, String type, String message, Date timestamp, boolean isRead) {
        this.notificationID = notificationID;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // Getters and setters
    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Method
    public static void fetchAllNotifications(final NotificationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("notifications")
                .addSnapshotListener((value, error) -> {
                    List<Notification> result = new ArrayList<>();
                    if (error != null) {
                        Log.e("Notification Fetching", "Error listening to notification updates: " + error);
                        callback.onCallback(result);
                        return;
                    }

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new Notification(
                                    document.getId(),
                                    document.getString("type"),
                                    document.getString("message"),
                                    document.getTimestamp("timestamp") != null ?
                                            Objects.requireNonNull(document.getTimestamp("timestamp")).toDate() : null,
                                    document.getBoolean("isRead") != null
                                            && Boolean.TRUE.equals(document.getBoolean("isRead"))
                            ));
                        }
                    }

                    callback.onCallback(result);
                });
    }

    // Method to create a new notification
    public static void createNotification(String type, String message, Date timestamp, boolean isRead) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new Notification object
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("type", type);
        notificationData.put("message", message);
        notificationData.put("timestamp", timestamp);
        notificationData.put("isRead", isRead);

        // Add the new notification to the "notifications" collection in Firestore
        db.collection("notifications")
                .add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Notification Creation", "Notification created with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification Creation", "Error creating notification: ", e);
                });
    }

    // Method to mark a notification as read
    public void markAsRead() {
        this.isRead = true;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications").document(this.notificationID)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> Log.d("Notification Update", "Notification marked as read"))
                .addOnFailureListener(e -> Log.e("Notification Update", "Error marking notification as read", e));
    }

}
