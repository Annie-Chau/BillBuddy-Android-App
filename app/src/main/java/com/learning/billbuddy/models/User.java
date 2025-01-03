package com.learning.billbuddy.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.stream.Collectors;

public class User {

    private String userID; // Firebase UID
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureURL; // URL from Firebase storage
    private String registrationMethod; // Enum{"Facebook", "Google", "Email", "Phone number"}
    private List<String> notificationsId;

    // Constructor
    public User(String userID, String name, String email, String phoneNumber, String profilePictureURL, String registrationMethod, List<String> notificationsId) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePictureURL = profilePictureURL;
        this.registrationMethod = registrationMethod;
        this.notificationsId = notificationsId;
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

    public List<String> getNotificationsId() {
        return notificationsId;
    }

    public void setNotifications(List<String> notifications) {
        this.notificationsId = notifications;
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

    public static List<User> fetchAllUsers() {
        List<User> result = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) {
                System.err.println("Error listening to user updates: " + error);
                return;
            }

            result.clear();

            if (value != null) {
                for (DocumentSnapshot document : value.getDocuments()) {
                    String userID = document.getString("userID");
                    String name = document.getString("name");
                    String email = document.getString("email");
                    String phoneNumber = document.getString("phoneNumber");
                    String profilePictureURL = document.getString("profilePictureURL");
                    String registrationMethod = document.getString("registrationMethod");
                    List<String> notificationsId = (List<String>) document.get("notificationsId");

                    User user = new User(userID, name, email, phoneNumber, profilePictureURL, registrationMethod, notificationsId);
                    result.add(user);
                }
            }
        });

        return result;
    }

    public List<Notification> getUserNotifications(List<Notification> allNotifications) {
        return allNotifications.stream()
                .filter(notification -> this.notificationsId.contains(notification.getNotificationID()))
                .collect(Collectors.toList());
    }
}
