package com.learning.billbuddy.models;

import java.util.Date;

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
    public void markAsRead() {
        this.isRead = true;
        // Add logic here to update the notification status in Firestore
    }
}
