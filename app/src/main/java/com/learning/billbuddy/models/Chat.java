package com.learning.billbuddy.models;

public class Chat {

    private String chatID; // Firestore Document ID
    private String groupID; // Reference to Group.groupID

    // Constructor
    public Chat(String chatID, String groupID) {
        this.chatID = chatID;
        this.groupID = groupID;
    }

    // Getters and setters
    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    // Method
    public void addMessage(Message message) {
        // Add logic here to add the message to the chat in Firestore
        // This might involve creating a subcollection of messages within the chat document
    }
}
