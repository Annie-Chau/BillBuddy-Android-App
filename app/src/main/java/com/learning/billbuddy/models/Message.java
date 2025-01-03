package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {

    private String messageID; // Firestore Document ID
    private String senderID; // Reference to User.userID
    private String content;
    private Date timestamp;

    // Constructor
    public Message(String messageID, String senderID, String content, Date timestamp) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Methods
    public void editContent(String newContent) {
        this.content = newContent;
        // Add logic here to update the message content in Firestore
    }

    public void deleteMessage() {
        // Add logic here to delete the message from Firestore
    }

    public static List<Message> fetchAllMessages() {
        List<Message> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("messages")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Message Fetching", "Error listening to message updates: " + error);
                        return;
                    }

                    result.clear();

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new Message(
                                    document.getId(),
                                    document.getString("senderID"),
                                    document.getString("content"),
                                    document.getTimestamp("timestamp") != null ?
                                            document.getTimestamp("timestamp").toDate() : null
                            ));
                        }
                    }
                });

        return result;
    }

}
