package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.utils.MessageCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("messages").document(this.messageID)
                .update("content", newContent)
                .addOnSuccessListener(aVoid -> Log.d("Message Edit", "Message successfully updated!"))
                .addOnFailureListener(e -> Log.e("Message Edit", "Error updating message", e));
    }

    public void deleteMessage() {
        // Add logic here to delete the message from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("messages").document(this.messageID)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Message Delete", "Message successfully deleted!"))
                .addOnFailureListener(e -> Log.e("Message Delete", "Error deleting message", e));
    }

    public static void fetchAllMessages(final MessageCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("messages")
                .addSnapshotListener((value, error) -> {
                    List<Message> result = new ArrayList<>();
                    if (error != null) {
                        Log.e("Message Fetching", "Error listening to message updates: " + error);
                        callback.onCallback(result);
                        return;
                    }

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new Message(
                                    document.getId(),
                                    document.getString("senderID"),
                                    document.getString("content"),
                                    document.getTimestamp("timestamp") != null ?
                                            Objects.requireNonNull(document.getTimestamp("timestamp")).toDate() : null
                            ));
                        }
                    }

                    callback.onCallback(result);
                });
    }

    // Method to create a new message
    public static void createMessage(String senderID, String content, Date timestamp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new Message object
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderID", senderID);
        messageData.put("content", content);
        messageData.put("timestamp", timestamp);

        // Add the new message to the "messages" collection in Firestore
        db.collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Message Creation", "Message created with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Message Creation", "Error creating message: ", e);
                });
    }

}
