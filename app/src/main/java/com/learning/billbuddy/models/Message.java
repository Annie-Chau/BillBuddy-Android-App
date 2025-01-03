package com.learning.billbuddy.models;

import java.util.Date;

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
}
