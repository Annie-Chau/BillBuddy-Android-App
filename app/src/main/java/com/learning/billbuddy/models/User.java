package com.learning.billbuddy.models;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.learning.billbuddy.utils.UserCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class User implements Serializable {

    private String userID; // Firebase UID
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePictureURL; // URL from Firebase storage
    private String registrationMethod; // Enum{"Facebook", "Google", "Email", "Phone number"}
    private List<String> notificationIds;

    // No-argument constructor
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }


    // Constructor
    public User(String userID, String name, String email, String phoneNumber, String profilePictureURL, String registrationMethod, List<String> notificationIds) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePictureURL = profilePictureURL;
        this.registrationMethod = registrationMethod;
        this.notificationIds = notificationIds;
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

    public List<String> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(List<String> notificationIds) {
        this.notificationIds = notificationIds;
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

    public static void fetchAllUsers(final UserCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listening to real-time updates
        db.collection("users")
                .addSnapshotListener((value, error) -> {
                    List<User> result = new ArrayList<>();
                    if (error != null) {
                        Log.e("User Fetching", "Error listening to user updates: " + error);
                        callback.onCallback(result);
                        return;
                    }

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            result.add(new User(
                                    document.getString("userID"),
                                    document.getString("name"),
                                    document.getString("email"),
                                    document.getString("phoneNumber"),
                                    document.getString("profilePictureURL"),
                                    document.getString("registrationMethod"),
                                    (List<String>) document.get("notificationIds")
                            ));
                        }
                    }

                    callback.onCallback(result);
                });
    }

    // Method to create a new user
    public static void createUser(String userID, String name, String email, String phoneNumber, String profilePictureURL, String registrationMethod, List<String> notificationIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new User object
        Map<String, Object> userData = new HashMap<>();
        userData.put("userID", userID);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("phoneNumber", phoneNumber);
        userData.put("profilePictureURL", profilePictureURL);
        userData.put("registrationMethod", registrationMethod);
        userData.put("notificationIds", notificationIds);

        // Add the new user to the "users" collection in Firestore
        db.collection("users")
                .document(userID)
                .set(userData)
                .addOnSuccessListener(documentReference -> Log.d("User Creation", "User created with ID: " + userID))
                .addOnFailureListener(e -> Log.e("User Creation", "Error creating user", e));
    }

    public List<Notification> getUserNotifications(List<Notification> allNotifications) {
        return allNotifications.stream()
                .filter(notification -> this.notificationIds.contains(notification.getNotificationID()))
                .collect(Collectors.toList());
    }
}
