package com.learning.billbuddy.models;

import java.util.List;

public class User {

    private String userID; // Firebase UID
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureURL; // URL from Firebase storage
    private String registrationMethod; // Enum{"Facebook", "Google", "Email", "Phone number"}
    private List<Notification> notifications;

    // Constructor
    public User(String userID, String name, String email, String phoneNumber, String profilePictureURL, String registrationMethod, List<Notification> notifications) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePictureURL = profilePictureURL;
        this.registrationMethod = registrationMethod;
        this.notifications = notifications;
    }

    // Getters and setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    public String getRegistrationMethod() {
        return registrationMethod;
    }

    public void setRegistrationMethod(String registrationMethod) {
        this.registrationMethod = registrationMethod;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    // Methods
    public void register() {
        // Implement registration logic here
        // This might involve interacting with Firebase Authentication
    }

    public void login() {
        // Implement login logic here
        // This might involve interacting with Firebase Authentication
    }

    public void joinGroup() {
        // Implement join group logic here
    }

    public void sendMessage() {
        // Implement send message logic here
    }

    public void createExpense() {
        // Implement create expense logic here
    }
}
