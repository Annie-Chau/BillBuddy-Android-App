package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Chat {

    private String chatID; // Firestore Document ID
    private String groupID; // Reference to Group.groupID
    private List<String> messageIds; // Reference Message.messageID

    // Constructor
    public Chat(String chatID, String groupID, List<String> messageIds) {
        this.chatID = chatID;
        this.groupID = groupID;
        this.messageIds = messageIds;
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

    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }

    // Method
    public List<Message> getChatMessages(List<Message> allMessages) {
        return allMessages.stream()
                .filter(message -> this.messageIds.contains(message.getMessageID()))
                .collect(Collectors.toList());
    }

    public static List<Chat> fetchAllChats() {
        List<Chat> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("chats")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Chat Fetching", "Error listening to chat updates: " + error);
                        return;
                    }

                    result.clear();

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new Chat(
                                    document.getId(),
                                    document.getString("groupID"),
                                    (List<String>) document.get("messageIds")
                            ));
                        }
                    }
                });

        return result;
    }

}
