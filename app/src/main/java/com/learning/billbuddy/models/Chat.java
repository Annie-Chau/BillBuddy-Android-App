package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.learning.billbuddy.utils.ChatCallback;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

    public static void fetchAllChats(final ChatCallback callback) {
        List<Chat> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("chats")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Chat Fetching", "Error listening to chat updates: " + error);
                        callback.onCallback(result);
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

                    callback.onCallback(result);
                });

    }

    public static void createChat(String groupID, List<String> messageIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new Chat object
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("groupID", groupID);
        chatData.put("messageIds", messageIds);

        // Add the new chat to the "chats" collection in Firestore
        db.collection("chats")
                .add(chatData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Chat Creation", "Chat created with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Chat Creation", "Error creating chat: ", e);
                });
    }

}
