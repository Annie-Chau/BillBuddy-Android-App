package com.learning.billbuddy.models;

import java.util.List;

public class Group {

    private String groupID; // Firestore Document ID
    private String name;
    private String description;
    private String avatarURL; // URL from Firebase storage
    private String ownerID; // Reference to User.userID
    private List<String> memberIDs; // List of User.userIDs, minimum 2 including the admin

    // Constructor
    public Group(String groupID, String name, String description, String avatarURL, String ownerID, List<String> memberIDs) {
        this.groupID = groupID;
        this.name = name;
        this.description = description;
        this.avatarURL = avatarURL;
        this.ownerID = ownerID;
        this.memberIDs = memberIDs;
    }

    // Getters and setters
    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public List<String> getMemberIDs() {
        return memberIDs;
    }

    public void setMemberIDs(List<String> memberIDs) {
        this.memberIDs = memberIDs;
    }

    // Methods
    public void addMember(String memberID) {
        this.memberIDs.add(memberID);
        // Add logic here to update the group in Firestore
    }

    public void removeMember(String memberID) {
        this.memberIDs.remove(memberID);
        // Add logic here to update the group in Firestore
    }

    public void createExpense(Expense expense) {
        // Add logic here to create an Expense and associate it with this group in Firestore
    }

    public void sendNotification(Notification notification) {
        // Add logic here to send a notification to group members
    }
}
